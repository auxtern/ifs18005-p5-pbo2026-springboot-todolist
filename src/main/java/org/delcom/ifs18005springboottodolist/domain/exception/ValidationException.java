package org.delcom.ifs18005springboottodolist.domain.exception;

/**
 * Exception yang dilempar saat terjadi kegagalan validasi aturan bisnis atau input domain.
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
