package trafficmonit.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrafficResult {

    @Getter@Setter
    Long totalNumberOfCars;

    @Getter@Setter
    LinkedHashMap<LocalDate, Long> carsPerDay;

    // top 3 intervals
    @Getter@Setter
    LinkedHashMap<LocalDateTime, Long> intervals;

    //least busy 90min period
    @Getter@Setter
    LeastBusyPeriodBuffer leastBusyPeriodBuffer;
}
