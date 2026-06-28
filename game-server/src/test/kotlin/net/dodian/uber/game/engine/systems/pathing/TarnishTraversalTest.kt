package net.dodian.uber.game.engine.systems.pathing

import net.dodian.cache.objects.GameObjectData
import net.dodian.cache.objects.GameObjectDef
import net.dodian.uber.game.engine.systems.cache.CollisionBuildService
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditObject
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditStore
import net.dodian.uber.game.engine.systems.cache.DecodedMapObject
import net.dodian.uber.game.engine.systems.cache.DecodedMapTile
import net.dodian.uber.game.engine.systems.cache.DecodedMapTileGrid
import net.dodian.uber.game.engine.systems.cache.MapIndexEntry
import net.dodian.uber.game.engine.systems.cache.SkippedObjectRepository
import net.dodian.uber.game.engine.systems.interaction.ClipProbeService
import net.dodian.uber.game.engine.systems.interaction.ApproachStatus
import net.dodian.uber.game.engine.systems.interaction.ObjectInteractionContext
import net.dodian.uber.game.engine.systems.interaction.ObjectApproachRoutingService
import net.dodian.uber.game.engine.systems.interaction.InteractionProcessor
import net.dodian.uber.game.engine.systems.interaction.ObjectClickIntent
import net.dodian.uber.game.engine.systems.interaction.ObjectInteractionMovementSettleService
import net.dodian.uber.game.engine.systems.interaction.objects.ObjectInteractionService
import net.dodian.uber.game.engine.systems.interaction.scheduler.InteractionExecutionResult
import net.dodian.uber.game.engine.systems.net.WalkRequest
import net.dodian.uber.game.engine.systems.net.WalkingRouteService
import net.dodian.uber.game.engine.state.InteractionSessionStateAdapter
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionDirection
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionFlag
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionMatrix
import net.dodian.uber.game.engine.systems.pathing.collision.InteractionReachService
import net.dodian.uber.game.engine.systems.pathing.collision.ProjectileLineService
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.objects.WorldObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TarnishTraversalTest {
    @Test
    fun `straight wall flags match Tarnish constants and are removable`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markWall(CollisionDirection.EAST, 0, 10, 10, 0, impenetrable = true)

        assertEquals(CollisionFlag.WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_EAST, collision.getFlags(10, 10, 0))
        assertEquals(CollisionFlag.WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_WEST, collision.getFlags(11, 10, 0))
        assertFalse(collision.canMove(10, 10, 11, 10, 0, 1, 1))
        assertFalse(collision.canMove(11, 10, 10, 10, 0, 1, 1))

        collision.unmarkWall(CollisionDirection.EAST, 0, 10, 10, 0, impenetrable = true)
        assertEquals(0, collision.getFlags(10, 10, 0))
        assertEquals(0, collision.getFlags(11, 10, 0))
        assertTrue(collision.canMove(10, 10, 11, 10, 0, 1, 1))
    }

    @Test
    fun `entire wall and diagonal corner use Tarnish paired flags`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markWall(CollisionDirection.NORTH, 0, 20, 20, 2, impenetrable = false)

        assertEquals(CollisionFlag.WALL_EAST or CollisionFlag.WALL_NORTH, collision.getFlags(20, 20, 0))
        assertEquals(CollisionFlag.WALL_SOUTH, collision.getFlags(20, 21, 0))
        assertEquals(CollisionFlag.WALL_WEST, collision.getFlags(21, 20, 0))

        collision.markWall(CollisionDirection.SOUTH, 0, 30, 30, 1, impenetrable = true)
        assertEquals(CollisionFlag.WALL_SOUTH_WEST or CollisionFlag.IMPENETRABLE_WALL_SOUTH_WEST, collision.getFlags(30, 30, 0))
        assertEquals(CollisionFlag.WALL_NORTH_EAST or CollisionFlag.IMPENETRABLE_WALL_NORTH_EAST, collision.getFlags(29, 29, 0))
    }

    @Test
    fun `occupants mark blocked and impenetrable flags separately`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markOccupant(0, 40, 40, 2, 2, impenetrable = true, add = true)

        for (x in 40..41) {
            for (y in 40..41) {
                assertEquals(CollisionFlag.BLOCKED or CollisionFlag.IMPENETRABLE_BLOCKED, collision.getFlags(x, y, 0))
                assertTrue(collision.isTileBlocked(x, y, 0))
            }
        }

        collision.markOccupant(0, 40, 40, 2, 2, impenetrable = true, add = false)
        for (x in 40..41) for (y in 40..41) assertEquals(0, collision.getFlags(x, y, 0))
    }

    @Test
    fun `diagonal cannot cut a blocked corner`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(11, 10, 0)
        collision.flagSolid(10, 11, 0)
        assertFalse(collision.canMove(10, 10, 11, 11, 0, 1, 1))
    }

    @Test
    fun `dijkstra detours and entity size is respected`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(11, 10, 0)
        val algorithm = DijkstraPathfindingAlgorithm { x, y, z, dx, dy -> collision.traversable(x, y, z, dx, dy) }
        val path = algorithm.find(10, 10, 12, 10, 0)
        assertTrue(path.isNotEmpty())
        assertEquals(12, path.last.x)
        assertTrue(collision.canMove(20, 20, 21, 20, 0, 2, 2))
        collision.flagSolid(22, 21, 0)
        assertFalse(collision.canMove(20, 20, 21, 20, 0, 2, 2))
    }

    @Test
    fun `dijkstra fallback finds closest reachable tile`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(12, 10, 0)
        val algorithm = DijkstraPathfindingAlgorithm { x, y, z, dx, dy -> collision.traversable(x, y, z, dx, dy) }
        val path = algorithm.find(10, 10, 12, 10, 0)
        assertTrue(path.isNotEmpty())
        assertEquals(11, path.last.x)
        assertEquals(10, path.last.y)
    }

    @Test
    fun `projectiles respect impenetrable clipping`() {
        val collision = CollisionManager(CollisionMatrix())
        assertTrue(ProjectileLineService.hasLineOfSight(Position(10, 10, 0), Position(13, 10, 0), collision))
        collision.flagSolid(12, 10, 0, impenetrable = true)
        assertFalse(ProjectileLineService.hasLineOfSight(Position(10, 10, 0), Position(13, 10, 0), collision))
    }

    @Test
    fun `rotated multi tile object mark and unmark are symmetric`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)
        builder.applyObject(1, 30, 30, 0, 10, 1, 2, 3, true, impenetrable = true)
        for (x in 30..32) for (y in 30..31) assertTrue(collision.isTileBlocked(x, y, 0))
        builder.removeObject(1, 30, 30, 0, 10, 1, 2, 3, true, impenetrable = true)
        for (x in 30..32) for (y in 30..31) assertFalse(collision.isTileBlocked(x, y, 0))
    }

    @Test
    fun `object reach uses Tarnish rotated footprint and walking flag`() {
        val objectId = 99_001
        GameObjectData.addDefinition(
            GameObjectData(
                id = objectId,
                name = "reach-test",
                description = "reach-test",
                sizeX = 2,
                sizeY = 3,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0x4,
            ),
        )
        val worldObject = WorldObject(objectId, 50, 50, 0, 10, 1)

        assertTrue(InteractionReachService.reachedObject(Position(50, 50, 0), worldObject))
        assertTrue(InteractionReachService.reachedObject(Position(50, 52, 0), worldObject))
        assertFalse(InteractionReachService.reachedObject(Position(49, 50, 0), worldObject))
        assertTrue(InteractionReachService.reachedObject(Position(52, 50, 0), worldObject))
    }

    @Test
    fun `terrain overlay water does not mark movement collision`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)
        val grid =
            testGrid {
                this[0][10][10] = testTile(10, 10, 0, overlay = 6)
                this[0][11][10] = testTile(11, 10, 0, attributes = DecodedMapTile.BLOCKED)
            }

        builder.applyTerrain(grid)

        assertFalse(collision.isTileBlocked(10, 10, 0))
        assertTrue(collision.isTileBlocked(11, 10, 0))
    }

    @Test
    fun `terrain down heights match Tarnish bridge lowering`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)
        val grid =
            testGrid {
                this[1][12][12] = testTile(12, 12, 1, attributes = DecodedMapTile.BRIDGE)
                this[2][12][12] = testTile(12, 12, 2, attributes = DecodedMapTile.BLOCKED)
                this[0][13][13] = testTile(13, 13, 0, attributes = DecodedMapTile.BLOCKED)
                this[1][13][13] = testTile(13, 13, 1, attributes = DecodedMapTile.BRIDGE)
            }

        builder.applyTerrain(grid)

        assertTrue(collision.isTileBlocked(12, 12, 1))
        assertFalse(collision.isTileBlocked(12, 12, 2))
        assertFalse(collision.isTileBlocked(13, 13, 0))
    }

    @Test
    fun `object placement uses Tarnish down heights`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)
        val objectId = 99_002
        val definition =
            GameObjectData(
                id = objectId,
                name = "bridge-object-test",
                description = "bridge-object-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0xF,
            )
        val grid =
            testGrid {
                this[1][14][14] = testTile(14, 14, 1, attributes = DecodedMapTile.BRIDGE)
                this[2][15][15] = testTile(15, 15, 2, attributes = DecodedMapTile.BRIDGE)
            }

        builder.applyObjectData(DecodedMapObject(objectId, 14, 14, 2, 10, 0, 0), definition, grid)
        builder.applyObjectData(DecodedMapObject(objectId, 15, 15, 2, 10, 0, 0), definition, grid)
        builder.applyObjectData(DecodedMapObject(objectId, 14, 14, 0, 10, 0, 0), definition, grid)

        assertTrue(collision.isTileBlocked(14, 14, 1))
        assertFalse(collision.isTileBlocked(14, 14, 2))
        assertTrue(collision.isTileBlocked(15, 15, 1))
        assertFalse(collision.isTileBlocked(14, 14, 0))
    }

    @Test
    fun `skipped Tarnish objects do not mark collision and remain audit-only`() {
        val collision = CollisionManager(CollisionMatrix())
        val skippedKey = SkippedObjectRepository.key(70, 70, 0)
        val builder = CollisionBuildService(collision, setOf(skippedKey))
        val definition =
            GameObjectData(
                id = 99_004,
                name = "skipped-test",
                description = "skipped-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            )
        val obj = DecodedMapObject(99_004, 70, 70, 0, 10, 0, 0)

        builder.applyObjectData(obj, definition)
        val audit = builder.auditObject(obj, null)

        assertFalse(collision.isTileBlocked(70, 70, 0))
        assertEquals("tarnish_removed_object", audit.skippedReason)
    }

    @Test
    fun `Tarnish removed object data is available`() {
        val skipped = SkippedObjectRepository.load(java.nio.file.Path.of("__missing_removed_objects__.json"))

        assertTrue(SkippedObjectRepository.key(3336, 3896, 0) in skipped)
        assertTrue(SkippedObjectRepository.key(2328, 3805, 0) in skipped)
    }

    @Test
    fun `clip probe uses effective object plane and ignores skipped audit records`() {
        val objectId = 99_005
        GameObjectData.addDefinition(
            GameObjectData(
                id = objectId,
                name = "audit-plane-test",
                description = "audit-plane-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            ),
        )
        CacheCollisionAuditStore.publish(
            regions = listOf(MapIndexEntry(0, 0, 0)),
            regionObjects =
                mapOf(
                    0 to
                        listOf(
                            CacheCollisionAuditObject(objectId, 40, 40, rawPlane = 1, effectivePlane = 0, type = 10, rotation = 0, regionId = 0),
                            CacheCollisionAuditObject(objectId, 41, 40, rawPlane = 0, effectivePlane = 0, type = 10, rotation = 0, regionId = 0, skippedReason = "tarnish_removed_object"),
                        ),
                ),
        )

        val loweredProbe = ClipProbeService.probeTile(40, 40, 0)
        val skippedProbe = ClipProbeService.probeTile(41, 40, 0)

        assertEquals(1, loweredProbe.objectMatches.size)
        assertEquals(1, loweredProbe.objectMatches.single().rawPlane)
        assertEquals(0, loweredProbe.objectMatches.single().effectivePlane)
        assertTrue(skippedProbe.objectMatches.isEmpty())
    }

    @Test
    fun `plain walking route falls back to closest reachable tile`() {
        val collision = CollisionManager.global()
        collision.clear()
        collision.flagSolid(12, 10, 0)
        val client = Client(null, 3)
        client.mapRegionX = 0
        client.mapRegionY = 0
        client.moveTo(10, 10, 0)
        val routed =
            WalkingRouteService.routePlainWalk(
                client,
                WalkRequest(164, 12, 10, false, intArrayOf(0), intArrayOf(0)),
            )

        assertTrue(routed)
        assertEquals(11, client.newWalkCmdX[client.newWalkCmdSteps - 1])
        assertEquals(10, client.newWalkCmdY[client.newWalkCmdSteps - 1])
        collision.clear()
    }

    @Test
    fun `type 22 without actions does not mark occupant collision`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)

        builder.applyObject(1, 60, 60, 0, 22, 0, 1, 1, solid = true, hasActions = false, decoration = false)

        assertFalse(collision.isTileBlocked(60, 60, 0))
    }

    @Test
    fun `object interaction clears stale walk only after Tarnish reach passes`() {
        val objectId = 99_003
        GameObjectData.addDefinition(
            GameObjectData(
                id = objectId,
                name = "settle-test",
                description = "settle-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            ),
        )
        CollisionManager.global().clear()
        val client = Client(null, 1)
        client.moveTo(100, 99, 0)
        client.newWalkCmdSteps = 1
        client.wQueueWritePtr = 1

        ObjectInteractionMovementSettleService.clearQueuedWalkIfReached(
            client,
            Position(100, 100, 0),
            objectId,
            GameObjectDef(objectId, 10, 0, Position(100, 100, 0)),
        )

        assertEquals(0, client.newWalkCmdSteps)
        assertEquals(client.wQueueReadPtr, client.wQueueWritePtr)

        val farClient = Client(null, 2)
        farClient.moveTo(90, 90, 0)
        farClient.newWalkCmdSteps = 1
        farClient.wQueueWritePtr = 1
        ObjectInteractionMovementSettleService.clearQueuedWalkIfReached(
            farClient,
            Position(100, 100, 0),
            objectId,
            GameObjectDef(objectId, 10, 0, Position(100, 100, 0)),
        )

        assertEquals(1, farClient.newWalkCmdSteps)
        assertEquals(1, farClient.wQueueWritePtr)
    }

    @Test
    fun `generic object approach routes from diagonal and clears once reached`() {
        val objectId = 99_004
        val objectData =
            GameObjectData(
                id = objectId,
                name = "approach-test",
                description = "approach-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            )
        val objectDef = GameObjectDef(objectId, 10, 0, Position(100, 100, 0))
        GameObjectData.addDefinition(objectData)
        CollisionManager.global().clear()

        val diagonalClient = Client(null, 4)
        diagonalClient.mapRegionX = 0
        diagonalClient.mapRegionY = 0
        diagonalClient.moveTo(99, 99, 0)

        val routed =
            ObjectApproachRoutingService.ensureReached(
                diagonalClient,
                objectId,
                Position(100, 100, 0),
                objectData,
                objectDef,
            )

        assertEquals(ApproachStatus.ROUTED, routed.status)
        assertTrue(diagonalClient.newWalkCmdSteps > 0)

        val reachedClient = Client(null, 5)
        reachedClient.moveTo(100, 99, 0)
        reachedClient.newWalkCmdSteps = 1
        reachedClient.wQueueWritePtr = 1

        val reached =
            ObjectApproachRoutingService.ensureReached(
                reachedClient,
                objectId,
                Position(100, 100, 0),
                objectData,
                objectDef,
            )

        assertEquals(ApproachStatus.REACHED, reached.status)
        assertEquals(0, reachedClient.newWalkCmdSteps)
        assertEquals(reachedClient.wQueueReadPtr, reachedClient.wQueueWritePtr)
    }

    @Test
    fun `reached action object with no handler completes as noop`() {
        val actionObjectId = 99_005
        val inertObjectId = 99_006
        val client = Client(null, 6)
        client.moveTo(100, 99, 0)
        val actionObject =
            GameObjectData(
                id = actionObjectId,
                name = "noop-action-test",
                description = "noop-action-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            )
        val inertObject =
            GameObjectData(
                id = inertObjectId,
                name = "noop-inert-test",
                description = "noop-inert-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = false,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            )
        GameObjectData.addDefinition(actionObject)
        GameObjectData.addDefinition(inertObject)

        val actionTiming =
            ObjectInteractionService.tryHandleTimed(
                ObjectInteractionContext.click(client, 1, actionObjectId, Position(100, 100, 0), actionObject),
            )
        val inertTiming =
            ObjectInteractionService.tryHandleTimed(
                ObjectInteractionContext.click(client, 1, inertObjectId, Position(100, 100, 0), inertObject),
            )

        assertTrue(actionTiming.handled)
        assertEquals("cache-action-noop", actionTiming.handlerName)
        assertFalse(inertTiming.handled)
    }

    @Test
    fun `cache action object no handler accepts closest diagonal vicinity fallback`() {
        val actionObjectId = 99_009
        val objectPosition = Position(2609, 3096, 0)
        val actionObject =
            GameObjectData(
                id = actionObjectId,
                name = "diagonal-vicinity-action-test",
                description = "diagonal-vicinity-action-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 1,
                walkingFlag = 0xF,
            )
        GameObjectData.addDefinition(actionObject)
        CollisionManager.global().clear()
        CollisionManager.global().setFlag(objectPosition.x, objectPosition.y, objectPosition.z, CollisionFlag.BLOCKED)

        val client = Client(null, 9)
        client.isActive = true
        client.validClient = true
        client.mapRegionX = (2608 shr 3) - 6
        client.mapRegionY = (3097 shr 3) - 6
        client.moveTo(2608, 3097, 0)
        client.newWalkCmdSteps = 0
        client.wQueueWritePtr = 0

        client.newWalkCmdSteps = 1
        client.wQueueWritePtr = 1
        client.newWalkCmdX[0] = objectPosition.x - client.mapRegionX * 8
        client.newWalkCmdY[0] = objectPosition.y - client.mapRegionY * 8
        val fallback =
            ObjectApproachRoutingService.ensureReached(
                player = client,
                objectId = actionObjectId,
                targetPosition = objectPosition,
                objectData = actionObject,
                objectDef = GameObjectDef(actionObjectId, 10, 0, objectPosition),
            )

        assertEquals(ApproachStatus.ROUTED, fallback.status)
        assertTrue(client.newWalkCmdSteps > 0)
        assertFalse(
            client.newWalkCmdX[0] == objectPosition.x - client.mapRegionX * 8 &&
                client.newWalkCmdY[0] == objectPosition.y - client.mapRegionY * 8,
        )
    }

    @Test
    fun `cache action object without server handler routes player toward object`() {
        val actionObjectId = 99_007
        val actionObject =
            GameObjectData(
                id = actionObjectId,
                name = "processor-noop-action-test",
                description = "processor-noop-action-test",
                sizeX = 1,
                sizeY = 1,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0,
            )
        GameObjectData.addDefinition(actionObject)
        CollisionManager.global().clear()
        val client = Client(null, 7)
        client.isActive = true
        client.validClient = true
        client.mapRegionX = (96 shr 3) - 6
        client.mapRegionY = (99 shr 3) - 6
        client.moveTo(96, 99, 0)
        client.newWalkCmdSteps = 0
        client.wQueueWritePtr = 0
        val target = Position(100, 100, 0)
        val intent =
            ObjectClickIntent(
                opcode = 132,
                createdCycle = 0L,
                option = 1,
                objectId = actionObjectId,
                objectPosition = target,
                objectData = actionObject,
                objectDef = GameObjectDef(actionObjectId, 10, 0, target),
            )

        InteractionSessionStateAdapter.schedule(client, intent)
        val result = InteractionProcessor.process(client)

        assertEquals(InteractionExecutionResult.WAITING, result)
        assertTrue(client.newWalkCmdSteps > 0 || client.wQueueWritePtr != client.wQueueReadPtr)
    }

    private fun testGrid(mutator: Array<Array<Array<DecodedMapTile>>>.() -> Unit): DecodedMapTileGrid {
        val tiles =
            Array(4) { plane ->
                Array(64) { x ->
                    Array(64) { y ->
                        testTile(x, y, plane)
                    }
                }
            }
        tiles.mutator()
        return DecodedMapTileGrid(regionId = 0, tiles = tiles)
    }

    private fun testTile(
        x: Int,
        y: Int,
        plane: Int,
        overlay: Int = 0,
        attributes: Int = 0,
    ): DecodedMapTile =
        DecodedMapTile(
            offsetX = x,
            offsetY = y,
            plane = plane,
            height = 0,
            overlay = overlay,
            overlayType = 0,
            overlayOrientation = 0,
            attributes = attributes,
            underlay = 0,
        )
}
