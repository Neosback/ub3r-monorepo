package com.osroyale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class CacheDownloader {

    public static void checkAndDownload() {
        String cacheDir = Utility.findcachedir();
        File cacheDat = new File(cacheDir, "main_file_cache.dat");
        if (cacheDat.exists()) {
            System.out.println("Cache already exists locally: " + cacheDat.getAbsolutePath());
            return;
        }

        String downloadUrl = Configuration.CACHE_DOWNLOAD_URL;
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            System.out.println("No cache download URL configured.");
            return;
        }

        System.out.println("Cache not found at " + cacheDir + ". Downloading from " + downloadUrl);
        File targetDir = new File(cacheDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File zipFile = new File(cacheDir, "cache.zip");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download cache: " + response);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body from cache download URL");
            }

            long totalBytes = body.contentLength();
            try (InputStream is = body.byteStream();
                 FileOutputStream fos = new FileOutputStream(zipFile)) {
                byte[] buffer = new byte[4096];
                long downloadedBytes = 0;
                int read;
                int lastPercent = -1;

                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    downloadedBytes += read;
                    if (totalBytes > 0) {
                        int percent = (int) ((downloadedBytes * 100) / totalBytes);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            if (Client.instance != null) {
                                Client.instance.drawLoadingText(percent, "Downloading Cache: " + percent + "%");
                            }
                            System.out.println("Downloading cache: " + percent + "%");
                        }
                    } else {
                        if (Client.instance != null) {
                            Client.instance.drawLoadingText(50, "Downloading Cache...");
                        }
                    }
                }
            }

            if (Client.instance != null) {
                Client.instance.drawLoadingText(100, "Unzipping Cache...");
            }
            System.out.println("Unzipping cache to " + cacheDir);
            Unzip.unZipIt(zipFile.getAbsolutePath(), cacheDir, true);
            System.out.println("Cache downloaded and extracted successfully.");
        } catch (Exception e) {
            System.err.println("Error downloading cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
