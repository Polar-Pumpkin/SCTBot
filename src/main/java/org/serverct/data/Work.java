package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.enums.workcheck.Highlight;
import org.serverct.enums.workcheck.Tag;
import org.serverct.enums.workcheck.WorkType;

import java.util.Calendar;
import java.util.List;

public @Data @AllArgsConstructor class Work {

    WorkType type;
    String url;
    Calendar date;
    List<Tag> tags;
    Highlight highlight;
    String rating;
}
