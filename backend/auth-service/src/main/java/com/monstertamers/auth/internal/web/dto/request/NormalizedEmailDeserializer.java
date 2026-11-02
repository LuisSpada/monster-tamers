package com.monstertamers.auth.internal.web.dto.request;

import java.util.Locale;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

class NormalizedEmailDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        String email = context.readValue(parser, String.class);
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
