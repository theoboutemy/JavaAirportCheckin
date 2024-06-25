package ase.exceptions;
public class NoMatchingBookingException extends Exception {
    public NoMatchingBookingException(String code) {
        super("No booking found with code: " + code);

    }
}