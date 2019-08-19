import CustomException.CountArgsException;
import CustomException.InvalidEmailException;
import CustomException.InvalidPhoneException;

import java.util.HashMap;
import java.util.regex.Pattern;

public class CustomerStorage {
    private HashMap<String, Customer> storage;

    public CustomerStorage() {
        storage = new HashMap<>();
    }

    public void addCustomer(String data) throws CountArgsException, InvalidEmailException, InvalidPhoneException {
        final int countArgs = 4;
        String[] components = data.split("\\s+");
        if (components.length != countArgs) {
            throw new CountArgsException();
        }
        if (! isEmailValid(components[2])) {
            throw new InvalidEmailException();
        }
        if (! isPhoneValid(components[3])) {
            throw new InvalidPhoneException();
        }
        String name = components[0] + " " + components[1];
        storage.put(name, new Customer(name, components[3], components[2]));
    }

    public void listCustomers() {
        storage.values().forEach(System.out::println);
    }

    public void removeCustomer(String name) {
        storage.remove(name);
    }

    public int getCount() {
        return storage.size();
    }

    private boolean isEmailValid(String email) {
        final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isPhoneValid(String phone) {
        final Pattern PHONE_PATTERN = Pattern.compile("((\\+7|8)\\d{10})");
        return PHONE_PATTERN.matcher(phone).matches();
    }

}