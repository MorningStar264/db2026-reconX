package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TradeNotFoundException
 *
 * WHAT:    Exception thrown when a requested trade cannot be found using the
 *          supplied trade reference.
 * HOW:     Extends {@link ReconException} and constructs an error message
 *          containing the missing trade reference.
 * WHY:     Separating missing-resource errors from other exception types
 *          allows the application to return an appropriate HTTP 404 response
 *          and clearly indicate that the requested trade does not exist.
 * ============================================================================
 */
public class TradeNotFoundException extends ReconException {
    public TradeNotFoundException(String tradeRef) {
        super("Trade not found: " + tradeRef);
    }
}