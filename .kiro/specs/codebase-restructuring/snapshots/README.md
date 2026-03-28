# Baseline Snapshots

This directory contains baseline snapshots created before the codebase restructuring begins.

## Snapshot Files

### baseline-package-structure.txt (13 KB)
- Complete list of all package directories (207 directories)
- Includes both Kotlin and Java package hierarchies
- Sorted alphabetically for easy comparison

### baseline-kotlin-files.txt (73 KB)
- Complete list of all Kotlin source files (831 files)
- Full paths relative to workspace root
- Sorted alphabetically

### baseline-java-files.txt (16 KB)
- Complete list of all Java source files (197 files)
- Full paths relative to workspace root
- Sorted alphabetically

### baseline-java-contents.txt (949 KB)
- Complete contents of all Java source files (26,020 lines)
- Each file prefixed with `=== <filepath> ===`
- Used to verify Java files remain unchanged after refactoring

### baseline-api-signatures.txt (151 KB)
- Package declarations and public API signatures from Kotlin files (2,514 lines)
- Includes class, interface, object, function, property declarations
- Used to verify API signatures are preserved during refactoring

### baseline-package-reference.md (3.6 KB)
- Human-readable documentation of current package structure
- Describes purpose of major packages
- Lists file counts and key statistics
- Identifies Java packages that must be preserved

## Git Baseline

- **Commit**: ea7b200ae
- **Tag**: `refactor-baseline`
- **Message**: "Pre-refactoring baseline: Add spec files and baseline snapshots"

## Usage

These snapshots will be used during property-based testing to verify:

1. **Property 3**: Package structure preservation during rename
2. **Property 4**: Public API signature preservation
3. **Property 5**: Java source file preservation
4. **Property 6**: Java networking package location preservation

## Verification Commands

```bash
# Compare package structure before/after
diff baseline-package-structure.txt <(find game-server/src/main -type d | sort)

# Compare Java file contents before/after
diff baseline-java-contents.txt <(find game-server/src/main/java -name "*.java" -exec sh -c 'echo "=== {} ===" && cat "{}"' \;)

# Count files
wc -l baseline-kotlin-files.txt baseline-java-files.txt

# Verify Java files unchanged
find game-server/src/main/java -name "*.java" | sort | diff - baseline-java-files.txt
```

## Statistics

- **Total Directories**: 207
- **Kotlin Files**: 831
- **Java Files**: 197
- **Java Code Lines**: 26,020
- **API Signature Lines**: 2,514

## Next Steps

After Phase 1 and Phase 2 refactoring, these snapshots will be compared against the new structure to verify:
- No Java files were modified
- Public API signatures remain identical (except for package paths)
- Package internal structure is preserved
- All references were updated correctly
