/**
 * 
 */
package ase.model;

import java.util.TreeMap;

/**
 * 
 */
public interface FlightObserver {
	public void flightDeparting(String flightReferenceCode);
	public void checkIn(Booking bk);
	

}
