package com.roomate.app.dto;

import com.roomate.app.entities.ChoreFrequencyUnitEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChoreCreateDto {
    private String choreName;
    private int frequency;
    private ChoreFrequencyUnitEnum frequencyUnit;
    private LocalDateTime deadline;

    public ChoreCreateDto() {}
}
