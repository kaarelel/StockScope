package com.kaarel.stockscope.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameWatchlistRequest(
        @NotBlank @Size(max = 120) String name
) {}
