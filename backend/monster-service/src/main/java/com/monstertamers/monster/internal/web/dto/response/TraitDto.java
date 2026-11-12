package com.monstertamers.monster.internal.web.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TraitDto {
    String name;
    String description;
}
