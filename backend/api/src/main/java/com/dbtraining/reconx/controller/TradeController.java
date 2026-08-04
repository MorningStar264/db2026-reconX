package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.service.TradeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/v1/trades")
public class TradeController {

    private final TradeQueryService queryService;
    private final TradeMapper mapper;

    public TradeController(TradeQueryService queryService, TradeMapper mapper) {
        this.queryService = queryService;
        this.mapper       = mapper;
    }
     
    @GetMapping
    public PagedResponse<TradeResponse> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) TradeStatus status,
        @RequestParam(required = false) Long counterpartyId,
        @PageableDefault(size = 20, sort = "tradeDate", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<Trade> page = queryService.search(from, to, status, counterpartyId, pageable);
        return PagedResponse.of(page.map(mapper::toResponse));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets deleted_at)")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                   @AuthenticationPrincipal Object principal) {
        queryService.softDelete(id, String.valueOf(principal));
        return ResponseEntity.noContent().build();
    }
    
    @Deprecated(since = "v1.4.0", forRemoval = true)
    @GetMapping(value = "/old-search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> oldSearch(HttpServletResponse response) {
        response.setHeader("Deprecation", "true");
        response.setHeader("Sunset", "Sat, 1 Jul 2026 00:00:00 GMT");
        response.setHeader("Link",
                "</api/v1/trades?status=...>; rel=\"successor-version\"");
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets deleted_at)")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                   @AuthenticationPrincipal Object principal) {
    service.softDelete(id, String.valueOf(principal));
    return ResponseEntity.noContent().build();
}
@Deprecated(since = "v1.4.0", forRemoval = true)
@GetMapping(value = "/old-search", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Void> oldSearch(HttpServletResponse response) {
    response.setHeader("Deprecation", "true");
    response.setHeader("Sunset", "Sat, 1 Jul 2026 00:00:00 GMT");
    response.setHeader("Link",
            "</api/v1/trades?status=...>; rel=\"successor-version\"");
    return ResponseEntity.status(HttpStatus.GONE).build();
}
}