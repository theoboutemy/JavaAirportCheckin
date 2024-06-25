package ase.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.time.LocalDateTime;
import ase.exceptions.InvalidBookingReferenceCodeException;
import ase.exceptions.InvalidFlightReferenceCodeException;
import ase.exceptions.InvalidNameException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;


public class Manager extends Thread implements FlightObserver, DeskObserver, BookingObserver{

    private static ArrayList<Booking> onHoldQueue = new ArrayList<>(); // Contains on waiting bookings
    private static ArrayList<Booking> ecoQueue = new ArrayList<>();
    private static ArrayList<Booking> businessQueue = new ArrayList<>();
    private static ArrayList<Booking> archiveQueue = new ArrayList<>(); // Contains the already processed booking
    private TreeMap<String, Flight> flights= new TreeMap<>();
    private ArrayList<Desk> desks = new ArrayList<>();
    Random random = new Random(); // Used to randomize the adding of passengers to the working queue
    private int programSpeed=1;
    private List<ManagerObserver> observers = new ArrayList<>();
    private ScheduledExecutorService executor;

    Logger logger;
    AppClock clock = AppClock.getInstance();

    //private final int ORIGINAl_AMOUNT_DESKS = 3;
    Logger log;
    int id;
    ManagerObserver observer;
    String flightReferenceCode,Action,filepath;
    Booking bk;  

    void initFlights() {
        
        ArrayList<String> lines = getFileLines("text_files/flights.txt");

        for (String line : lines) {
            String[] infos = line.split(";");

            if (infos.length != 11) {
                System.out.println("Flight could not be registered : wrong amount of argument");
                System.out.println(infos.length);
                continue;
            }
            try {
                //modified by Théo. The manager registers as an Observer to each flight
                Flight newFlight=new Flight(infos[0], infos[1], infos[2], Integer.parseInt(infos[3]), Integer.parseInt(infos[4]), Integer.parseInt(infos[5]), Integer.parseInt(infos[6]), Integer.parseInt(infos[7]), Integer.parseInt(infos[8]), Integer.parseInt(infos[9]), LocalDateTime.parse(infos[10], DateTimeFormatter.ISO_LOCAL_DATE_TIME));//Added random local date time to remove error
                this.flights.put(infos[0], newFlight );
                newFlight.registerObserver(this);
            } catch (InvalidFlightReferenceCodeException e) {
                System.out.println("Flight could not be registered : " + e.getMessage());
            }
        }
    }

    void initBooking() {
        ArrayList<String> lines = getFileLines("text_files/bookings.txt");

        for (String line : lines) {
            String[] infos = line.split(";");

            if (infos.length != 5) {
                System.out.println("Booking could not be registered : wrong amount of argument");
                continue;
            }

            try {
                boolean checkedIn = false;

                if (Objects.equals(infos[3], "true")) {
                    checkedIn = true;
                }
                Booking newBooking = new Booking(infos[0], infos[1], infos[2], checkedIn, infos[4]);
                onHoldQueue.add(newBooking);
                newBooking.registerObserver(this);
            } catch (InvalidBookingReferenceCodeException | InvalidNameException e) {
                System.out.println("Booking could not be registered : " + e.getMessage());
            }
        }
    }

    public Manager() {
        logger = Logger.getInstance();
        initFlights();
        initBooking();

        addPeopleInQueue();
        executor = Executors.newSingleThreadScheduledExecutor();
    }

// Move a booking from the onHoldQueue to the Queue
    public synchronized void addPeopleInQueue() {
        if (onHoldQueue.isEmpty()) {
            return;
        }
        int Adding =  desks.size() + 1;
        Iterator<Booking> iterator = onHoldQueue.iterator();
        while (iterator.hasNext() && Adding > 0) {
            Booking bk = iterator.next();
            bk.setBaggageWeight((float) (Math.random() * (50.0 - 10.0) + 10.0)); // Generates a value between 10 and 50kg
            bk.setBaggageVolume((float) (Math.random() * (30000.0 - 10000.0) + 10000.0)); // Generates a value between 10k and 30k of volume
            // Handle class priority here
            if(bk.getCabinClass().equals("economy")){
                ecoQueue.add(bk);
            }
            else if (bk.getCabinClass().equals("business")){
                businessQueue.add(bk);
            }

            iterator.remove();
            Adding--;
        }
        notifyObserverCheckIn();
        
    }

    public TreeMap<String, Flight> getFlights() {
        return flights;
    }

// Each desk is given a booking to check in
    public synchronized boolean iterationDesk() {

        for (int i = 0; i < desks.size(); i++) {
            if (ecoQueue.isEmpty() && businessQueue.isEmpty() && onHoldQueue.isEmpty()) {
                return false;
            }
            if (ecoQueue.isEmpty() && businessQueue.isEmpty()) {
                addPeopleInQueue();
            }
            if (desks.get(i).getDeskState() != Desk.STATE.open) {
                continue;
            }
            // handle class priority here
            Booking bk = checkClassPriority();
            Desk dk = desks.get(i);
            if (dk.getState() == State.TERMINATED) {
                dk.checkIn(bk, flights.get(bk.getFlightCode()));
                dk.run();
            } else {
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            archiveQueue.add(bk);
            if (bk.getCabinClass().equals("economy")){
                ecoQueue.remove(bk);
            }
            if (bk.getCabinClass().equals("business")){
                businessQueue.remove(bk);
            }

            notifyObserverCheckIn();
            // notify Appview
        }
        return true;
    }
    public synchronized Booking checkClassPriority(){
        if (!ecoQueue.isEmpty() && !businessQueue.isEmpty()){
            Booking ecoBk = ecoQueue.getFirst();
            Booking busiBk = businessQueue.getFirst();
            Flight flEco = flights.get(ecoBk.getFlightCode());
            Flight flBus = flights.get(busiBk.getFlightCode());

            if (flBus == null) {
                return busiBk;
            }
            if (flEco == null) {
                return ecoBk;
            }
            int ecoBkTimeBeforeDepart = flEco.getBoardingTimeRemaining();
            int busiBkTimeBeforeDepart = flBus.getBoardingTimeRemaining();
            if (ecoBkTimeBeforeDepart > busiBkTimeBeforeDepart && ecoBkTimeBeforeDepart < 25){    // business class is always prior except if the eco class passenger has his flight depart earlier and in less than 5min
                return ecoBk;
            }
            else{
                return busiBk;
            }
        }
        else if (!businessQueue.isEmpty()){
            Booking busiBk = businessQueue.getFirst();
            return busiBk;
        }
        else{
            Booking ecoBk = ecoQueue.getFirst();
            return ecoBk;
        }
    }


    public int addDesk() {
        
        if (!desks.isEmpty()) {
            id = desks.getLast().getID() + 1;
        } else {
            id = 1; 
        }        
        // ???? is that working for you ? 
        
        Desk newDesk = new Desk(id);
        newDesk.start();
        desks.add(newDesk);
        newDesk.registerObserver(this);
        
        return id;
    }

    public void closeDesk(int id) {
        for (Desk dk : desks) {
            if (dk.getID() == id) {
                dk.closeDesk();
                notifyObserverDesk();
            }
        }
        logger.write("Desk " + id + " has closed");
    }

    public void setProgramSpeed(int programSpeed) {
        this.programSpeed = programSpeed;
        clock.setProgramSpeed(programSpeed);
    }

    public int getProgramSpeed(){
        return (10000/programSpeed);
    }
    


    public void registerObserver(ManagerObserver observer) {
    	observers.add(observer);
    }

    public void notifyObserverFlight() {
    	for (ManagerObserver observer: observers) {
    		observer.flightDeparting(this.flights);
            observer.ecoQueueUpdated(this.ecoQueue, this.businessQueue);
            observer.businessQueueUpdated(this.ecoQueue, this.businessQueue);
            observer.deskUpdated(this.desks);
    	}
    }

    public void notifyObserverCheckIn() {
        for (ManagerObserver observer: observers) {
            observer.ecoQueueUpdated(this.ecoQueue, this.businessQueue);
            observer.businessQueueUpdated(this.ecoQueue, this.businessQueue);
            observer.newCheckIn(flights);
        }
    }

    public void notifyObserverDesk() {
        for (ManagerObserver observer: observers) {
            observer.deskUpdated(this.desks);
        }
    }


    public void run() {
        while (iterationDesk()) {

            addPeopleInQueue();
            notifyObserverCheckIn();
            // notifyAppView
            try{
                Thread.sleep((long) (5000.0/programSpeed));
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
    public Logger getLogger(){
        return logger;
    }

    @Override
    public synchronized void flightDeparting(String flightReferenceCode){
           // Schedule the removal of the flights from the queue after 5 seconds
           executor.schedule(()->{
            flights.remove(flightReferenceCode);
            notifyObserverFlight();
        },10, TimeUnit.SECONDS);
        logger.write("Flight " + flightReferenceCode + " has departed.");
        notifyObserverFlight();  
    }

    @Override
    public void checkIn(Booking bk){
        notifyObserverCheckIn();
    }

    @Override
    public void newAction(String Action){
        notifyObserverDesk();
    }

    @Override
    public void checkInSuccessful(Booking bk){
        notifyObserverCheckIn();
    }

    public static ArrayList<String> getFileLines(String filepath) {
        BufferedReader reader;
        ArrayList<String> lines = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();

            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

            reader.close();

            if (lines.isEmpty()) {
                throw new java.lang.NullPointerException("Config file is empty");
            }
        }


        catch (IOException e) {
            throw new NullPointerException("Config file was not found");
        }
        return lines;
    }
}