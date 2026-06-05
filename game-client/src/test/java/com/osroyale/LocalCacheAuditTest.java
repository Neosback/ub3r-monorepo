package com.osroyale;

import org.junit.Test;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class LocalCacheAuditTest {

    @Test
    public void currentTarnishCacheHasRequiredStartupAndOnDemandData() {
        LocalCacheAudit.Result result = LocalCacheAudit.audit(Path.of(Utility.findcachedir()));

        assertTrue(result.describeFailures(), result.failures().isEmpty());
    }

    @Test
    public void currentTarnishCacheTreatsEmptyMusicIndexAsNonVisualAndDecodesVisualIndexes() throws Exception {
        Path cache = Path.of(Utility.findcachedir());

        LocalCacheAudit.Result result = LocalCacheAudit.audit(cache);

        assertFalse(result.warnings().toString(), result.warnings().contains("Cache index is empty: idx3"));
        assertTrue(readAndDecode(cache, 1, 0).length > 0);
        assertTrue(readAndDecode(cache, 4, 0).length > 0);
        assertTrue(readAndDecode(cache, 5, 0).length > 0);
    }

    @Test
    public void localOnDemandDecodeUsesSwiftFupGzipPayloadShape() throws Exception {
        byte[] payload = new byte[] {1, 2, 3, 4, 5};

        assertArrayEquals(payload, LocalCacheProvider.decodeOnDemand(gzip(payload)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void localOnDemandDecodeRejectsNonGzipPayloadsInsteadOfTreatingThemAsJagexContainers() {
        LocalCacheProvider.decodeOnDemand(new byte[] {0, 0, 0, 0, 1, 99});
    }

    private static byte[] gzip(byte[] payload) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(payload);
        }
        return out.toByteArray();
    }

    private static byte[] readAndDecode(Path cache, int index, int file) throws Exception {
        try (RandomAccessFile data = new RandomAccessFile(cache.resolve("main_file_cache.dat").toFile(), "r");
             RandomAccessFile idx = new RandomAccessFile(cache.resolve("main_file_cache.idx" + index).toFile(), "r")) {
            RSFileStore store = new RSFileStore(data, idx, index + 1);
            return LocalCacheProvider.decodeOnDemand(store.readFile(file));
        }
    }
}
