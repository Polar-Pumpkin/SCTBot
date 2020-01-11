package org.serverct.enums.workcheck;

import lombok.Getter;

public enum Punishment {
    WARN("警告"),
    MUTE("禁言"),
    KICK("踢出"),
    ;

    private @Getter
    String name;
    Punishment(String desc) {
        name = desc;
    }
}
