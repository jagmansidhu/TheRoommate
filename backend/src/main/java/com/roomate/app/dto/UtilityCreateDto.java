package com.roomate.app.dto;

import com.roomate.app.entities.UtilDistributionEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.roomate.app.entities.ChoreFrequencyUnitEnum;

@Data
public class UtilityCreateDto {
    private String utilityName;
    private String description;
    private double utilityPrice;
    private UtilDistributionEnum utilDistributionEnum;

    private ChoreFrequencyUnitEnum frequencyUnit;
    private LocalDateTime deadline;
    private LocalDateTime startingDate;

    private UUID roomId;

    private Map<UUID, Double> customSplit;
}
