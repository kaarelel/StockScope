package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.AlertDto;
import com.kaarel.stockscope.alerts.dto.CreateAlertRequest;
import com.kaarel.stockscope.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertService {

    private final AlertRepository repository;
    private final StockService stockService;

    public AlertService(AlertRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<AlertDto> findAllForSession(String sessionId) {
        return repository.findAllBySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .map(AlertService::toDto)
                .toList();
    }

    public AlertDto create(String sessionId, CreateAlertRequest request) {
        String normalizedSymbol = request.symbol().toUpperCase();
        if (stockService.findBySymbol(normalizedSymbol).isEmpty()) {
            throw new UnknownSymbolException(normalizedSymbol);
        }
        Alert alert = new Alert(sessionId, normalizedSymbol, request.condition(), request.targetPrice());
        return toDto(repository.save(alert));
    }

    public void delete(String sessionId, Long id) {
        Alert alert = repository.findByIdAndSessionId(id, sessionId)
                .orElseThrow(() -> new AlertNotFoundException(id));
        repository.delete(alert);
    }

    static AlertDto toDto(Alert a) {
        return new AlertDto(
                a.getId(),
                a.getSymbol(),
                a.getCondition(),
                a.getTargetPrice(),
                a.isActive(),
                a.getCreatedAt(),
                a.getLastTriggeredAt());
    }
}
