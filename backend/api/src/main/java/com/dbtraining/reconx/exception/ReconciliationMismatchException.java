package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * ReconciliationMismatchException
 *
 * WHAT:    Exception thrown when two trades being reconciled do not match
 *          according to the reconciliation rules.
 * HOW:     Extends {@link ReconException} and carries a descriptive message
 *          explaining the reconciliation mismatch.
 * WHY:     Separating reconciliation mismatches from other exception types
 *          allows the application to distinguish business reconciliation
 *          failures from validation or system errors.
 * ============================================================================
 */
public class ReconciliationMismatchException extends ReconException {
    public ReconciliationMismatchException(String message) { super(message); }
}