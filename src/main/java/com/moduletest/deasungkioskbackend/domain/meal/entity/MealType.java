package com.moduletest.deasungkioskbackend.domain.meal.entity;

import java.time.LocalTime;

public enum MealType {
    LUNCH("점심식사", LocalTime.of(12, 10), LocalTime.of(13, 10)),
    DINNER("저녁식사", LocalTime.of(18, 0), LocalTime.of(19, 0));

    private final String label;
    private final LocalTime startTime;
    private final LocalTime endTime;

    MealType(String label, LocalTime startTime, LocalTime endTime) {
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getLabel() {
        return label;
    }

    public static MealType fromCurrentTime(LocalTime now) {
        for (MealType type : values()) {
            if (!now.isBefore(type.startTime) && !now.isAfter(type.endTime)) {
                return type;
            }
        }
        return null;
    }
}
