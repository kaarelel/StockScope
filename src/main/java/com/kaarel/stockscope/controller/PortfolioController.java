package com.kaarel.stockscope.controller;

import com.kaarel.stockscope.model.PortfolioRequest;
import com.kaarel.stockscope.model.PortfolioResponse;
import com.kaarel.stockscope.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<PortfolioResponse> simulate(@Valid @RequestBody PortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.simulate(request));
    }
}
