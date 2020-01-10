package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.enums.HighlightType;
import org.serverct.enums.TagType;
import org.serverct.enums.WorkType;

import java.util.Calendar;
import java.util.List;

public @Data @AllArgsConstructor class Work {

    WorkType type;
    String url;
    Calendar date;
    List<TagType> tags;
    HighlightType highlight;
    String rating;
}
