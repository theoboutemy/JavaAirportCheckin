package ase.model;

import java.util.ArrayList;
import java.util.List;


import ase.exceptions.AlreadyCheckedInException;
import ase.exceptions.InvalidBookingReferenceCodeException;
import ase.exceptions.InvalidNameException;
import ase.exceptions.NoMatchingNameException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Booking {
    private String bookingReferenceCode;
    private Name passengerName;
    private String flightCode;
    private boolean checkedIn;
    private float baggageVolume;
    private float baggageWeight;
    private float excessBaggageWeight;
    private float excessBaggageVolume;
    private float excessBaggageFees;
    private String cabinClass;
    private List<BookingObserver> observers= new ArrayList<>();

    /**
     * @throws InvalidBookingReferenceCodeException and InvalidNameException
     * Checks that all required parameters are provided.
     * Sets up booking details and trims to remove white spaces.
     * Creates Regex pattern to validate reference code.
     */

    public Booking(String referenceCode, String name, String flightCode, boolean checkInStatus, String cabinClass) throws InvalidBookingReferenceCodeException, InvalidNameException{

        String codeFormat="[A-Z]{6}\\d{4}";
        Pattern codePattern = Pattern.compile(codeFormat);
        Matcher matcher = codePattern.matcher(referenceCode);

        if (referenceCode.trim()== "" ) {
            throw new IllegalStateException("Reference code cannot be empty.");
        }
        else if (name.trim()=="") {
            throw new IllegalStateException("Name cannot be empty.");
        }
        else if(flightCode.trim()==""){
            throw new IllegalStateException("Flight code cannot be empty.");

        }
        else if(!matcher.matches()){
            throw new InvalidBookingReferenceCodeException(referenceCode);
        }
        else {
            this.bookingReferenceCode = referenceCode.trim();
            this.flightCode = flightCode.trim();
            this.checkedIn = checkInStatus;
            this.passengerName = new Name(name.trim());
            this.cabinClass = cabinClass;
        }
    }

    /**
     * @return String
     */
    //Get the booking reference code
    public String getReferenceCode() {
        return bookingReferenceCode;
    }

    //Get the last name
    public String getLastName() {
        return passengerName.getLastName();

    }

    //Get the flightCode
    public String getFlightCode() {
        return flightCode;
    }

    //Check the check in status
    public boolean getCheckedIn() {
        return checkedIn;
    }

    //Get the Cabin Class 
    public String getCabinClass(){
        return cabinClass;
    }


    //Get the baggage volume
    public float getBaggageVolume() {
        return baggageVolume;
    }

    //Get the baggage weight
    public float getBaggageWeight() {
        return baggageWeight;
    }
    public float getExcessBaggageFees() {
        return excessBaggageFees;
    }
    //Get the excess baggage weight
    public float getExcessBaggageWeight() {
        return excessBaggageWeight;
    }

    //Calculates, sets and returns baggage information, and updates check in status
    public float checkIn(String lastName, float baggageVolume, float baggageWeight, float maxVolume, float maxWeight, float weightFeeRate, float volumeFeeRate)throws AlreadyCheckedInException, NoMatchingNameException, InvalidNameException{
        //Validate the last name entered
        if (!lastName.matches("[a-zA-Z\\\\s'-]+")) {
            throw new InvalidNameException("last name can only contain alphabetic characters, spaces, hyphens and apostrophes");
        }

        //Validate the baggageVolume and baggageWeight
        if (baggageVolume < 0) {
            throw new IllegalStateException("Baggage volume cannot be a negative number.");
        }
        if (baggageWeight < 0) {
            throw new IllegalStateException("Baggage Weight cannote be a negative number.");
        }

        //Check if the provided last name matches the last name in the booking
        if (passengerName.getLastName().equalsIgnoreCase(lastName.trim())) {


            //Check if the passenger is already checked in
            if (!this.checkedIn) {

                this.baggageVolume = baggageVolume;
                this.baggageWeight = baggageWeight;

                excessBaggageVolume = Math.max(baggageVolume - maxVolume, 0);
                excessBaggageWeight = Math.max(baggageWeight - maxWeight, 0);

                float weightFee = excessBaggageWeight * weightFeeRate;
                float volumeFee = excessBaggageVolume * volumeFeeRate;

                excessBaggageFees = weightFee + volumeFee;
                this.checkedIn = true;
                notifyObservers();
            }
            else {
                throw new AlreadyCheckedInException(bookingReferenceCode);
            }
        }
        else {
            throw new NoMatchingNameException(lastName);
        }

        return excessBaggageFees;
    }

    public void registerObserver(BookingObserver observer){
        observers.add(observer);
    }

    public void notifyObservers(){
        for (BookingObserver observer: observers){
            observer.checkInSuccessful(this);
        }
    }


    @Override
    public String toString() {
        return   bookingReferenceCode + "   " +
                 passengerName + "  "+
                 baggageVolume +"cm3   "+
                 baggageWeight +"kg  "+
                 cabinClass;
    }

    public void setBaggageVolume(float baggageVolume) {
        this.baggageVolume = baggageVolume;
    }

    public void setBaggageWeight(float baggageWeight) {
        this.baggageWeight = baggageWeight;
    }
}