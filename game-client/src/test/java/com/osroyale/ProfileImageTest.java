package com.osroyale;

import com.osroyale.profile.ProfileManager;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProfileImageTest {

    @Test
    public void profileSpriteLoadsSynchronouslyAndRejectsMalformedOrWrongSizedPngs() throws Exception {
        Path directory = Files.createTempDirectory("tarnish-profile-image");
        Path valid = directory.resolve("valid.png");
        ProfileManager.saveImage(image(50, 50, 0xFF336699), valid.toString());

        Sprite sprite = new Sprite(valid.toString(), 50, 50);
        assertNotNull(sprite.raster);
        assertEquals(2_500, sprite.raster.length);

        Path malformed = directory.resolve("malformed.png");
        Files.write(malformed, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertNull(new Sprite(malformed.toString(), 50, 50).raster);

        Path wrongSize = directory.resolve("wrong-size.png");
        ImageIO.write(image(10, 10, 0xFFFFFFFF), "png", wrongSize.toFile());
        assertNull(new Sprite(wrongSize.toString(), 50, 50).raster);
    }

    @Test
    public void atomicProfileReplacementNeverExposesAPartialPng() throws Exception {
        Path directory = Files.createTempDirectory("tarnish-profile-race");
        Path profile = directory.resolve("profile.png");
        ProfileManager.saveImage(image(50, 50, 0xFF112233), profile.toString());

        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(() -> runSafely(failure, () -> {
            start.await();
            for (int iteration = 0; iteration < 100; iteration++) {
                ProfileManager.saveImage(image(50, 50, 0xFF000000 | iteration), profile.toString());
            }
        }), "profile-writer"));
        threads.add(new Thread(() -> runSafely(failure, () -> {
            start.await();
            for (int iteration = 0; iteration < 500; iteration++) {
                Sprite sprite = new Sprite(profile.toString(), 50, 50);
                assertNotNull("atomic replacement exposed an invalid PNG", sprite.raster);
                assertEquals(2_500, sprite.raster.length);
            }
        }), "profile-reader"));

        threads.forEach(Thread::start);
        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        if (failure.get() != null) {
            throw new AssertionError("concurrent profile image test failed", failure.get());
        }
        assertNotNull(ImageIO.read(profile.toFile()));
        try (var files = Files.list(directory)) {
            assertFalse("temporary PNG files must be cleaned up",
                    files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    private static BufferedImage image(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    private static void runSafely(AtomicReference<Throwable> failure, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            failure.compareAndSet(null, throwable);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
