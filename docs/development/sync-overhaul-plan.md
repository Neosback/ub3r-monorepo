# Player/NPC Sync Overhaul Plan

*Written 2026-07-18. Review of the junior dev's audit, plus a fresh pass over the actual code and four
reference servers. No code has been changed — this is the plan.*

**Scope:** `PlayerUpdating`, `NpcUpdating`, `engine/sync/**`, `ByteMessage`, `ZoneUpdateBus`.
**Constraints:** must stay wire-compatible with our custom 377 client (packet 81 player sync,
packet 65 NPC sync, custom hit-block layout), JDK 11, minimal GC churn.

---

## Part 1 — Fact-check of the junior review

Their review was written as if `PlayerUpdating.java` *is* the sync system. It isn't — the real
orchestration lives in `engine/sync/**` (Kotlin): `WorldSynchronizationService` drives one of
**three** selectable player encoders (`STAGED` default, `CANONICAL` fallback, `OPTIMIZED` opt-in).
Several of their claims change verdict once you account for that.

| # | Claim | Verdict | Reality |
|---|-------|---------|---------|
| 1 | `putString()` uses platform charset | **Correct** | `ByteMessage.java:562` uses `value.getBytes()`. Real bug, trivial fix. Impact is limited to non-ASCII text (player names/chat are mostly ASCII), so "HIGH" overstates it — but fix it. Use `ISO_8859_1`, which matches `getString()`'s `(char) temp` decoding and the client's byte-per-char reads. |
| 2 | `getBytesReverse()` doesn't advance reader index | **Correct defect, dead code** | The method is genuinely wrong (`buf.getByte(i)` absolute reads), but it has **zero callers** anywhere in `game-server/src`. There is no "potential misparse crash". Delete the method rather than fix it. |
| 3 | `synchronized(this)` in `appendPrimaryHit` serializes parallel sync | **Wrong diagnosis** | The entire sync cycle runs on the single game thread (`WorldSynchronizationService.run()` calls `GameThreadContext.validateGameThread`). There is no parallel sync to bottleneck; the lock is uncontended and effectively free. It's *pointless*, not slow — remove it for clarity. The real lesson: hit values are read from live mutable `Player` fields at encode time, which is only safe because encode is single-threaded. If we ever parallelize (Part 4), hit data must be snapshotted in the prep phase instead of locked. |
| 4 | Magic numbers (`putBits(2, 3)` removal, `0x40`/`0x100` mask overflow) | Correct, cosmetic | Worth named constants, no behavior change. |
| 5 | `buf`/`stream` mixed naming in `NpcUpdating` (lines 67–68) | Correct, cosmetic | Same object under two names. Also `ByteMessage buf = stream;` at line 46 should just go. |
| — | "1073-line PlayerUpdating, split into Movement/Block/Appearance encoders" | **Partially right, misses the cause** | The file is big largely because it contains encoder entry points for *three different sync modes* (`update` for canonical, `writeStagedLocalAdd` etc. for staged, `writeIncrementalUpdate`/`writeSelfOnlyUpdate`/templates for optimized). Deleting the unused mode shrinks it more than any refactor would. |
| — | "BitSet allocation per player per tick" | **Only in OPTIMIZED mode** | `toBitSet` (PlayerUpdating.java:988) runs only from `writeIncrementalUpdate`, which only `RootPlayerInfoService` calls — the opt-in mode. In the default config this allocation never happens. |
| — | Debug boolean flags | Correct, minor | `DEBUG_REGION_UPDATES` etc. → `logger.isTraceEnabled()`. |
| — | ThreadLocal scratch / appearance cache / block cache "good" | Agreed | These are genuinely solid. |

**Bottom line:** of their three "HIGH" bugs, one is real-but-small (charset), one is dead code, and
one is misdiagnosed. The genuinely important problems are elsewhere — see below.

---

## Part 2 — What the junior review missed

### 2.1 Three parallel sync implementations (the actual mess) — HIGH

`PlayerSynchronizationMode` selects between:

- **STAGED** (default) — `StagedPlayerSynchronizationService` + `StagedNpcSynchronizationService`. Plan → encode → enqueue → commit-on-accept. This is the good one.
- **CANONICAL** — `PlayerUpdating.update()` / `NpcUpdating.update()`, the legacy in-place path.
- **OPTIMIZED** — `RootPlayerInfoService` (545 lines) plus the entire `engine/sync/playerinfo/` tree:
  `admission/` (6 files), `dispatch/` (6 files), `state/` (7 files), `fragments/` (5 files),
  `template/` (3 files). **~25 Kotlin files reachable only from a mode that requires explicit
  opt-in via `PLAYER_SYNC_MODE=optimized`.**

Three implementations of the same wire packet means every protocol fix must be made three times
(or silently isn't — they have already drifted: canonical prioritizes adds by distance with a
15/tick cap, staged uses a byte budget with no per-tick add cap, optimized uses an admission queue).
This is the single biggest source of bugs, confusion, and file size. **Decision needed: commit to
STAGED, keep CANONICAL temporarily as an emergency env-var fallback, delete OPTIMIZED outright.**
That deletion alone removes ~2,500+ lines including roughly 40% of `PlayerUpdating.java`
(`writeIncrementalUpdate`, `writeSelfOnlyUpdate`, `writeLocalRemovals`, `writeLocalAdditions`,
`writeRetainedLocalUpdate`, `buildPlayerSyncTemplate*`, `shouldSkipPlayerSync`,
`PlayerVisibilitySignature`, `toBitSet`, `resolvePlayerSlot`).

### 2.2 Canonical NPC path: null-hole desync landmine — HIGH (if canonical stays)

`NpcUpdating.update()` writes the local count *before* filtering:

```java
stream.putBits(8, size);          // size includes any null holes
for (...) {
    Npc npc = player.getLocalNpcs()[i];
    if (npc == null) continue;    // skips WITHOUT writing removal bits
```

If a null ever appears in `[0, size)`, the client expects `size` per-NPC bit entries but receives
fewer → every subsequent bit read is misaligned → garbage NPC movement or crash. Compaction at the
end of each tick makes mid-list nulls unlikely, but nothing enforces it. The staged NPC service
handles this correctly (writes removal bits for null slots). Fix canonical to write the removal
sequence for nulls, or delete canonical NPC path when staged becomes the only mode.

Also in the same method:

- Line 83–84: `boolean exceptions = removeNpc(player, npc);` dereferences `npc` (inside
  `removeNpc` → `npc.canBeSeenBy`), then the *next line* checks `npc == null`. Dead check, and the
  order documents a falsehood.
- Same condition tests `npc.isVisible()` twice.
- `pruneLocalNpcsToProtocolCap` bumps the membership revision once per pruned NPC instead of once.
- `findNearbyNpcs` fallback allocates `new ArrayList<>(Server.npcManager.getNpcs())` **per viewer
  per tick** when no viewport snapshot exists.

### 2.3 Staged NPC plan: O(n²) membership scan — MEDIUM

`StagedNpcSynchronizationService.Plan.contains()` linearly scans up to 255 `nextSlots` for *every*
nearby NPC candidate, every viewer, every tick. The player-side `Scratch` already solved this with a
`BooleanArray` membership set + touched-list reset. Mirror that on the NPC side.

### 2.4 Staged encode allocates fresh update-block buffers per viewer — MEDIUM

`StagedPlayerSynchronizationService.encode()` does `ByteMessage.raw(8192)` and the NPC service
`ByteMessage.raw(16384)` per viewer per tick. The buffers are Netty-pooled (so this isn't raw GC
churn), but it's still an allocator round-trip and a `ByteMessage` wrapper allocation per viewer,
while `ThreadLocalSyncScratch` sits right there with reusable `playerUpdateBlock`/`npcUpdateBlock`
buffers that the canonical path uses. Unify: staged encode should use the thread-local scratch.
(Careful when Part 4 parallelizes — thread-locals still work per worker thread.)

### 2.5 NPC additions have no byte budget — MEDIUM

The player staged plan caps additions by estimated bytes (`MAX_STAGED_PAYLOAD_BUDGET = 60KB`); the
NPC plan caps only the *count* (25/tick). A burst of NPCs with large blocks (forced text, transforms)
can exceed the packet limit; today that trips `require(packetBytes <= 65535)` which then
**disconnects the viewer** via `handleViewerSyncFailure`. Add the same byte-estimation budget the
player path has, and also verify our client's read buffer for packets 81/65 actually tolerates
~60KB — Apollo caps adds at 20/cycle specifically because the vanilla client has a 5,000-byte
stream buffer. Check `game-client` `Buffer`/stream size before trusting the 60KB budget.

### 2.6 STAGED mode never skips idle viewers — MEDIUM (bandwidth/CPU, only matters at scale)

The skip logic (`shouldSkipPlayerSync`, revision stamps, idle templates) belongs to the OPTIMIZED
mode. In default STAGED mode, every viewer gets a fully planned + encoded packet 81 every tick even
when nothing changed. The NPC side does have a skip path but only in canonical mode
(`WorldSynchronizationService.encodeNpcs`). If/when this matters (100+ players), the cheap version
is Void/OPTIMIZED-style: precompute one tiny idle payload per (local-count) and reuse it for viewers
whose self-state, locals, and viewport stamps are unchanged. Keep the revision-stamp machinery
(`PlayerSyncRevisionIndex`, activity indexes) — it's the right foundation for this — but wire it
into the staged path, and delete the template cache until then.

### 2.7 Zone bus: lambda deltas, no pre-encoding, 104-tile radius — MEDIUM

`ZoneUpdateBus` queues a closure per event (`PacketZoneDelta` holding a `(Client) -> Unit`), and
flush delivers by *calling the closure per matching viewer*, building the outgoing packet object
per viewer per event. Ground-item spam in a crowded area = events × viewers packet constructions.
This is what the Engine-TS/rsmod comparison in the earlier review was about, and that part of the
earlier recommendation stands (see 3.4).

---

## Part 3 — Ideas worth stealing from the reference servers

### 3.1 Apollo (`apollo-kotlin-experiments`) — same protocol family, most directly portable

- **r377 encoders exist** (`game/src/main/java/org/apollo/game/release/r377/`). When in doubt about
  a wire detail, diff against these, not 317 lore. Use them as the reference for any protocol
  question during the refactor.
- **Appearance tickets** (`PlayerSynchronizationTask.hasCachedAppearance`): each viewer keeps
  `int[] appearanceTickets` (one slot per player index); each player has an `appearanceTicket`
  bumped when appearance changes. On add-local, if the viewer already holds the current ticket, the
  appearance block is **skipped entirely** — the 377 client caches appearance per player index, so
  re-adds (players walking in/out of view, teleport re-inserts) don't need the ~60-byte appearance
  payload. We currently force appearance on every ADD_LOCAL (`forceAppearance = true` in
  `PlayerUpdateBlockSet`). This is the biggest *bandwidth* win available in crowded areas, and it's
  wire-compatible (the add segment's "block update?" bit already supports it). Needs a client-side
  sanity check that our Mystic client really does retain appearance for re-added indices —
  verify before shipping.
- **Phaser-based parallel sync** (`ParallelClientSynchronizer`): five barrier phases
  (pre-player → pre-npc → player-encode → npc-encode → post). Each phase is embarrassingly parallel
  per entity. This is the model for Part 4, and it runs fine on JDK 11 (`Phaser` +
  `Executors.newFixedThreadPool`).
- **Chat block excluded from self** by cloning the block set — we already handle this with
  `includeChat`; no action, just confirmation we match.

### 3.2 Void (`game-server-main`) — the low-allocation playbook

Void is a 634 server (skip-count protocol, doesn't map 1:1 to 377 wire format), but its memory
discipline is exactly what "JDK 11, handle GC extremely well" wants:

- **Persistent per-viewport writers**: each viewer owns two preallocated buffers
  (`viewport.playerChanges`, `viewport.playerUpdates`) that are written, flushed, and
  `position(0)`-reset every tick. Zero per-tick buffer allocation, ever. Our staged path can adopt
  this per-`Client` (two pooled ByteBufs allocated at login, released at logout) instead of
  allocate-per-tick.
- **Int-index tracking sets**: locals stored as `int[]` of player indexes + primitive state arrays,
  not object lists/HashSets. Our `playersUpdating` is a `HashSet<Player>` consulted with
  `contains()` in hot loops; the staged `Scratch.membership` BooleanArray is already the right
  pattern — finish the job and drop the HashSet entirely once staged is the only mode.
- **Budget-aware appearance deferral** (`updateFlag()`): when the updates buffer is near the packet
  cap, Void *drops the appearance bit for this tick* rather than overflowing — the flag stays set
  and goes out next tick. Cleaner than our all-or-nothing `require(...)`-then-disconnect.
- **Movement computed from `lastSeen`** so a skipped viewer stays consistent — relevant only if we
  add idle-skip (2.6).

### 3.3 rsmod — architecture direction, not wire format

rsmod delegates player info to rsprot (OSRS revisions; not our protocol), but its **zone pipeline**
(`PlayerZoneUpdateProcessor`, `ZoneUpdateMap`, `SharedZoneEnclosedBuffers`) is the cleanest model:

- Per-player `visibleZoneKeys` (the 7×7 zone window, filtered to the build area).
- On zone change: compute `newZones` = now-visible minus previously-visible; send **reset + persistent
  state** (spawned locs, ground objs) only for those; transient deltas only for zones visible >1 cycle
  (avoids the double-send race when an obj spawns the same tick a zone becomes visible).
- **Enclosed buffers computed once per tick** (`computeEnclosedBuffers()` before per-player
  processing, `clear()` after): all "everyone sees this" zone events pre-encoded into one shared
  byte buffer per zone, sent as a single packet per zone per viewer; "follows" (private) events
  filtered per viewer.

### 3.4 Engine-TS — zone bookkeeping (confirms the earlier review)

The earlier Engine-TS comparison holds up: `ZoneGrid` bitfield for "does any zone near X have
players" in O(words), enclosed/follows event split, and **`entityEvents` map so an entity's pending
zone events are cleaned up when it's removed** (we currently have no cleanup — a ground item created
and despawned in the same tick sends both events). Its `BuildArea` active/loaded zone sets are the
same shape as rsmod's `visibleZoneKeys`.

---

## Part 4 — The plan

Ordered so each phase is independently shippable and testable. Wire format never changes except
where explicitly noted (4C-1, which uses an existing protocol affordance).

### Phase 0 — Decide, then delete (biggest cleanup, zero behavior change in default config)

1. Commit to **STAGED** as the one true player+NPC encoder. Keep **CANONICAL** behind the existing
   env var for one release as an emergency fallback; schedule its deletion.
2. Delete **OPTIMIZED**: `RootPlayerInfoService`, `engine/sync/playerinfo/admission/**`,
   `dispatch/**`, `state/**`, `player/fragments/**`, `template/**`, and the `PlayerUpdating`
   methods only they call (`writeIncrementalUpdate`, `writeSelfOnlyUpdate`, `writeLocalRemovals`,
   `writeLocalAdditions`, `writeRetainedLocalUpdate`, `buildPlayerSyncTemplate*`,
   `buildPlayerSyncTemplateKey`, `shouldSkipPlayerSync`, `PlayerVisibilitySignature`, `toBitSet`,
   `resolvePlayerSlot`). Keep `PlayerVisibilityRules`, `PlayerSyncRevisionIndex`, and the activity
   indexes — staged/canonical and the NPC skip path use them, and 4C-3 will want them.
3. `PlayerUpdating.java` drops to roughly 500–600 lines before any restructuring.

*Risk: low. Verification: build + boot + default-mode smoke test; grep proves nothing else
references the deleted package.*

### Phase A — Correctness fixes (small, surgical)

1. `ByteMessage.putString`: `value.getBytes(StandardCharsets.ISO_8859_1)`.
2. Delete `ByteMessage.getBytesReverse` (dead + broken). If a future decoder needs it, write it
   with `readByte()` semantics then.
3. Remove `synchronized` from `appendPrimaryHit`/`appendPrimaryHit2`.
4. `NpcUpdating.update` (while canonical exists): write removal bits for null slots instead of
   `continue`; hoist the `npc == null` check above `removeNpc`; drop the duplicated
   `isVisible()`; single membership-revision bump in `pruneLocalNpcsToProtocolCap`; kill the
   `buf` alias.
5. Constants: `UPDATE_TYPE_REMOVE = 3`, `MASK_OVERFLOW_FLAG = 0x40`, `MASK_OVERFLOW_THRESHOLD =
   0x100`, `PLAYER_LIST_TERMINATOR = 2047` — in one shared place (they're currently re-hardcoded in
   `PlayerUpdating`, `StagedPlayerSynchronizationService`, `NpcUpdating`, and both block sets).
6. Replace the `DEBUG_*` booleans with `logger.isTraceEnabled()` guards.

*Risk: low. Verification: golden-bytes test (below) proves output unchanged except charset case.*

### Phase B — Allocation & hot-loop hygiene (JDK 11 / GC goal)

1. Staged player+NPC encode: use `ThreadLocalSyncScratch` update-block buffers (or Void-style
   per-Client persistent buffers) instead of `ByteMessage.raw(...)` per viewer per tick.
2. NPC `Plan.contains` → membership `BooleanArray` + touched-list, mirroring the player `Scratch`.
3. Kill the `findNearbyNpcs` ArrayList-copy fallback (require a viewport snapshot, or iterate the
   NPC manager directly without copying).
4. Once canonical dies: replace `playersUpdating: HashSet<Player>` with the staged plan's slot
   arrays as the single source of local membership (Void's int-index tracking-set model).
5. NPC additions: add the byte-estimation budget (mirror `MAX_STAGED_PAYLOAD_BUDGET`), and adopt
   Void's graceful deferral — if the budget is hit, stop adding / defer appearance-bearing blocks
   to next tick instead of letting `require()` disconnect the player.
6. Audit `MAX_STAGED_PAYLOAD_BUDGET` (60KB) against the actual `game-client` stream buffer size
   for packets 81/65 before trusting it. Apollo's vanilla-client ceiling was 5,000 bytes; ours is
   custom but must be *measured*, not assumed.

*Risk: low-medium. Verification: golden-bytes tests unchanged; allocation profile via
`-verbose:gc` or JFR before/after under a bot swarm.*

### Phase C — Bandwidth & scalability wins

1. **Apollo appearance tickets** on add-local: per-viewer `int[] appearanceTickets` (players) —
   skip the appearance block when the viewer holds the current ticket. Prereq: confirm the Mystic
   client retains appearance for re-added player indices (test: two clients, walk out of view and
   back, verify rendering with the block skipped). Expected: largest byte reduction in crowded areas.
2. **Zone pipeline rebuild** (rsmod/Engine-TS model), replacing lambda `PacketZoneDelta`s:
   - Deltas become small data records (type + position + payload ints), not closures.
   - Split **enclosed** (everyone) vs **follows** (per-viewer filtered); pre-encode enclosed
     events once per zone per tick into a shared byte buffer.
   - Per-player `visibleZoneKeys`; on zone change send persistent state only for newly visible
     zones (this also replaces the current 104-tile-radius broadcast matching).
   - Entity→pending-events map so despawned entities' queued events are cancelled.
   - `ZoneGrid` bitfield for cheap "any player near this zone" checks when queueing NPC/zone work.
3. **Idle-skip for staged mode** (only when player counts make it matter): reuse the existing
   revision stamps + activity indexes to detect "nothing changed for this viewer", and send a
   precomputed minimal packet. Do this *after* C-1/C-2 — they may make it unnecessary.

*Risk: medium (C-1 and C-2 touch what the client sees). Verification: two-client manual matrix +
golden tests per scenario.*

### Phase D — Parallel encode (optional, last)

Apollo's phase-barrier model, adapted:

1. Prep phase (parallel-safe): snapshot everything encode reads — pre-encode shared blocks for
   **every** flagged entity (today `buildPlayerRootCache` already does this; make the encode path
   *never* fall back to reading live state on cache miss — the fallback is where hit-tearing lives).
2. Encode phase: per-viewer plan+encode fanned out over a fixed thread pool with a `Phaser`;
   each task touches only its viewer's state + read-only snapshots. `ThreadLocalSyncScratch`
   already works per worker thread.
3. Commit + flush back on the game thread (or keep flush on Netty threads as now).

Only do this if profiling shows `SYNC_PLAYER_ENCODE` actually dominating at target player counts —
the phases above may make it moot. Until then the `GameThreadContext` single-thread assertion is a
feature, keep it.

### Phase E — Structure & naming (after the dust settles)

With OPTIMIZED gone and staged the only path, the natural split is smaller than the junior's
proposal:

- `PlayerMovementCodec` — self + local movement bit writing (`updateLocalPlayerMovement`,
  direction translation).
- `PlayerAppearanceCodec` — `getAppearanceBytes` + cache (also drop the static
  `appendPlayerAppearance`/`appendForcedChatText`/`appendPlayerChatText` statics onto it).
- `PlayerUpdateBlockSet` / `NpcUpdateBlockSet` — stay as-is (already clean).
- `StagedPlayerSynchronizationService` — remains the orchestrator; `PlayerUpdating` shrinks to a
  thin protocol helper or disappears into the codecs.

---

## Part 5 — Guaranteeing "everyone gets the update they need"

The user-visible invariants the system must hold (and how we verify them):

| Event | Mechanism today (staged) | Gap / action |
|---|---|---|
| Player enters view | Viewport snapshot → plan additions (byte-budgeted) | No per-tick add cap means one giant tick can front-load; fine. Ticket-based appearance skip (C-1) must not skip *first-ever* sight — tickets start at 0/unseen, covered by design. |
| Player leaves view / logs out | `isRetained` (registry identity + visibility + distance) → removal bits | Solid. Registry identity check also covers slot reuse. |
| Teleport | `didTeleport` → not retained → remove + re-add | Confirm re-add lands same tick when budget allows (canonical had explicit reinsert queueing; staged relies on candidate ordering — add a test). |
| Death → respawn | Teleport path + appearance/hit flags | Covered by teleport test. |
| Gear on/off | `APPEARANCE` flag → shared UPDATE_LOCAL block prep → all viewers with them local | Solid; keep `buildPlayerRootCache` prepping blocks for **all** flagged players even in skip scenarios. |
| Hit splats | HIT/HIT2 flags, same path | After removing `synchronized`, single-threaded encode keeps reads consistent; Phase D moves this to snapshots. |
| Ground items / objects | ZoneUpdateBus radius broadcast | Phase C-2 replaces with zone visibility, and fixes the missing new-zone persistent replay being tied to `didMapRegionChange` only (`prepareViewerSynchronization` → `updateGroundItems`). |

**Test scaffolding to build in Phase 0 (before touching anything):**

1. **Golden-bytes harness**: deterministic world fixture (2–5 fake players, scripted movement /
   teleport / gear / hits), run one sync tick, assert exact packet-81/65 bytes. Record goldens
   *before* each phase, diff after. This is what makes every phase safe against a 377 client we
   can't unit-test.
2. Keep and extend `PlayerSyncInvariantValidator` — run it in CI fixtures, not just at runtime.
3. Manual two-client matrix (enter/leave/teleport/die/gear/hit/crowd) once per phase that touches
   Phase C territory.
4. Watch the existing telemetry (`sync.player.packet_bytes`, block cache hit counters,
   `SYNC_*` phase millis) before/after each phase — the counters are already there; use them as the
   perf regression harness.

---

## Part 6 — Priority order (TL;DR)

| Order | Item | Why first |
|---|---|---|
| 1 | Phase 0: delete OPTIMIZED, commit to STAGED | Removes ~2.5k lines and 2 of 3 duplicate protocol implementations; everything after gets easier |
| 2 | Golden-bytes test harness | Safety net for all subsequent phases |
| 3 | Phase A correctness fixes | Small, real, cheap |
| 4 | Phase B allocation hygiene | Directly serves the JDK 11 / GC requirement |
| 5 | C-2 zone pipeline | Biggest structural perf win; fixes stale-event and broadcast-radius issues |
| 6 | C-1 appearance tickets | Biggest bandwidth win; needs client verification first |
| 7 | C-3 idle skip, then Phase D parallelism | Only if profiling at target population says so |
| 8 | Phase E restructuring | Cosmetic; do last so you only refactor code that survived |
