package trafficmonit;

import trafficmonit.domain.TrafficResult;
import trafficmonit.service.TrafficService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class TrafficResolver {

    TrafficService trafficService = new TrafficService();

    public void resolve() throws IOException {

        // read in the file
        String file = "traffic.txt";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        // assuming utf-8
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(file), "UTF-8"));

        String curLine;
        while ((curLine = bufferedReader.readLine()) != null){
            //process the line as required
            trafficService.processLine(curLine);

        }
        // close io
        bufferedReader.close();

        // output the results

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
        System.out.printf("Least busy interval ends at %s %n", trafficService.getLeastBusy90MinInterval());
    }
}
