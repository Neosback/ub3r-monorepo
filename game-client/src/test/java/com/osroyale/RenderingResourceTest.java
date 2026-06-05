package com.osroyale;

import org.junit.Test;

import java.io.RandomAccessFile;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RenderingResourceTest {

    @Test
    public void currentCachePopulatesCoinModelAndRendersVisibleSoftwareItemSprite() throws Exception {
        StreamLoader configArchive = configArchive();
        ItemDefinition.unpackConfig(configArchive);
        Model.init();
        ItemDefinition.mruNodes1 = new Cache(100);
        ItemDefinition.mruNodes2 = new Cache(50);
        Rasterizer3D.Rasterizer3D_buildPalette(0.80000000000000004D, 0, 512);

        ItemDefinition coins = ItemDefinition.lookup(995);
        byte[] modelData = readAndDecode(Path.of(Utility.findcachedir()), 1, coins.inventoryModel);
        Model.loadModel(modelData, coins.inventoryModel);

        assertNotNull(Model.modelHeaders[coins.inventoryModel]);
        Model inventoryModel = Model.getModel(coins.inventoryModel);
        assertNotNull(inventoryModel);
        assertTrue(inventoryModel.verticesCount > 0);
        assertTrue(inventoryModel.triangleFaceCount > 0);

        boolean previousGpu = Rasterizer3D.renderOnGpu;
        boolean previousWorld = Rasterizer3D.world;
        try {
            Rasterizer3D.renderOnGpu = false;
            Rasterizer3D.world = true;
            Sprite sprite = ItemDefinition.getSprite(995, 1, 0);

            assertNotNull(sprite);
            assertTrue("coin sprite should contain visible non-black pixels", hasVisiblePixels(sprite));
        } finally {
            Rasterizer3D.renderOnGpu = previousGpu;
            Rasterizer3D.world = previousWorld;
        }
    }

    @Test
    public void floorDefinitionsDecodeNonZeroTerrainColorsFromCurrentCache() throws Exception {
        FloorDefinition.init(configArchive());

        assertTrue("underlays should decode from cache", FloorDefinition.underlays.length > 0);
        assertTrue("overlays should decode from cache", FloorDefinition.overlays.length > 0);
        assertTrue("at least one underlay must have non-zero rgb", containsNonZeroRgb(FloorDefinition.underlays));
        assertTrue("at least one overlay must have non-zero rgb or texture", containsRenderableOverlay(FloorDefinition.overlays));
    }

    @Test
    public void softwareMinimapDrawsNonZeroPixelsForSimpleTile() {
        int[][][] heights = new int[4][105][105];
        SceneGraph scene = new SceneGraph(heights);
        int[] pixels = new int[512 * 512];

        scene.addTile(0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x336699, 0);
        scene.drawTileMinimapSD(pixels, 1 + 512, 0, 1, 1);

        assertTrue("software minimap should paint non-zero tile pixels", containsNonZeroPixel(pixels));
    }

    private static boolean hasVisiblePixels(Sprite sprite) {
        if (sprite.raster == null) {
            return false;
        }
        for (int pixel : sprite.raster) {
            int rgb = pixel & 0x00FFFFFF;
            if (rgb != 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsNonZeroPixel(int[] pixels) {
        for (int pixel : pixels) {
            if (pixel != 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsNonZeroRgb(FloorDefinition[] floors) {
        for (FloorDefinition floor : floors) {
            if (floor != null && floor.rgb != 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsRenderableOverlay(FloorDefinition[] floors) {
        for (FloorDefinition floor : floors) {
            if (floor != null && (floor.rgb != 0 || floor.texture >= 0)) {
                return true;
            }
        }
        return false;
    }

    private static StreamLoader configArchive() throws Exception {
        Path cache = Path.of(Utility.findcachedir());
        try (RandomAccessFile data = new RandomAccessFile(cache.resolve("main_file_cache.dat").toFile(), "r");
             RandomAccessFile idx = new RandomAccessFile(cache.resolve("main_file_cache.idx0").toFile(), "r")) {
            RSFileStore store = new RSFileStore(data, idx, 1);
            return new StreamLoader(store.readFile(2));
        }
    }

    private static byte[] readAndDecode(Path cache, int index, int file) throws Exception {
        try (RandomAccessFile data = new RandomAccessFile(cache.resolve("main_file_cache.dat").toFile(), "r");
             RandomAccessFile idx = new RandomAccessFile(cache.resolve("main_file_cache.idx" + index).toFile(), "r")) {
            RSFileStore store = new RSFileStore(data, idx, index + 1);
            return LocalCacheProvider.decodeOnDemand(store.readFile(file));
        }
    }
}
