package ase.exceptions;
public class NoMatchingNameException extends Exception{
	public NoMatchingNameException (String name) {
		super(name + " is not the last name associated with this booking.");
	}
}
