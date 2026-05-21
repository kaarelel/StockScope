package com.kaarel.stockscope.alerts.dto;

import com.kaarel.stockscope.alerts.AlertCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAlertRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "[A-Za-z0-9.\\-]+", message = "symbol may contain letters, digits, dot, and dash")
        String symbol,

        @NotNull
        AlertCondition condition,

        @NotNull
        @Positive
        Double targetPrice
) {}
