# Region/Zone Management — Fact-Check + Plan

*Written 2026-07-18. Reviews two write-ups against real Tarnish, Engine-TS, and ub3r source —
not just against each other. Continues the [sync-consolidation-plan.md](sync-consolidation-plan.md)
line of work (its Part C-2 already flagged the zone pipeline; this document makes it concrete and
folds in the region-management question). No code changed yet — this is the plan.*

---

## Part 1 — What ub3r already has (ground truth)

`ChunkManager` + `ChunkEntityIndex` ([ChunkManager.kt](../../game-server/src/main/kotlin/net/dodian/uber/game/model/chunk/ChunkManager.kt))
is a `ConcurrentHashMap<Long, ChunkEntityIndex>` spatial index over **8×8-tile chunks**
(`Chunk.SIZE = 8`, [Chunk.kt](../../skills/api/src/main/kotlin/net/dodian/uber/game/model/chunk/Chunk.kt))
— the *same granularity* as Engine-TS's "Zone." Each `ChunkEntityIndex` holds entities keyed by
`EntityType` (`PLAYER, NPC, OBJECT, GROUND_ITEM`). `Player.syncChunkMembership()` already does
remove-from-old / add-to-new on chunk change, same as Tarnish's region add/remove. On top of that,
`ViewportIndex.build()` (sync engine) already deduplicates the "surrounding chunks" scan per unique
neighborhood, once per tick, across every viewer that shares it.

Two things stand out once you check what's actually wired up:

- **`EntityType.OBJECT` and `EntityType.GROUND_ITEM` have zero usages anywhere in the codebase.**
  The enum anticipates chunk-indexed objects/ground-items; nothing ever calls
  `chunkEntityIndex.add()` for either.
- **Ground items have no spatial index at all.** [`Ground.kt`](../../game-server/src/main/kotlin/net/dodian/uber/game/engine/systems/world/item/Ground.kt)
  stores every ground item in three global `CopyOnWriteArrayList`s, and `findGroundItem()` does a
  **linear scan of the entire list** filtering by id/x/y/z — on every pickup attempt, examine, and
  lookup, world-wide, regardless of where the player is standing.
- Static objects, by contrast, **are** already properly indexed — `CacheCollisionAuditStore.objectsForTile()`
  is a region-packed lookup used by `ObjectClipService`, `StaticObjectOverrides`, `ClipProbeService`,
  and object-click logging. That side of "Region tracks objects" is already solved, just under a
  different name.

This is the single most concrete, highest-value finding in this review, and it didn't come from
either write-up directly — it came from checking what they were proposing against what's actually
there.

---

## Part 2 — Fact-check: "Should You Use Tarnish's RegionManager?"

Read `RegionManager.java`, `Region.java`, `RegionBlock.java` from `tarnish-main/game-server`.

| Claim | Verdict | Reality |
|---|---|---|
| "Region class — tracks ground items, objects, NPCs per region" | **Half right** | `RegionBlock` does hold `Map<Position, Set<GroundItem>>` and `Map<Position, List<GameObject>>` alongside player/NPC decks. ub3r's object-half is already covered (`CacheCollisionAuditStore`); the ground-item half is the real, unaddressed gap (Part 1). |
| "getSurroundingRegions() — cached 3×3 grid lookup" | **Real, but the caching strategy doesn't transfer as-is** | Confirmed in `RegionManager.getSurroundingRegions()` — computes the 3×3 neighbor set and caches it **permanently** on the `Region` object itself (`Optional<Region[]> surroundingRegions`, set once, never invalidated). That's safe in Tarnish only because regions, once created, live for the process lifetime and never change membership shape. ub3r's `ViewportIndex` already solves the same problem more appropriately for a live world: it recomputes per tick but **deduplicates across every viewer sharing a neighborhood**, so it's cache-per-tick rather than cache-forever-and-hope-nothing-changed. This is not a gap in the sync path. |
| "Region change events — callback when player enters new region" | **False as stated** | `Region`/`RegionBlock` have **no event or callback mechanism at all** — `addPlayer`/`removePlayer` just mutate a `Deque`, nothing more. There is nothing to "port" here: ub3r's `syncChunkMembership()` already does the exact same silent index swap Tarnish does. If you want actual pub/sub callbacks on chunk enter/leave (useful for content scripts), that's new functionality *beyond* Tarnish parity, not a port. |
| "Tarnish's Int2ObjectOpenHashMap isn't thread-safe, keep your ConcurrentHashMap" | **Correct, keep as-is** | Confirmed: `RegionManager.activeRegions` is an unguarded `Int2ObjectOpenHashMap`, only safe under Tarnish's single-thread-touches-world assumption. No reason to downgrade ub3r's `ConcurrentHashMap`. |

**Bottom line on this write-up:** its central recommendation ("port Region + getSurroundingRegions
+ change events") oversells two pieces that ub3r already has in a better-suited form (surrounding-chunk
caching, chunk membership swap) and undersells the one piece that's a real gap (ground-item indexing,
which it gestures at but frames as part of a bigger "port the whole Region class" ask rather than the
narrow, surgical fix it actually is).

---

## Part 3 — Fact-check: the Engine-TS zone write-up (your other dev)

Read `ZoneGrid.ts`, `Zone.ts` (full, 577 lines), `ZoneEvent.ts`, `ZoneEventType.ts` from `Engine-TS-274`.

| Claim | Verdict | Reality |
|---|---|---|
| "ZoneGrid — O(1) flag/unflag, O(radius²/32) radius queries instead of scanning all zones" | **The mechanism is real; the win is not exercised anywhere** | The bitfield code is exactly as described and correctly implemented (verified the shift/mask math). But `grep -rn "\.isFlagged("` across the **entire** Engine-TS-274 repo turns up zero callers of `ZoneGrid.isFlagged()` — only `flag()`/`unflag()` are ever called (from `Zone.enter`/`Zone.leave`). The radius-query "big win" the write-up sells is dead code in its own source project. It's infrastructure for a use case (skip processing zones nobody's watching) that was never wired up there. |
| "Zone event pre-encoding — encode once, send to everyone" | **Real and independently corroborated** | `Zone.ts` genuinely queues `ZoneEvent`s per change (`addObj`, `removeLoc`, etc.) into a per-zone `Set`. This exact pattern — pre-encode enclosed events once per zone per tick — is also implemented and *actively wired into the live update path* in **rsmod** (`PlayerZoneUpdateProcessor` + `SharedZoneEnclosedBuffers.computeSharedBuffers()`, confirmed in the [sync-overhaul-plan.md](sync-overhaul-plan.md) review). Two independent, more mature engines converge on this — much stronger signal than the ZoneGrid claim. |
| "Enclosed vs Follows split" | **Real, same corroboration** | `ZoneEventType.ENCLOSED` (everyone in zone) vs `FOLLOWS` (receiver-scoped) genuinely branches per event type throughout `Zone.ts` (e.g. `addObj`: `RESPAWN` → `ENCLOSED`, `DESPAWN`-with-owner → `FOLLOWS`). rsmod's `filterIsInstance<PartialFollowsZoneProt>` + `isHidden(observerUUID)` filtering is the same idea in production code. |
| "Event cleanup on entity removal (entityEvents map)" | **Real** | `queueEvent`/`clearQueuedEvents` in `Zone.ts:551-576` — confirmed exactly as described: an entity's pending events are tracked and purged together so a same-tick add+remove never emits a stale event. ub3r's `ZoneUpdateBus` has no equivalent today. |
| "Full lifecycle — zone owns players/npcs/locs/objs" | **This is the same finding as Part 1, from the other direction** | `Zone.objs`/`Zone.locs` are genuine per-zone linked lists with `getObj(x,z,type)` scoped to just that 8×8 area — this is what a proper ground-item/object index looks like at zone granularity. It's the same gap Tarnish's `RegionBlock` illustrates, converged on independently by two different reference servers. |

**Bottom line on this write-up:** the "port ZoneGrid" pitch is weaker than presented — even
Engine-TS doesn't use the part being sold as the performance win. The event-pre-encoding /
enclosed-follows-split / entity-event-cleanup trio, however, is well-corroborated (Engine-TS *and*
rsmod both do it, and rsmod does it in a shipped, exercised code path) and is exactly the ZoneUpdateBus
rework already scoped in [sync-consolidation-plan.md Part C, item C-2](sync-consolidation-plan.md).
This review doesn't change that recommendation — it strengthens the evidence for it and downgrades
the ZoneGrid piece from "port it" to "skip unless profiling says otherwise."

---

## Part 4 — What to actually do

Don't adopt Tarnish's `Region` class or Engine-TS's `Zone` class wholesale. ub3r's `ChunkManager`/
`ChunkEntityIndex` is already the right shape (8×8 granularity, thread-safe, `EntityType`-keyed) —
extend it rather than standing up a parallel concept. Two independent, surgical additions:

### 4a. Index ground items into `ChunkEntityIndex` (new, high value, not in the existing sync plans)

`GroundItem` needs to become chunk-addressable and `Ground.kt`'s add/remove/find paths need to go
through `ChunkEntityIndex` using the already-declared `EntityType.GROUND_ITEM`, instead of scanning
three global lists. Concretely:

1. Give `GroundItem` a `position: Position` accessor matching the `Entity` interface `ChunkEntityIndex`
   already generics over (it likely needs to adapt to whatever `Entity` requires — check `Entity.kt`).
2. `Ground.addItem()` / `Ground.deleteItem()` call `chunkEntityIndex.add()`/`.remove()` in addition
   to (or instead of, depending on what else touches the three flat lists) the existing bookkeeping.
3. `findGroundItem(id, x, y, z)` resolves the chunk for `(x, y)` via `Server.chunkManager.getLoaded()`
   and filters only that chunk's `EntityType.GROUND_ITEM` set — O(items in one 8×8 tile chunk)
   instead of O(every ground item in the world).
4. Keep the three-list split (`ground_items` / `untradeable_items` / `tradeable_items`) if something
   else depends on iterating them separately (e.g. despawn timers) — this is additive indexing, not
   a replacement of the ownership model.

This is a self-contained, testable change with an obvious before/after performance story and no
protocol/wire implications — good candidate to do independent of and before the zone/sync rework.

### 4b. Fold zone-event pre-encoding into `ChunkEntityIndex` (this *is* sync-consolidation-plan Part C-2, now concretized)

Rather than introducing a separate `Zone` type alongside `Chunk` (which would recreate the exact
"two systems doing the same job" problem the sync consolidation work is busy eliminating elsewhere),
extend `ChunkEntityIndex` — the thing that's already keyed at the right granularity — with:

- A per-chunk pending-`ZoneDelta` collection, split `ENCLOSED` (broadcast to everyone with the chunk
  in view) vs `FOLLOWS` (receiver-scoped), replacing `ZoneUpdateBus`'s current per-event closures.
- Pre-encode `ENCLOSED` deltas into one shared buffer per chunk per tick (mirrors
  `buildPlayerRootCache` — already the pattern used for player/NPC shared blocks).
- An entity→pending-deltas map so a despawned entity's queued events are cancelled (fixes the
  known gap: a ground item spawned and picked up same-tick currently sends both events).
- Persistent-state replay (spawned locs/objs still present) decoupled from `didMapRegionChange()` —
  replay for *newly visible* chunks specifically, matching rsmod's `processNewVisibleZones` vs
  `processVisibleZoneUpdates` split.

This directly replaces the item already on the consolidation plan's list (C-2); the only change
from that plan is *where it lives* — inside `ChunkEntityIndex`, not a new parallel `Zone` concept.

### 4c. Skip for now: `ZoneGrid` bitfield, generalized `getSurroundingRegions()` caching

- **ZoneGrid**: not worth porting speculatively — its own origin project never queries it. Revisit
  only if profiling ever shows "is anyone near this chunk" checks as a hot path (most likely
  candidate: deciding whether to run NPC AI/respawn ticks for empty areas of the map — check if
  that's currently unconditional over all loaded NPCs before deciding this is worth it).
- **Chunk-level neighborhood caching outside the sync path**: `ChunkManager.find()`/`forEach()`/
  `forEachNearby()` recompute their chunk-radius scan on every call, with no reuse across callers
  in the same tick (unlike `ViewportIndex`, which is sync-only). Only worth generalizing if a
  specific non-sync call site (interaction reach checks, AI targeting) is shown to be hot — not
  verified as a current bottleneck, so this is a "watch for it," not a "do it."

### 4d. No action: region-change callbacks

Tarnish doesn't have them either — nothing to port. If you want content scripts to react to a
player entering a new chunk/region (distinct from the network-sync-driven `didMapRegionChange`
already used for the client-facing region packet), that's a small, separate feature request:
add an optional listener list to `ChunkEntityIndex.add()`/`.remove()` or to
`Player.syncChunkMembership()`. Worth scoping only if something concrete needs it.

---

## Part 5 — Priority

| Order | Item | Why |
|---|---|---|
| 1 | 4a — ground item chunk indexing | Concrete, measurable, currently-unindexed O(n) scan on a hot path (every pickup/examine); no wire/protocol risk; independent of the sync rework |
| 2 | 4b — zone-event pre-encoding into `ChunkEntityIndex` | Already scheduled as sync-consolidation-plan Part C-2; this review adds strong cross-engine corroboration (Engine-TS + rsmod) and clarifies it should extend `ChunkEntityIndex`, not introduce a parallel `Zone` type |
| 3 | 4c — ZoneGrid / generalized neighborhood caching | Only if profiling asks for it |
| 4 | 4d — region-change callbacks | Only if a concrete content need shows up |
