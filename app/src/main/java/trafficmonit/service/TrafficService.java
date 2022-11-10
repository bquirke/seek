package trafficmonit.service;

import lombok.Getter;
import trafficmonit.domain.TrafficResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrafficService {

    @Getter
    private final TrafficResult result;

    private int currentLowest90MinPeriodTotal = 0;

    public TrafficService(){
        this.result = TrafficResult.builder()
                .totalNumberOfCars(0L)
                .carsPerDay(new LinkedHashMap<LocalDate, Long>())
                .intervals(new LinkedHashMap<LocalDateTime, Long>())
                .build();
    }

    public void processLine(String intervalOfTraffic){
        String[] line = intervalOfTraffic.split(" ");

        LocalDateTime date = parseDate(line);
        Long numCars = parseNumOfCars(line);

        incrementTotalNumberOfCars(numCars);

        addDailyIntervalIncrement(date, numCars);

        // add interval
        this.result.getIntervals().put(date, numCars);

        // eval least busy period
        updateLeastBusy90MinInterval(date);
    }

    // this will be a look-back function that keeps track of the interval that ends the quietest 90min period
    private void updateLeastBusy90MinInterval(LocalDateTime date) {
        // we have processed 3 intervals
        if(this.result.getIntervals().size() > 2){
            // get the list of keys
            List<LocalDateTime> dates = new LinkedList<>(this.result.getIntervals().keySet());
            int indexOfLastInterval = dates.indexOf(date);

            int totalNumCarsInLast90Mins = 0;

            // with the index of our current date, do a lookback calculation of the last 3 intervals
            for(int i = indexOfLastInterval; i > indexOfLastInterval - 3; i--){
                LocalDateTime intervalToFetch = dates.get(i);
                Long numberOfCars = this.result.getIntervals().get(intervalToFetch);

                totalNumCarsInLast90Mins += numberOfCars;
            }

            // evaluate if this period was the least busy
            if((totalNumCarsInLast90Mins < currentLowest90MinPeriodTotal) || (currentLowest90MinPeriodTotal == 0)){
                currentLowest90MinPeriodTotal = totalNumCarsInLast90Mins;

                this.result.setLeastBusyPeriodStartAt(date);
            }

        }
    }

    private void addDailyIntervalIncrement(LocalDateTime date, Long numCarsForDate) {
        LocalDate day = date.toLocalDate();
        // check if the day has already been added
        if(getResult().getCarsPerDay().containsKey(day)){
            Long currentDaysTotal = getResult().getCarsPerDay().get(day);
            //TODO remove this access pattern... doesnt feel right... just use this.result
            getResult().getCarsPerDay().put(day, currentDaysTotal + numCarsForDate);
        }
        else{
            getResult().getCarsPerDay().put(day, numCarsForDate);
        }
    }

    private void incrementTotalNumberOfCars(Long currentIntervalNumCars) {
        //increment the count
        Long currentTotal = getResult().getTotalNumberOfCars();
        getResult().setTotalNumberOfCars(currentTotal == null ? 0 : currentTotal + currentIntervalNumCars);
    }

    public Long getTotalNumberOfCars(){
        return getResult().getTotalNumberOfCars();
    }

    public LinkedHashMap<LocalDate, Long> getCarsPerDay(){
        return getResult().getCarsPerDay();
    }

    public Map<LocalDateTime, Long> getTopThreeIntervals(){
        LinkedHashMap<LocalDateTime, Long> sorted = new LinkedHashMap<>();
        this.result.getIntervals().entrySet().stream()
                .sorted(Map.Entry.<LocalDateTime, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry ->  sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    public LocalDateTime getLeastBusy90MinInterval(){
        return this.result.getLeastBusyPeriodStartAt();
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
