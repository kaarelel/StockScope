package com.kaarel.stockscope.controller;

import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false, defaultValue = "symbol") String sort,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        return stockService.search(search, sector, sort, direction);
    }

    @GetMapping("/sectors")
    public List<String> sectors() {
        return stockService.sectors();
    }

    @GetMapping("/gainers")
    public List<Stock> gainers(@RequestParam(defaultValue = "5") int limit) {
        return stockService.topGainers(limit);
    }

    @GetMapping("/losers")
    public List<Stock> losers(@RequestParam(defaultValue = "5") int limit) {
        return stockService.topLosers(limit);
    }

    @GetMapping("/active")
    public List<Stock> mostActive(@RequestParam(defaultValue = "5") int limit) {
        return stockService.mostActive(limit);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> detail(@PathVariable String symbol) {
        return stockService.findBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
