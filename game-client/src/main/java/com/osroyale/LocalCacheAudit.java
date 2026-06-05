package com.osroyale;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LocalCacheAudit {

    private static final String[] STARTUP_ARCHIVES = {
            "title", "config", "interface", "media", "versionlist"
    };

    private LocalCacheAudit() {
    }

    public static Result audit(Path cacheDirectory) {
        ArrayList<String> failures = new ArrayList<>();
        ArrayList<String> warnings = new ArrayList<>();

        if (cacheDirectory == null) {
            failures.add("Cache directory is null");
            return new Result(failures, warnings);
        }

        if (!Files.isDirectory(cacheDirectory)) {
            failures.add("Cache directory does not exist: " + cacheDirectory);
            return new Result(failures, warnings);
        }

        Path dataPath = cacheDirectory.resolve("main_file_cache.dat");
        if (!Files.isRegularFile(dataPath)) {
            failures.add("Missing main cache data file: " + dataPath);
            return new Result(failures, warnings);
        }

        RSFileStore[] stores = openStores(cacheDirectory, failures, warnings);
        if (stores == null) {
            return new Result(failures, warnings);
        }

        StreamLoader configArchive = null;
        StreamLoader versionArchive = null;
        for (int groupId = 1; groupId <= STARTUP_ARCHIVES.length; groupId++) {
            byte[] archiveData = stores[0].readFile(groupId);
            String archiveName = STARTUP_ARCHIVES[groupId - 1];
            if (archiveData == null || archiveData.length == 0) {
                failures.add("Missing startup archive idx0/" + groupId + " (" + archiveName + ")");
                continue;
            }
            try {
                StreamLoader loader = new StreamLoader(archiveData);
                if ("config".equals(archiveName)) {
                    configArchive = loader;
                } else if ("versionlist".equals(archiveName)) {
                    versionArchive = loader;
                }
            } catch (RuntimeException ex) {
                failures.add("Startup archive does not decode idx0/" + groupId + " (" + archiveName + "): " + ex.getMessage());
            }
        }

        verifyConfigArchive(configArchive, failures);
        verifyVersionArchive(versionArchive, stores, failures, warnings);
        verifyIndexData(stores, failures, warnings);

        return new Result(failures, warnings);
    }

    private static RSFileStore[] openStores(Path cacheDirectory, List<String> failures, List<String> warnings) {
        try {
            RandomAccessFile data = new RandomAccessFile(cacheDirectory.resolve("main_file_cache.dat").toFile(), "r");
            RSFileStore[] stores = new RSFileStore[RSFileStore.CACHE_INDEX_COUNT];
            for (int index = 0; index < stores.length; index++) {
                Path indexPath = cacheDirectory.resolve("main_file_cache.idx" + index);
                if (!Files.isRegularFile(indexPath)) {
                    failures.add("Missing cache index file: " + indexPath);
                    continue;
                }
                long size = Files.size(indexPath);
                if (size == 0) {
                    if (index != 3) {
                        warnings.add("Cache index is empty: idx" + index);
                    }
                }
                RandomAccessFile indexFile = new RandomAccessFile(indexPath.toFile(), "r");
                stores[index] = new RSFileStore(data, indexFile, index + 1);
            }
            return stores;
        } catch (IOException ex) {
            failures.add("Unable to open cache files: " + ex.getMessage());
            return null;
        }
    }

    private static void verifyConfigArchive(StreamLoader configArchive, List<String> failures) {
        if (configArchive == null) {
            return;
        }
        requireArchiveFile(configArchive, "obj.dat", failures);
        requireArchiveFile(configArchive, "obj.idx", failures);
        requireArchiveFile(configArchive, "underlays.dat", failures);
        requireArchiveFile(configArchive, "overlays.dat", failures);
        requireArchiveFile(configArchive, "textures.dat", failures);
        requireArchiveFile(configArchive, Configuration.SPRITE_FILE_NAME + ".dat", failures);
        requireArchiveFile(configArchive, Configuration.SPRITE_FILE_NAME + ".idx", failures);
    }

    private static void verifyVersionArchive(StreamLoader versionArchive, RSFileStore[] stores, List<String> failures, List<String> warnings) {
        if (versionArchive == null) {
            return;
        }
        byte[] mapIndex = versionArchive.getFile("map_index");
        if (mapIndex == null || mapIndex.length < 2) {
            failures.add("versionlist archive is missing map_index");
            return;
        }
        Buffer buffer = new Buffer(mapIndex);
        int count = buffer.readUnsignedShort();
        if (count <= 0) {
            failures.add("map_index has no regions");
            return;
        }
        boolean foundReadableMap = false;
        int limit = Math.min(count, 256);
        for (int i = 0; i < limit && buffer.position + 6 <= mapIndex.length; i++) {
            buffer.readUnsignedShort();
            int mapFile = buffer.readUnsignedShort();
            int landscapeFile = buffer.readUnsignedShort();
            if (mapFile > 0 && stores[4] != null && stores[4].readFile(mapFile) != null) {
                foundReadableMap = true;
            }
            if (landscapeFile > 0 && stores[4] != null && stores[4].readFile(landscapeFile) != null) {
                foundReadableMap = true;
            }
            if (foundReadableMap) {
                break;
            }
        }
        if (!foundReadableMap) {
            warnings.add("No readable map or landscape file found in first " + limit + " map_index entries");
        }
    }

    private static void verifyIndexData(RSFileStore[] stores, List<String> failures, List<String> warnings) {
        requireGzipReadableStoreFile(stores, 1, 0, "first model", failures);
        if (stores[2] != null && stores[2].readFile(0) == null) {
            warnings.add("No animation file found at idx2/0");
        }
        requireGzipReadableStoreFile(stores, 5, 0, "first texture/image group", failures);
    }

    private static void requireGzipReadableStoreFile(RSFileStore[] stores, int index, int file, String label, List<String> failures) {
        if (stores[index] == null) {
            failures.add("Missing " + label + " at idx" + index + "/" + file);
            return;
        }
        byte[] data = stores[index].readFile(file);
        if (data == null) {
            failures.add("Missing " + label + " at idx" + index + "/" + file);
            return;
        }
        try {
            byte[] decoded = LocalCacheProvider.decodeOnDemand(data);
            if (decoded == null || decoded.length == 0) {
                failures.add("Decoded " + label + " is empty at idx" + index + "/" + file);
            }
        } catch (IllegalArgumentException ex) {
            failures.add("Unable to GZIP-decode " + label + " at idx" + index + "/" + file + ": " + ex.getMessage());
        }
    }

    private static void requireArchiveFile(StreamLoader archive, String name, List<String> failures) {
        byte[] data = archive.getFile(name);
        if (data == null || data.length == 0) {
            failures.add("config archive is missing " + name);
        }
    }

    private static void requireReadableStoreFile(RSFileStore[] stores, int index, int file, String label, List<String> failures) {
        if (stores[index] == null || stores[index].readFile(file) == null) {
            failures.add("Missing " + label + " at idx" + index + "/" + file);
        }
    }

    public static final class Result {
        private final List<String> failures;
        private final List<String> warnings;

        private Result(List<String> failures, List<String> warnings) {
            this.failures = Collections.unmodifiableList(new ArrayList<>(failures));
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        }

        public List<String> failures() {
            return failures;
        }

        public List<String> warnings() {
            return warnings;
        }

        public String describeFailures() {
            if (failures.isEmpty()) {
                return "";
            }
            return String.join(System.lineSeparator(), failures);
        }
    }
}
