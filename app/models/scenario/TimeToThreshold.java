package models.scenario;

import br.ufes.inf.lprm.context.model.Entity;
import br.ufes.inf.lprm.context.model.IntrinsicContext;
import br.ufes.inf.lprm.context.model.Reading;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public class TimeToThreshold extends IntrinsicContext<Long> {
    static public long MIN_VALUE = 0;
    static public long MAX_VALUE = Long.MAX_VALUE;
    public TimeToThreshold(String id, Entity bearer) {
        super(id, bearer);
        setValue(MAX_VALUE);
    }
    @Override
    public String toString() {
        return bearer + " TTT: " + getValue();
    }
    static public long computeTTT (List<Reading<Double>> readings, ProductType productType) {
        return computeTTT(readings, productType, LocalDateTime.now());
    }
    static public long computeTTT (List<Reading<Double>> readings, ProductType productType, LocalDateTime now) {
        if (readings.size() < 2) return MAX_VALUE;
        else {
            Reading<Double> temp1 = (readings.get(readings.size() - 2));
            Reading<Double> temp2 = (readings.get(readings.size() - 1));
            double m = (temp2.getValue() - temp1.getValue()) / (temp2.getExecutionTime() - temp1.getExecutionTime());
            double maxTemperature = productType.getMaxThreshold();
            double maxTime = ((maxTemperature - temp2.getValue()) / m) + temp2.getExecutionTime();
            double minTemperature = productType.getMinThreshold();
            double minTime = ((minTemperature - temp2.getValue()) / m) + temp2.getExecutionTime();
            long time = (long) (m >= 0 ? maxTime : minTime);
            long ttt = Duration
                    .between(
                            now,
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId())
                    )
                    .getSeconds();
            if (ttt > MAX_VALUE) return  MAX_VALUE;
            if (ttt < MIN_VALUE) return  MIN_VALUE;
            return ttt;
        }
    }
}
