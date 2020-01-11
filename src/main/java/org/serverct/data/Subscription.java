package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.enums.SubscriptionTarget;

public @Data @AllArgsConstructor class Subscription {

    SubscriptionTarget target;
    int interval;
    String broadcast;
}
