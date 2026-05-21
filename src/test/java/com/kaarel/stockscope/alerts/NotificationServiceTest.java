package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.NotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    @Test
    void findForSession_unread_filter_uses_dedicated_query() {
        Notification n = sample();
        when(repository.findAllBySessionIdAndReadFalseOrderByCreatedAtDesc("s1")).thenReturn(List.of(n));

        List<NotificationDto> result = service.findForSession("s1", true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
    }

    @Test
    void findForSession_returns_all_when_unread_filter_off() {
        when(repository.findAllBySessionIdOrderByCreatedAtDesc("s1")).thenReturn(List.of(sample(), sample()));

        List<NotificationDto> result = service.findForSession("s1", false);

        assertThat(result).hasSize(2);
    }

    @Test
    void markRead_throws_for_unknown_id() {
        when(repository.findByIdAndSessionId(9L, "s1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead("s1", 9L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void markRead_flags_read_true_and_returns_dto() {
        Notification n = sample();
        when(repository.findByIdAndSessionId(1L, "s1")).thenReturn(Optional.of(n));

        NotificationDto result = service.markRead("s1", 1L);

        assertThat(result.read()).isTrue();
        assertThat(n.isRead()).isTrue();
    }

    @Test
    void markAllRead_returns_repository_update_count() {
        when(repository.markAllReadForSession("s1")).thenReturn(5);

        assertThat(service.markAllRead("s1")).isEqualTo(5);
    }

    @Test
    void recordTrigger_persists_and_does_not_evict_below_cap() {
        Alert alert = alert("s1");
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.countBySessionId("s1")).thenReturn(50L);

        service.recordTrigger("s1", alert, 105.0, "msg");

        verify(repository, never()).findOldestIdsForSession(any(), any());
        verify(repository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    void recordTrigger_evicts_overflow_in_single_batch_when_cap_exceeded() {
        Alert alert = alert("s1");
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.countBySessionId("s1")).thenReturn(103L);
        List<Long> oldestIds = List.of(1L, 2L, 3L);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(repository.findOldestIdsForSession(any(), pageable.capture())).thenReturn(oldestIds);

        service.recordTrigger("s1", alert, 105.0, "msg");

        assertThat(pageable.getValue()).isEqualTo(PageRequest.of(0, 3));
        verify(repository).deleteAllByIdInBatch(oldestIds);
    }

    private Notification sample() {
        return new Notification("s1", alert("s1"), 105.0, "msg");
    }

    private Alert alert(String session) {
        return new Alert(session, "AAPL", AlertCondition.ABOVE, 100.0);
    }
}
