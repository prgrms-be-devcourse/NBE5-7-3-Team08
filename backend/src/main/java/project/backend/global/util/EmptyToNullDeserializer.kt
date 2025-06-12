package project.backend.global.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class EmptyToNullDeserializer : JsonDeserializer<String>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
        val value = p.valueAsString
        return if ((value == null || value.isBlank())) null else value
    }
}