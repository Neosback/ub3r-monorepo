# Sync Redo — Execution Plan + Current-Bug Fixes

*Written 2026-07-18. Builds on [sync-overhaul-plan.md](sync-overhaul-plan.md) and
[sync-consolidation-plan.md](sync-consolidation-plan.md). Those two carry the architecture detail;
this doc is the concrete order of work, updated for where the code actually is today — the Tarnish
wire port already landed (hits, chat, appearance, framing), and it brought three live bugs with it.
All findings below verified against the code and caches, not the log text.*

---

## Part A — Diagnosis of the current bugs

### A1. `tarnish_appearance_invalid ... reason=unterminated string` — validator bug, wire is fine

The appearance bytes the server sends are **correct** (they match the client's
`Player.updateAppearance` read order exactly, and my byte-accounting of Admin's block comes out at
69/70 bytes as logged). The false positive is in
[TarnishAppearanceValidator.java:75](../../game-server/src/main/java/net/dodian/uber/game/model/entity/player/TarnishAppearanceValidator.java):

```java
for (int i = 0; i < 5; i++) readLine(buffer);
```

The real layout after the name long is: **title string, 4-byte titleColor int, clanChannel,
clanTag, clanTagColor** — i.e. *4* strings with an *int* after the first. The validator reads *5*
strings and no int, so readLine #2 swallows the titleColor's zero bytes plus the next terminator,
everything shifts, and readLine #5 scans through the combat-level double (no 0x0A byte in
`doubleToLongBits(3.0)` etc.) until it falls off the end → "unterminated string" on every
appearance encode, thrice per tick per viewer.

**Fix (one line-ish):**
```java
readLine(buffer);          // title
buffer.getInt();           // title color
for (int i = 0; i < 3; i++) readLine(buffer);  // clan channel, tag, tag color
```

Nothing else is wrong with the block — Admin renders because the client parses it fine.

### A2. Admin freeze + "no objects load" — corrupt map archive 72 in the regenerated cache

Verified by parsing the file stores directly (sector-chain validation of every archive):

| Cache | md5 (.dat) | map archives valid |
|---|---|---|
| `game-server/data/cache` (old SwiftFUP source) | `fac18c5f…` — **the pristine tarnish-218 cache** (matches the original `cache-tarnish-218.zip` md5) | 4,624 / 4,624 |
| Bundled `src/main/kotlin/org/jire/swiftfup/cache` (new SwiftFUP source, added in commit `7a857eb96`, regenerated 07:48 that morning) | `9a9565f2…` | 4,623 / 4,624 — **archive 72 has a broken sector chain** |
| Client `~/.tarnish/cache` | `9a9565f2…` — byte-identical to the bundled copy | same broken archive 72 |

Decoding `map_index` from the new cache: **archive 72 is the terrain file for map square
(40,48)** (with landscape 73). Admin's freeze position (2626,3130) is in square (41,48) — the
viewport there spans (40,48), and the test plan itself says "a second player approaching from map
square (40,48)". So the freeze/diagnostics hunt was chasing appearance data while the actual
corruption was the neighboring terrain archive in the *client's own cache*; the fix then bundled
that same corrupted cache into the repo as the SwiftFUP source, making server checksums match the
corruption so the update server can never heal it. Their own new `FileResponses` counter will be
reporting it at boot: `swiftfup_cache_unreadable unreadableArchives=1 samples=[...archive 72]`.

Mechanism for the visible symptoms: the client requests terrain 72 on region load near that
boundary; the response is missing/unservable; depending on where it dies, the scene build stalls
(the freeze / `method54()` nonzero) or aborts after terrain but before landscape-object placement
(map renders, scenery missing).

**Fix — one move heals everything:** make SwiftFUP serve the pristine cache again. Either revert
`SwiftFupCache.path()` to `data/cache`, or overwrite the bundled directory with the `data/cache`
files. Because the client checksums its local files against the *server's* checksums per request,
the client's corrupt local archive 72 will fail the checksum and be re-downloaded automatically —
no need to tell anyone to delete `~/.tarnish/cache`.

### A3. Two caches of truth — reunify

Commit `7a857eb96` left the game server itself
([CacheBootstrapService.kt:10](../../game-server/src/main/kotlin/net/dodian/uber/game/engine/systems/cache/CacheBootstrapService.kt))
decoding **`data/cache`** for collision/objects while SwiftFUP serves the **bundled** copy. Two
different builds = the exact phantom-object / clip-mismatch class of bug the tarnish-218 swap fixed
last month. Both consumers must read one directory. Recommendation: keep `data/cache` as the single
source (it's the pristine one), delete the bundled binaries from the repo (131 MB in git history is
its own problem — consider git-lfs or fetching at build time), and make `SwiftFupCache.path()`
resolve `data/cache`.

### A4. Small follow-ups in the same area (do while there)

- `putString` still uses the platform charset ([ByteMessage.java:562](../../game-server/src/main/java/net/dodian/uber/game/netty/codec/ByteMessage.java)) → `ISO_8859_1`.
- The pointless `synchronized` wrappers still sit on `appendPrimaryHit`/`appendPrimaryHit2`.
- Forced chat: never emit an empty string (client does `charAt(0)` unguarded).
- The appearance validator warn should be rate-limited by (player, hash) — three identical warns
  per tick per viewer is the flood its own design doc said to suppress.

**Do not undo the Tarnish port.** Hits, chat, appearance, framing, and the 206/134/etc. decoders
are consistently ported on both sides now (verified: hit block, chat block, appearance block,
size table for the object path 85/101/151/60/156 all match). Undoing would be a second migration
with the same risk. Fix forward with A1–A3.

---

## Part B — Execution order for the sync redo

The consolidation plan's target stands: **one staged pipeline, Luna-style block codecs, Tarnish
wire contract**. The contract decision is now made *de facto* (Tarnish, already shipped). Updated
order, in PR-sized steps:

**PR 1 — Hotfixes (Part A).** Validator int fix, SwiftFUP → pristine `data/cache`, cache
reunification, warn rate-limit, charset, empty-forced-chat guard, drop the `synchronized`.
Verify: log spam gone; boot log shows `unreadableArchives=0`; walk Admin from (41,48) into (40,48)
with a second client observing — no freeze, scenery present.

**PR 2 — Golden-bytes harness.** Deterministic fixture (players + NPCs: walk/run/teleport/gear/
chat/forced-chat/single-hit/multi-hit/death), assert exact packet 81/65 bytes, fixtures generated
against the *current client's* parsers. Add the Admin appearance snapshot as a regression fixture
(its exact look + iron full helm + bronze pickaxe, decoded by a copy of the client's read order —
the validator, once fixed, is that decoder; promote it to the test).
Also add the **opcode audit test**: for every opcode the server can send, encode one instance and
check its byte count against the client's `PACKET_SIZES` table (fixed sizes must match exactly;
var-byte/var-short must agree in kind). This permanently guards the framing switch — any future
206-style drift fails CI instead of eating the packet stream.

**PR 3 — Delete to one mode.** Remove `PlayerSynchronizationMode`, the OPTIMIZED tree
(~25 files under `engine/sync/playerinfo/` + template/fragments), and the CANONICAL paths
(`PlayerUpdating.update`, `NpcUpdating.update`, add-prioritization variants, canonical NPC skip
branch — fold `shouldSkipNpcSync` into the staged NPC service). Golden bytes must be identical
before/after.

**PR 4 — Block codec layer.** One class per update block in explicit client mask order
(Luna's `AbstractUpdateBlockSet` shape); the Tarnish formats now each live in exactly one place;
dissolve the static helpers; shared `MovementCodec`. Golden bytes identical.

**PR 5 — Budgets + crowd behavior.** Player payload budget 60 KB → ≤ 32 KB (client buffer is
40,000 bytes); NPC byte budget (currently count-only, overflow = disconnect); Void-style
appearance/addition deferral when near budget; Tarnish defaults (~100 local cap, 15 adds/tick,
adaptive view distance).

**PR 6 — Allocation hygiene.** Staged encoders on `ThreadLocalSyncScratch` instead of
`ByteMessage.raw(8192/16384)` per viewer per tick; NPC plan membership bitset (kill the O(n²)
`contains`); retire the `playersUpdating` HashSet.

**PR 7 — Bandwidth.** Appearance tickets (client caches appearance per index for the whole
session — reset tickets at client session start only); Luna persistent/transient chunk requests
replacing the lambda `ZoneUpdateBus` deltas.

**PR 8 (optional, profiling-gated) — Parallel encode.** Apollo Phaser phases; prep phase must
fully pre-encode all flagged entities' blocks so encode is read-only (this is also when the hit
snapshot replaces any lingering shared-state reads).

Each PR re-runs: golden harness, opcode audit, `:game-server:test`, `syncTest`, `betaCheck`,
`:game-client:test`, and the two-client manual matrix (enter/leave view, teleport, death, gear,
hits, crowd churn) for PRs that touch wire behavior (1, 4, 5, 7).

---

## Part C — Why the freeze hunt went sideways (process note)

The Admin investigation instrumented the *player updating* path exhaustively (mask ring buffers,
appearance hashes, watchdogs) because the freeze correlated with Admin's login — but the root
cause was a corrupt *map archive* in the client's local cache, one square over from where Admin
stood. Two lessons already encoded in the plans, worth stating plainly:

1. **Validate at the source of truth first.** A 30-line sector-chain check over the caches (what
   found archive 72 here) is cheaper than any amount of packet instrumentation. The new
   `unreadableArchives` counter at boot is exactly right — make it a **hard startup failure**
   (or at minimum WARN with region names resolved via map_index), not a DEBUG line.
2. **Never let the corrupted copy become the source of truth.** Bundling `~/.tarnish/cache` into
   the repo turned a one-machine corruption into a fleet-wide one, and made the self-healing
   checksum mechanism enforce the corruption. Cache flows one way: pristine archive → server →
   clients.

---

## Part D — Execution status (2026-07-18, end of day)

All planned sync work through PR 7's ticket half is **done and green** (195 server tests,
3 client tests, all passing; goldens byte-stable across the refactor):

| Item | Status |
|---|---|
| Golden-bytes harness (`SyncGoldenBytesTest`, 9 scenarios, canonical-cross-verified before deletion) | ✅ done |
| Delete OPTIMIZED (25 files: `playerinfo/**`, `template/**`, `player/fragments/**`, mode enum) | ✅ done |
| Delete CANONICAL (`PlayerUpdating.update`, `NpcUpdating.update` + null-hole landmine, packet writers, `Client.update()`) | ✅ done — STAGED is unconditional |
| `PlayerVisibilityRules` / `PlayerSyncInvariantValidator` relocated to `engine/sync/player` | ✅ done |
| Correctness/hygiene: dead `getBytesReverse` deleted, mask-overflow + removal-type + terminator constants named, telemetry trimmed to live counters | ✅ done |
| Budgets: player 60KB→32KB (client buffer is 40KB), NPC additions byte-budgeted (was count-only) | ✅ done |
| Allocation: staged encoders reuse `ThreadLocalSyncScratch` (no per-viewer `raw()`); NPC plan membership `IntHashSet` (was O(n²) scan); `playersUpdating` already BitSet-backed | ✅ done |
| Appearance tickets (Apollo model): globally-unique ticket bumped on real byte change, per-viewer seen-array, skip block on re-add, stamped in `commit()` only on delivery acceptance; `AppearanceTicketSyncTest` locks first-add/re-add/changed-gear wire bytes | ✅ done |
| `PlayerUpdating.java` 1070 → ~490 lines; `NpcUpdating.java` 314 → ~220 | ✅ |

Still open (deliberately): the zone pipeline rebuild (PR 7's second half / region-zone plan 4b),
ground-item chunk indexing (region-zone plan 4a), idle-skip, and the profiling-gated parallel
encode. Manual two-client matrix (enter/leave/teleport/death/gear/hits, and specifically a re-add
after walking out of view to eyeball the ticket skip) still needs a live run before shipping.
