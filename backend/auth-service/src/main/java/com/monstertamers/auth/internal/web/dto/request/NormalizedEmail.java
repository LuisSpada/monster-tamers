package com.monstertamers.auth.internal.web.dto.request;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Trims and lower-cases the email while the request body is read.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@JacksonAnnotationsInside
@JsonDeserialize(using = NormalizedEmailDeserializer.class)
public @interface NormalizedEmail {
}
