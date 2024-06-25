package ase.exceptions;
public class InvalidBookingReferenceCodeException extends Exception{
    public InvalidBookingReferenceCodeException (String referenceCode) {
        super( referenceCode + " is not in the expected format for booking reference codes.");
    }
}
