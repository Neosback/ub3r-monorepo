# Skill Plugin Template

Use this as the default pattern when adding a new skill or extending an existing one.

## Module boundary

New skill code belongs in its owning Gradle project under `skills/<skill>`.
Use only `:skills:api`, `:skills:runtime`, and `:skills:testkit` from
there; the host bridge remains inside `game-server`. A module can never
import `Client`, send protocol packets, or mutate raw item arrays.

Run `./gradlew skillsCheck` before submitting skill work. The legacy sources
under `game-server/.../skill` are migration-only; do not add new behavior
there.

## Goals

- one clear entry point per skill: `object <Skill>NameSkillPlugin : SkillPlugin`
- route ownership declared in one place
- orchestration kept in shared runtime systems, not ad-hoc loops inside plugin files
- bridge helpers used when wrapping existing `ObjectContent` / `ItemContent`
- explicit `PolicyPreset` on every route binding
- concise authoring via one content-facing import surface where practical

## Preferred file shape

```kotlin
package net.dodian.uber.game.skill.example

import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.ContentModuleManifestProvider
import net.dodian.uber.game.api.content.ContentPlayer
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.api.plugin.skills.manifest
import net.dodian.uber.game.engine.systems.action.PolicyPreset

object ExampleSkill {
    @JvmStatic
    fun start(player: SkillPlayer, request: ExampleRequest): Boolean {
        // domain logic only
        return true
    }
}

object ExampleSkillPlugin : SkillPlugin, ContentModuleManifestProvider {
    override val definition =
        skillPlugin(name = "Example", skill = Skill.EXAMPLE) {
            objectClick(preset = PolicyPreset.GATHERING, option = 1, 1234) { interaction ->
                val player = interaction.player
                if (!player.inventory.contains(5678)) {
                    player.ui.message("You need the required item.")
                    false
                } else {
                    player.actions.animate(867)
                    player.skills.gainXp(25, Skill.EXAMPLE)
                }
            }
        }

    // Route keys are derived from the typed definition; no duplicate id list.
    override val contentManifest = definition.manifest(
        id = "skill.example",
        owner = "gameplay",
        featureFlag = "example-skill",
    )
}
```

## Route selection guide

### Single import surface for content helpers

For general content helpers (events/actions/scheduling), prefer:

- `import net.dodian.uber.game.api.content.ContentPredef.*`

This keeps module code concise while preserving strict plugin route ownership.

### Use direct plugin DSL when the skill is already plugin-native

Prefer these when the handler is simple and does not need a compatibility wrapper:

- `objectClick(...)`
- `npcClick(...)`
- `itemOnItem(...)`
- `itemClick(...)`
- `itemOnObject(...)`
- `magicOnObject(...)`
- `button(...)`

### Use bridge helpers when wrapping existing content objects

Prefer these while migrating old skills or when a content object is still shared elsewhere:

- `bindObjectContentClick(...)`
- `bindObjectContentMagic(...)`
- `bindObjectContentUseItem(...)`
- `bindItemContentClick(...)`

## Conventions

### 0. Mandatory module contract

Each plugin-owned skill module should expose:

- `<Skill>Data.kt` with route ids/constants and policy defaults (for example `object <Skill>RouteIds`)
- `<Skill>Actions.kt` with stable action identifiers (for example `object <Skill>ActionIds`)
- `<Skill>.kt` with `object <Skill>SkillPlugin : SkillPlugin` as the route ownership entrypoint

New modules must also implement `ContentModuleManifestProvider`. Use
`definition.manifest(...)`; it derives the declared route keys so validation
cannot drift from the routes that actually register. Existing modules are
reported as `LEGACY` until they add one.

Do not scatter route id arrays and action id strings across multiple files.

### 1. Keep exported surface small and plugin-owned

Expose only:

- the skill domain object (`ExampleSkill`)
- the plugin (`ExampleSkillPlugin`)

Keep wrapper content objects `internal` unless another package truly needs them.

Prefer private wrapper classes instantiated inside `*SkillPlugin` (plugin-owned instances). Avoid singleton `object ... : ObjectContent` wrappers in skill modules.

Do not import, accept, or expose `Client` from a skill module. Use the typed
interaction object and its `SkillPlayer` capabilities instead. The dispatcher
is the only layer allowed to adapt a protocol player into skill content.

`SkillPlayer` is also a `ContentPlayer`. Use its typed capabilities for common
content work: `inventory`, `equipment`, `economy`, `actions`, `ui`, `world`,
`social`, and `features`. Do not add direct packet or entity-field access to
avoid a missing capability; extend the façade instead.

### 2. Put behavior in the domain object, not the plugin body

Good:

```kotlin
objectClick(preset = PolicyPreset.GATHERING, option = 1, 1234) { interaction ->
    ExampleSkill.start(interaction.player, ExampleRequest(interaction.objectId, interaction.position))
}
```

Avoid large inline lambdas with lots of orchestration.

### 3. Always declare `preset = PolicyPreset...`

This is required for consistent routing and audit checks.

### 4. Avoid legacy ownership split

Do not introduce new skill behavior that only lives in:

- `ObjectContentRegistry`
- `ItemContentRegistry`
- direct packet listeners
- ad-hoc branches in `InteractionProcessor`

If it is skill-owned, route it through the skill plugin system.

### 4.1 Dialogue routing for new modules

For new content modules, route dialogue through:

- `NpcDialogueDsl` for NPC option flows
- `DialogueFactory`/`DialogueService` for general dialogue chains

Avoid introducing new direct `Client.showNPCChat(...)` / `Client.showPlayerChat(...)` usage.

### 5. Prefer shared runtime actions

Use the runtime helpers already in place for loops/cycles:

- gathering/production queues
- `ContentActions`
- skill runtime action helpers
- progression/random event services

Do not build new `while (true)` loops or ad-hoc player action schedulers inside plugins.

## Current migration rule of thumb

For existing mixed-mode skills:

1. keep legacy wrapper behavior temporarily if needed, but prefer class wrappers over singleton objects
2. register those wrappers through the skill plugin bridge helpers
3. once stable, inline or remove wrappers if they no longer add value

## Magic-on-object guidance

`magicOnObject(...)` is now a first-class skill plugin route.

Use it directly for plugin-native skills, or `bindObjectContentMagic(...)` while migrating existing `ObjectContent` wrappers. Prefer explicit spell ids over wildcard spell ownership unless the whole object is genuinely owned by one skill for all spells.
