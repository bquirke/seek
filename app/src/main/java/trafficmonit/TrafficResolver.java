package trafficmonit;

import trafficmonit.domain.LeastBusyPeriodBuffer;
import trafficmonit.domain.TrafficResult;
import trafficmonit.service.TrafficService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrafficResolver {

    TrafficService trafficService;


    public TrafficResolver(){
        TrafficResult result = TrafficResult.builder()
                .totalNumberOfCars(0L)
                .carsPerDay(new LinkedHashMap<LocalDate, Long>())
                .intervals(new LinkedHashMap<LocalDateTime, Long>())
                .leastBusyPeriodBuffer(new LeastBusyPeriodBuffer())
                .build();

        this.trafficService = new TrafficService(result);
    }

    public void resolve() throws IOException {

        // read in the file
        String file = "traffic.txt";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        // assuming utf-8
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(file), StandardCharsets.UTF_8));

        String curLine;
        while ((curLine = bufferedReader.readLine()) != null){
            //process the line as required
            trafficService.processLine(curLine);
        }
        // close io
        bufferedReader.close();

        // output the results

        writeResultsToStdOut();
    }

    private void writeResultsToStdOut() {
        // total number of cars
        System.out.printf("Total number of cars is: %s%n", trafficService.getTotalNumberOfCars());

        // total per day included in file
        System.out.println("\n\nTotal cars per day:");
        for (Map.Entry<LocalDate, Long> day : trafficService.getCarsPerDay().entrySet()){
            System.out.printf("%s %s%n", day.getKey(), day.getValue());
        }

        // Top 3 intervals
        System.out.println("\n\nTop 3 half hour intervals are as follows:");
        for (Map.Entry<LocalDateTime, Long> day : trafficService.getTopThreeIntervals().entrySet()){
            System.out.printf("%s %s%n", day.getKey(), day.getValue());
        }

        // Least busy 90min period
        System.out.println("\n\nLeast busy 90 min period is:");
        LeastBusyPeriodBuffer leastBusyPeriod = trafficService.getLeastBusyPeriod();
        for(LocalDateTime interval: leastBusyPeriod.getIntervalListForPeriod()){
            System.out.printf("%s%n", interval);
        }

        System.out.printf("With a total number of %s cars ", leastBusyPeriod.getTotalNumberOfCarsInPeriod());
    }
}
