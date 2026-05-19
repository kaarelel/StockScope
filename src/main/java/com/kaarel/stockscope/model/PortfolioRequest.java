package com.kaarel.stockscope.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PortfolioRequest(
        @NotNull @Positive Double totalAmount,
        @NotEmpty @Valid List<Allocation> allocations
) {
    public record Allocation(
            @NotNull String symbol,
            @NotNull @Positive Double weightPct
    ) {}
}
