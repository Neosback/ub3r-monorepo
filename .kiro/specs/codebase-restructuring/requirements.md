# Requirements Document

## Introduction

This document defines requirements for restructuring the Dodian game server Kotlin codebase to improve developer experience, align with RSPS community conventions, and establish clear package boundaries. The refactoring focuses on "easy wins first" - starting with naming cleanup and package reorganization before tackling deeper architectural changes.

The restructuring will transform the current `net.dodian.uber` package structure into a more intuitive `net.dodian` hierarchy that separates content (what players do) from core systems (what the server is), while maintaining backward compatibility during migration.

## Glossary

- **Refactoring_Tool**: The automated refactoring system that performs package and class renaming operations
- **Package_Structure**: The hierarchical organization of Kotlin source files under net.dodian
- **Content_Code**: Game features that define player interactions (commands, dialogues, items, NPCs, objects, skills)
- **Core_System**: Runtime infrastructure that powers the game engine (entities, world, combat, synchronization)
- **RSPS**: RuneScape Private Server - the game server type being developed
- **Utility_Module**: Helper classes providing cross-cutting functionality (math, text, collections, validation)
- **Migration_Phase**: A discrete stage of the refactoring process with specific scope and deliverables
- **Dodian_Conventions**: Package naming and organization patterns familiar to Dodian/Winterlove RSPS developers

## Requirements

### Requirement 1: Remove "uber" Package Segment

**User Story:** As a developer, I want the base package to be `net.dodian` instead of `net.dodian.uber`, so that the package structure is cleaner and more professional.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL rename all packages from `net.dodian.uber.*` to `net.dodian.*`
2. THE Refactoring_Tool SHALL update all import statements to reference the new package paths
3. WHEN the refactoring is complete, THE Package_Structure SHALL contain no references to "uber"
4. THE Refactoring_Tool SHALL preserve all class names and file locations within their respective packages

### Requirement 2: Standardize Utility Package Naming

**User Story:** As a developer, I want consistent utility class naming, so that I can quickly locate helper functions without confusion.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL rename `net.dodian.utilities` to `net.dodian.util`
2. THE Refactoring_Tool SHALL remove the "Utility" prefix from all utility classes (UtilityFormatting → Formatting, UtilityRandoms → Randoms)
3. THE Refactoring_Tool SHALL update all references to renamed utility classes throughout the codebase
4. WHEN utility classes are renamed, THE Refactoring_Tool SHALL preserve all public method signatures
5. THE Refactoring_Tool SHALL organize utility classes into domain-specific subpackages (util.math, util.text, util.collection, util.time, util.validation)

### Requirement 3: Reorganize Content Packages to Singular Form

**User Story:** As a developer, I want content packages to use singular naming (command, item, npc), so that the structure follows standard Kotlin conventions and is easier to navigate.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL rename `game.content.commands` to `game.content.command`
2. THE Refactoring_Tool SHALL rename `game.content.interfaces` to `game.content.interface`
3. THE Refactoring_Tool SHALL rename `game.content.items` to `game.content.item`
4. THE Refactoring_Tool SHALL rename `game.content.npcs` to `game.content.npc`
5. THE Refactoring_Tool SHALL rename `game.content.objects` to `game.content.object`
6. THE Refactoring_Tool SHALL rename `game.content.skills` to `game.skill`
7. THE Refactoring_Tool SHALL update all import statements and package declarations to reflect the new singular naming
8. WHEN content packages are renamed, THE Refactoring_Tool SHALL preserve the internal structure of each package

### Requirement 4: Separate Skills from Content

**User Story:** As a developer, I want skills to be organized under `game.skill` instead of `game.content.skills`, so that core game systems are distinguished from player-facing content.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL move all skill-related packages from `game.content.skills` to `game.skill`
2. THE Refactoring_Tool SHALL create a `game.skill.core` package for shared skill infrastructure
3. WHEN skills are relocated, THE Refactoring_Tool SHALL maintain individual skill package names (agility, cooking, crafting, fishing, mining, smithing, woodcutting, etc.)
4. THE Refactoring_Tool SHALL update all references to skill classes throughout the codebase

### Requirement 5: Preserve Java Networking Code Location

**User Story:** As a developer, I want Java networking code to remain in its current location, so that we avoid risky changes to stable networking infrastructure during this refactoring.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL NOT modify any Java source files during Kotlin package restructuring
2. THE Refactoring_Tool SHALL NOT move or rename packages containing networking, codec, or session management code written in Java
3. WHEN Kotlin code references Java networking classes, THE Refactoring_Tool SHALL preserve those references unchanged

### Requirement 6: Maintain Backward Compatibility During Migration

**User Story:** As a developer, I want the codebase to remain compilable and runnable during refactoring, so that I can test changes incrementally without breaking the server.

#### Acceptance Criteria

1. WHEN a package is renamed, THE Refactoring_Tool SHALL update all references in the same operation
2. THE Refactoring_Tool SHALL verify that the codebase compiles successfully after each major refactoring step
3. IF compilation errors are detected, THEN THE Refactoring_Tool SHALL report the errors with file locations and descriptions
4. THE Refactoring_Tool SHALL preserve all public API signatures during package reorganization

### Requirement 7: Organize Content Subpackages by Feature Domain

**User Story:** As a developer, I want content subpackages organized by clear feature domains, so that related functionality is grouped together logically.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL organize command classes into domain subpackages (command.dev, command.player, command.staff, command.social, command.travel)
2. THE Refactoring_Tool SHALL organize item classes into behavior subpackages (item.consumable, item.equipment, item.combination, item.spawn)
3. THE Refactoring_Tool SHALL organize NPC classes into behavior subpackages (npc.interaction, npc.spawn, npc.shop)
4. THE Refactoring_Tool SHALL organize object classes into behavior subpackages (object.interaction, object.travel, object.banking)
5. WHEN content is reorganized, THE Refactoring_Tool SHALL preserve existing class names and functionality

### Requirement 8: Establish Clear Package Boundaries

**User Story:** As a developer, I want clear separation between content, core systems, engine, and utilities, so that I understand where to add new features and where to find existing code.

#### Acceptance Criteria

1. THE Package_Structure SHALL contain a top-level `engine` package for game loop, event bus, scheduler, and task management
2. THE Package_Structure SHALL contain a top-level `game` package for gameplay systems (entity, world, combat, skill, content)
3. THE Package_Structure SHALL contain a top-level `util` package for cross-cutting helper functions
4. THE Package_Structure SHALL contain a top-level `config` package for configuration and environment management
5. THE Refactoring_Tool SHALL move engine-related code from `game.engine` to the top-level `engine` package
6. THE Refactoring_Tool SHALL move configuration code from `game.config` to the top-level `config` package

### Requirement 9: Document Package Purpose and Conventions

**User Story:** As a developer, I want documentation explaining the purpose of each major package, so that I know where to place new code and understand the organizational principles.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL create a package documentation file at `docs/development/package-structure.md`
2. THE package documentation SHALL describe the purpose of each top-level package (bootstrap, config, engine, game, util, tools)
3. THE package documentation SHALL provide examples of what belongs in content vs core systems
4. THE package documentation SHALL explain the singular naming convention for packages
5. THE package documentation SHALL include a visual tree representation of the target package structure

### Requirement 10: Prioritize Low-Risk Easy Wins

**User Story:** As a developer, I want the refactoring to start with low-risk changes, so that we build confidence and momentum before tackling complex restructuring.

#### Acceptance Criteria

1. THE Refactoring_Tool SHALL execute package renaming in phases, starting with Phase 1 (naming cleanup) and Phase 2 (content reorganization)
2. THE Refactoring_Tool SHALL NOT attempt Phase 3 (domain extraction) or Phase 4 (data cleanup) until Phase 1 and Phase 2 are complete and verified
3. WHEN a phase is complete, THE Refactoring_Tool SHALL verify compilation and report any issues before proceeding
4. THE Refactoring_Tool SHALL provide a rollback strategy for each phase in case issues are discovered

### Requirement 11: Preserve RSPS-Familiar Terminology

**User Story:** As an RSPS developer, I want the codebase to use terminology familiar to the RSPS community, so that new contributors can onboard quickly.

#### Acceptance Criteria

1. THE Package_Structure SHALL use RSPS-standard terms (npc, object, item, skill, combat, world, entity)
2. THE Refactoring_Tool SHALL NOT rename domain concepts to generic software engineering terms (e.g., keep "npc" not "agent", keep "object" not "interactable")
3. THE Package_Structure SHALL maintain recognizable RSPS patterns (content.command, content.dialogue, skill.agility, skill.mining)
4. WHERE RSPS conventions conflict with standard Kotlin conventions, THE Package_Structure SHALL favor RSPS conventions for domain code

### Requirement 12: Eliminate Ambiguous Package Names

**User Story:** As a developer, I want to avoid ambiguous or meaningless package names, so that I can understand code organization without guessing.

#### Acceptance Criteria

1. THE Package_Structure SHALL NOT contain packages named "misc", "stuff", "common", or "shared"
2. THE Package_Structure SHALL NOT contain packages named "impl" unless they implement a clearly defined interface or abstraction
3. IF a package contains mixed responsibilities, THEN THE Refactoring_Tool SHALL split it into focused subpackages with clear names
4. THE Refactoring_Tool SHALL rename any existing ambiguous packages to descriptive names based on their actual contents

