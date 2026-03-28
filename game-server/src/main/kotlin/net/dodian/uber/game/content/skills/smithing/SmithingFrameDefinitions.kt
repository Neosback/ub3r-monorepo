package net.dodian.uber.game.content.skills.smithing

object SmithingFrameDefinitions {
    val smithingFrame: Array<Array<SmithingFrameEntry>>
        get() =
            SmithingDefinitions.smithingTiers
                .map { tier ->
                    tier.products.map { product ->
                        SmithingFrameEntry(
                            itemId = product.itemId,
                            outputAmount = product.outputAmount,
                            levelRequired = product.levelRequired,
                            barsRequired = product.barsRequired,
                            barCountLineId = product.barCountLineId,
                            itemNameLineId = product.itemNameLineId,
                        )
                    }.toTypedArray()
                }.toTypedArray()
}
