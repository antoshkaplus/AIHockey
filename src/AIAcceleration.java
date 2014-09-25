import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.signum;

/**
 * Created by antoshkaplus on 9/22/14.
 */
public abstract class AIAcceleration {

    private DistanceComparator distanceComparator = new DistanceComparator();
    private SpeedDecreasingComparator speedDecreasingComparator = new SpeedDecreasingComparator();
    private SpeedIncreasingComparator speedIncreasingComparator = new SpeedIncreasingComparator();

    private Record bufferRecord = new Record();

    private Record parseRecord(String str) {
        int i = str.indexOf(',');
        double speed = Double.parseDouble(str.substring(0, i));
        double distance = Double.parseDouble(str.substring(i+1));
        return new Record(distance, speed);
    }

    protected abstract boolean isSpeedDecreasing();
    protected abstract List<Record> getRecords();

    private Comparator<Record> getSpeedComparator() {
        return isSpeedDecreasing() ? speedDecreasingComparator : speedIncreasingComparator;
    }

    // be careful with last elements ';'
    protected List<Record> readRecords(String data) {
        String[] speedDistance = data.split(";");
        List<Record> records = new ArrayList<Record>(speedDistance.length + 1);
        for (String s : speedDistance) {
            records.add(parseRecord(s));
        }
        records.add(new Record(records.get(records.size()-1).distance, 0));
        return records;
    }

    protected List<Record> readMultiRecords(String[] data) {
        List<Record> records = new ArrayList<Record>();
        double distance = 0;
        for (int k = 0; k < data.length; ++k) {
            String[] s = data[k].split(";");
            if (k != 0) {
                distance = records.get(records.size()-1).distance;
            }
            for (int i = 0; i < s.length; ++i) {
                Record r = parseRecord(s[i]);
                if (i == 0) {
                    distance -= r.distance;
                } else {
                    r.distance += distance;
                    records.add(r);
                }
            }
        }
        return records;
    }

    private RecordTicks getRecordBySpeed(List<Record> friction, double speed) {
        RecordTicks r = new RecordTicks();
        bufferRecord.speed = speed;
        int i = Collections.binarySearch(friction, bufferRecord, getSpeedComparator());
        if (i < 0) {
            i = abs(i+1);
            int k_0 = i-1;
            int k_1 = i;
            if (k_0 < 0) {
                k_0 = 0;
            }
            if (k_1 == friction.size()) {
                k_1 = k_0;
            }
            Record r_0 = friction.get(k_0);
            Record r_1 = friction.get(k_1);
            double part = (speed - r_0.speed)/(r_1.speed - r_0.speed);
            if (k_0 == k_1) part = 0;
            r.record = new Record(r_0.distance + part*(r_1.distance - r_0.distance), speed);
            r.tick = k_0 + part;
        } else {
            if (i == friction.size()) throw new RuntimeException();
            r.record = friction.get(i);
            r.tick = i;
        }
        return r;
    }

    // before using it you need to get distance and add, subtract from it
    private RecordTicks getRecordByDistance(List<Record> friction, double distance) {
        RecordTicks r = new RecordTicks();
        bufferRecord.distance = distance;
        int i = Collections.binarySearch(friction, bufferRecord, distanceComparator);
        if (i < 0) {
            i = abs(i+1);
            int k_0 = i-1;
            int k_1 = i;
            if (k_0 < 0) {
                k_0 = 0;
            }
            if (k_1 == friction.size()) {
                k_1 = k_0;
            }
            Record r_0 = friction.get(k_0);
            Record r_1 = friction.get(k_1);
            double part = (distance - r_0.distance)/(r_1.distance - r_0.distance);
            r.record = new Record(distance, r_0.speed + part*(r_1.speed - r_0.speed));
            r.tick = k_0 + part;
        } else {
            if (i == friction.size()) throw new RuntimeException();
            r.record = friction.get(i);
            r.tick = i;
        }
        return r;
    }

    public SpeedTicks afterDistance(double distance, double startingSpeed) {
        RecordTicks r = getRecordBySpeed(getRecords(), startingSpeed);
        // add current distance to one that we looking
        double t = r.tick;
        r = getRecordByDistance(getRecords(), r.record.distance + distance);
        return new SpeedTicks(r.record.speed, r.tick - t);
    }

    // returns distance and speed
    public DistanceSpeed afterTicks(double ticks, double startingSpeed) {
        RecordTicks rt = getRecordBySpeed(getRecords(), startingSpeed);
        int i = (int)ceil(rt.tick + ticks);
        if (i >= getRecords().size()) {
            i = getRecords().size()-1;
            //throw new RuntimeException();
        }
        Record r = getRecords().get(i);
        return new DistanceSpeed(r.distance - rt.record.distance, r.speed);
    }

    // returns distance and ticks
    public DistanceTicks whenSpeed(double endSpeed, double startingSpeed) {
        RecordTicks r = getRecordBySpeed(getRecords(), startingSpeed);
        RecordTicks r_end = getRecordBySpeed(getRecords(), endSpeed);
        return new DistanceTicks(
                r_end.record.distance - r.record.distance,
                r_end.tick - r.tick);
    }

    public class DistanceTicks {
        DistanceTicks() {}
        DistanceTicks(double distance, double ticks) {
            this.distance = distance;
            this.ticks = ticks;
        }

        double distance;
        double ticks;
    }

    public class DistanceSpeed {
        DistanceSpeed() {}
        DistanceSpeed(double distance, double speed) {
            this.distance = distance;
            this.speed = speed;
        }

        double distance;
        double speed;
    }

    public class SpeedTicks {
        SpeedTicks() {}
        SpeedTicks(double speed, double ticks) {
            this.speed = speed;
            this.ticks = ticks;
        }

        double speed;
        double ticks;
    }

    // keep speed and distance traveled
    public class Record {
        // now speed
        double speed;
        // traveled distance from beginning
        double distance;

        Record() {}
        Record(double distance, double speed) {
            this.distance = distance;
            this.speed = speed;
        }
    }

    // tick from beginning
    public class RecordTicks {
        Record record;
        double tick;
    }


    // need to distinguish between increasing and decreasing values

    private class SpeedDecreasingComparator implements Comparator<Record> {
        // o1 - o2
        @Override
        public int compare(Record o1, Record o2) {
            // speed in decreasing order
            return -(int)signum(o1.speed - o2.speed);
        }
    }

    private class SpeedIncreasingComparator implements Comparator<Record> {
        @Override
        public int compare(Record o1, Record o2) {
            return (int)signum(o1.speed - o2.speed);
        }
    }

    private class DistanceComparator implements Comparator<Record> {
        @Override
        public int compare(Record o1, Record o2) {
            // distance in increasing order
            return (int)signum(o1.distance - o2.distance);
        }
    }
}
