package com.pbl.campus.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventCategory {

    LECTURE("LECTURE", "讲座"),
    SPORTS("SPORTS", "文体"),
    CLUB("CLUB", "社团"),
    VOLUNTEER("VOLUNTEER", "志愿"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    EventCategory(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
