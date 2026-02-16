package com.tomassirio.wanderer.commons.domain;

public enum AchievementType {
    // Distance achievements (in kilometers) - TRIP
    DISTANCE_100KM("First Century", "Walk 100km in a single trip", 100, AchievementCategory.TRIP),
    DISTANCE_200KM("4daagse", "Walk 200km in a single trip", 200, AchievementCategory.TRIP),
    DISTANCE_500KM("Long Hauler", "Walk 500km in a single trip", 500, AchievementCategory.TRIP),
    DISTANCE_800KM("Camino Complete", "Walk 800km in a single trip", 800, AchievementCategory.TRIP),
    DISTANCE_1000KM(
            "Thousand Miles", "Walk 1000km in a single trip", 1000, AchievementCategory.TRIP),
    DISTANCE_1600KM(
            "The Proclaimer",
            "Walk 500 miles and 500 more (~1600km)",
            1600,
            AchievementCategory.TRIP),
    DISTANCE_2200KM(
            "The Hobbit", "Walk from the Shire to Mordor (2200km)", 2200, AchievementCategory.TRIP),

    // Update achievements - TRIP
    UPDATES_10("Getting Started", "Post 10 updates on a single trip", 10, AchievementCategory.TRIP),
    UPDATES_50(
            "Regular Reporter", "Post 50 updates on a single trip", 50, AchievementCategory.TRIP),
    UPDATES_100(
            "Century Poster", "Post 100 updates on a single trip", 100, AchievementCategory.TRIP),

    // Duration achievements (in days) - TRIP
    DURATION_7_DAYS("Week Warrior", "Trip lasting 7 days", 7, AchievementCategory.TRIP),
    DURATION_30_DAYS("Monthly Trekker", "Trip lasting 30 days", 30, AchievementCategory.TRIP),
    DURATION_45_DAYS("Six Week Explorer", "Trip lasting 45 days", 45, AchievementCategory.TRIP),
    DURATION_60_DAYS("Two Month Journey", "Trip lasting 60 days", 60, AchievementCategory.TRIP),

    // Social achievements - Followers - USER
    FOLLOWERS_10("Popular Walker", "Reach 10 followers", 10, AchievementCategory.USER),
    FOLLOWERS_50("Influencer", "Reach 50 followers", 50, AchievementCategory.USER),
    FOLLOWERS_100("Community Leader", "Reach 100 followers", 100, AchievementCategory.USER),

    // Social achievements - Friends - USER
    FRIENDS_5("Making Friends", "Make 5 friends", 5, AchievementCategory.USER),
    FRIENDS_20("Social Butterfly", "Make 20 friends", 20, AchievementCategory.USER),
    FRIENDS_50("Friend Collector", "Make 50 friends", 50, AchievementCategory.USER);

    private final String name;
    private final String description;
    private final int threshold;
    private final AchievementCategory category;

    AchievementType(String name, String description, int threshold, AchievementCategory category) {
        this.name = name;
        this.description = description;
        this.threshold = threshold;
        this.category = category;
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

    public AchievementCategory getCategory() {
        return category;
    }
}
