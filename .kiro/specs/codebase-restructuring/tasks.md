# Implementation Plan: Codebase Restructuring

## Overview

This plan implements a two-phase refactoring of the Dodian game server Kotlin codebase from `net.dodian.uber.*` to `net.dodian.*`. The refactoring uses IntelliJ IDEA's built-in refactoring tools to ensure all references are updated atomically. Each phase includes compilation verification and property-based testing to validate correctness.

Phase 1 focuses on removing the "uber" package segment and standardizing utility naming. Phase 2 reorganizes content packages to singular form and separates skills from content. The approach prioritizes low-risk changes and maintains backward compatibility throughout.

## Tasks

- [-] 1. Pre-refactoring preparation and baseline snapshot
  - Create Git commit with tag `refactor-baseline`
  - Create snapshot of current package structure for comparison
  - Create snapshot of all public API signatures
  - Create snapshot of all Java file contents
  - Document current package paths in a reference file
  - _Requirements: 6.1, 6.2, 10.3_

- [ ] 2. Phase 1: Rename utilities package and organize utility classes
  - [~] 2.1 Rename utilities package to util
    - Use IntelliJ: Right-click `net.dodian.utilities` → Refactor → Rename → `util`
    - Verify all import statements updated automatically
    - _Requirements: 2.1, 2.3_
  
  - [~] 2.2 Create utility domain subpackages
    - Create `net.dodian.util.math` package
    - Create `net.dodian.util.text` package
    - Create `net.dodian.util.crypto` package
    - _Requirements: 2.5_
  
  - [~] 2.3 Move and rename UtilityRandoms to util.math.Randoms
    - Use IntelliJ: Right-click `UtilityRandoms` → Refactor → Move → `util.math`
    - Use IntelliJ: Right-click `UtilityRandoms` → Refactor → Rename → `Randoms`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.4 Move and rename UtilityGeometry to util.math.Geometry
    - Use IntelliJ: Right-click `UtilityGeometry` → Refactor → Move → `util.math`
    - Use IntelliJ: Right-click `UtilityGeometry` → Refactor → Rename → `Geometry`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.5 Move Range to util.math
    - Use IntelliJ: Right-click `Range` → Refactor → Move → `util.math`
    - _Requirements: 2.5_
  
  - [~] 2.6 Move and rename UtilityFormatting to util.text.Formatting
    - Use IntelliJ: Right-click `UtilityFormatting` → Refactor → Move → `util.text`
    - Use IntelliJ: Right-click `UtilityFormatting` → Refactor → Rename → `Formatting`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.7 Move and rename UtilityText to util.text.Text
    - Use IntelliJ: Right-click `UtilityText` → Refactor → Move → `util.text`
    - Use IntelliJ: Right-click `UtilityText` → Refactor → Rename → `Text`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.8 Move and rename UtilityNames to util.text.Names
    - Use IntelliJ: Right-click `UtilityNames` → Refactor → Move → `util.text`
    - Use IntelliJ: Right-click `UtilityNames` → Refactor → Rename → `Names`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.9 Move ISAACCipher and MD5 to util.crypto
    - Use IntelliJ: Right-click `ISAACCipher` → Refactor → Move → `util.crypto`
    - Use IntelliJ: Right-click `MD5` → Refactor → Move → `util.crypto`
    - _Requirements: 2.5_
  
  - [~] 2.10 Move UtilityDirection to util root and rename to Direction
    - Use IntelliJ: Right-click `UtilityDirection` → Refactor → Move → `util`
    - Use IntelliJ: Right-click `UtilityDirection` → Refactor → Rename → `Direction`
    - _Requirements: 2.2, 2.3_
  
  - [~] 2.11 Analyze and distribute Misc.kt contents
    - Review all functions in Misc.kt
    - Move functions to appropriate domain packages (math, text, etc.)
    - If functions are truly miscellaneous, keep in util root temporarily
    - Mark remaining miscellaneous code for future cleanup
    - _Requirements: 2.5_
  
  - [~] 2.12 Analyze and distribute Utils.kt contents
    - Review all functions in Utils.kt
    - Move functions to appropriate domain packages
    - If functions are truly miscellaneous, keep in util root temporarily
    - Mark remaining miscellaneous code for future cleanup
    - _Requirements: 2.5_

- [ ] 3. Phase 1: Remove "uber" package segment
  - [~] 3.1 Remove uber from game package
    - Use IntelliJ: Right-click `net.dodian.uber.game` → Refactor → Move → `net.dodian.game`
    - Verify all Kotlin import statements updated
    - Verify Kotlin references to Java classes preserved
    - _Requirements: 1.1, 1.2, 5.3_
  
  - [~] 3.2 Remove uber from webapi package
    - Use IntelliJ: Right-click `net.dodian.uber.webapi` → Refactor → Move → `net.dodian.webapi`
    - Verify all import statements updated
    - _Requirements: 1.1, 1.2_
  
  - [~] 3.3 Verify no "uber" references remain in Kotlin code
    - Use IntelliJ: Search Everywhere → "uber" in Kotlin files
    - Check for string literals containing "uber" (reflection, logging)
    - Update any string literals manually if found
    - _Requirements: 1.3_

- [ ] 4. Phase 1: Compilation verification and testing
  - [~] 4.1 Verify compilation succeeds
    - Run `./gradlew compileKotlin`
    - If errors occur, analyze and fix before proceeding
    - _Requirements: 6.2, 10.3_
  
  - [ ]* 4.2 Write property test for Property 1: No "uber" package segment
    - **Property 1: No "uber" Package Segment**
    - **Validates: Requirements 1.1, 1.3**
    - Use Kotest property testing to sample all package declarations
    - Verify no package path contains "uber"
  
  - [ ]* 4.3 Write property test for Property 2: No "Utility" prefix
    - **Property 2: No "Utility" Prefix in Util Classes**
    - **Validates: Requirements 2.2**
    - Sample all class names in net.dodian.util package
    - Verify no class name starts with "Utility"
  
  - [ ]* 4.4 Write property test for Property 3: Package structure preservation
    - **Property 3: Package Structure Preservation During Rename**
    - **Validates: Requirements 1.4, 3.8**
    - Compare before/after snapshots of renamed packages
    - Verify internal structure remains identical
  
  - [ ]* 4.5 Write property test for Property 4: Public API signature preservation
    - **Property 4: Public API Signature Preservation**
    - **Validates: Requirements 2.4, 6.4**
    - Compare public API signatures before/after
    - Verify signatures match exactly
  
  - [ ]* 4.6 Write property test for Property 5: Java source file preservation
    - **Property 5: Java Source File Preservation**
    - **Validates: Requirements 5.1**
    - Compare Java file contents before/after
    - Verify byte-for-byte identical
  
  - [ ]* 4.7 Write property test for Property 6: Java networking package location preservation
    - **Property 6: Java Networking Package Location Preservation**
    - **Validates: Requirements 5.2**
    - Verify net.dodian.uber.game.netty and net.dodian.cache exist at original paths
  
  - [~] 4.8 Create Git commit for Phase 1 completion
    - Commit all changes with message "Phase 1: Remove uber segment and standardize utilities"
    - Create tag `refactor-phase1-complete`
    - _Requirements: 10.3_

- [~] 5. Checkpoint - Ensure Phase 1 tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Phase 2: Reorganize content packages to singular form
  - [~] 6.1 Rename commands to command
    - Use IntelliJ: Right-click `game.content.commands` → Refactor → Rename → `command`
    - Verify all import statements and package declarations updated
    - _Requirements: 3.1, 3.7_
  
  - [~] 6.2 Rename interfaces to interface
    - Use IntelliJ: Right-click `game.content.interfaces` → Refactor → Rename → `interface`
    - Verify all references updated
    - _Requirements: 3.2, 3.7_
  
  - [~] 6.3 Rename items to item
    - Use IntelliJ: Right-click `game.content.items` → Refactor → Rename → `item`
    - Verify all references updated
    - _Requirements: 3.3, 3.7_
  
  - [~] 6.4 Rename npcs to npc
    - Use IntelliJ: Right-click `game.content.npcs` → Refactor → Rename → `npc`
    - Verify all references updated
    - _Requirements: 3.4, 3.7_
  
  - [~] 6.5 Rename objects to object
    - Use IntelliJ: Right-click `game.content.objects` → Refactor → Rename → `object`
    - Verify all references updated
    - _Requirements: 3.5, 3.7_

- [ ] 7. Phase 2: Separate skills from content
  - [~] 7.1 Move skills package to game.skill
    - Use IntelliJ: Right-click `game.content.skills` → Refactor → Move → `game.skill`
    - Verify all skill package names preserved (agility, cooking, crafting, etc.)
    - _Requirements: 3.6, 4.1, 4.3_
  
  - [~] 7.2 Create game.skill.core package for shared infrastructure
    - Create `game.skill.core` package
    - Identify shared skill infrastructure code
    - Move shared code to skill.core if it exists
    - _Requirements: 4.2_
  
  - [~] 7.3 Verify all skill references updated
    - Use IntelliJ: Search for old skill package references
    - Update any remaining references manually
    - _Requirements: 4.4_

- [ ] 8. Phase 2: Organize content subpackages by feature domain
  - [~] 8.1 Organize command subpackages
    - Create subpackages: command.dev, command.player, command.staff, command.social, command.travel
    - Move command classes to appropriate subpackages based on functionality
    - _Requirements: 7.1, 7.5_
  
  - [~] 8.2 Organize item subpackages
    - Create subpackages: item.consumable, item.equipment, item.combination, item.spawn
    - Move item classes to appropriate subpackages based on behavior
    - _Requirements: 7.2, 7.5_
  
  - [~] 8.3 Organize NPC subpackages
    - Create subpackages: npc.interaction, npc.spawn, npc.shop
    - Move NPC classes to appropriate subpackages based on behavior
    - _Requirements: 7.3, 7.5_
  
  - [~] 8.4 Organize object subpackages
    - Create subpackages: object.interaction, object.travel, object.banking
    - Move object classes to appropriate subpackages based on behavior
    - _Requirements: 7.4, 7.5_

- [ ] 9. Phase 2: Elevate engine and config to top-level packages
  - [~] 9.1 Move game.engine to top-level engine package
    - Use IntelliJ: Right-click `game.engine` → Refactor → Move → `net.dodian.engine`
    - Verify all subpackages preserved (lifecycle, loop, metrics, scheduler, sync, tasking)
    - _Requirements: 8.1, 8.5_
  
  - [~] 9.2 Move game.config to top-level config package
    - Use IntelliJ: Right-click `game.config` → Refactor → Move → `net.dodian.config`
    - Verify all configuration code moved correctly
    - _Requirements: 8.4, 8.6_
  
  - [~] 9.3 Verify package boundaries are clear
    - Review top-level packages: bootstrap, config, engine, game, util, webapi
    - Ensure each package has clear responsibility
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 10. Phase 2: Compilation verification and testing
  - [~] 10.1 Verify compilation succeeds
    - Run `./gradlew compileKotlin`
    - If errors occur, analyze and fix before proceeding
    - _Requirements: 6.2, 10.3_
  
  - [ ]* 10.2 Write property test for Property 7: RSPS terminology maintenance
    - **Property 7: RSPS Terminology and Pattern Maintenance**
    - **Validates: Requirements 11.1, 11.3**
    - Sample domain packages
    - Verify package names use RSPS terms (npc, object, item, skill)
    - Verify packages follow RSPS patterns (content.command, skill.agility)
  
  - [ ]* 10.3 Write property test for Property 8: No ambiguous package names
    - **Property 8: No Ambiguous Package Names**
    - **Validates: Requirements 12.1**
    - Sample all package names
    - Verify no packages named "misc", "stuff", "common", or "shared"
  
  - [ ]* 10.4 Run all property tests from Phase 1
    - Re-run property tests 1-6 to ensure Phase 2 didn't break Phase 1 guarantees
    - Verify all properties still hold
  
  - [~] 10.5 Create Git commit for Phase 2 completion
    - Commit all changes with message "Phase 2: Reorganize content and separate skills"
    - Create tag `refactor-phase2-complete`
    - _Requirements: 10.3_

- [~] 11. Checkpoint - Ensure Phase 2 tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Generate package structure documentation
  - [~] 12.1 Create package structure documentation file
    - Create file at `docs/development/package-structure.md`
    - _Requirements: 9.1_
  
  - [~] 12.2 Document top-level package purposes
    - Document purpose of bootstrap, config, engine, game, util, tools, webapi
    - Provide clear descriptions of what belongs in each package
    - _Requirements: 9.2_
  
  - [~] 12.3 Document content vs core systems distinction
    - Explain what qualifies as "content" (player-facing features)
    - Explain what qualifies as "core systems" (infrastructure)
    - Provide examples of each category
    - _Requirements: 9.3_
  
  - [~] 12.4 Document singular naming convention
    - Explain why packages use singular form (command, item, npc)
    - Provide rationale for convention
    - _Requirements: 9.4_
  
  - [~] 12.5 Create visual tree representation
    - Generate ASCII tree or markdown tree of final package structure
    - Include all major packages and key subpackages
    - _Requirements: 9.5_

- [ ] 13. Post-refactoring verification
  - [~] 13.1 Run full test suite
    - Run `./gradlew test`
    - Verify all existing tests still pass
    - _Requirements: 6.2_
  
  - [~] 13.2 Verify server starts successfully
    - Start the game server
    - Check for any startup errors or warnings
    - Verify no missing class errors
    - _Requirements: 6.2_
  
  - [~] 13.3 Perform smoke testing
    - Test player login
    - Test command execution
    - Test skill functionality
    - Test item and NPC interactions
    - Test combat system
    - Test database persistence
    - _Requirements: 6.2_
  
  - [~] 13.4 Search for remaining issues
    - Use IntelliJ: Search for "uber" in all files (including comments)
    - Use IntelliJ: Search for "Utility" prefix in util package
    - Use IntelliJ: Search for ambiguous package names (misc, stuff, common)
    - Update any remaining references
    - _Requirements: 1.3, 2.2, 12.1_

- [~] 14. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster completion
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at phase boundaries
- Use IntelliJ IDEA's refactoring tools for all package and class renames to ensure atomic updates
- Create Git commits at phase boundaries for rollback capability
- Property tests validate universal correctness properties across the entire codebase
- Manual smoke testing complements automated testing to catch runtime issues
