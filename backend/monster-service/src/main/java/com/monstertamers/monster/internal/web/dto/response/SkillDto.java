package com.monstertamers.monster.internal.web.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SkillDto {
    String name;
    String description;

    /** Placeholder until skills are modelled. */
    public static SkillDto stub() {
        return SkillDto.builder()
                .name("Quick Strike")
                .description("A fast attack on the nearest enemy.")
                .build();
    }
}
