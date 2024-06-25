package ase.model;
import java.time.LocalDateTime;

public interface TimeChangeObserver {

    public void onTimeChange(LocalDateTime time);
    
}
