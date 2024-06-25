/**
 * 
 */
package ase.exceptions;

/**
 * 
 */
public class FlightAlreadyDepartedException extends Exception{
	public FlightAlreadyDepartedException(String flightCode) {
		super(flightCode + " has already departed");
	}

}
