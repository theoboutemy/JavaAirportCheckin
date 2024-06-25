package ase.model;
import java.time.LocalDateTime;

public interface Clock {
    public LocalDateTime getCurrentTime();
    public void registerTimeChangeObserver(TimeChangeObserver observer);
    public void removeTimeChangeObserver(TimeChangeObserver observer);
    public void registerFlightTimeObserver(LocalDateTime departureTime, TimeChangeObserver FlightTimeObserver);
    public void removeFlightTimeObserver(LocalDateTime departureTime, TimeChangeObserver FlightTimeObserver);
}
