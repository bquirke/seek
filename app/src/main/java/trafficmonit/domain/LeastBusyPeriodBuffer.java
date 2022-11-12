package trafficmonit.domain;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class LeastBusyPeriodBuffer {

    public LeastBusyPeriodBuffer(){
        this.intervalListForPeriod = new LinkedList<>();
    }

    @Getter@Setter
    long totalNumberOfCarsInPeriod = 0;

    @Getter@Setter
    LinkedList<LocalDateTime> intervalListForPeriod;

    public void addToBuffer(LocalDateTime interval){
        this.intervalListForPeriod.add(interval);
    }

    public void clearBuffer() {
        this.intervalListForPeriod.clear();
    }
}
