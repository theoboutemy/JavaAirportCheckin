package ase.model;

import ase.exceptions.*;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;


public class Flight implements TimeChangeObserver, BookingObserver{
    private String flightReferenceCode, destinationAirport, carrier;
    private int maxPassengers;
    private float flightMaxBaggageWeight, flightMaxBaggageVolume;
    private float passengerMaxBaggageWeight, passengerMaxBaggageVolume;
    private Map<String, Booking> bookings = new HashMap<String, Booking>();
    private float totalBaggageVolume,totalBaggageWeight;
    private float totalWeightExceeded,totalVolumeExceeded;
    private float totalExcessFee, excessFee;
    private float weightFeeRate,volumeFeeRate ;
    private int passengerCount=0;
    enum STATUS {Boarding, Departed};
    private STATUS status;
    private LocalDateTime depatureTime;
    private List<FlightObserver> observers = new ArrayList<>();
    private LocalDateTime currentTime= LocalDateTime.now();
    private AppClock clock = AppClock.getInstance();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private float fill = 0;
    private DecimalFormat df = new DecimalFormat("#.#");

    /**
     *Constructs a new Flights object with the given parameters
     * @param flightReferenceCode The unique reference code for the flight.
     * @param destinationAirport The airport where the flight is going.
     * @param carrier            The airline company operating the flight.
     * @param capacity          The maximum number of passengers allowed on the flight.
     * @param flightBaggageWeightMax   The maximum weight of baggage allowed per passenger.
     * @param flightBaggageVolumeMax   The maximum volume of baggage allowed per passenger.
     * @param passengerBaggageWeightMax The maximum allowed weight of baggage per passenger.
     * @param passengerBaggageVolumeMax The maximum allowed volume of baggage per passenger.
     * @param weightFeeRate     The fee rate for exceeding the maximum weight of baggage.
     * @param volumeFeeRate     The fee rate for exceeding the maximum volume of baggage.
     * Checks that all required parameters are provided.
     * Trims to remove white spaces.
     * Creates Regex pattern to validate reference code.
     */

    public Flight (String flightReferenceCode, String destinationAirport, String carrier, int capacity, float flightBaggageWeightMax, float flightBaggageVolumeMax, float passengerBaggageWeightMax, float passengerBaggageVolumeMax, float weightFeeRate, float volumeFeeRate, LocalDateTime depatureTime) throws InvalidFlightReferenceCodeException {

        String codeFormat="[A-Z]{4}\\d{1}";
        Pattern codePattern = Pattern.compile(codeFormat);
        Matcher matcher = codePattern.matcher(flightReferenceCode);

        if(flightReferenceCode.trim()=="") {
            throw new IllegalStateException("reference code cannot be empty");
        }
        else if(destinationAirport.trim()=="") {
            throw new IllegalStateException("Destination airport cannot be empty");
        }
        else if (carrier.trim()=="") {
            throw new IllegalStateException("Carrier cannot be empty");
        }
        else if (capacity<=0) {
            throw new IllegalStateException("Capacity must be a positive integer");
        }
        else if (flightBaggageWeightMax<=0) {
            throw new IllegalStateException("Flight's maximum baggage weight must be a positive integer");
        }
        else if (flightBaggageVolumeMax<=0) {
            throw new IllegalStateException("Flight's maximum baggage volume must be a positive integer");
        }
        else if (passengerBaggageWeightMax<=0) {
            throw new IllegalStateException("Passenger's maximum baggage weight must be a positive integer");
        }
        else if (passengerBaggageVolumeMax<=0) {
            throw new IllegalStateException("Passenger's maximum baggage volume must be a positive integer");
        }

        else if (!matcher.matches()) {
            throw new InvalidFlightReferenceCodeException(flightReferenceCode);
        }
        else {
            this.flightReferenceCode = flightReferenceCode.trim();
            this.destinationAirport = destinationAirport.trim();
            this.carrier = carrier.trim();
            this.maxPassengers = capacity;
            this.flightMaxBaggageWeight = flightBaggageWeightMax;
            this.flightMaxBaggageVolume = flightBaggageVolumeMax;
            this.passengerMaxBaggageWeight = passengerBaggageWeightMax;
            this.passengerMaxBaggageVolume = passengerBaggageVolumeMax;
            this.weightFeeRate = weightFeeRate;
            this.volumeFeeRate = volumeFeeRate;
            this.depatureTime = depatureTime;
        }

        this.status = STATUS.Boarding;
        
//        to be removed... code to test observer pattern.
        
        		// departFlight();
        		// notifyObserver();

        // SubscribetoClock
        this.subscribeToClock();
        
        
    }

    /**
     * Retrieves the unique flight reference code for the flight.
     * @return String The unique flight reference code for the flight.
     */
    public String getReferenceCode() {
        return this.flightReferenceCode;
    }

    /**
     * Retrieves the airport where the flight is going
     * @return String The airport where the flight is going.
     */
    public String getDestinationAirport() {
        return this.destinationAirport;
    }

    /**
     * Retrieves the name of the airline company operating the flight.
     * @return String The airline company operating the flight.
     */
    public String getCarrier() {
        return this.carrier;
    }

    /**
     * Retrieves  the maximum number of passengers allowed on board for a specific flight.
     * @return int The maximum number of passengers for a flight.
     */
    public int getMaxPassengers() {
        return this.maxPassengers;
    }

    /**
     * Retrieves information about the maximum weight of baggages allowed for a specific flight.
     * @return float The maximum weight for a flight.
     */
    public float getMaxBaggageWeight() {
        return this.flightMaxBaggageWeight;
    }

    /**
     * Retrieves information about the maximum volume of baggages allowed for a specific flight.
     * @return float The maximum volume for a flight.
     */
    public float getMaxBaggageVolume() {
        return this.flightMaxBaggageVolume;
    }

    /**
     * Retrieves the maximum allowed volume of baggage per passenger.
     * @return float The maximum baggage volume alloted to passengers per flight.
     */
    public float getMaxAllowedVolume(){
        return this.passengerMaxBaggageVolume;
    }

    /**
     * Retrieves the maximum allowed weight of baggage per passenger.
     * @return float The maximum baggage weight alloted to passengers per flight.
     */
    public float getMaxAllowedWeight(){
        return this.passengerMaxBaggageWeight;
    }

    /**
     * Retrieves the fee rate for exceeding the maximum weight of baggage alloted to passengers.
     * @return float The fee rate for exceeding the maximum weight of baggage allotted to passengers.
     */
    public float getWeightFeeRate(){
        return this.weightFeeRate;
    }

    /**
     * Retrieves the fee rate for exceeding the maximum volume of baggage alloted to passengers.
     * @return float The fee rate for exceeding the maximum volume of baggage allotted to passengers.
     */
    public float getVolumeFeeRate(){
        return this.volumeFeeRate;
    }

    /**
     * Retrieves a collection of all bookings for this flight.
     * @return Map<String, Booking>
     */
    public Map<String, Booking> getBookings(){
        return bookings;
    }

    /**
     * Retrieves the total volume of baggage for all passengers on a specific flight.
     * Calculates the value from the booking map.
     * @return float he total excess baggage volume for all bookings in a flight.
     */
    public float getTotalBaggageVolume() {
        for (Booking booking : bookings.values()) {
            totalBaggageVolume += booking.getBaggageVolume();
        }return totalBaggageVolume;
    }

    /**
     * Retrieves the total weight of baggage for all passengers on a specific flight.
     * Calculates the value from the booking map.
     * @return float he total excess baggage weight for all bookings in a flight.
     */
    public float getTotalBaggageWeight() {
        for (Booking booking : bookings.values()) {
            totalBaggageWeight += booking.getBaggageWeight();
        }return totalBaggageWeight;
    }

    
    /**
     * Retrieves the total fee for excess baggage for all passengers on a specific flight.
     * Calculates the value from the booking map.
     * @return float The total excess baggage fee for all bookings in a flight.
     */
    public float getTotalExcessFee() {
        for (Booking booking : bookings.values()) {
            totalExcessFee += booking.getExcessBaggageFees();
        }return totalExcessFee;
    }

    /**
     * Retrives the number of passengers on this flight.
     * Counts the number of passengers with checked-in status set to true from the booking map.
     * @return int The count of checked-in passengers in a flight.
     */
    public int getCheckedInPassengers(){

        int checkInPassengers = 0;
        for (Booking booking: bookings.values()) {
            if(booking.getCheckedIn()) {
                checkInPassengers++;
            }
        }
        return checkInPassengers;
    }

    /**
     * Checks if the current total baggage weight exceeds the baggage weight capacity for this flight.
     * Returns true if the weight capacity for a flight is exceeded, false otherwise.
     * @return boolean
     */
    public boolean isWeightCapacityExceeded() {
        return totalBaggageWeight>flightMaxBaggageWeight;
    }

    /**
     * Checks if the current total baggage volume exceeds the baggage volume capacity for this flight.
     *  Returns true if the volume capacity for a flight is exceeded, false otherwise.
     * @return boolean
     */
    public boolean isVolumeCapacityExceeded() {
        return totalBaggageVolume>flightMaxBaggageVolume;
    }

    /**
     * Checks if the current number of checked-in passengers exceeds the passenger capacity for a flight.
     *  Returns true if the passenger capacity for a flight is exceeded, false otherwise.
     * @return boolean
     */
    public boolean isPassengerCapacityExceeded() {
        return getCheckedInPassengers()> maxPassengers;
    }

    /**
     * Checks in a passenger with their last name, booking code.
     * Calculates any excess baggage fees incurred by the passenger.
     *@param lastName The last name of the passenger.
     * @param bookingCode The unique code for the booking.
     * @param baggageVolume The volume of the passenger's baggage.
     * @param baggageWeight The weight of the passenger's baggage.
     * @return float The excess baggage fee incurred by the passenger.
     * @throws NoMatchingBookingException If no booking matches the provided booking code.
     * @throws AlreadyCheckedInException If the passenger has already been checked in.
     * @throws NoMatchingNameException If no passenger with the provided last name is found in the booking.
     * @throws InvalidNameException If the provided last name is invalid.
     * @throws InvalidBookingReferenceCodeException  If  the provided booking reference code is invalid.
     */
    public float checkIn(String lastName, String bookingCode, float baggageVolume, float baggageWeight) throws InvalidBookingReferenceCodeException, NoMatchingBookingException, AlreadyCheckedInException, NoMatchingNameException, InvalidNameException{
        Booking b1 = bookings.get(bookingCode);
        float excessFee = -1;
        if (b1 != null) {
          
            excessFee=b1.checkIn(lastName, baggageVolume,baggageWeight, passengerMaxBaggageVolume,passengerMaxBaggageWeight,weightFeeRate,volumeFeeRate);
          
        }
        else{
            throw new NoMatchingBookingException("Booking not found: " + bookingCode);
        }
       
        return excessFee;
    
    }

    /**
     * Checks if a booking with the provided booking code exists.
     * @param BookingCode  The booking code for the passenger
     * @return true if it exists,false otherwise
     */
    public boolean checkBoookingCode(String BookingCode){
        Booking b1 = bookings.get(BookingCode);
        boolean codeStatus= b1==null?false:true;
        return (codeStatus);
    }

    /**
     * Adds a new booking to the collection of bookings for this flight.
     * @param code The unique code for the booking.
     * @param newBooking The booking to be added
     */
    public void addBooking(String code,Booking newBooking ) {
        bookings.put(code, newBooking);
    }
    
    public void registerObserver(FlightObserver observer) {
    	observers.add(observer);
    }
    
    public void notifyObserver() {
    	for (FlightObserver observer: observers) {
    		observer.flightDeparting(flightReferenceCode);
    	}
    }
    public void notifyObserverCheckIn(Booking bk) {
    	for (FlightObserver observer: observers) {
    		observer.checkIn(bk);
    	}
    }
    
    public void departFlight() {
    	this.status = STATUS.Departed;

    }
    public STATUS getStatus() {
        return this.status;
    }
    public int getBoardingTimeRemaining(){
        int departureMinute = depatureTime.getMinute();
        int departureHour = depatureTime.getHour();
        int clockMinute = clock.getCurrentTime().getMinute();
        int clockHour = depatureTime.getHour();
        if (clockHour == departureHour){
            return departureMinute - clockMinute;
        }
        else{
            return (departureMinute+60) - clockMinute;
        }
    }



    // Function to determine behaviour when flight time arrives
    @Override
    public void onTimeChange(LocalDateTime time){
        this.departFlight();
        this.notifyObserver();

    }

    public void subscribeToClock(){
        clock.registerFlightTimeObserver(depatureTime, this);
    }

    @Override
    public void checkInSuccessful(Booking bk) {
        passengerCount+=1;
        totalBaggageWeight+=bk.getBaggageWeight();
        fill =((float)passengerCount/maxPassengers)*100;
        notifyObserverCheckIn(bk);
      
    }




    /**
     * @return String
     */
    @Override
    public String toString() {
        return "Departure time: "+ depatureTime.format(formatter) + 
        "\t"+ " Flight Code: " + flightReferenceCode +" \t"+ passengerCount + " checked in of " + maxPassengers + "."+
        " Plane is "+ df.format(fill) + "% full. Hold is " + ((float)getTotalBaggageWeight()/flightMaxBaggageWeight)*100 + "full." 
        + "\t  Status: " + status;
        
      
    }

    public LocalDateTime getDepartureTime() {
        return depatureTime;
    }

   
}



