/**
 * 
 */
package ase.exceptions;

/**
 * 
 */
public class DeskAlreadyClosedException extends Exception{
	public DeskAlreadyClosedException(String deskId) {
		super(deskId + " is already closed");
	}

}
