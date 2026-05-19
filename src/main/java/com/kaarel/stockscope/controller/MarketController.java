package com.kaarel.stockscope.controller;

import com.kaarel.stockscope.model.MarketOverview;
import com.kaarel.stockscope.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/overview")
    public MarketOverview overview() {
        return marketService.overview();
    }
}
