package com.monstertamers.web.docs;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint as public in the API docs, so it is shown without the bearer lock.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirements
public @interface PublicEndpoint {
}
