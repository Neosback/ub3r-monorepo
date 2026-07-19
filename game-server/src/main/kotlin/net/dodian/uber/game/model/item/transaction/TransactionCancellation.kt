package net.dodian.uber.game.model.item.transaction

public class TransactionCancellation(public val err: TransactionResult.Err) :
    IllegalStateException()
