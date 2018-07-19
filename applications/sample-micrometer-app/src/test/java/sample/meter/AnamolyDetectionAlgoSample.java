package sample.meter;

import org.HdrHistogram.Histogram;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;

public class AnamolyDetectionAlgoSample {

    //99 Percentile values for the last 5 mins collected every 15 seconds
    private List<Long> p_99_for_5_mins = Arrays.asList(
            10L, 15L, 9L, 7L,
            35L, 5L, 10L, 15L,
            12L, 13L, 20L, 14L,
            34L, 24L, 22L, 11L,
            6L, 20L, 16L, 5L
    );

    private static final Long EXPECTED_SLA = 15L;

    @Test
    public void testAnamolyDetection() {
        //Fitting a Gaussian Distribution on the data
        Tuple2<Double, Double> meanAndVariance = calculateMeanAndVarianceOfData(p_99_for_5_mins);
        System.out.println(meanAndVariance);

        //Tweak Epsilon value below which the 99 percentile would be considered anomalous. Use SLA for service
        // as the upper bound
        Double epsilon = calculateEpsilonUsingExpectedSLA(EXPECTED_SLA, meanAndVariance.getT1(), meanAndVariance.getT2());

        System.out.println(epsilon);
        System.out.println(calculateProbabilityOfLatency(12L, meanAndVariance.getT1(), meanAndVariance.getT2()));
    }

    private Double calculateEpsilonUsingExpectedSLA(Long expectedSla, Double mean, Double variance) {
        return calculateProbabilityOfLatency(expectedSla, mean, variance);
    }
    
    private Double calculateProbabilityOfLatency(Long latency, Double mean, Double variance) {
        return 1/Math.sqrt(2 * Math.PI * variance) * Math.pow(Math.E, -Math.pow(latency - mean, 2)/(2 * variance));
    }

    private Tuple2<Double, Double> calculateMeanAndVarianceOfData(List<Long> data) {
        Histogram histogram = new Histogram(3);
        for (Long record : data) {
            histogram.recordValue(record);
        }

        return Tuples.of(histogram.getMean(), Math.pow(histogram.getStdDeviation(), 2));
    }
}
