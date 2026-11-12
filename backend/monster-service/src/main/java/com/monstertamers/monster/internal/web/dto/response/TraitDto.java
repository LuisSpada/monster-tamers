package com.monstertamers.monster.internal.web.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TraitDto {
    String name;
    String description;

    /** Placeholder until traits are modelled. */
    public static TraitDto stub() {
        return TraitDto.builder()
                .name("Steady")
                .description("Keeps calm under pressure.")
                .build();
    }
}
