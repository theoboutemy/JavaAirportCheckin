package ase.exceptions;
public class InvalidFlightReferenceCodeException extends Exception{
    public InvalidFlightReferenceCodeException(String ref){
        super("Ref code: " + ref + " is invalid");
    }
}
