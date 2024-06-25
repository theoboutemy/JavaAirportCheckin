package ase.exceptions;

public class InvalidNameException extends Exception{
	public InvalidNameException(String error) {
		super("The name you entered is invalid because " + error);
		
	}

}
