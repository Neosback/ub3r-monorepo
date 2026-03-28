package net.dodian.uber.game.content.platform

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

object ContentDataLoader {
    @PublishedApi
    internal val mapper =
        ObjectMapper(TomlFactory())
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

    @JvmStatic
    inline fun <reified T> loadOptional(resourcePath: String): T? {
        val stream = javaClass.classLoader.getResourceAsStream(resourcePath) ?: return null
        stream.use { input ->
            return mapper.readValue(input)
        }
    }

    @JvmStatic
    inline fun <reified T> loadRequired(resourcePath: String): T {
        return loadOptional<T>(resourcePath)
            ?: throw IllegalStateException("Missing required content data file: $resourcePath")
    }
}
