package com.kaarel.stockscope.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddItemRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "[A-Za-z0-9.\\-]+", message = "symbol may contain letters, digits, dot, and dash")
        String symbol
) {}
