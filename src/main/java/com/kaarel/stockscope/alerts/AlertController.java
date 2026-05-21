package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.AlertDto;
import com.kaarel.stockscope.alerts.dto.CreateAlertRequest;
import com.kaarel.stockscope.session.SessionId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertDto> list(@SessionId String sessionId) {
        return service.findAllForSession(sessionId);
    }

    @PostMapping
    public ResponseEntity<AlertDto> create(
            @SessionId String sessionId,
            @Valid @RequestBody CreateAlertRequest request,
            UriComponentsBuilder uriBuilder) {
        AlertDto created = service.create(sessionId, request);
        URI location = uriBuilder.path("/api/alerts/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@SessionId String sessionId, @PathVariable Long id) {
        service.delete(sessionId, id);
    }
}
