package vn.edu.pxu.bank.exception;

public class InsufficientBalanceException extends IllegalStateException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
