package com.pbl.campus.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventStatus {

    OPEN("OPEN", "报名中"),
    ONGOING("ONGOING", "进行中"),
    ENDED("ENDED", "已结束");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    EventStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
