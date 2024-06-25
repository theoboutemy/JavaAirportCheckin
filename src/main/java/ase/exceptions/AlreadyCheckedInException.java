package ase.exceptions;
public class AlreadyCheckedInException extends Exception {
	public AlreadyCheckedInException(String bookingCode) {
		super ("The booking " + bookingCode + "is already checked in.");
	}
}
