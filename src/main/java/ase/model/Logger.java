package ase.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Logger {

    private static Logger instance;
    private ArrayList<String> logs = new ArrayList<>();

    private Logger(){
    }

    public static Logger getInstance(){
        if (instance == null)
            instance = new Logger();
        return instance;
    }

    public void save(String newLog){
        logs.add(newLog);
    }

    public void write(String newLog){
        save(newLog);
       
    }

    public void createLogReport(){
        try {
            FileWriter myWriter = new FileWriter("LogReport.txt");
            myWriter.write("----------------------- Logs Report -----------------------\n");
            for (String log : logs) {
                myWriter.write(log+"\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Error while writing the report");
        }
    }

    //To use the logger:
    // Logger log = Logger.getInstance();
    // log.write("your log");  e.g. log.write("Passenger A in queue 2")
}
