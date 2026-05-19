package com.kaarel.stockscope.controller;

import com.kaarel.stockscope.model.MarketInsight;
import com.kaarel.stockscope.service.InsightsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public List<MarketInsight> all() {
        return insightsService.all();
    }
}
