package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.AlertDto;
import com.kaarel.stockscope.alerts.dto.CreateAlertRequest;
import com.kaarel.stockscope.model.RiskLevel;
import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private AlertService service;

    @Test
    void create_uppercases_symbol_and_persists() {
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL")));
        when(repository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertDto result = service.create("s1", new CreateAlertRequest("aapl", AlertCondition.ABOVE, 250.0));

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.condition()).isEqualTo(AlertCondition.ABOVE);
        assertThat(result.targetPrice()).isEqualTo(250.0);
        assertThat(result.active()).isTrue();

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSessionId()).isEqualTo("s1");
    }

    @Test
    void create_rejects_unknown_symbol() {
        when(stockService.findBySymbol("ZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("s1", new CreateAlertRequest("ZZZZ", AlertCondition.BELOW, 10.0)))
                .isInstanceOf(UnknownSymbolException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_loads_only_for_session() {
        Alert alert = new Alert("s1", "AAPL", AlertCondition.ABOVE, 250.0);
        when(repository.findByIdAndSessionId(1L, "s1")).thenReturn(Optional.of(alert));

        service.delete("s1", 1L);

        verify(repository).delete(alert);
    }

    @Test
    void delete_throws_when_session_does_not_own_alert() {
        when(repository.findByIdAndSessionId(7L, "intruder")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("intruder", 7L))
                .isInstanceOf(AlertNotFoundException.class);
    }

    @Test
    void findAllForSession_returns_dtos_in_repository_order() {
        Alert a = new Alert("s1", "AAPL", AlertCondition.ABOVE, 250.0);
        Alert b = new Alert("s1", "MSFT", AlertCondition.BELOW, 400.0);
        when(repository.findAllBySessionIdOrderByCreatedAtDesc("s1")).thenReturn(List.of(a, b));

        List<AlertDto> result = service.findAllForSession("s1");

        assertThat(result).extracting(AlertDto::symbol).containsExactly("AAPL", "MSFT");
    }

    private Stock stock(String symbol) {
        return new Stock(symbol, "Name", "Sector", 100.0, 99.0, 1.0, 0, 0, 0, 100, 1, 10, 0, RiskLevel.LOW, "");
    }
}
