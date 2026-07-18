# Sync Consolidation Plan — One Mode, One Contract

*Written 2026-07-18. Companion to [sync-overhaul-plan.md](sync-overhaul-plan.md); this document
supersedes its Phase 0/E and adds the client-contract findings from reviewing `game-client`,
`luna-master`, and `tarnish-main/game-server`. Review only — no code changed.*

**Goal:** exactly one player-sync path and one NPC-sync path that are correct, fast, low-allocation
(JDK 11), and provably matched to what our 377 client parses.

---

## Part 1 — What the client actually expects (the packet 81/65 contract)

From `game-client` `Client.java` (`updatePlayers` at :16721, `updateNPCs` at :5580):

### Structure (packet 81)

1. **Self movement** (bits): 1-bit update flag → 2-bit type. Type 0 = block-only; type 1 = walk
   (3-bit dir + 1-bit hasBlock); type 2 = run (3+3 dirs + 1-bit hasBlock); type 3 = teleport
   (2-bit plane, 1-bit discardQueue, 1-bit hasBlock, 7-bit y, 7-bit x).
2. **Local list** (bits): 8-bit count, then per local: 1-bit changed → if set, 2-bit type where
   **type 3 = remove**, types 0–2 as above.
3. **Additions** (bits): loop while `bitPosition + 10 < packetSize*8`: 11-bit index
   (**2047 = terminator**), 1-bit hasBlock, 1-bit discardWalkQueue, 5-bit Δy, 5-bit Δx
   (signed: `>15 → −32`), relative to the viewer.
4. **Mask blocks** (bytes): per flagged mob, mask byte; if `mask & 0x40`, second byte `<< 8`
   (16-bit ceiling — a mask bit above 0x8000 can never be encoded).

### Hard constraints the server must never violate

- **The client hard-crashes on any mismatch**: `buffer.position != packetSize` →
  `RuntimeException("eek")` (:16738); `count > playerCount` → throw (:16814); null entry in the
  rebuilt list → throw. There is no graceful recovery — every encode bug is a client crash.
  This is why the golden-bytes harness must come before any refactor.
- **Incoming buffer is 40,000 bytes** (`Buffer.create()` → `new byte[40_000]`). Our
  `MAX_STAGED_PAYLOAD_BUDGET = 60 * 1024` **exceeds the client's buffer** — a max-budget packet
  would overflow the client array. Set the budget to ≤ ~32 KB (and the same ceiling for the NPC
  packet, which currently has *no* byte budget at all).
- **Empty forced-chat crashes the client**: mask 0x4 does `textSpoken.charAt(0)` with no length
  check. Server must never send an empty forced-chat string.
- **Client `readString()` uses the platform default charset** (`new String(array, ...)`), same
  class of bug as our server's `putString()`. Fix the server to ISO-8859-1 (safe for all ASCII);
  optionally fix the client to match exactly.

### Two useful client behaviors to exploit

- **Appearance is cached per player index for the whole session**:
  `playerSynchronizationBuffers[index]` is written whenever an appearance block arrives (:14784)
  and **re-applied automatically when a player is re-added** (:16876). It is cleared only at
  login/world-reset (:11876), *not* on region change. → The Apollo appearance-ticket optimization
  (skip the appearance block on re-add when unchanged) is fully supported; server-side tickets just
  need to reset when the client session (re)starts.
- **Implicit trailing removal**: if the 8-bit count is smaller than the client's list, the client
  drops the tail itself (:16808). We don't rely on it and shouldn't start, but it explains why some
  servers get away with truncation.

---

## Part 2 — CRITICAL: there are two client protocol truths right now

The **committed (HEAD) client matches the current server**. The **working-tree client (uncommitted
changes in `Client.java` / `Player.java`) has been ported to the Tarnish wire format** — verified
byte-for-byte against `tarnish-main/game-server` `SendPlayerUpdate.java`. The drifted blocks:

| Block | HEAD client = current server | Working-tree client = Tarnish |
|---|---|---|
| Player hit 0x20 / 0x200 | `short` damage, byte type, `short` curHp, `short` maxHp | byte multi-flag [+ count + per-hit: byte dmg, byteA/byteS type, byte icon], byte hp%, negByte max% (health scaled to 100/200) |
| NPC hit 0x40 / 0x8 | same short-based layout | same Tarnish multi-hit layout |
| Chat 0x80 | LE short packed, byte rights, **plain string** (10-terminated) | LE short packed, byte rights, **negByte length + reversed packed data** (`TextInput.decode`, vanilla 377 style) |
| Appearance 0x10 | 3 header bytes (gender, headIcon, skullIcon); byte combat; short skill | **4 header bytes (+ bountyIcon)**; plus title string, int titleColor, clanChannel/clanTag/clanTagColor strings, combat as `double` long-bits, byte crown, short skill |
| Forced move 0x400 | shortA, shortA for the two speeds | **LE-shortA**, shortA |

Everything else (movement bits, add-local, graphics 0x100, anim 0x8, forced chat 0x4, face 0x1/0x2,
mask overflow, terminators) is identical in both.

**Consequence:** if the working-tree client is built and pointed at the current server, the first
hit splat, chat message, or appearance update misparses and the client throws `eek`. Any sync
refactor must first pin **one** contract.

**Decision #1 (blocking): pick the wire contract.** Recommendation: **adopt the Tarnish formats** —
the working-tree client is clearly the direction of travel (it's the Tarnish client port this repo
is built around), the Tarnish hit block is strictly more capable (multi-hits + icons + percent-based
hp bars), and `tarnish-main` gives us a known-good server-side encoder to copy from
(`SendPlayerUpdate.java:422` for hits, `:253` for chat, `:263` for appearance). The server-side
changes are confined to five block encoders once Part 4's codec layer exists. Until server and
client land together, do not ship the working-tree client.

---

## Part 3 — Ideas worth taking from Luna and Tarnish

### Luna (`luna-master`) — where our block set came from; take the finished version

- **One class per update block, one ordered list** (`AbstractUpdateBlockSet` + `UpdateBlock`
  subclasses, `computeBlocks()` returning an immutable ordered list). The encode order — which
  *must* match the client's mask-check order — lives in exactly one place, and each wire format
  (including the five drifted blocks above) lives in exactly one small class. Our
  `PlayerUpdateBlockSet` is a flattened mini-version of this; adopting the full shape makes the
  Tarnish format switch a five-file change and makes future drift impossible to miss.
- **Stateless, thread-safe block sets** — designed to be shared across encoder threads. Ours are
  already stateless; keep it that way so the future parallel phase is free.
- **`ChunkUpdatableRequest` persistent/transient flag**: a queued chunk update is either
  *transient* (one-shot: hit a ground item, remove it) or *persistent* (re-applied to any player
  entering visibility: a spawned object that must stay shown). This is a simpler version of
  rsmod's persistent-zone-state model that maps directly onto our existing chunk system, and it
  fixes our current gap where persistent state replay is tied only to `didMapRegionChange`.

### Tarnish (`tarnish-main/game-server`) — precedent for this exact client

- **It's Apollo's engine** (`ParallelClientSynchronizer`/`SequentialClientSynchronizer` + task
  classes) driving *our* client family. Direct proof the Phaser parallel-phase model works with
  this client — de-risks the optional parallel phase in the overhaul plan.
- **Known-good encoders for the target wire format** (`SendPlayerUpdate`, `SendNpcUpdate`) —
  the reference implementation for Decision #1.
- **Adaptive view distance** (`Viewport`): when a viewport hits capacity, shrink the per-player
  view distance; grow it back (toward 15) when under capacity. Smooth crowd degradation instead
  of a hard cliff at the cap — cheap to port onto our viewport snapshot.
- **Tuned constants for this client**: `CAPACITY = 100` locals (not the protocol max of 255),
  `ADD_THRESHOLD = 15` adds/tick, `VIEW_DISTANCE = 15`. The client *can* track 255, but capping at
  ~100 keeps packets far from the 40 KB ceiling; worth adopting as defaults.
- Anti-idea (skip): Tarnish rebuilds every block for every viewer with no shared-block caching and
  allocates a fresh `PacketBuilder` per appearance — our shared-block prep + appearance cache is
  already better. Keep ours.

---

## Part 4 — The single-mode plan

Target: **one pipeline** = the staged model (plan → encode → send-accept → commit), for both
players and NPCs, with Luna-style block codecs underneath.

### Step 0 — Pin the contract, build the safety net *(do first, everything depends on it)*

1. Resolve Decision #1 (recommend: Tarnish formats).
2. Golden-bytes harness: deterministic fixture (3–5 scripted players + NPCs: walk, run, teleport,
   gear change, chat, forced chat, hits, death/respawn), encode one tick, assert exact packet
   81/65 bytes against fixtures generated from the *chosen* client's parser. Re-record per step.
3. Add a paranoia assert mirroring the client: after encode, verify written byte count == declared
   size (the server-side twin of the client's `eek` check), behind a config flag.

### Step 1 — Delete down to one mode

Remove, in one PR:

- `PlayerSynchronizationMode` and the mode switch in `WorldSynchronizationService.encodePlayers`
  / `encodeNpcs` — staged becomes unconditional.
- **OPTIMIZED**: `RootPlayerInfoService`, `engine/sync/playerinfo/admission/**`, `dispatch/**`,
  `state/**`, `player/fragments/**`, `template/**` (~25 files), plus the `PlayerUpdating` methods
  only they call (`writeIncrementalUpdate`, `writeSelfOnlyUpdate`, `writeLocalRemovals`,
  `writeLocalAdditions`, `writeRetainedLocalUpdate`, `buildPlayerSyncTemplate*`,
  `buildPlayerSyncTemplateKey`, `shouldSkipPlayerSync`, `PlayerVisibilitySignature`, `toBitSet`,
  `resolvePlayerSlot`).
- **CANONICAL**: `PlayerUpdating.update()`, `addLocalPlayers` + the three
  `addPrioritized*`/`addLocalPlayersFromCollection` variants, `insertPrioritizedCandidate`,
  `pruneLocalsToProtocolCap`; `NpcUpdating.update()`, `findNearbyNpcs`,
  `pruneLocalNpcsToProtocolCap`; `Client.sendPlayerSynchronization` / `sendNpcSynchronization`;
  the canonical NPC skip branch in `encodeNpcs` (fold `shouldSkipNpcSync` into the staged NPC
  service instead — it's the one piece of canonical-era machinery worth keeping).
- Keep: `StagedPlayerSynchronizationService`, `StagedNpcSynchronizationService`,
  `PlayerVisibilityRules`, viewport index/snapshot, shared block caches + `buildPlayerRootCache`
  prep, appearance byte cache, revision/activity indexes, scratch buffers,
  `PlayerSyncInvariantValidator`.

This deletes the canonical NPC null-hole desync landmine and the O(all-NPCs) ArrayList fallback for
free (they only exist on the canonical path). After this, `PlayerUpdating` is a thin protocol
helper (~350 lines) and every sync bug has exactly one home.

*Also delete the emergency-fallback idea from the earlier plan: with the golden harness in place,
a fallback mode is insurance we pay for in duplicate maintenance. One mode, tested, is the point.*

### Step 2 — Luna-style block codec layer (and the Tarnish format switch)

1. `PlayerUpdateBlockSet` / `NpcUpdateBlockSet` become an ordered list of small block codecs
   (`ForceMovementBlock`, `GraphicBlock`, `AnimationBlock`, `ForcedChatBlock`, `ChatBlock`,
   `InteractionBlock`, `AppearanceBlock`, `FaceCoordinateBlock`, `HitBlock`, `Hit2Block`), each
   owning: its mask bit, its phase rules (self excludes chat; add forces appearance), and its wire
   encoding. Mask computation and mask-overflow writing stay in the set.
2. Implement the five Tarnish-format blocks (hits ×2 with multi-hit + icon + scaled hp, packed
   chat, extended appearance with title/clan/crown, LE forced-move speed) — copied from
   `tarnish-main` `SendPlayerUpdate.java` and verified against the working-tree client's parser.
   Server-side `Hit` model gains hitsplat icon + optional multi-hit array (Tarnish `Hit`/`Hitsplat`
   are the reference).
3. The static helpers on `PlayerUpdating` (`appendPlayerChatText`, `appendForcedChatText`,
   `appendPlayerAppearance`) dissolve into their block classes. Movement bit-writing moves to a
   shared `MovementCodec` used for self + locals + add-local.
4. Land server + client together behind one release; golden fixtures regenerated from the new
   client parser.

### Step 3 — Correctness & hygiene (from the overhaul plan, unchanged but re-scoped)

- `putString` → ISO-8859-1; delete dead `getBytesReverse`; remove the pointless `synchronized` in
  the hit appenders; never emit empty forced chat (guard in `ForcedChatBlock`).
- Named constants for removal (`1,1 + 2,3`), terminator 2047, mask overflow — defined once in the
  codec layer, used everywhere.
- Debug booleans → `logger.isTraceEnabled()`.

### Step 4 — Budgets and crowd behavior

- Player payload budget: **≤ 32 KB** (client buffer is 40 KB), replacing the wrong 60 KB constant.
- NPC additions get the same byte-estimation budget (currently count-only).
- Void-style graceful deferral: when near budget, defer appearance-bearing blocks/additions to
  next tick instead of `require()`-disconnecting the viewer.
- Adopt Tarnish crowd defaults: local cap ~100, adds ≤ 15/tick, view distance 15 with adaptive
  shrink under pressure (optional, small).

### Step 5 — Allocation hygiene (JDK 11 / GC goal)

- Staged encode paths use `ThreadLocalSyncScratch` (or per-`Client` persistent pooled buffers,
  Void-style) instead of `ByteMessage.raw(8192/16384)` per viewer per tick.
- NPC `Plan.contains` O(n²) scan → membership `BooleanArray` + touched list (copy the player-side
  `Scratch` pattern).
- Retire `playersUpdating: HashSet<Player>` — after Step 1 the staged plan's slot arrays are the
  single source of local membership.

### Step 6 — Bandwidth wins

- **Appearance tickets** (Apollo model, now *validated against our client*): per-viewer
  `int[appearanceTicket per slot]`, reset when the viewer's client session restarts (login /
  world reinit — the only time the client clears `playerSynchronizationBuffers`). On add-local,
  skip the appearance block when the viewer already holds the current ticket.
- **Chunk update requests with persistent/transient flag** (Luna model) replacing the lambda-based
  `ZoneUpdateBus` deltas: pre-encodable data records, persistent state replayed to players entering
  visibility (fixes replay being tied to `didMapRegionChange` only), transient events cancelled
  when their entity despawns. The fuller rsmod enclosed/follows pre-encoding from the overhaul
  plan remains the end-state; Luna's flag is the incremental step that fits our chunk system today.

### Step 7 — Optional: parallel encode

Unchanged from the overhaul plan (Apollo Phaser phases; prep-phase must fully snapshot so encode is
read-only). Tarnish proves the model against this exact client family. Only if profiling demands it.

---

## Part 5 — Verification matrix (per step)

| Check | How |
|---|---|
| Bytes unchanged (Steps 1, 3, 5) | Golden harness: fixtures identical before/after |
| Bytes changed intentionally (Step 2) | Fixtures regenerated from the chosen client's parser; two-client manual test: hits (single + multi + icons), chat, titles/clan display, forced movement |
| No client `eek` crashes | Server-side size assert (Step 0.3) + soak with bot swarm |
| Everyone gets needed updates | Two-client matrix: enter view, leave view, teleport, death/respawn, gear on/off, hit splats, crowded-area add/remove churn |
| Budget safety | Fixture with 100+ flagged players in one spot; assert packet ≤ 32 KB and graceful deferral, not disconnect |
| GC | JFR/`-verbose:gc` allocation-rate comparison before/after Step 5 under bot load |
| Perf | Existing `sync.*` telemetry counters and phase timings, compared per step |

## Decision summary

| # | Decision | Recommendation |
|---|---|---|
| 1 | Wire contract: HEAD client vs working-tree (Tarnish) client | **Tarnish formats**; land server+client together; until then, don't run the working-tree client against this server |
| 2 | Which mode survives | **STAGED** for both players and NPCs; delete OPTIMIZED *and* CANONICAL (no fallback mode — the golden harness is the safety net) |
| 3 | Local cap / add rate | Tarnish-tuned: ~100 locals, 15 adds/tick, 32 KB budget |
