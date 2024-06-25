/*This class represents the appication's clock logic.
 *It notifies flights of when their departure times have arrived.
 *The speed of the clock is impacted by the program speed.
 */

package ase.model;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.time.format.DateTimeFormatter;

public class AppClock implements Clock, Runnable {

    public static AppClock instance;
    private ArrayList<TimeChangeObserver> timeChangeObserver;
    private Map<LocalDateTime, List<TimeChangeObserver>> flightTimeObserver;
    private LocalDateTime currentTime;
    private Timer timer;
    private static final long TIME_CHANGE_INTERVAL = 1000;
    private DateTimeFormatter formatter= DateTimeFormatter.ofPattern("HH:MM");
    private LocalDateTime startTime = LocalDateTime.parse("2024-03-29T08:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private int programSpeed = 1;

    

    // Implement singleton so there's just one instance of the app clock
    private AppClock(){
        this.currentTime = startTime;
        this.flightTimeObserver = new TreeMap<>();
        this.timeChangeObserver = new ArrayList<>();
     
    }

    //return existing instance of appclock
    public static AppClock getInstance(){
        if (instance==null){
            instance = new AppClock();
        }
        return instance;
    }

    // Update time at set interval by units specified in the update time function
    public void startClock(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                updateTime();
                notifyTimeChangeObserver();
                
            }
        }, 0, TIME_CHANGE_INTERVAL/this.programSpeed);
    }

    // Add 1 minute to the current time
    private void updateTime(){
        currentTime = currentTime.plusMinutes(1);
        checkFlightTimeObservers();
        notifyTimeChangeObserver();
    }

    // update the program speed and restart the timer to reflect the new program speed.
    public void setProgramSpeed(int speed){
        this.programSpeed = speed;
        if (timer != null) {
            timer.cancel(); 
        }
        startClock();
    }

    // Override the getCurrentTime function to return the current time
    @Override
    public LocalDateTime getCurrentTime(){
        return currentTime;

    }

    // Add object as a time change observer
    @Override
    public void registerTimeChangeObserver(TimeChangeObserver observer){
        timeChangeObserver.add(observer);
    }

    // remove object as a time change observer
    @Override
    public void removeTimeChangeObserver(TimeChangeObserver observer){
        timeChangeObserver.remove(observer);

    }

    // Function to notify all time change observers
    public void notifyTimeChangeObserver(){
        for (TimeChangeObserver observer: timeChangeObserver){
            observer.onTimeChange(currentTime);
        }
    }

    // add object as a flight time observer
    @Override
    public void registerFlightTimeObserver(LocalDateTime departureTime, TimeChangeObserver FlightTimeObserver){
        flightTimeObserver.computeIfAbsent(departureTime, k->new ArrayList<>()).add(FlightTimeObserver);
    }

    // remove object as a flight time observer
    @Override
    public void removeFlightTimeObserver(LocalDateTime departureTime, TimeChangeObserver FlightTimeObserver){
        flightTimeObserver.getOrDefault(departureTime, new ArrayList<>()).remove(FlightTimeObserver);

    }

    // Check for the next departure time in the tree map and notify observers of when it has arrived.
    private void checkFlightTimeObservers(){
        LocalDateTime nextDepartureTime = flightTimeObserver.keySet().stream().findFirst().orElse(null);
        if (nextDepartureTime!=null && currentTime.compareTo(nextDepartureTime)>=0){
            List<TimeChangeObserver> observers = flightTimeObserver.remove(nextDepartureTime);
            if (observers != null){
                for (TimeChangeObserver flightObserver: observers){
                    flightObserver.onTimeChange(nextDepartureTime);
                }
            }
        }
    }

    

    public void run(){
        startClock();
    }

    @Override
    public String toString(){
        return currentTime.format(formatter);
    }

   
}
