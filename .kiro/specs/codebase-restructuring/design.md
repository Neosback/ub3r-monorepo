# Design Document: Codebase Restructuring

## Overview

This design outlines a phased approach to restructuring the Dodian game server Kotlin codebase from `net.dodian.uber.*` to a cleaner `net.dodian.*` hierarchy. The refactoring prioritizes low-risk "easy wins" - starting with package naming cleanup and content reorganization before tackling deeper architectural changes.

The restructuring addresses three primary goals:

1. **Developer Experience**: Remove the awkward "uber" package segment and establish intuitive package boundaries
2. **RSPS Conventions**: Align with RuneScape Private Server community standards while following Kotlin best practices
3. **Maintainability**: Separate content (what players do) from core systems (what the server is) with clear organizational principles

The design implements a two-phase migration strategy:

- **Phase 1**: Remove "uber" package segment and standardize utility naming
- **Phase 2**: Reorganize content packages to singular form and separate skills from content

Each phase maintains backward compatibility through atomic refactoring operations that update all references simultaneously. The approach leverages Kotlin's IDE refactoring tools to ensure compilation succeeds after each major step.

## Architecture

### Package Hierarchy Transformation

The refactoring transforms the current structure:

```
net.dodian.uber.game.*
net.dodian.uber.webapi.*
net.dodian.utilities.*
```

Into a cleaner hierarchy:

```
net.dodian.bootstrap.*
net.dodian.config.*
net.dodian.engine.*
net.dodian.game.*
net.dodian.util.*
net.dodian.webapi.*
```

### Phased Migration Strategy

**Phase 1: Naming Cleanup**
- Remove "uber" package segment from all Kotlin code
- Rename `utilities` → `util`
- Remove "Utility" prefix from utility classes
- Organize utilities into domain-specific subpackages

**Phase 2: Content Reorganization**
- Convert plural content packages to singular form (commands → command, items → item, etc.)
- Move skills from `game.content.skills` to `game.skill`
- Organize content subpackages by feature domain
- Elevate engine and config to top-level packages

### Refactoring Tool Strategy

The design uses IntelliJ IDEA's built-in refactoring capabilities:

1. **Package Rename**: Right-click package → Refactor → Rename
2. **Class Rename**: Right-click class → Refactor → Rename
3. **Move Package**: Right-click package → Refactor → Move
4. **Search Everywhere**: Verify no references remain to old names

Each refactoring operation automatically updates:
- Package declarations
- Import statements
- Fully qualified references
- Documentation references

### Preservation Boundaries

**Java Code Preservation**:
- All Java networking code in `game-server/src/main/java/net/dodian/uber/game/netty/` remains unchanged
- Cache system in `game-server/src/main/java/net/dodian/cache/` remains unchanged
- Java entity models remain in their current location
- Kotlin code referencing Java classes will be updated to match any Java package changes

**Compilation Verification**:
- Run `./gradlew compileKotlin` after each major refactoring step
- Address any compilation errors before proceeding to next operation
- Maintain a rollback strategy using Git commits at phase boundaries

## Components and Interfaces

### Refactoring Execution Components

**Phase1Executor**
- Responsibility: Execute all Phase 1 refactoring operations
- Operations:
  - Remove "uber" from all package paths
  - Rename utilities package and classes
  - Organize utilities into subpackages
- Verification: Ensure compilation succeeds after each operation

**Phase2Executor**
- Responsibility: Execute all Phase 2 refactoring operations
- Operations:
  - Convert content packages to singular form
  - Move skills to top-level game.skill package
  - Organize content subpackages by domain
  - Elevate engine and config packages
- Verification: Ensure compilation succeeds after each operation

**CompilationVerifier**
- Responsibility: Verify codebase compiles after refactoring operations
- Interface:
  ```kotlin
  fun verifyCompilation(): CompilationResult
  data class CompilationResult(
      val success: Boolean,
      val errors: List<CompilationError>
  )
  data class CompilationError(
      val file: String,
      val line: Int,
      val message: String
  )
  ```

**PackageDocumentationGenerator**
- Responsibility: Generate package structure documentation
- Output: `docs/development/package-structure.md`
- Content:
  - Purpose of each top-level package
  - Examples of content vs core systems
  - Visual tree representation
  - Naming conventions and organizational principles

### Package Organization Principles

**Top-Level Package Purposes**:

- `bootstrap`: Application entry point and initialization
- `config`: Configuration management and environment variables
- `engine`: Game loop, scheduler, task management, synchronization
- `game`: Gameplay systems (entity, world, combat, skill, content)
- `util`: Cross-cutting helper functions
- `webapi`: External API endpoints
- `tools`: Development and administrative utilities

**Content vs Core Systems**:

- **Content** (`game.content.*`): Player-facing features that define what players can do
  - Commands, dialogues, items, NPCs, objects, interfaces, events
  - Organized by feature domain (command.dev, item.consumable, npc.shop)
  
- **Core Systems** (`game.*` excluding content): Infrastructure that powers the game
  - Entity models, world management, combat mechanics
  - Organized by system responsibility (entity, world, combat)

- **Skills** (`game.skill.*`): Hybrid category elevated from content
  - Core skill infrastructure in `game.skill.core`
  - Individual skills as subpackages (agility, cooking, mining, etc.)

### Utility Package Organization

The utilities will be reorganized from flat structure to domain-specific subpackages:

**Current Structure**:
```
net.dodian.utilities/
  UtilityFormatting.kt
  UtilityRandoms.kt
  UtilityText.kt
  UtilityGeometry.kt
  UtilityDirection.kt
  UtilityNames.kt
  Misc.kt
  Utils.kt
  ISAACCipher.kt
  MD5.kt
  Range.kt
```

**Target Structure**:
```
net.dodian.util/
  math/
    Randoms.kt
    Geometry.kt
    Range.kt
  text/
    Formatting.kt
    Text.kt
    Names.kt
  collection/
    (future utilities)
  time/
    (future utilities)
  validation/
    (future utilities)
  crypto/
    ISAACCipher.kt
    MD5.kt
  Direction.kt
```

**Migration Strategy for Misc.kt and Utils.kt**:
- Analyze contents and distribute functions to appropriate domain packages
- If functions are truly miscellaneous, keep in util root temporarily
- Mark for future cleanup in Phase 3 or Phase 4

## Data Models

### Package Mapping Model

The refactoring requires tracking package transformations:

```kotlin
data class PackageMapping(
    val oldPath: String,
    val newPath: String,
    val phase: MigrationPhase
)

enum class MigrationPhase {
    PHASE_1_NAMING,
    PHASE_2_CONTENT,
    PHASE_3_DOMAIN,  // Future
    PHASE_4_DATA     // Future
}
```

### Phase 1 Package Mappings

```kotlin
val phase1Mappings = listOf(
    // Remove uber segment
    PackageMapping(
        oldPath = "net.dodian.uber.game",
        newPath = "net.dodian.game",
        phase = PHASE_1_NAMING
    ),
    PackageMapping(
        oldPath = "net.dodian.uber.webapi",
        newPath = "net.dodian.webapi",
        phase = PHASE_1_NAMING
    ),
    
    // Rename utilities
    PackageMapping(
        oldPath = "net.dodian.utilities",
        newPath = "net.dodian.util",
        phase = PHASE_1_NAMING
    )
)
```

### Phase 1 Class Mappings

```kotlin
val phase1ClassMappings = listOf(
    ClassMapping(
        oldName = "UtilityFormatting",
        newName = "Formatting",
        newPackage = "net.dodian.util.text"
    ),
    ClassMapping(
        oldName = "UtilityRandoms",
        newName = "Randoms",
        newPackage = "net.dodian.util.math"
    ),
    ClassMapping(
        oldName = "UtilityText",
        newName = "Text",
        newPackage = "net.dodian.util.text"
    ),
    ClassMapping(
        oldName = "UtilityGeometry",
        newName = "Geometry",
        newPackage = "net.dodian.util.math"
    ),
    ClassMapping(
        oldName = "UtilityDirection",
        newName = "Direction",
        newPackage = "net.dodian.util"
    ),
    ClassMapping(
        oldName = "UtilityNames",
        newName = "Names",
        newPackage = "net.dodian.util.text"
    )
)
```

### Phase 2 Package Mappings

```kotlin
val phase2Mappings = listOf(
    // Content packages to singular
    PackageMapping(
        oldPath = "net.dodian.game.content.commands",
        newPath = "net.dodian.game.content.command",
        phase = PHASE_2_CONTENT
    ),
    PackageMapping(
        oldPath = "net.dodian.game.content.interfaces",
        newPath = "net.dodian.game.content.interface",
        phase = PHASE_2_CONTENT
    ),
    PackageMapping(
        oldPath = "net.dodian.game.content.items",
        newPath = "net.dodian.game.content.item",
        phase = PHASE_2_CONTENT
    ),
    PackageMapping(
        oldPath = "net.dodian.game.content.npcs",
        newPath = "net.dodian.game.content.npc",
        phase = PHASE_2_CONTENT
    ),
    PackageMapping(
        oldPath = "net.dodian.game.content.objects",
        newPath = "net.dodian.game.content.object",
        phase = PHASE_2_CONTENT
    ),
    
    // Skills separation
    PackageMapping(
        oldPath = "net.dodian.game.content.skills",
        newPath = "net.dodian.game.skill",
        phase = PHASE_2_CONTENT
    ),
    
    // Elevate engine and config
    PackageMapping(
        oldPath = "net.dodian.game.engine",
        newPath = "net.dodian.engine",
        phase = PHASE_2_CONTENT
    ),
    PackageMapping(
        oldPath = "net.dodian.game.config",
        newPath = "net.dodian.config",
        phase = PHASE_2_CONTENT
    )
)
```

### Target Package Structure

The final package structure after Phase 2:

```
net.dodian/
├── bootstrap/           # Application entry point
├── config/              # Configuration and environment
├── engine/              # Game loop, scheduler, sync, tasking
│   ├── lifecycle/
│   ├── loop/
│   ├── metrics/
│   ├── net/
│   ├── phases/
│   ├── processing/
│   ├── scheduler/
│   ├── sync/
│   └── tasking/
├── game/                # Gameplay systems
│   ├── content/         # Player-facing features
│   │   ├── command/     # Singular form
│   │   │   ├── dev/
│   │   │   ├── player/
│   │   │   ├── social/
│   │   │   ├── staff/
│   │   │   └── travel/
│   │   ├── dialogue/
│   │   ├── event/
│   │   ├── interface/   # Singular form
│   │   │   ├── bank/
│   │   │   ├── combat/
│   │   │   ├── dialogue/
│   │   │   └── ...
│   │   ├── item/        # Singular form
│   │   │   ├── combination/
│   │   │   ├── consumable/
│   │   │   ├── equipment/
│   │   │   └── spawn/
│   │   ├── npc/         # Singular form
│   │   │   ├── interaction/
│   │   │   ├── shop/
│   │   │   └── spawn/
│   │   └── object/      # Singular form
│   │       ├── banking/
│   │       ├── interaction/
│   │       └── travel/
│   ├── event/           # Event bus and game events
│   ├── model/           # Core data models
│   │   ├── chunk/
│   │   ├── entity/
│   │   ├── item/
│   │   ├── music/
│   │   ├── object/
│   │   └── player/
│   ├── persistence/     # Database and save systems
│   ├── skill/           # Elevated from content
│   │   ├── core/        # Shared skill infrastructure
│   │   ├── agility/
│   │   ├── cooking/
│   │   ├── crafting/
│   │   ├── farming/
│   │   ├── fishing/
│   │   ├── fletching/
│   │   ├── herblore/
│   │   ├── mining/
│   │   ├── prayer/
│   │   ├── runecrafting/
│   │   ├── slayer/
│   │   ├── smithing/
│   │   ├── thieving/
│   │   └── woodcutting/
│   └── systems/         # Gameplay systems
│       ├── action/
│       ├── animation/
│       ├── combat/
│       ├── interaction/
│       ├── ui/
│       ├── world/
│       └── zone/
├── util/                # Cross-cutting utilities
│   ├── crypto/
│   │   ├── ISAACCipher.kt
│   │   └── MD5.kt
│   ├── math/
│   │   ├── Geometry.kt
│   │   ├── Randoms.kt
│   │   └── Range.kt
│   └── text/
│       ├── Formatting.kt
│       ├── Names.kt
│       └── Text.kt
└── webapi/              # External API
```

### Java Code Preservation

The following Java packages remain unchanged during Kotlin refactoring:

```
net.dodian.cache.*                    # Cache system (Java)
net.dodian.uber.game.netty.*          # Networking infrastructure (Java)
net.dodian.uber.game.model.entity.*   # Entity models (Java)
```

These packages contain stable networking and cache infrastructure that should not be modified during this refactoring. Future phases may address Java code organization separately.

### Refactoring Operation Sequence

**Phase 1 Operations** (in order):

1. Rename `net.dodian.utilities` → `net.dodian.util`
2. Create domain subpackages: `util.math`, `util.text`, `util.crypto`
3. Move and rename utility classes:
   - `UtilityRandoms` → `util.math.Randoms`
   - `UtilityGeometry` → `util.math.Geometry`
   - `Range` → `util.math.Range`
   - `UtilityFormatting` → `util.text.Formatting`
   - `UtilityText` → `util.text.Text`
   - `UtilityNames` → `util.text.Names`
   - `ISAACCipher` → `util.crypto.ISAACCipher`
   - `MD5` → `util.crypto.MD5`
4. Analyze and distribute `Misc.kt` and `Utils.kt` contents
5. Remove "uber" segment: `net.dodian.uber.game` → `net.dodian.game`
6. Remove "uber" segment: `net.dodian.uber.webapi` → `net.dodian.webapi`
7. Verify compilation

**Phase 2 Operations** (in order):

1. Rename `game.content.commands` → `game.content.command`
2. Rename `game.content.interfaces` → `game.content.interface`
3. Rename `game.content.items` → `game.content.item`
4. Rename `game.content.npcs` → `game.content.npc`
5. Rename `game.content.objects` → `game.content.object`
6. Move `game.content.skills` → `game.skill`
7. Move `game.engine` → `engine` (top-level)
8. Move `game.config` → `config` (top-level)
9. Verify compilation

### Content Subpackage Organization

**Command Organization**:
```
game.content.command/
├── dev/          # Developer commands (spawn, teleport, debug)
├── player/       # Player commands (stats, commands list)
├── social/       # Social commands (yell, pm)
├── staff/        # Staff commands (kick, ban, mute)
└── travel/       # Travel commands (home, wild)
```

**Item Organization**:
```
game.content.item/
├── combination/  # Item combination handlers
├── consumable/   # Food, potions, consumables
├── equipment/    # Wearable items with special effects
└── spawn/        # Item spawning logic
```

**NPC Organization**:
```
game.content.npc/
├── interaction/  # NPC click handlers
├── shop/         # Shop NPCs
└── spawn/        # NPC spawning logic
```

**Object Organization**:
```
game.content.object/
├── banking/      # Bank objects
├── interaction/  # Object click handlers
└── travel/       # Teleport objects, doors, stairs
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system - essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: No "uber" Package Segment

*For any* package path in the Kotlin source tree after Phase 1 completion, the package path should not contain the segment "uber"

**Validates: Requirements 1.1, 1.3**

### Property 2: No "Utility" Prefix in Util Classes

*For any* class in the `net.dodian.util` package after Phase 1 completion, the class name should not start with the prefix "Utility"

**Validates: Requirements 2.2**

### Property 3: Package Structure Preservation During Rename

*For any* package that is renamed during refactoring, the internal structure (subpackages, class names, file organization) should remain identical except for the package path itself

**Validates: Requirements 1.4, 3.8**

### Property 4: Public API Signature Preservation

*For any* public class, method, or function that exists before refactoring, the signature (name, parameters, return type, visibility) should remain identical after refactoring, even if the package path changes

**Validates: Requirements 2.4, 6.4**

### Property 5: Java Source File Preservation

*For any* Java source file in the codebase before refactoring, the file content should remain byte-for-byte identical after Kotlin package restructuring completes

**Validates: Requirements 5.1**

### Property 6: Java Networking Package Location Preservation

*For any* Java package under `net.dodian.uber.game.netty` or `net.dodian.cache` before refactoring, the package should exist at the same path after refactoring completes

**Validates: Requirements 5.2**

### Property 7: RSPS Terminology and Pattern Maintenance

*For any* domain concept in the RSPS vocabulary (npc, object, item, skill, combat, world, entity, command, dialogue), the package structure should use that exact term rather than generic software engineering alternatives, and should follow recognizable RSPS organizational patterns (content.command, content.dialogue, skill.agility, skill.mining)

**Validates: Requirements 11.1, 11.3**

### Property 8: No Ambiguous Package Names

*For any* package in the final structure, the package name should not be one of the ambiguous terms: "misc", "stuff", "common", "shared", or "impl" (unless impl implements a clearly defined interface)

**Validates: Requirements 12.1**


## Error Handling

### Compilation Errors

**Detection Strategy**:
- Run `./gradlew compileKotlin` after each major refactoring operation
- Parse compiler output to extract error locations and messages
- Group errors by type (unresolved reference, package not found, type mismatch)

**Recovery Strategy**:
- If compilation fails after a refactoring operation, use Git to revert to the last known good state
- Analyze error messages to identify missed references or incorrect transformations
- Apply manual fixes for edge cases that automated refactoring missed
- Re-run compilation verification

**Common Error Scenarios**:

1. **Unresolved Import**: Import statement references old package path
   - Cause: Refactoring tool missed a reference
   - Fix: Manually update import to new package path
   
2. **Package Declaration Mismatch**: File location doesn't match package declaration
   - Cause: File moved but package declaration not updated
   - Fix: Update package declaration or move file to correct location
   
3. **Circular Dependency**: Package reorganization creates circular imports
   - Cause: Moving packages changes dependency graph
   - Fix: Identify cycle and extract shared code to separate package

### Refactoring Tool Limitations

**IntelliJ IDEA Refactoring Edge Cases**:

1. **String Literals**: Package names in string literals (e.g., reflection, logging) are not updated
   - Detection: Search for old package names in string literals after refactoring
   - Fix: Manually update string literals or use constants
   
2. **Comments and Documentation**: Package references in comments may not be updated
   - Detection: Search for old package names in comments
   - Fix: Update documentation manually
   
3. **Build Scripts**: Package references in Gradle build files may not be updated
   - Detection: Check build.gradle.kts for old package references
   - Fix: Update build script manually

4. **External Configuration**: Package names in XML, JSON, or properties files
   - Detection: Search configuration files for old package names
   - Fix: Update configuration files manually

### Rollback Strategy

**Git-Based Rollback**:
- Create a Git commit before starting each phase
- Tag commits with phase identifiers: `refactor-phase1-start`, `refactor-phase1-complete`
- If errors are unrecoverable, use `git reset --hard <tag>` to rollback

**Phase Boundaries**:
- Phase 1 Start: Tag `refactor-phase1-start`
- Phase 1 Complete: Tag `refactor-phase1-complete` (after compilation verification)
- Phase 2 Start: Tag `refactor-phase2-start`
- Phase 2 Complete: Tag `refactor-phase2-complete` (after compilation verification)

### Validation Failures

**Package Structure Validation**:
- If required packages are missing after refactoring, create them manually
- If unexpected packages remain (e.g., "uber" still exists), investigate and remove
- If package organization doesn't match target structure, perform additional move operations

**API Compatibility Validation**:
- If public API signatures change unexpectedly, revert the operation
- If method signatures are preserved but behavior changes, investigate for logic errors
- If compilation succeeds but tests fail, analyze test failures for refactoring-related issues

## Testing Strategy

### Dual Testing Approach

This refactoring will be validated using both unit tests and property-based tests:

- **Unit tests**: Verify specific package transformations, edge cases, and error conditions
- **Property tests**: Verify universal properties across the entire codebase structure

Both testing approaches are complementary and necessary for comprehensive validation.

### Unit Testing Strategy

Unit tests will focus on:

1. **Specific Package Transformations**:
   - Test that `net.dodian.utilities` becomes `net.dodian.util`
   - Test that `net.dodian.uber.game` becomes `net.dodian.game`
   - Test that `game.content.commands` becomes `game.content.command`
   - Test that `game.content.skills` becomes `game.skill`

2. **Specific Class Renames**:
   - Test that `UtilityFormatting` becomes `Formatting` in `util.text`
   - Test that `UtilityRandoms` becomes `Randoms` in `util.math`
   - Test that `UtilityGeometry` becomes `Geometry` in `util.math`

3. **Package Existence Verification**:
   - Test that top-level packages exist: `engine`, `game`, `util`, `config`
   - Test that `game.skill.core` package exists
   - Test that documentation file exists at `docs/development/package-structure.md`

4. **Edge Cases**:
   - Test that Java files remain unchanged
   - Test that Java networking packages remain at original locations
   - Test that cache system packages remain unchanged

Unit tests should be written as simple verification scripts that can be run after each phase completes.

### Property-Based Testing Strategy

Property-based tests will verify universal correctness properties across the entire codebase. We will use **Kotest Property Testing** library for Kotlin.

**Configuration**:
- Each property test will run a minimum of 100 iterations
- Tests will use random sampling of the codebase structure
- Each test will reference its corresponding design property

**Property Test Implementation**:

1. **Property 1: No "uber" Package Segment**
   - Generator: Sample all package declarations in Kotlin files
   - Property: No package path contains "uber"
   - Tag: **Feature: codebase-restructuring, Property 1: For any package path in the Kotlin source tree after Phase 1 completion, the package path should not contain the segment "uber"**

2. **Property 2: No "Utility" Prefix in Util Classes**
   - Generator: Sample all class names in `net.dodian.util` package
   - Property: No class name starts with "Utility"
   - Tag: **Feature: codebase-restructuring, Property 2: For any class in the net.dodian.util package after Phase 1 completion, the class name should not start with the prefix "Utility"**

3. **Property 3: Package Structure Preservation**
   - Generator: Sample packages that were renamed
   - Property: Internal structure (subpackages, files) matches before/after snapshots
   - Tag: **Feature: codebase-restructuring, Property 3: For any package that is renamed during refactoring, the internal structure should remain identical except for the package path itself**

4. **Property 4: Public API Signature Preservation**
   - Generator: Sample public classes and methods from before/after snapshots
   - Property: Signatures match exactly (name, parameters, return type, visibility)
   - Tag: **Feature: codebase-restructuring, Property 4: For any public class, method, or function that exists before refactoring, the signature should remain identical after refactoring**

5. **Property 5: Java Source File Preservation**
   - Generator: Sample Java files from before/after snapshots
   - Property: File contents are byte-for-byte identical
   - Tag: **Feature: codebase-restructuring, Property 5: For any Java source file in the codebase before refactoring, the file content should remain byte-for-byte identical after Kotlin package restructuring completes**

6. **Property 6: Java Networking Package Location Preservation**
   - Generator: Sample Java networking and cache packages
   - Property: Packages exist at original paths
   - Tag: **Feature: codebase-restructuring, Property 6: For any Java package under net.dodian.uber.game.netty or net.dodian.cache before refactoring, the package should exist at the same path after refactoring completes**

7. **Property 7: RSPS Terminology Maintenance**
   - Generator: Sample domain packages
   - Property: Package names use RSPS terms (npc, object, item, skill, etc.) and follow RSPS patterns
   - Tag: **Feature: codebase-restructuring, Property 7: For any domain concept in the RSPS vocabulary, the package structure should use that exact term and follow recognizable RSPS organizational patterns**

8. **Property 8: No Ambiguous Package Names**
   - Generator: Sample all package names
   - Property: No package named "misc", "stuff", "common", or "shared"
   - Tag: **Feature: codebase-restructuring, Property 8: For any package in the final structure, the package name should not be one of the ambiguous terms: misc, stuff, common, or shared**

### Testing Execution Plan

**Pre-Refactoring**:
1. Create snapshot of current package structure
2. Create snapshot of all public API signatures
3. Create snapshot of all Java file contents
4. Commit to Git with tag `refactor-baseline`

**Phase 1 Testing**:
1. Execute Phase 1 refactoring operations
2. Run `./gradlew compileKotlin` to verify compilation
3. Run unit tests for Phase 1 specific transformations
4. Run property tests 1, 2, 3, 4, 5, 6
5. If all tests pass, commit with tag `refactor-phase1-complete`

**Phase 2 Testing**:
1. Execute Phase 2 refactoring operations
2. Run `./gradlew compileKotlin` to verify compilation
3. Run unit tests for Phase 2 specific transformations
4. Run all property tests (1-8)
5. If all tests pass, commit with tag `refactor-phase2-complete`

**Post-Refactoring**:
1. Run full test suite: `./gradlew test`
2. Verify server starts successfully
3. Perform smoke testing of core functionality
4. Generate package structure documentation

### Manual Verification Checklist

After automated testing completes, perform manual verification:

- [ ] Server starts without errors
- [ ] Player can log in successfully
- [ ] Commands execute correctly
- [ ] Skills function properly
- [ ] Items and NPCs interact correctly
- [ ] Combat system works
- [ ] Database persistence works
- [ ] No console errors or warnings related to missing classes

### Test Tooling

**Required Dependencies**:
```kotlin
// build.gradle.kts
testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
testImplementation("io.kotest:kotest-assertions-core:5.8.0")
testImplementation("io.kotest:kotest-property:5.8.0")
```

**Test Utilities**:
- Package scanner: Walk source tree and extract package declarations
- API signature extractor: Parse Kotlin files and extract public signatures
- File comparison utility: Compare file contents before/after
- RSPS terminology validator: Check package names against RSPS vocabulary

