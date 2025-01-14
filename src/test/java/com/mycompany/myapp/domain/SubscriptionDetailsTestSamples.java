package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SubscriptionDetailsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static SubscriptionDetails getSubscriptionDetailsSample1() {
        return new SubscriptionDetails()
            .id(1L)
            .subscriptionName("subscriptionName1")
            .subscriptionAmount(1)
            .taxAmount(1)
            .totalAmount(1)
            .additionalComments("additionalComments1")
            .category("category1")
            .notificationBeforeExpiry(1)
            .notificationTo("notificationTo1")
            .notificationCc("notificationCc1")
            .notificationBcc("notificationBcc1");
    }

    public static SubscriptionDetails getSubscriptionDetailsSample2() {
        return new SubscriptionDetails()
            .id(2L)
            .subscriptionName("subscriptionName2")
            .subscriptionAmount(2)
            .taxAmount(2)
            .totalAmount(2)
            .additionalComments("additionalComments2")
            .category("category2")
            .notificationBeforeExpiry(2)
            .notificationTo("notificationTo2")
            .notificationCc("notificationCc2")
            .notificationBcc("notificationBcc2");
    }

    public static SubscriptionDetails getSubscriptionDetailsRandomSampleGenerator() {
        return new SubscriptionDetails()
            .id(longCount.incrementAndGet())
            .subscriptionName(UUID.randomUUID().toString())
            .subscriptionAmount(intCount.incrementAndGet())
            .taxAmount(intCount.incrementAndGet())
            .totalAmount(intCount.incrementAndGet())
            .additionalComments(UUID.randomUUID().toString())
            .category(UUID.randomUUID().toString())
            .notificationBeforeExpiry(intCount.incrementAndGet())
            .notificationTo(UUID.randomUUID().toString())
            .notificationCc(UUID.randomUUID().toString())
            .notificationBcc(UUID.randomUUID().toString());
    }
}
