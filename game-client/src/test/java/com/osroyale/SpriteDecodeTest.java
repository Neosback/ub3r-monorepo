package com.osroyale;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SpriteDecodeTest {

    @Test
    public void byteArraySpriteDecodesSynchronouslyWithImageIo() throws Exception {
        Sprite sprite = new Sprite(pngBytes(2, 3));

        assertEquals(2, sprite.width);
        assertEquals(3, sprite.height);
        assertNotNull(sprite.raster);
        assertEquals(6, sprite.raster.length);
    }

    @Test
    public void invalidByteArraySpriteFailsWithoutAwtImageFetcher() {
        Sprite sprite = new Sprite(new byte[] {1, 2, 3, 4});

        assertNull(sprite.raster);
    }

    @Test
    public void pathSpriteDecodesDeterministically() throws Exception {
        Path image = Files.createTempFile("tarnish-sprite", ".png");
        Files.write(image, pngBytes(4, 5));

        Sprite sprite = new Sprite(image.toString(), 4, 5);

        assertEquals(4, sprite.width);
        assertEquals(5, sprite.height);
        assertNotNull(sprite.raster);
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, 0xFFFF00FF);
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
