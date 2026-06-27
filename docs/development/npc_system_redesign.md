# NPC System Redesign — V4 (Final, Implementation-Ready)

Status: **locked blueprint** — build this, don't re-design it.
Audience: Arch, Neosback, Dodian devs
Thesis: **Kotlin owns behavior. JSONC owns bulk placement. The engine owns safety.**

V4 is the practical version. The goal is not a scripting language — it's NPC content
that's easy to author, hard to break, and safe to migrate one NPC at a time. The
clean authoring surface from V3 stays; the implementation boundaries get stricter and
more realistic. This doc folds in the dev review ("typed slots, not loose vars") and
the V4.1 Day-One addendum (§13). There is no V5 — further design here is
procrastination.

---

## 1. Final decision

| Area | Owner |
|---|---|
| Complex behavior + dialogue logic | Kotlin DSL |
| Shops, teleporters, banks, basic types | Kotlin archetypes |
| Bulk spawns + coordinates | JSONC |
| Rare conditional/quest spawns | JSONC `condition` key (**not** inline Kotlin) |
| Drops | Kotlin template (`drops {}`) |
| Runtime execution | new `ActiveNpcSession` executor |
| Type safety + validation | internal strict IR, boot-time validation |
| Migration | legacy bridge, no flag day |

Do **not** make JSONC the behavior language. Do **not** keep the current NPC module
shape forever. Do **not** rewrite the system at once.

---

## 2. Where we are today (V4 is hardening, not greenfield)

Confirmed against the codebase — most pieces already exist:

- **Auto-registration:** any `object : NpcModule` is found by classpath scan in
  [`ContentModuleIndex`](../../game-server/src/main/kotlin/net/dodian/uber/game/api/plugin/ContentModuleIndex.kt#L132)
  and registered by
  [`NpcContentRegistry`](../../game-server/src/main/kotlin/net/dodian/uber/game/engine/systems/interaction/npcs/NpcContentRegistry.kt).
  V4 adds a parallel scan for `NpcTemplateModule`.
- **A dialogue step model already exists** in
  [`NpcDialogueDsl.kt`](../../game-server/src/main/kotlin/net/dodian/uber/game/npc/NpcDialogueDsl.kt)
  (sealed `Seq*` steps, auto-pagination, auto-finish, `whenCondition … otherwise`).
  V4's IR is an evolution of it — add stable named targets + validation + a hardened
  executor.
- **Spawns are file-based** (~199 JSONC families,
  [`NpcSpawnRepository`](../../game-server/src/main/kotlin/net/dodian/uber/game/npc/NpcSpawnRepository.kt));
  DB spawn loading is off. **`condition`/`conditionKey` already resolves through
  `NpcSpawnConditionRegistry`** — V4.1 #4 (conditional spawns) is mostly already
  built; we just expose it in the schema.
- **Cancellation hooks exist:**
  [`PlayerActionCancellationService`](../../game-server/src/main/kotlin/net/dodian/uber/game/engine/systems/action/PlayerActionCancellationService.kt)
  / `ContentActions.cancel` / `cancelInteractionTask`. §12 builds on these.
- **Override-only stats exist** (`MYSQL_DEFAULT_STAT = -1` = "use def").
- **Drops are currently fragmented** across `NpcDropStore` (runtime),
  `data/def/monsters-json/<id>.json`, and SQL — keyed by id. §10 pulls them into the
  template; the block just populates `NpcDropStore` at registration (incremental).

---

## 3. Core principle: authors write primitives, the engine wraps them

Content devs write plain Kotlin. The builder catches each primitive, wraps it in the
strict internal IR (`TemplateKey`, `ScriptId`, `TextExpr`, `ScriptStep`, …), runs
collision/reference checks, and produces a locked `NpcContentDefinition`. **Authors
never hand-write an IR class.** A broken reference (`thenGo = "praty_room"`) means the
**server refuses to boot** with a loud error.

---

## 4. The authoring surface

### Simple NPC — stays trivial
```kotlin
object Guard : NpcTemplateModule {
    override val template = npcTemplate("guard") {
        name("Guard")
        ids(3094, 3095)
        stats(attack = 900, strength = 900, defence = 900, hitpoints = 1200) // override-only
        attack()
        onDeath { ctx -> /* triggers */ }
    }
}
```

### Branching / shop NPC — same file, scales up
```kotlin
object Aubury : NpcTemplateModule {
    override val template = npcTemplate("aubury") {
        name("Aubury")
        ids(10681)
        shopkeeper(
            shop = ShopId.AUBURYS_MAGIC_STORE,
            greeting = textSlot("shopkeeper.greeting", "Do you want to buy some runes?"),
            emote = DialogueEmote.EVIL1,
        )
        onOption("teleport", target = "aubury.teleport.check")
        script("aubury.teleport.check") {
            branch(predicate("balloons_active"),
                thenGo = "aubury.teleport.party_room",
                elseGo = "aubury.teleport.edgeville")
        }
        script("aubury.teleport.party_room") { teleport(3045, 3372, 0); npc("Welcome to the party room!"); end() }
        script("aubury.teleport.edgeville") { teleport(3086, 3488, 0, radius = 2); npc("Welcome to Edgeville!"); end() }
    }
}
```

### Authoring rules
- **Inline flow for linear dialogue** (`onTalk { npc(...); player(...); end() }`) — the
  easiest path, Arch's chat style.
- **Named `script(...)` + `thenGo/elseGo` only for branches, reuse, or override targets.**
- **`textSlot(...)` only on fields a spawn may override.** Normal lines stay literal —
  `npc("Hello.")`, never `npc(textSlot("line1", "Hello."))`.

---

## 5. Typed slots (replaces loose vars)

The single biggest call vs V3: **no generic var bag — typed override slots only.** The
type is known, overrides are validatable, the DSL stays readable, and 90% of NPCs never
touch a slot.

```kotlin
textSlot("shopkeeper.greeting", "Do you want to buy some runes?")
shopSlot("shopkeeper.shop", ShopId.AUBURYS_MAGIC_STORE)
emoteSlot("shopkeeper.greeting_emote", DialogueEmote.EVIL1)
```
Internally a slot is a typed `SlotValue` + `SlotKey`; steps reference it via
`TextExpr.SlotRef`/`ShopExpr.SlotRef`/`EmoteExpr.SlotRef`, **resolved when the
interaction starts** — so a JSONC override is read live (the V2 "captured literal" bug
is structurally impossible). A spawn may only override declared slots, and only with a
matching type.

---

## 6. Archetypes (safe-merge)

Helper functions that inject standard behavior: `shopkeeper`, `banker`, `monster`,
`teleporter`, `slayerMaster`, `skillcapeSeller`, …

Rules:
1. Archetype scripts are **auto-namespaced** (`shopkeeper.open`, `shopkeeper.main`).
2. Duplicate script id **throws at build time** — never silent overwrite.
3. Intentional replacement requires the explicit `overrideScript("shopkeeper.decline") { … }`.
4. Archetypes may declare slots and expose only chosen override points.

---

## 7. Spawns (JSONC + generated schema)

Bulk spawns stay in JSONC; module owns behavior. A Gradle task dumps valid template
keys / npcIds-per-template / slot keys+types / stat shape into `npc_spawn.schema.json`
on every compile, so the IDE catches typos at the keystroke.

```jsonc
{
  "$schema": "../../schemas/npc_spawn.schema.json",
  "uid": "aubury.varrock.center",
  "template": "aubury",
  "npcId": 10681,
  "x": 3253, "y": 3402, "z": 0, "face": "SOUTH", "walkRadius": 2,
  "overrides": { "slots": { "shopkeeper.greeting": "Ah, a Varrock local! Looking for runes?" } }
}
```

Spawn rules: JSONC owns coordinates; overrides may change **declared slots only** (no
arbitrary behavior injection in MVP); conditional/quest spawns use a `condition` key
(already resolved by `NpcSpawnConditionRegistry`) — **coordinates never bleed into
Kotlin**. Server validation is authority even though the schema catches most typos.

---

## 8. Stats

Keep override-only; drop preset classes.
```kotlin
data class NpcStatOverride(
    val attack: Int? = null, val strength: Int? = null, val defence: Int? = null,
    val hitpoints: Int? = null, val ranged: Int? = null, val magic: Int? = null,
    val respawnTicks: Int? = null,
)
```
Precedence: **spawn override > template override > cache/db definition.** Map the old
`-1` sentinel to `null` at the load boundary so the new model stays clean.

---

## 9. Internal IR (engine-only)

`@JvmInline value class` for `TemplateKey / ScriptId / PredicateKey / SpawnUid /
SlotKey`; `enum DialogueActor { PLAYER, NPC }` (never a string — kills the `"ncp"`
typo class); `TextExpr/ShopExpr/EmoteExpr = Literal | SlotRef`; `ScriptStep = Say |
Choice | Branch | OpenShop | Teleport | GoTo | End`; and
`NpcContentDefinition(key, name, npcIds, slots, stats, drops, interactions, scripts,
hooks)`. Authors don't touch these.

---

## 10. Drops live in the template (V4.1 #2)

If the Kotlin file owns the NPC's life, it owns its wallet. Co-locating drops is Goal #1
of this whole redesign; today they're split across `NpcDropStore` + `monsters-json` + SQL.

```kotlin
drops {
    guaranteed(ItemId.BONES)
    roll(chance = 1 inChance 4)   { item(ItemId.RAW_RAT_MEAT) }
    roll(chance = 1 inChance 100) { item(ItemId.LONG_BONE) }
}
```
The block populates `NpcDropStore.forNpc(id)` at registration — incremental, no rewrite
of the drop runtime. Migration: port a type's drops into its template, delete its
`monsters-json`/SQL rows.

---

## 11. Hooks (build only what content needs)

MVP: `onOption(...)`, `onTalk(...)`, `onDeath(...)`, `attack()`. Add `onSpawn`,
`onRespawn`, `onAttack(ctx)` when a feature actually requires them — don't overbuild.

---

## 12. Validation + runtime safety (refuse to boot / paranoid executor)

**Boot-time validation** (server refuses to start on any failure):
- unique template keys & spawn uids; non-empty names; ≥1 npcId; unique script ids
- every option/choice/branch/goto **target exists**; every predicate exists
- every slot reference exists **and matches its declared type**; every shop/emote exists
- **spawn check uses the referenced template** (the critique's bug):
  ```kotlin
  val def = defByKey[spawn.template] ?: error("Spawn ${spawn.uid} → missing template '${spawn.template.value}'")
  require(spawn.npcId in def.npcIds) {
      "Spawn ${spawn.uid.value} uses npcId ${spawn.npcId}, but template '${spawn.template.value}' allows ${def.npcIds}"
  }
  ```

**`ActiveNpcSession`** wraps every interaction, routing cancellation through the
existing services (§2). Guards:
1. **Watchdog** — too many non-yielding steps/tick (GoTo loops) → kill + log deadlock.
2. **Leash** — `distanceTo(npc) > range` each tick → close.
3. **Entity validity** — player logout/death/teleport/other-exclusive-action → cancel.
   NPC check is **`isDead || isDying || isPlayingDeathAnimation`** (V4.1 #5 — the
   micro-tick "talk to a falling corpse" stall).
4. **Damage interrupt** — took damage this tick → close dialogue.
5. **State lock** — `OpenShop/GiveItem/TakeItem/StartTrade/StartDuel/OpenBank` refuse
   while trading/banking/dueling (dupe-buster).

---

## 13. V4.1 Day-One traps (patch these as you build)

1. **In-flight reload** — a `::reloadnpcs` while a player sits on Aubury step 2 leaves an
   `ActiveNpcSession` pointing at a GC'd AST → NPE/illegal access. The registry must hold
   all live sessions; **first line of `hotReload()` is
   `liveSessions.forEach { it.forceKill("SERVER_CONTENT_RELOAD") }`** before swapping defs.
2. **Drops in the template** — see §10.
3. **ID ranges** — overload `ids()` for `IntRange` so generic mobs stay readable:
   `ids(41..48, 1769..1776, 3050)` flattens into the `Set<Int>`.
4. **Conditional spawns via JSONC** — keep coords out of Kotlin; use the `condition`
   key (already wired). Repository skips the entity when the predicate is false.
5. **Ghost-entity stall** — folded into §12 guard #3 (`isDying`/death-animation).

---

## 14. Package layout + naming

```
net/dodian/uber/game/npc/
  v4/        core/  monster/  boss/  named/  skilling/  shop/  unknown/
  engine/    NpcContentDefinition.kt  NpcTemplateBuilder.kt  NpcContentCompiler.kt
             NpcContentRegistry.kt    ActiveNpcSession.kt     PredicateRegistry.kt
```
Generic mobs share one class (`Rat`, `Guard`, `GreenDragon`, `Aubury`, `FishingSpot`).
No `Rat1/2/3` unless behavior actually differs. Classpath scan → folders are for humans.

---

## 15. Rollout (legacy bridge, no flag day)

```
Classpath scan ─┬─ NpcTemplateModule (V4) → ActiveNpcSession
                └─ legacy NpcModule  (V1) → old handler
```
If an npcId has a V4 template, V4 owns it; else legacy. Phases:

1. **Sandbox IR + DSL** in `npc/v4/`. **Write the validator unit test first** — feed it
   a mock template with two scripts named `"test.main"` and assert it blows up; also
   dup-script-id, missing branch/choice target, unknown predicate, wrong slot type,
   spawn-npcId-not-in-template. No server behavior change.
2. **Runtime skeleton** — `NpcContentCompiler` + `NpcContentRegistry` + `ActiveNpcSession`.
   Prove one V4 NPC can talk, choose, open a shop, teleport, and cancel safely.
3. **Port Aubury** (the proof NPC: named + shopkeeper + talk + trade + teleport option +
   predicate branch + 2 spawns + per-spawn greeting override + drops).
4. **Port a generic combat NPC** (Man/Guard): stats override, attack, `onDeath`, multi-id.
5. **JSONC spawn schema** (Gradle dump). DX layer — don't block the engine on it.
6. **Long tail**, by category: shopkeepers → bankers → simple talkers → guards/mobs →
   teleporters → skilling spots → bosses → quest/minigame. Delete the old engine when
   legacy usage hits zero.

**First PR is done when:** Aubury is ported, you walk to him in Yanille, buy a rune,
walk away mid-conversation, and the dialogue window closes (leash check), green CI.

---

## 16. Non-goals for MVP

No full JSONC behavior scripting, hot reload (validation first), visual editor,
DB-backed dynamic behavior, arbitrary JSONC script replacement, quest DSL, behavior
trees, or cutscene DSL. MVP proves exactly one pipeline:
**Kotlin DSL → strict IR → validation → safe runtime → JSONC spawns.**

---

## 17. Data migration — MySQL → JSON

The ub3r server currently stores NPC definitions and spawns in MySQL tables
(`uber3_npcs`, etc.). These have been exported to JSON files for review and will need
migration work to align with the new JSONC-based system.

### Exported data files

| File | Source | Contents |
|------|--------|----------|
| `scripts/npc_Def.json` | MySQL `uber3_npcs` | All NPC definitions: id, name, examine, combat, emotes, hitpoints, respawn, size, stats |
| `scripts/npc_Spawn.json` | MySQL `uber3_npc_spawns` | All spawn locations: npcId, x, y, height, face, hitpoints, movechance |
| `data/def/monsters-json/<id>.json` | MySQL/NpcDropStore | Per-monster drop tables (3,248 files) |
| `data/def/items-json/<id>.json` | MySQL | Item definitions (26,990 files) |
| `data/def/npc/` | Kotlin sources | Generated NPC module Kotlin files |

### What needs migration

**Phase 1 — Audit the exported data:**
- `npc_Def.json` has ~8,000 entries — many have `hitpoints: 0` or `combat: 0` (incomplete)
- `npc_Spawn.json` has spawn data that needs to be converted to the new JSONC spawn schema
- Some spawns reference NPCs that don't exist in `npc_Def.json`

**Phase 2 — Convert to new schema:**
- NPC definitions → Kotlin `NpcTemplateModule` files (per §4 authoring surface)
- Spawn data → JSONC spawn files (per §7 spawn schema)
- Drop data → `drops {}` blocks in templates (per §10 drops)

**Phase 3 — Validate and load:**
- Server should validate all JSONC on boot (per §12 validation)
- Load NPC definitions from Kotlin modules (not MySQL)
- Load spawns from JSONC files (not MySQL)
- Load drops from templates (not NpcDropStore/monsters-json)

**Phase 4 — Deprecate MySQL:**
- Remove `uber3_npcs` table reads
- Remove `uber3_npc_spawns` table reads
- Keep MySQL for player data only

### Import tool

`scripts/import_tarnish_data.py` already handles:
- Parsing Tarnish server's Kotlin `NpcSpawnDef` format
- Converting facing constants to names
- Generating JSONC spawn files

This tool can be extended to handle the ub3r MySQL exports.

### Key migration decisions for senior dev

1. **Which NPCs get Kotlin modules?** → Complex NPCs (shops, quest, bosses) first
2. **Which NPCs stay as JSONC-only?** → Generic mobs (Man, Guard, Rat) that need no behavior
3. **How to handle the `hitpoints: 0` entries?** → Some are intentionally 0 (non-combat NPCs)
4. **How to handle duplicate NPC IDs?** → Different spawns may use same NPC ID
5. **What about the `monsters-json` drop files?** → Migrate into template `drops {}` blocks

---

## 18. Object Content — parallel system to NPC content

**Status:** high-level overview — senior dev designs final draft.

The server has a parallel content system for **game objects** (banks, ranges, altars,
doors, portals, stalls, etc.) that mirrors the NPC content system. When a player clicks
an object, the engine looks up a registered `ObjectContent` handler for that object ID.

### How it works today

1. **ObjectInteractionListener** (opcode 132) receives the click
2. **ObjectInteractionService** resolves the handler via `ObjectContentRegistry`
3. **ObjectClickLoggingService** logs "OBJECT UNHANDLED" if no handler is found
4. **ObjectClipService** handles collision/pathfinding overlays

### Current object content handlers

Registered via `ObjectContent` interface + `ObjectBinding` matchers:

- **Agility courses** — `FirstClickDslObjectContent` (gnome, barbarian, wilderness, werewolf)
- **Cooking ranges** — `RangeObjectContent` (use-item-on-object for cooking)
- **Smithing anvils** — `AnvilObjectContent` (click + use-item + magic)
- **Smithing furnaces** — `FurnaceObjectContent` (click + use-item + magic)
- **Prayer altars** — `AltarObjectContent` (click + use-item for prayer)
- **Thieving stalls** — `StallObjectContent` (click to thieve)
- **Thieving chests** — `ChestObjectContent` (click to thieve)
- **Thieving plunder** — `PlunderObjectContent` (pyramid plunder)

### What's MISSING (the "OBJECT UNHANDLED" problem)

The error logs show objects like **Bank booth (10355)** are unhandled because no
`ObjectContent` handler is registered. These are the missing categories:

**High priority (banking, core gameplay):**
- Bank booths — click to open bank (object IDs: 10355, 24009, 3193, etc.)
- Bank deposit boxes — click to deposit (object ID: 10529)
- Bank deposit chests — click to deposit (various IDs)

**Medium priority (skilling, portals):**
- Fairy rings — click to use (various IDs)
- Portal nexus — click to teleport (various IDs)
- Ladders/stairs — click to climb (various IDs)
- Doors — click to open/close (various IDs)
- Obstacles — click to pass (agility shortcuts)

**Low priority (decoration, misc):**
- Spinning wheels — use-item-on-object
- Looms — use-item-on-object
- Crafting benches — use-item-on-object
- Fishing spots — already handled via NPC system
- Mining rocks — already handled via NPC system

### Object content registration pattern

```kotlin
// In a skill file (e.g., Smithing.kt):
object BankBoothContent : ObjectContent {
    override fun bindings() = listOf(
        ObjectBinding(
            objectId = 10355,
            matcher = PositionMatcher.all(), // or specific positions
            handler = { ctx ->
                ctx.client.openBank()
                true
            }
        )
    )
}

// Auto-registered via classpath scan in ContentModuleIndex
```

### Key difference from NPC content

- NPCs have dialogue, shops, drops — complex behavior
- Objects have simpler behavior — click to open, use-item to skill
- Object content is registered by **object ID** (not NPC ID)
- Object content supports **position matchers** (same object at different positions)
- Object content supports **interaction types**: CLICK, ITEM_ON_OBJECT, MAGIC

### What the senior dev needs to design

1. **Bank booth handler** — most critical, opens bank interface
2. **Bank deposit handler** — deposits items from inventory
3. **Portal/ladder/door handlers** — teleportation and navigation
4. **Obstacle handlers** — agility shortcut pass-through
5. **Stall/chest/thieving handlers** — extend existing thieving system

### Reference: ObjectContent interface

```kotlin
interface ObjectContent {
    fun bindings(): List<ObjectBinding>
    fun clickInteractionPolicy(option: Int, objectId: Int, pos: Position, obj: GameObjectData?): ContentObjectInteractionPolicy?
    fun itemOnObjectInteractionPolicy(objectId: Int, pos: Position, obj: GameObjectData?, itemId: Int, itemSlot: Int, interfaceId: Int): ContentObjectInteractionPolicy?
    fun magicOnObjectInteractionPolicy(objectId: Int, pos: Position, obj: GameObjectData?, spellId: Int): ContentObjectInteractionPolicy?
}
```

### Reference: ObjectBinding

```kotlin
data class ObjectBinding(
    val objectId: Int,
    val matcher: PositionMatcher,
    val priority: Int = 0,
    val handler: (ObjectClickContext) -> Boolean,
)
```

---

## 19. TL;DR

Kotlin modules own behavior, dialogue, and drops. JSONC owns bulk coordinates and
conditional placement. Typed slots are the *only* per-spawn customization in MVP. The
engine refuses to boot on a broken reference and runs every interaction inside a
paranoid `ActiveNpcSession`. Ship behind a legacy bridge, port Aubury first, and migrate
NPC-by-NPC until the old engine is deletable. Build the validator test before the
compiler. Lock in V4 — no V5.

Object content follows the same pattern as NPC content: Kotlin handlers registered by
object ID, auto-discovered via classpath scan. Most critical missing handlers are bank
booths, deposit boxes, portals, and doors. Senior dev designs the final object content
module structure.

MySQL data (npc_Def.json, npc_Spawn.json, monsters-json/) needs migration to the new
Kotlin module + JSONC spawn system. Audit exports, convert to new schema, validate on
boot, then deprecate MySQL reads.
