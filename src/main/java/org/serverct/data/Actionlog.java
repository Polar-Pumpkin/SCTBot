package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;

public @Data @AllArgsConstructor class Actionlog {

    org.serverct.enums.memberdata.Actionlog type;
    Calendar date;
    String additional;
}
