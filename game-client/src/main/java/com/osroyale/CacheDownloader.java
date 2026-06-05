package com.osroyale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CacheDownloader {

    private static final String DOWNLOAD_URL = "https://www.dropbox.com/scl/fi/ef6nc8zjvmtmgwl8v3ilw/cache-tarnish-218.zip?rlkey=1d1zkppmf0gnfi37pfze838aq&st=e06marr0&dl=1";

    public static void init() {
        File cacheDir = new File(Utility.findcachedir());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File cacheDat = new File(cacheDir, "main_file_cache.dat");
        if (!cacheDat.exists() || cacheDat.length() == 0) {
            downloadAndExtract();
        }
    }

    private static void downloadAndExtract() {
        File tempZip = new File(System.getProperty("java.io.tmpdir"), "tarnish_cache.zip");
        try {
            System.out.println("Starting cache download...");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(DOWNLOAD_URL).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download cache: " + response);
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("Empty response body from cache download link");
                }

                long totalBytes = body.contentLength();
                try (InputStream is = body.byteStream();
                     FileOutputStream fos = new FileOutputStream(tempZip)) {
                    
                    byte[] buffer = new byte[8192];
                    int read;
                    long downloadedBytes = 0;
                    int lastPercent = -1;

                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        downloadedBytes += read;

                        if (totalBytes > 0) {
                            int percent = (int) ((downloadedBytes * 100) / totalBytes);
                            if (percent != lastPercent) {
                                lastPercent = percent;
                                String msg = "Downloading Cache: " + percent + "%";
                                System.out.println(msg);
                                if (Client.instance != null) {
                                    Client.instance.drawLoadingText(percent, msg);
                                }
                            }
                        } else {
                            String msg = "Downloading Cache: " + (downloadedBytes / 1024 / 1024) + " MB";
                            System.out.println(msg);
                            if (Client.instance != null) {
                                Client.instance.drawLoadingText(50, msg);
                            }
                        }
                    }
                }
            }

            System.out.println("Cache download finished. Starting extraction...");
            if (Client.instance != null) {
                Client.instance.drawLoadingText(100, "Extracting Cache...");
            }
            unzip(tempZip, new File(Utility.findcachedir()));
            System.out.println("Cache extraction finished.");

        } catch (Exception e) {
            e.printStackTrace();
            if (Client.instance != null) {
                Client.instance.drawLoadingText(0, "Cache download error: " + e.getMessage());
            }
        } finally {
            if (tempZip.exists()) {
                tempZip.delete();
            }
        }
    }

    private static void unzip(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[8192];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());
                
                // Security check: ensure path is within destDir
                String destDirPath = destDir.getCanonicalPath();
                String filePath = file.getCanonicalPath();
                if (!filePath.startsWith(destDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
