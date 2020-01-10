package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.enums.ActionlogType;

import java.util.Calendar;

public @Data @AllArgsConstructor class Actionlog {

    ActionlogType type;
    Calendar date;
    String additional;
}
