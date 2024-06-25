package ase.controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ase.model.Manager;
import ase.view.AppView;

public class AppController {
    //defines the controller of the MVC
    private AppView view;
    private Manager model;

    public AppController(AppView view, Manager model) {
        //the controller communicates with the model and the view
        this.view = view;
        this.model = model;

        view.addListener(new SetListener(), new SetChangeListener());
        view.initDesks();
    }

    public class SetListener implements ActionListener {
        public void addDesk(JPanel panel) {
            // method to split a panel dynamically
            JPanel newPanel = new JPanel();
            Border border = BorderFactory.createLineBorder(Color.BLACK);
            newPanel.setBorder(border);
            newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
            newPanel.add(Box.createHorizontalStrut(200));

            JTextField newText = new JTextField("Desk is opened.");
            JButton removeButton = new JButton("Close this desk.");

            removeButton.setActionCommand("removeDesk");
            //the button is associated with a listener
            view.addRemoveButtonListener(removeButton, this);
            //the button is linked with the associated panel
            removeButton.putClientProperty("panel", newPanel);

            newPanel.add(removeButton);
            newPanel.add(newText);
            panel.add(newPanel);

            // we now need to update the gui
            view.revalidate();
            view.repaint();
            
            //
            Integer deskID = model.addDesk();
            view.addDeskInMap(newPanel,deskID);
        }

        public void removeDesk(JButton closeButton) {
            //behaviour when the user clicks on the button to close a desk
            JPanel deskPanel = (JPanel) closeButton.getClientProperty("panel");
            int id = view.getDesksMap().get(deskPanel);
            view.removeDeskInMap(deskPanel);
            //the desk is removed from the view
            model.closeDesk(id);
            //the model is updated
            view.getMiddlePanel().remove(deskPanel);
            view.revalidate();
            view.repaint();
        }

        public void actionPerformed(ActionEvent e) {
            //defines the differents behaviours according to the usser interactions with the GUI

            String action = e.getActionCommand();
            if (action.equals("addDesk")) {
                if (view.getDesksMap().size()<5){
                    JPanel middlePanel = view.getMiddlePanel();

                    addDesk(middlePanel);
                }
                else{
                    
                    JOptionPane.showMessageDialog(null, "No more than 5 desks can be opened at the same time");
                }

                

            } else if (action.equals("removeDesk")) {
                // delete the panel
                JButton closeButton = (JButton) e.getSource();
                removeDesk(closeButton);
                
            }
        }
    }

    private class SetChangeListener implements ChangeListener {
        //defines the behaviour when the user uses the slider
        @Override
        public void stateChanged(ChangeEvent e) {
            int newValue = view.getSlider().getValue();
            model.setProgramSpeed(newValue);
        }
    }

}
