package CustomException;

public class InvalidEmailException extends CustomException {

    private static final String message = "Invalid Email. Please check email format and try again.";

    public InvalidEmailException() {
        super(message);
    }

}
