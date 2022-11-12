package trafficmonit.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import trafficmonit.domain.LeastBusyPeriodBuffer;
import trafficmonit.domain.TrafficResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TrafficServiceTest {

    TrafficService underTest;

    @Mock
    TrafficResult resultMock;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Before
    public void setUpABlankTrafficResultByDefault(){
        TrafficResult result = TrafficResult.builder()
                .totalNumberOfCars(0L)
                .carsPerDay(new LinkedHashMap<LocalDate, Long>())
                .intervals(new LinkedHashMap<LocalDateTime, Long>())
                .leastBusyPeriodBuffer(new LeastBusyPeriodBuffer())
                .build();

        underTest = new TrafficService(result);
    }

    @Test
    public void testIncrementTotalNumberOfCars(){
        // given
        Long toTest = 2L;

        //when
        underTest.incrementTotalNumberOfCars(toTest);

        //then
        assertEquals(toTest, underTest.getTotalNumberOfCars());
    }

    @Test
    public void testAddDailyIntervalIncrementNewDay(){
        // given
        LocalDateTime date = LocalDateTime.parse("2021-12-01T05:00:00", formatter);
        LocalDate actualDay = date.toLocalDate();
        Long numToTest = 2L;

        //when
        underTest.addDailyIntervalIncrement(date, numToTest);

        //then
        LinkedHashMap<LocalDate, Long> resultToTest = underTest.getCarsPerDay();
        assertEquals(1, resultToTest.size());
        assertTrue(resultToTest.containsKey(actualDay));
        assertEquals(numToTest, resultToTest.get(actualDay));
    }

    @Test
    public void testAddDailyIntervalIncrementExistingSameDay(){
        // given
        String dateStr = "2021-12-01T05:00:00";
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
        LocalDate actualDay = date.toLocalDate();
        long numToTest = 2L;

        long existingTotal = 2L;
        underTest.getCarsPerDay().put(LocalDateTime.parse(dateStr, formatter).toLocalDate(), existingTotal); // add pre-existing day

        //when
        underTest.addDailyIntervalIncrement(date, numToTest);

        //then
        LinkedHashMap<LocalDate, Long> resultToTest = underTest.getCarsPerDay();
        assertEquals(1, resultToTest.size());
        assertTrue(resultToTest.containsKey(actualDay));

        Long totalToTest = Long.sum(numToTest, existingTotal);
        assertEquals(totalToTest, resultToTest.get(actualDay));
    }

    @Test
    public void testAddDailyIntervalIncrementExistingWithNewDay(){
        // given
        String dateStr = "2021-12-01T05:00:00";
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);
        LocalDate actualDay = date.toLocalDate();
        Long numToTest = 2L;

        LocalDate existingDay = LocalDateTime.parse(dateStr, formatter).toLocalDate().minusDays(1);
        Long existingTotal = 2L;
        underTest.getCarsPerDay().put(existingDay, existingTotal); // add pre-existing day

        //when
        underTest.addDailyIntervalIncrement(date, numToTest);

        //then
        LinkedHashMap<LocalDate, Long> resultToTest = underTest.getCarsPerDay();
        assertEquals(2, resultToTest.size());
        assertTrue(resultToTest.containsKey(actualDay));

        assertEquals(numToTest, resultToTest.get(actualDay));
    }

    @Test
    public void testGetTopThreeIntervals(){
        //given
        LinkedHashMap<LocalDateTime, Long> intervals = new LinkedHashMap<>();
        intervals.put(LocalDateTime.parse("2021-12-01T05:00:00", formatter), 2L);
        LocalDateTime thirdLargest = LocalDateTime.parse("2021-12-01T05:30:00", formatter);
        intervals.put(thirdLargest, 3L);
        LocalDateTime secondLargest = LocalDateTime.parse("2021-12-01T06:00:00", formatter);
        intervals.put(secondLargest, 6L);
        LocalDateTime largest = LocalDateTime.parse("2021-12-01T06:30:00", formatter);
        intervals.put(largest, 20L);
        underTest = new TrafficService(TrafficResult.builder()
                .intervals(intervals)
                .build());
        
        //when
        LinkedHashMap<LocalDateTime, Long> resultToTest = underTest.getTopThreeIntervals();
        
        //then
        Iterator<Map.Entry<LocalDateTime, Long>> resultIt = resultToTest.entrySet().iterator();
        //assert order
        assertEquals(largest, resultIt.next().getKey());
        assertEquals(secondLargest, resultIt.next().getKey());
        assertEquals(thirdLargest, resultIt.next().getKey());
    }

    @Test
    public void updateLeastBusy90MinInterval_HaventProcessedEnoughIntervals(){
        //given
        LinkedHashMap<LocalDateTime, Long> intervals = new LinkedHashMap<>();
        intervals.put(LocalDateTime.parse("2021-12-01T05:00:00", formatter), 2L);
        underTest = new TrafficService(TrafficResult.builder()
                .intervals(intervals)
                .leastBusyPeriodBuffer(new LeastBusyPeriodBuffer())
                .build());

        //when
        underTest.updateLeastBusy90MinInterval(LocalDateTime.parse("2021-12-01T05:30:00", formatter));

        //then
        assertEquals(0, underTest.getLeastBusyPeriod().getIntervalListForPeriod().size());

    }

    @Test
    public void updateLeastBusy90MinInterval_NoContigous90Mins(){
        //given
        LinkedHashMap<LocalDateTime, Long> intervals = new LinkedHashMap<>();
        intervals.put(LocalDateTime.parse("2021-12-01T05:00:00", formatter), 2L);
        intervals.put(LocalDateTime.parse("2021-12-01T09:00:00", formatter), 2L);
        intervals.put(LocalDateTime.parse("2021-12-01T13:00:00", formatter), 2L);
        intervals.put(LocalDateTime.parse("2021-12-01T15:00:00", formatter), 2L);
        underTest = new TrafficService(TrafficResult.builder()
                .intervals(intervals)
                .leastBusyPeriodBuffer(new LeastBusyPeriodBuffer())
                .build());

        //when
        underTest.updateLeastBusy90MinInterval(LocalDateTime.parse("2021-12-01T15:00:00", formatter));

        //then
        assertEquals(0, underTest.getLeastBusyPeriod().getIntervalListForPeriod().size());

    }

    @Test
    public void updateLeastBusy90MinIntervalSuccessTest(){
        //given
        LocalDateTime startOfQuietestPeriod = LocalDateTime.parse("2021-12-01T06:30:00", formatter);
        LocalDateTime midOfPeriod = LocalDateTime.parse("2021-12-01T07:00:00", formatter);
        LocalDateTime endOfQuietestPeriod = LocalDateTime.parse("2021-12-01T07:30:00", formatter);

        LinkedHashMap<LocalDateTime, Long> intervals = new LinkedHashMap<>();
        intervals.put(LocalDateTime.parse("2021-12-01T05:00:00", formatter), 10L);
        intervals.put(LocalDateTime.parse("2021-12-01T05:30:00", formatter), 10L);
        intervals.put(LocalDateTime.parse("2021-12-01T06:00:00", formatter), 123L);

        intervals.put(startOfQuietestPeriod, 0L); // quietest
        intervals.put(midOfPeriod, 0L); // quietest
        intervals.put(endOfQuietestPeriod, 3L); // quietest

        underTest = new TrafficService(TrafficResult.builder()
                .intervals(intervals)
                .leastBusyPeriodBuffer(new LeastBusyPeriodBuffer())
                .build());

        //when
        underTest.updateLeastBusy90MinInterval(endOfQuietestPeriod);

        //then
        assertEquals(3, underTest.getLeastBusyPeriod().getIntervalListForPeriod().size());
        LinkedList<LocalDateTime> resultToTest = underTest.getLeastBusyPeriod().getIntervalListForPeriod();
        Iterator<LocalDateTime> resultIterator = resultToTest.iterator();

        assertEquals(startOfQuietestPeriod, resultIterator.next());
        assertEquals(midOfPeriod, resultIterator.next());
        assertEquals(endOfQuietestPeriod, resultIterator.next());

    }
}
