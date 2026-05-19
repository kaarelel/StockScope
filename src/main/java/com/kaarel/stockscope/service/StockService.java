package com.kaarel.stockscope.service;

import com.kaarel.stockscope.model.Stock;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    private final MockStockData data;

    public StockService(MockStockData data) {
        this.data = data;
    }

    public List<Stock> search(String query, String sector, String sortBy, String direction) {
        var stream = data.all().stream();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            stream = stream.filter(s ->
                    s.symbol().toLowerCase().contains(q) ||
                            s.name().toLowerCase().contains(q));
        }

        if (sector != null && !sector.isBlank() && !"all".equalsIgnoreCase(sector)) {
            stream = stream.filter(s -> s.sector().equalsIgnoreCase(sector));
        }

        Comparator<Stock> comparator = comparatorFor(sortBy);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return stream.sorted(comparator).toList();
    }

    public Optional<Stock> findBySymbol(String symbol) {
        if (symbol == null) return Optional.empty();
        return data.all().stream()
                .filter(s -> s.symbol().equalsIgnoreCase(symbol))
                .findFirst();
    }

    public List<Stock> topGainers(int limit) {
        return data.all().stream()
                .sorted(Comparator.comparingDouble(Stock::dayChangePct).reversed())
                .limit(limit)
                .toList();
    }

    public List<Stock> topLosers(int limit) {
        return data.all().stream()
                .sorted(Comparator.comparingDouble(Stock::dayChangePct))
                .limit(limit)
                .toList();
    }

    public List<Stock> mostActive(int limit) {
        return data.all().stream()
                .sorted(Comparator.comparingLong(Stock::volumeMillions).reversed())
                .limit(limit)
                .toList();
    }

    public List<String> sectors() {
        return data.all().stream()
                .map(Stock::sector)
                .distinct()
                .sorted()
                .toList();
    }

    private Comparator<Stock> comparatorFor(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Stock::symbol);
        return switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(Stock::name);
            case "price" -> Comparator.comparingDouble(Stock::price);
            case "change", "daychangepct" -> Comparator.comparingDouble(Stock::dayChangePct);
            case "marketcap" -> Comparator.comparingLong(Stock::marketCapBillions);
            case "volume" -> Comparator.comparingLong(Stock::volumeMillions);
            case "sector" -> Comparator.comparing(Stock::sector);
            case "risk" -> Comparator.comparing(Stock::risk);
            default -> Comparator.comparing(Stock::symbol);
        };
    }
}
