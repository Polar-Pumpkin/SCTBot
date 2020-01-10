package org.serverct.enums;

import lombok.Getter;

public enum TagType {
    NONE("无"),
    EXCELLENT("优秀"),
    ESSENCE("精华"),
    PERMITTED("授权搬运"),
    ;

    private @Getter
    String name;
    TagType(String desc) {
        name = desc;
    }
}
