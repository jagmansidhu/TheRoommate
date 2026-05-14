package com.roomate.app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChoreFrequencyUnitEnum {
    WEEKLY(7),
    BIWEEKLY(14),
    MONTHLY(30);

    private final int days;
}
