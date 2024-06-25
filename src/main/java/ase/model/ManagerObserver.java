package ase.model;

import java.util.ArrayList;
import java.util.TreeMap;

public interface  ManagerObserver {
    void flightDeparting(TreeMap<String, Flight> flights);
    void ecoQueueUpdated(ArrayList<Booking> ecoQueue, ArrayList<Booking> businessQueue);
    void businessQueueUpdated(ArrayList<Booking> ecoQueue, ArrayList<Booking> businessQueue);
    void deskUpdated(ArrayList<Desk> desks);
    void newCheckIn(TreeMap<String, Flight> flights);
}
