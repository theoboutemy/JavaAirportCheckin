package ase;

import ase.controller.AppController;
import ase.model.Manager;
import ase.view.AppView;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Manager m1 = new Manager();
        m1.start();
        AppView app = new AppView(m1);
        m1.notifyObserverCheckIn();
        m1.notifyObserverDesk();
        m1.notifyObserverFlight();// when the init is done, we have to provide the info to the GUI
        AppController controller = new AppController(app, m1);
    }
}