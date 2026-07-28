package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * InvalidTradeException
 *
 * WHAT:    Exception thrown when a trade fails business validation rules.
 * HOW:     Extends {@link ReconException} and carries a descriptive validation
 *          error message explaining why the trade is invalid.
 * WHY:     Separating business validation failures from other exception types
 *          allows the application to return an appropriate client error
 *          response and clearly identify invalid trade data.
 * ============================================================================
 */
public class InvalidTradeException extends ReconException {
    public InvalidTradeException(String message) { super(message); }
}