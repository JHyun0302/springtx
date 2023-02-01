package hello.springtx.order;

/**
 * 결제 잔고가 부족하면 발생하는 비지니스 예외
 */
public class NotEnoughMoneyException extends Exception {
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
