package org.serverct.enums.workcheck;

import lombok.Getter;

public enum WorkType {
    // 开发部
    PLUGIN("插件"),

    // 搬运部
    REDISTRIBUTE("搬运"),

    // 美工部
    ARTWORK("美工"),

    // 建筑部
    BUILDING("建筑"),

    // 翻译部
    TRANSLATION("文档翻译"),
    ;

    private @Getter
    String name;
    WorkType(String desc) {
        name = desc;
    }
}
