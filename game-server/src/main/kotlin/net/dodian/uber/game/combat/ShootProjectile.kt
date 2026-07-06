package net.dodian.uber.game.combat

import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.Entity
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import net.dodian.uber.game.netty.listener.out.Projectile
import net.dodian.uber.game.netty.listener.out.SendMessage

object SpotAnimNames {
    private val nameToId = mapOf(
        "bronze_arrow_travel" to 10,
        "bronze_arrow_launch" to 19,
        "iron_arrow_travel" to 9,
        "iron_arrow_launch" to 18,
        "steel_arrow_travel" to 11,
        "steel_arrow_launch" to 20,
        "mithril_arrow_travel" to 12,
        "mithril_arrow_launch" to 21,
        "adamant_arrow_travel" to 13,
        "adamant_arrow_launch" to 22,
        "rune_arrow_travel" to 15,
        "rune_arrow_launch" to 24,
        "ii_dragon_arrow_normal_projanim" to 1120,
        "dragon_arrow_launch" to 1116,
        "crossbowbolt_travel" to 27,
        
        "smoke_rush_travel" to 384,
        "smoke_rush_impact" to 385,
        "shadow_rush_travel" to 378,
        "shadow_rush_impact" to 379,
        "blood_rush_travel" to 372,
        "blood_rush_impact" to 373,
        "ice_rush_travel" to 360,
        "ice_rush_impact" to 361,
        
        "smoke_burst_travel" to 388,
        "smoke_burst_impact" to 389,
        "shadow_burst_impact" to 382,
        "spell_blood_burst_impact" to 376,
        "ice_burst_travel" to 366,
        "ice_burst_impact" to 367,
        
        "smoke_blitz_travel" to 386,
        "smoke_blitz_impact" to 387,
        "shadow_blitz_travel" to 380,
        "shadow_blitz_impact" to 381,
        "blood_blitz_travel" to 374,
        "blood_blitz_impact" to 375,
        "ice_blitz_travel" to 362,
        "ice_blitz_impact" to 363,
        
        "smoke_barrage_travel" to 390,
        "smoke_barrage_impact" to 391,
        "shadow_barrage_impact" to 383,
        "spell_blood_barrage_impact" to 377,
        "ice_barrage_impact" to 369
    )

    fun getId(name: String): Int {
        val rscmId = net.dodian.uber.game.rscm.RSCM.get("spotanim", name)
        if (rscmId != -1) return rscmId
        return nameToId[name] ?: -1
    }
}

object ShootProjectile {
    const val DEFAULT_HEIGHT = 40
    const val DEFAULT_CURVE = 16
    const val DEFAULT_DELAY = 51
    const val DEFAULT_SLOPE = 16
}

data class ProjectileDef(
    val startHeight: Int = 43,
    val endHeight: Int = 31,
    val curve: Int = 16,
    val delay: Int = 51,
    val slope: Int = 16
)

private val PROJECTILE_DEFS = mapOf(
    // Ranged
    "bronze_arrow_travel" to ProjectileDef(slope = 10),
    "iron_arrow_travel" to ProjectileDef(slope = 10),
    "steel_arrow_travel" to ProjectileDef(slope = 10),
    "mithril_arrow_travel" to ProjectileDef(slope = 10),
    "adamant_arrow_travel" to ProjectileDef(slope = 10),
    "rune_arrow_travel" to ProjectileDef(slope = 10),
    "ii_dragon_arrow_normal_projanim" to ProjectileDef(slope = 10),

    // Magic
    "smoke_rush_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "shadow_rush_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "blood_rush_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "ice_rush_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    
    "smoke_burst_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "ice_burst_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    
    "smoke_blitz_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "shadow_blitz_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "blood_blitz_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    "ice_blitz_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51),
    
    "smoke_barrage_travel" to ProjectileDef(startHeight = 43, endHeight = 31, delay = 51)
)

fun Position.shoot(
    gfxName: String,
    target: Entity,
    delay: Int? = null,
    startHeight: Int? = null,
    endHeight: Int? = null,
    slope: Int? = null
): Int {
    return projectile(
        caster = null,
        gfxName = gfxName,
        sourceTile = this,
        targetTile = target.position,
        target = target,
        delay = delay,
        startHeight = startHeight,
        endHeight = endHeight,
        slope = slope
    )
}

fun Position.shoot(
    gfxName: String,
    targetTile: Position,
    delay: Int? = null,
    startHeight: Int? = null,
    endHeight: Int? = null,
    slope: Int? = null
): Int {
    return projectile(
        caster = null,
        gfxName = gfxName,
        sourceTile = this,
        targetTile = targetTile,
        target = null,
        delay = delay,
        startHeight = startHeight,
        endHeight = endHeight,
        slope = slope
    )
}

fun Entity.shoot(
    gfxName: String,
    target: Entity,
    delay: Int? = null,
    startHeight: Int? = null,
    endHeight: Int? = null,
    slope: Int? = null
): Int {
    val defaultDef = PROJECTILE_DEFS[gfxName]
    return projectile(
        caster = this,
        gfxName = gfxName,
        sourceTile = this.position,
        targetTile = target.position,
        target = target,
        delay = delay,
        startHeight = startHeight ?: defaultDef?.startHeight ?: 43,
        endHeight = endHeight ?: defaultDef?.endHeight ?: 31,
        slope = slope
    )
}

fun Entity.shoot(
    gfxName: String,
    targetTile: Position,
    delay: Int? = null,
    startHeight: Int? = null,
    endHeight: Int? = null,
    slope: Int? = null
): Int {
    val defaultDef = PROJECTILE_DEFS[gfxName]
    return projectile(
        caster = this,
        gfxName = gfxName,
        sourceTile = this.position,
        targetTile = targetTile,
        target = null,
        delay = delay,
        startHeight = startHeight ?: defaultDef?.startHeight ?: 43,
        endHeight = endHeight ?: defaultDef?.endHeight ?: 31,
        slope = slope
    )
}

private fun projectile(
    caster: Entity?,
    gfxName: String,
    sourceTile: Position,
    targetTile: Position,
    target: Entity?,
    delay: Int?,
    startHeight: Int?,
    endHeight: Int?,
    slope: Int?
): Int {
    val gfxId = SpotAnimNames.getId(gfxName)
    if (gfxId == -1) return -1

    val defaultDef = PROJECTILE_DEFS[gfxName]

    val dx = kotlin.math.abs(sourceTile.x - targetTile.x)
    val dy = kotlin.math.abs(sourceTile.y - targetTile.y)
    val distance = kotlin.math.max(dx, dy)

    val startDelay = delay ?: defaultDef?.delay ?: ShootProjectile.DEFAULT_DELAY
    val projSlope = slope ?: defaultDef?.slope ?: ShootProjectile.DEFAULT_SLOPE

    val speed = 50 + (distance * 5)
    val totalTime = startDelay + speed

    val targetIndex = if (target != null) {
        val isNpc = target is Npc
        if (isNpc) target.slot + 1 else -(target.slot + 1)
    } else {
        0
    }

    if (net.dodian.uber.game.engine.config.gameWorldId == 2) {
        val msg = "[W2-PROJECTILE] gfx=$gfxName, source=(${sourceTile.x},${sourceTile.y},${sourceTile.z}), target=(${targetTile.x},${targetTile.y},${targetTile.z}), dist=$distance, delay=$startDelay, speed=$speed, slope=$projSlope, targetIndex=$targetIndex"
        println(msg)
        if (caster is Client) {
            caster.send(SendMessage(msg))
        }
    }

    // Pass the actual caster position (mutable, will be read during serialization after movement finalize)
    // or the static source tile if no caster entity exists.
    val sourceObject = caster?.position ?: sourceTile
    val targetObject = target ?: targetTile

    sendProjectile(
        source = sourceObject,
        targetObject = targetObject,
        gfxMoving = gfxId,
        startHeight = startHeight ?: defaultDef?.startHeight ?: ShootProjectile.DEFAULT_HEIGHT,
        endHeight = endHeight ?: defaultDef?.endHeight ?: ShootProjectile.DEFAULT_HEIGHT,
        targetIndex = targetIndex,
        delay = startDelay,
        speed = speed,
        slope = projSlope
    )

    return totalTime
}

private fun sendProjectile(
    source: Position,
    targetObject: Any,
    gfxMoving: Int,
    startHeight: Int,
    endHeight: Int,
    targetIndex: Int,
    delay: Int,
    speed: Int,
    slope: Int
) {
    val projectile = Projectile(source, targetObject, 50, speed, gfxMoving, startHeight, endHeight, targetIndex, delay, slope, 64)
    for (p in PlayerRegistry.players) {
        if (p != null) {
            val viewer = p as Client
            if (viewer.position != null && viewer.position.withinDistance(source, 64)) {
                viewer.send(projectile)
            }
        }
    }
}
