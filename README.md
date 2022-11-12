# seek

## Approach

#### Overall Basic Requirements (assumptions)
These should hopefully be obtained easily by reading the code, but I'll just lay out my thoughts
on the traffic monitoring app.

- A simple I/O program
  - No frontend needed
  - From the coding PDF I believe writing to stdout is enough
- No data validation or data munging
  - This is based off "You can assume clean input, as these files are machine-generated." from the problem PDF
- The file is always in order and sorted

### Prerequisites
You will need to have a Java 8 JDK installed

### To run
To install just check out the repo and run the below
```bash
./gradlew clean run
```
You may need to run
```bash
chmod +x gradlew
```
- traffic.txt must be under the resources folder. One has been provided.

### Running tests
```bash
./gradlew clean test 
```

### Self Criticism
- More testing
  - More testing can always be done!
  - Specifically I could have included and integration test. This would have insured the reading of the file was accurate.
- Could maybe have added deserialization to go straight into interval objects instead of creating them myself
  - Felt it wasn't necessary given scope and time
- Part 3 of the problem was solved in trafficmonit.service.TrafficService.updateLeastBusy90MinInterval
  - This could maybe have been a sliding buffer algo done once at the end of processing instead of the continuous updating approach I went for
