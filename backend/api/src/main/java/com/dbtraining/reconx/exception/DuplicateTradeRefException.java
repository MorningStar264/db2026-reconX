package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * DuplicateTradeRefException
 *
 * WHAT:    Exception thrown when an attempt is made to create or persist a
 *          trade using a trade reference that already exists.
 * HOW:     Extends {@link ReconException} and carries an error message
 *          identifying the duplicate trade reference.
 * WHY:     Ensures that every trade has a unique business identifier,
 *          preventing duplicate records from entering the reconciliation
 *          engine.
 * ============================================================================
 */
public class DuplicateTradeRefException extends ReconException {
    public DuplicateTradeRefException(String tradeRef) {
        super("Duplicate tradeRef: " + tradeRef);
    }
}