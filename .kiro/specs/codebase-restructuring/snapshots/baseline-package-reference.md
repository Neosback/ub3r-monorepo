# Baseline Package Reference

This document provides a reference of the current package structure before refactoring begins.

## Kotlin Packages

### Top-Level Structure
- `net.dodian.uber.game.*` - Main game server code (Kotlin)
- `net.dodian.uber.webapi.*` - Web API endpoints (Kotlin)
- `net.dodian.utilities.*` - Utility classes (Kotlin)

### Game Package Structure
- `net.dodian.uber.game.bootstrap` - Application entry point
- `net.dodian.uber.game.config` - Configuration management
- `net.dodian.uber.game.engine` - Game loop and engine infrastructure
  - `engine.lifecycle` - Lifecycle management
  - `engine.loop` - Game loop implementation
  - `engine.metrics` - Performance metrics
  - `engine.net` - Network event handling
  - `engine.phases` - Game phases
  - `engine.processing` - Processing pipeline
  - `engine.scheduler` - Task scheduling
  - `engine.sync` - Synchronization
  - `engine.tasking` - Task management
- `net.dodian.uber.game.content` - Player-facing content
  - `content.commands` - Player commands (plural)
  - `content.dialogue` - Dialogue system
  - `content.event` - Game events
  - `content.interfaces` - UI interfaces (plural)
  - `content.items` - Item handlers (plural)
  - `content.npcs` - NPC handlers (plural)
  - `content.objects` - Object handlers (plural)
  - `content.skills` - Skill implementations (plural)
    - `skills.agility`
    - `skills.cooking`
    - `skills.crafting`
    - `skills.farming`
    - `skills.fishing`
    - `skills.fletching`
    - `skills.herblore`
    - `skills.mining`
    - `skills.prayer`
    - `skills.runecrafting`
    - `skills.slayer`
    - `skills.smithing`
    - `skills.thieving`
    - `skills.woodcutting`
- `net.dodian.uber.game.event` - Event bus
- `net.dodian.uber.game.model` - Core data models
  - `model.chunk`
  - `model.entity`
  - `model.item`
  - `model.music`
  - `model.object`
  - `model.player`
- `net.dodian.uber.game.persistence` - Database and save systems
- `net.dodian.uber.game.systems` - Gameplay systems
  - `systems.action`
  - `systems.animation`
  - `systems.combat`
  - `systems.interaction`
  - `systems.ui`
  - `systems.world`
  - `systems.zone`

### Utilities Package Structure
- `net.dodian.utilities` - Utility classes
  - `ISAACCipher.kt` - Cryptography
  - `MD5.kt` - Hashing
  - `Misc.kt` - Miscellaneous utilities
  - `Range.kt` - Range utility
  - `UtilityDirection.kt` - Direction utilities
  - `UtilityFormatting.kt` - Text formatting
  - `UtilityGeometry.kt` - Geometry calculations
  - `UtilityNames.kt` - Name utilities
  - `UtilityRandoms.kt` - Random number generation
  - `UtilityText.kt` - Text utilities
  - `Utils.kt` - General utilities

### Web API Package Structure
- `net.dodian.uber.webapi` - Web API endpoints

## Java Packages (To Be Preserved)

### Networking Infrastructure
- `net.dodian.uber.game.netty.*` - Netty networking code (Java)
  - Must remain unchanged during Kotlin refactoring

### Cache System
- `net.dodian.cache.*` - Cache system (Java)
  - Must remain unchanged during Kotlin refactoring

### Entity Models
- `net.dodian.uber.game.model.entity.*` - Entity models (Java)
  - Must remain unchanged during Kotlin refactoring

## File Counts

- Kotlin files: 831
- Java files: 197
- Total package directories: 207

## Notes

- All Kotlin code is under `net.dodian.uber.*` or `net.dodian.utilities.*`
- All Java code should remain unchanged during this refactoring
- Content packages use plural naming (commands, items, npcs, objects, interfaces, skills)
- Utility classes have "Utility" prefix (UtilityFormatting, UtilityRandoms, etc.)
- Skills are currently under `game.content.skills` but will be moved to `game.skill`
