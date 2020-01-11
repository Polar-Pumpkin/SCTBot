package org.serverct.enums.workcheck;

import lombok.Getter;

public enum Tag {
    NONE("无"),
    EXCELLENT("优秀"),
    ESSENCE("精华"),
    PERMITTED("授权搬运"),
    ;

    private @Getter
    String name;
    Tag(String desc) {
        name = desc;
    }
}
