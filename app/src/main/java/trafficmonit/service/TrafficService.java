package trafficmonit.service;

import lombok.Getter;
import trafficmonit.domain.LeastBusyPeriodBuffer;
import trafficmonit.domain.TrafficResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TrafficService {

    private final TrafficResult result;

    private int currentLowest90MinPeriodTotal = 0;

    public TrafficService(TrafficResult result){
        this.result = result;
    }

    public void processLine(String intervalOfTraffic){
        // parse the line as needed
        String[] line = intervalOfTraffic.split(" ");
        LocalDateTime date = parseDate(line);
        Long numCars = parseNumOfCars(line);

        // increment total cars seen for part 1 of challenge
        incrementTotalNumberOfCars(numCars);

        // update days seen in the file and car totals per day.... part 2
        addDailyIntervalIncrement(date, numCars);

        // add interval
        this.result.getIntervals().put(date, numCars);

        // eval the least busy 90min period .... part 4
        updateLeastBusy90MinInterval(date);
    }

    // this is a look-back function that keeps track a custom buffer object of the current quietest period
    protected void updateLeastBusy90MinInterval(LocalDateTime thirdInterval) {
        // we have processed at least 3 intervals
        if(this.result.getIntervals().size() > 2){
            // get the list of keys
            List<LocalDateTime> dates = new LinkedList<>(this.result.getIntervals().keySet());

            // find indices we need to check
            int indexOfLastInterval = dates.indexOf(thirdInterval);

            LocalDateTime firstInterval = dates.get(indexOfLastInterval - 2);
            LocalDateTime secondInterval = dates.get(indexOfLastInterval - 1);

            // evaluate time between them
            // its set to 60 as the timestamps are for the BEGINNING of the half hour.
            // that means we are evaluating 5:00 - 6:00 timestamps, but this is actually 90mins of cars
            if((ChronoUnit.MINUTES.between(firstInterval, thirdInterval) == 60 )){

                long totalNumCarsInLast90Mins = this.result.getIntervals().get(firstInterval)
                        + this.result.getIntervals().get(secondInterval)
                        + this.result.getIntervals().get(thirdInterval);

                // evaluate if this period was the least busy
                if((totalNumCarsInLast90Mins < this.result.getLeastBusyPeriodBuffer().getTotalNumberOfCarsInPeriod())
                        || (this.result.getLeastBusyPeriodBuffer().getTotalNumberOfCarsInPeriod() == 0)){
                    updateTheLeastBusyPeriodBuffer(dates, indexOfLastInterval, totalNumCarsInLast90Mins);
                }
            }
        }
    }

    private void updateTheLeastBusyPeriodBuffer(List<LocalDateTime> dates, int indexOfLastInterval, long totalNumCarsInLast90Mins) {
        this.result.getLeastBusyPeriodBuffer().setTotalNumberOfCarsInPeriod(totalNumCarsInLast90Mins);

        // clear the last period added
        this.result.getLeastBusyPeriodBuffer().clearBuffer();

        // Add intervals in order
        this.result.getLeastBusyPeriodBuffer().addToBuffer(dates.get(indexOfLastInterval - 2));
        this.result.getLeastBusyPeriodBuffer().addToBuffer(dates.get(indexOfLastInterval - 1));
        this.result.getLeastBusyPeriodBuffer().addToBuffer(dates.get(indexOfLastInterval));
    }

    protected void addDailyIntervalIncrement(LocalDateTime date, Long numCarsForDate) {
        LocalDate day = date.toLocalDate();
        // check if the day has already been added
        if(this.result.getCarsPerDay().containsKey(day)){
            Long currentDaysTotal = this.result.getCarsPerDay().get(day);

            this.result.getCarsPerDay().put(day, currentDaysTotal + numCarsForDate);
        }
        else{
            this.result.getCarsPerDay().put(day, numCarsForDate);
        }
    }

    protected void incrementTotalNumberOfCars(Long currentIntervalNumCars) {
        //increment the count
        Long currentTotal = this.result.getTotalNumberOfCars();
        this.result.setTotalNumberOfCars(currentTotal == null ? 0 : currentTotal + currentIntervalNumCars);
    }

    public LinkedHashMap<LocalDateTime, Long> getTopThreeIntervals(){
        LinkedHashMap<LocalDateTime, Long> sorted = new LinkedHashMap<>();
        this.result.getIntervals().entrySet().stream()
                .sorted(Map.Entry.<LocalDateTime, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry ->  sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    public Long getTotalNumberOfCars(){
        return this.result.getTotalNumberOfCars();
    }

    public LinkedHashMap<LocalDate, Long> getCarsPerDay(){
        return this.result.getCarsPerDay();
    }

    public LeastBusyPeriodBuffer getLeastBusyPeriod(){
        return this.result.getLeastBusyPeriodBuffer();
    }

    /*
    *
    * Helper functions
    *
    * */

    private Long parseNumOfCars(String[] line) {
        return Long.parseLong(line[1]);
    }

    private LocalDateTime parseDate(String[] line) {
        // iso 8601 complaint formatting in java 8
        // no 'z' (timezone) offset included so using LocalDateTime here is acceptable I believe
        String dateStr = line[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
        return date;
    }
}
