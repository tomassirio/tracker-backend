package com.tomassirio.wanderer.commons.domain;

public enum AchievementType {
    // Distance achievements (in kilometers)
    DISTANCE_100KM("First Century", "Walk 100km in a single trip", 100),
    DISTANCE_500KM("Long Hauler", "Walk 500km in a single trip", 500),
    DISTANCE_800KM("Camino Complete", "Walk 800km in a single trip", 800),
    DISTANCE_1000KM("Thousand Miles", "Walk 1000km in a single trip", 1000),

    // Update achievements
    UPDATES_10("Getting Started", "Post 10 updates on a single trip", 10),
    UPDATES_50("Regular Reporter", "Post 50 updates on a single trip", 50),
    UPDATES_100("Century Poster", "Post 100 updates on a single trip", 100),

    // Duration achievements (in days)
    DURATION_7_DAYS("Week Warrior", "Trip lasting 7 days", 7),
    DURATION_30_DAYS("Monthly Trekker", "Trip lasting 30 days", 30),
    DURATION_60_DAYS("Two Month Journey", "Trip lasting 60 days", 60);

    private final String name;
    private final String description;
    private final int threshold;

    AchievementType(String name, String description, int threshold) {
        this.name = name;
        this.description = description;
        this.threshold = threshold;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getThreshold() {
        return threshold;
    }
}
