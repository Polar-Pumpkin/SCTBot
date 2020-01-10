package org.serverct.enums;

import lombok.Getter;

public enum HighlightType {
    BOLD("比普通好点(加粗)"),
    GREEN("优异(绿色)"),
    GREEN_BOLD("优秀(绿色加粗)"),
    BLUE("精良(蓝色)"),
    BLUE_BOLD("精良(蓝色加粗)"),
    PURPLE("稀有(紫色)"),
    PURPLE_BOLD("稀有(紫色加粗)"),
    GOLD("史诗(金色)"),
    GOLD_BOLD("史诗(金色加粗)"),
    PINK("不思议(粉色加粗)"),
    ;

    private @Getter
    String name;
    HighlightType(String desc) {
        name = desc;
    }
}
