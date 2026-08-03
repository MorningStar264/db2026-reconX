package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T16:26:33+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 26.0.1 (Oracle Corporation)"
)
@Component
public class TradeMapperImpl implements TradeMapper {

    @Override
    public TradeResponse toResponse(Trade trade) {
        if ( trade == null ) {
            return null;
        }

        Long counterpartyId = null;
        String counterpartyName = null;
        Long instrumentId = null;
        String instrumentSymbol = null;
        String tradeRef = null;
        String status = null;
        Long id = null;
        BigDecimal quantity = null;
        BigDecimal price = null;
        LocalDate tradeDate = null;
        Instant createdAt = null;
        Instant modifiedAt = null;

        counterpartyId = tradeCounterpartyId( trade );
        counterpartyName = tradeCounterpartyName( trade );
        instrumentId = tradeInstrumentId( trade );
        instrumentSymbol = tradeInstrumentSymbol( trade );
        tradeRef = trade.getTradeRef();
        status = TradeMapper.statusToString( trade.getStatus() );
        id = trade.getId();
        quantity = trade.getQuantity();
        price = trade.getPrice();
        tradeDate = trade.getTradeDate();
        createdAt = trade.getCreatedAt();
        modifiedAt = trade.getModifiedAt();

        TradeResponse tradeResponse = new TradeResponse( id, tradeRef, counterpartyId, counterpartyName, instrumentId, instrumentSymbol, quantity, price, tradeDate, status, createdAt, modifiedAt );

        return tradeResponse;
    }

    @Override
    public Trade toEntity(TradeRequest req) {
        if ( req == null ) {
            return null;
        }

        Trade trade = new Trade();

        trade.setTradeRef( req.tradeRef() );
        trade.setTradeDate( req.tradeDate() );
        trade.setQuantity( req.quantity() );
        trade.setPrice( req.price() );

        return trade;
    }

    private Long tradeCounterpartyId(Trade trade) {
        Counterparty counterparty = trade.getCounterparty();
        if ( counterparty == null ) {
            return null;
        }
        return counterparty.getId();
    }

    private String tradeCounterpartyName(Trade trade) {
        Counterparty counterparty = trade.getCounterparty();
        if ( counterparty == null ) {
            return null;
        }
        return counterparty.getName();
    }

    private Long tradeInstrumentId(Trade trade) {
        Instrument instrument = trade.getInstrument();
        if ( instrument == null ) {
            return null;
        }
        return instrument.getId();
    }

    private String tradeInstrumentSymbol(Trade trade) {
        Instrument instrument = trade.getInstrument();
        if ( instrument == null ) {
            return null;
        }
        return instrument.getSymbol();
    }
}
