package org.serverct.enums;

import lombok.Getter;

public enum ActionlogType {
    JOIN("加入小组"),
    QUIT("退出小组"),
    PPOINT("P点数异动"),
    PASS("作品检查通过"),
    FAIL("作品检查未通过"),
    LOG("使用机器人登记并保存档案"),
    ;

    private @Getter String name;
    ActionlogType(String desc) {
        name = desc;
    }
}
