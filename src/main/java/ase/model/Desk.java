package ase.model;
import java.util.ArrayList;
import java.util.List;
import ase.exceptions.AlreadyCheckedInException;
import ase.exceptions.InvalidNameException;
import ase.exceptions.NoMatchingNameException;


public class Desk extends Thread{

    //Variable declaration
    enum STATE {open, closed, busy}
    private String currentAction;
    private STATE state;
    private int ID;
    private Object lock=new Object();
	private List<DeskObserver> observers = new ArrayList<>();
    private Logger log = Logger.getInstance();


    private Booking bk;
    private Flight fl;

	


    public Desk(int Id) {
    	this.ID= Id;
    	this.state = STATE.open;
    }
    
    //Checks in passengers assigned to the desks
    public synchronized void checkIn(Booking bk, Flight fl) {
        this.bk = bk;
        this.fl = fl;
        bk.registerObserver(fl);
	}
    
    
    //Returns the current action of the desk
    public  String getCurrentAction() {
        synchronized (lock){
        while(state== STATE.open &&currentAction.isEmpty()){
            try{
               lock.wait();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } 
        }   
        return currentAction;
    }
    }

    // Sets the current action of the desk
    public  void setCurrentAction(String action) {
        synchronized(lock){
            currentAction = action;
            lock.notifyAll();
        }
    }

    // Returns the availabilty status of the Desk
    public STATE getDeskState() {
    	return this.state;
    }
    
    // Changes the state of a desk
    public synchronized void closeDesk( ) {
    	state = STATE.closed;
        currentAction="Desk closed";
        log.write(currentAction);
    }

    // Returns Desk ID
    public int getID(){
        return ID;
    }

    // Thread run method
    public void run() {
        if (fl == null && bk == null) {
            return;
        }
        if (fl == null || fl.getStatus() != Flight.STATUS.Boarding) {
            currentAction = "Desk " + ID + " did not check in " + bk.getLastName() + " because flight " + bk.getFlightCode() + " has already departed";
        } else {
            try {

                float excessFee = bk.checkIn(bk.getLastName(), (float)Math.random()*30,(float)Math.random()*300, (float)Math.random()*50, (float)Math.random()*500, fl.getWeightFeeRate(), fl.getVolumeFeeRate());
                currentAction = "Desk " + ID + " has successfully checked in " + bk.getReferenceCode() + ". " + bk.getLastName() + " checked in 1 bag of " + bk.getBaggageWeight() + ". Excess fee to pay: " +  excessFee;
            }
            catch(AlreadyCheckedInException | NoMatchingNameException | InvalidNameException e) {
                currentAction = "Desk " + ID + "failed to check in " + bk.getReferenceCode() +" : " + e.getMessage();
            }
        }
        notifyObservers();
        log.write(currentAction);
    }

    //Adds Observer
    public void registerObserver(DeskObserver observer){
		observers.add(observer);
	}

    // Notifies Observer of new action on Desk
	public void notifyObservers(){
		for (DeskObserver observer: observers){
			observer.newAction(this.currentAction);
		}
	}

    //Returns current action to the GUI
	@Override
    public String toString() {
        return  currentAction;}
        
}
    
