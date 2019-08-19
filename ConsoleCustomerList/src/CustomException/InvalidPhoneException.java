package CustomException;

public class InvalidPhoneException extends CustomException {

    private static final String message = "Invalid Phone. Please check phone format and try again.";

    public InvalidPhoneException() {
        super(message);
    }

}
