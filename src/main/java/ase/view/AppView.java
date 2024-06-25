package ase.view;

import ase.controller.AppController;
import ase.model.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument.Iterator;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.TreeMap;

public class AppView extends JFrame implements ManagerObserver, TimeChangeObserver {
    private JPanel timePanel, speedPanel, topPanel, middlePanel, lowPanel;
    private JButton addDeskButton;
    private JScrollPane scrollPaneQueueOfPassenger;
    private JScrollPane scrollPaneFlights;
    private JScrollPane scrollPaneDesks;
    private Manager model;
    private HashMap<JPanel, Integer> desksMap = new HashMap<>();
    private JSlider slider;
    private JTextField sliderLabel;
    private AppClock clock = AppClock.getInstance();
    private final int ORIGINAl_AMOUNT_DESKS = 3;
    private DateTimeFormatter formatter= DateTimeFormatter.ofPattern("HH:mm");
    


//This is the view of the MVC pattern
    public AppView(Manager model) {
        this.model = model;
        model.registerObserver(this); // the view is an observer of the model

        initGUI();
        click();

        // Subscribe to App Clock
        clock.startClock();
        subscribeToClock();

        setVisible(true);

    }

    private void initGUI() {
        //initialisation of the window
        setTitle("Airpot simulation");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // we want the code to end when whe close the window
        
        //init the different panels
        initTopPanel();
        initMiddlePanel();
        initLowPanel();

        //override the WindowClosing method 
        this.addWindowListener(new WindowAdapter() {
            @Override
            //when we close the window, we write the report.
            public void windowClosing(WindowEvent e) {
                //here KARIM*
                model.getLogger().createLogReport();
                System.out.println("System closing, the log report is written.");
                super.windowClosing(e);
            }
        });

    }

    public void initDesks() {
        //is called to init as many desks as in ORIGINAl_AMOUNT_DESKS
        for (int i = 0; i < ORIGINAl_AMOUNT_DESKS; i++) {
            addDeskButton.doClick();
        }
    }

    private void initLowPanel() {
        //init the low panel, containing the flights

        lowPanel = new JPanel();
        lowPanel.setLayout(new BoxLayout(lowPanel, BoxLayout.Y_AXIS));
        scrollPaneFlights = new JScrollPane(lowPanel);
        scrollPaneFlights.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneFlights.setPreferredSize(new Dimension(1450, 300));

        // adds the the JScrollPane to the main pane
        JTextField flightText = new JTextField("No flight at the moment");
        lowPanel.add(flightText);
        this.add(scrollPaneFlights, BorderLayout.SOUTH);
    }

    private void initMiddlePanel() {
        //init the middle panel, containing the desks
        middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));

        scrollPaneDesks = new JScrollPane(middlePanel);
        scrollPaneDesks.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPaneDesks.setPreferredSize(new Dimension(1450, 300));

        addDeskButton = new JButton("Open a new desk.");
        middlePanel.add(addDeskButton);
        this.add(scrollPaneDesks, BorderLayout.CENTER);

    }

    private void initTopPanel() {
        //init the North panel, containing the queue of passengers, the time and velocity of the simulation
        JPanel topContainer = new JPanel();

        // slider inspired from https://www.geeksforgeeks.org/java-swing-jslider/
        JPanel timeAndSpeedContainer = new JPanel();
        sliderLabel = new JTextField();
        speedPanel = new JPanel();
        timePanel = new JPanel();
        JTextField speeLabel = new JTextField("Speed and time of the simulation");
        // create a slider
        slider = new JSlider(1,5, 1);
        // paint the ticks and tracks
        slider.setPaintTrack(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        // set spacing
        slider.setMajorTickSpacing(1);
     
        speedPanel.add(speeLabel);
        speedPanel.add(slider);
        speedPanel.add(sliderLabel);
        timeAndSpeedContainer.add(speedPanel);
        timeAndSpeedContainer.add(timePanel);

        //queue part of the panel
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        scrollPaneQueueOfPassenger = new JScrollPane(topPanel);
        scrollPaneQueueOfPassenger.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneQueueOfPassenger.setPreferredSize(new Dimension(1450, 300));

        topContainer.add(timeAndSpeedContainer);
        topContainer.add(scrollPaneQueueOfPassenger);
        this.add(topContainer, BorderLayout.NORTH);

    }

    private void click() {
        //action command when there is a click on the addDesk button
        addDeskButton.setActionCommand("addDesk");
    }


    //several getters and setters
    public JSlider getSlider() {
        return slider;
    }

    public JTextField getSliderLabel() {
        return sliderLabel;
    }

    public JPanel getMiddlePanel() {
        return middlePanel;
    }

    public HashMap<JPanel, Integer> getDesksMap() {
        return desksMap;
    }

    public void addDeskInMap(JPanel key, Integer value) {
        //a treemap is used to store the JPanel of the desks panels
        this.desksMap.put(key, value);
    }

    public void removeDeskInMap(JPanel key) {
        this.desksMap.remove(key);
    }

    //defines the behaviour when the View is notified of a change in the flights
    private synchronized void updateFlights(TreeMap<String, Flight> newFlights) {
        lowPanel.removeAll();
        java.util.Iterator<Map.Entry<String, Flight>> iterator = newFlights.entrySet().iterator();
 
        while (iterator.hasNext()) {
            Map.Entry<String, Flight> entry = iterator.next();
            Flight flight = entry.getValue();
            JTextField flightText = new JTextField(flight.toString());
            lowPanel.add(flightText);
        }
        

        revalidate();
        repaint();
    }

    //defines the behaviour when the View is notified of a change in the desks
    private synchronized void updateDesks(ArrayList<Desk> desks) {

        for (Map.Entry<JPanel, Integer> entry : desksMap.entrySet()) {
            JPanel deskPanel = entry.getKey();
            Integer deskID = entry.getValue();
            Component[] components = deskPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JTextArea) {
                    deskPanel.remove(component);
                }
            }
            for (Desk desk : desks) {
                if (desk.getID() == deskID) {
                    JTextArea newText = new JTextArea(desk.toString());
                    newText.setEditable(false);
                    // to automatically go to line when the text is too long
                    newText.setLineWrap(true); 
                    newText.setWrapStyleWord(true); 
                    newText.setColumns(20); 
                    newText.setAlignmentX(Component.CENTER_ALIGNMENT); //so the text is centered
                    newText.setAlignmentY(Component.CENTER_ALIGNMENT); 
                    deskPanel.add(newText);
                    break;
                }
            }
        }

        revalidate();
        repaint();
    }

    //defines the behaviour when the View is notified of a change in the queue
    private synchronized void updateQueue(ArrayList<Booking> bookings) {
      

        topPanel.removeAll();
        topPanel.add(new JTextField("There are currently " + bookings.size() + " people in the queue."));
        for (Booking booking : bookings) {
            JTextField queueText = new JTextField(booking.toString());
            topPanel.add(queueText);

        }
        revalidate();
        repaint();
     
    }


//implementation of the methods from the ManagerObserver interface
    // Override ecoQueueUpdated function for appview
    @Override
    public void ecoQueueUpdated(ArrayList<Booking> ecoQueue, ArrayList<Booking> businessQueue) {
        ArrayList<Booking> queue = new ArrayList<>();
        queue.addAll(ecoQueue);
        queue.addAll(businessQueue);
        updateQueue(queue);
    }
    // Override businessQueueUpdated function for appview
    @Override
    public void businessQueueUpdated(ArrayList<Booking> ecoQueue, ArrayList<Booking> businessQueue) {
        ArrayList<Booking> queue = new ArrayList<>();
        queue.addAll(ecoQueue);
        queue.addAll(businessQueue);
        updateQueue(queue);
    }
    // Override deskUpdated function for appview
    @Override
    public void deskUpdated(ArrayList<Desk> desks) {
        updateDesks(desks);

    }
    // Override flightDeparting function for appview
    @Override
    public void flightDeparting(TreeMap<String, Flight> flights) {
        updateFlights(flights);
    }

    // Override onTimeChange function for appview
    @Override
    public void onTimeChange(LocalDateTime time) {
        timePanel.removeAll();
        JTextField timeLabel = new JTextField(time.format(formatter));
        Font font = new Font("Arial", Font.BOLD, 30);
        timeLabel.setFont(font);
        timePanel.add(timeLabel);
        revalidate();
        repaint();
    }

    @Override 
    public void newCheckIn(TreeMap<String, Flight> flights){
        updateFlights(flights);
        
    }


    // Function to add AppView as a clock observer
    public void subscribeToClock() {
        clock.registerTimeChangeObserver(this);
    }

    //to add a listener for the different events
    public void addListener(ActionListener al, ChangeListener cl) {
        addDeskButton.addActionListener(al);
        slider.addChangeListener(cl);

    }

    //to add a listener dynamically, it needs to be associated to a Listener
    public void addRemoveButtonListener(JButton button, ActionListener al) {
        button.addActionListener(al);
    }
}
