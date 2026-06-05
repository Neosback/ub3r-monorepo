package com.osroyale;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

final class LocalCacheProvider {

    private LocalCacheProvider() {
    }

    static byte[] decodeOnDemand(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length < 2 || data[0] != 0x1f || data[1] != (byte) 0x8b) {
            throw new IllegalArgumentException("Local on-demand payload is not GZIP-compressed");
        }
        try (ByteArrayInputStream input = new ByteArrayInputStream(data);
             GZIPInputStream gzip = new GZIPInputStream(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = gzip.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to decode local on-demand GZIP payload", ex);
        }
    }
}
