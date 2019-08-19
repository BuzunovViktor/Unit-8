package CustomException;

public class CountArgsException extends CustomException {

    private static final String message = "Wrong command format. Use help for more instructions.";

    public CountArgsException() {
        super(message);
    }

}
