package net.dodian.game.persistence.repository

interface QueryRepository<in TReq, TRes> {
    suspend fun execute(request: TReq): DbResult<TRes>
}

