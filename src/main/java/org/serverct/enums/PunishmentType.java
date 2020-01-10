package org.serverct.enums;

import lombok.Getter;

public enum PunishmentType {
    WARN("警告"),
    MUTE("禁言"),
    KICK("踢出"),
    ;

    private @Getter
    String name;
    PunishmentType(String desc) {
        name = desc;
    }
}
