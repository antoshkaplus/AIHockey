import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/17/14.
 * Should be singleton class
 *
 * not thread safe... be careful
 */

public class AIFriction {

    // computing every tick
    private List<Record> hockeyistFriction;
    private List<Record> puckFriction;

    private DistanceComparator distanceComparator = new DistanceComparator();
    private SpeedComparator speedComparator = new SpeedComparator();

    // getters use it... don't use it anywhere else
    private Record bufferRecord = new Record();
    private static AIFriction instance = null;

    private AIFriction() {
        hockeyistFriction = readRecords(hockeyistData);
        puckFriction = readMultiRecords(puckData);
    }

    Record parseRecord(String str) {
        int i = str.indexOf(',');
        double speed = Double.parseDouble(str.substring(0, i));
        double distance = Double.parseDouble(str.substring(i+1));
        return new Record(distance, speed);
    }

    // be careful with last elements ';'
    List<Record> readRecords(String data) {
        String[] speedDistance = data.split(";");
        List<Record> records = new ArrayList<Record>(speedDistance.length + 1);
        for (String s : speedDistance) {
            records.add(parseRecord(s));
        }
        records.add(new Record(records.get(records.size()-1).distance, 0));
        return records;
    }

    List<Record> readMultiRecords(String[] data) {
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


    public static AIFriction getInstance() {
        if (instance == null) {
            instance = new AIFriction();
        }
        return instance;
    }

    private RecordTicks getRecordBySpeed(List<Record> friction, double speed) {
        RecordTicks r = new RecordTicks();
        bufferRecord.speed = speed;
        int i = Collections.binarySearch(friction, bufferRecord, speedComparator);
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

    // before uing it you need to get distance and add, subtract from it
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

    private SpeedTicks afterDistance(List<Record> friction, double distance, double startingSpeed) {
        RecordTicks r = getRecordBySpeed(friction, startingSpeed);
        // add current distance to one that we looking
        double t = r.tick;
        r = getRecordByDistance(friction, r.record.distance + distance);
        return new SpeedTicks(r.record.speed, r.tick - t);
    }

    // returns speed and ticks
    SpeedTicks hockeyistAfterDistance(double distance, double startingSpeed) {
        return afterDistance(hockeyistFriction, distance, startingSpeed);
    }

    SpeedTicks puckAfterDistance(double distance, double startingSpeed) {
        return afterDistance(puckFriction, distance, startingSpeed);
    }

    // returns distance and speed
    DistanceSpeed hockeyistAfterTicks(double ticks, double startingSpeed) {
        RecordTicks rt = getRecordBySpeed(hockeyistFriction, startingSpeed);
        int i = (int)ceil(rt.tick + ticks);
        if (i >= hockeyistFriction.size()) {
            i = hockeyistFriction.size()-1;
            //throw new RuntimeException();
        }
        Record r = hockeyistFriction.get(i);
        return new DistanceSpeed(r.distance - rt.record.distance, r.speed);
    }

    // returns distance and ticks
    DistanceTicks hockeyistStopDistance(double startingSpeed) {
        RecordTicks r = getRecordBySpeed(hockeyistFriction, startingSpeed);
        return new DistanceTicks(
                hockeyistFriction.get(hockeyistFriction.size()-1).distance - r.record.distance,
                hockeyistFriction.size() - r.tick);
    }

    //DistanceTicks hockeyistStopDistance



    class DistanceTicks {
        DistanceTicks() {}
        DistanceTicks(double distance, double ticks) {
            this.distance = distance;
            this.ticks = ticks;
        }

        double distance;
        double ticks;
    }

    class DistanceSpeed {
        DistanceSpeed() {}
        DistanceSpeed(double distance, double speed) {
            this.distance = distance;
            this.speed = speed;
        }

        double distance;
        double speed;
    }

    class SpeedTicks {
        SpeedTicks() {}
        SpeedTicks(double speed, double ticks) {
            this.speed = speed;
            this.ticks = ticks;
        }

        double speed;
        double ticks;
    }


    // keep speed and distance traveled
    class Record implements Comparable<Double> {
        // now speed
        double speed;
        // traveled distance from beginning
        double distance;

        Record() {}
        Record(double distance, double speed) {
            this.distance = distance;
            this.speed = speed;
        }

        @Override
        public int compareTo(Double o) {
            return (int)signum(speed - o);
        }
    }

    // tick from beginning
    class RecordTicks {
        Record record;
        double tick;
    }

    class SpeedComparator implements Comparator<Record> {
        // o1 - o2
        @Override
        public int compare(Record o1, Record o2) {
            // speed in decreasing order
            return -(int)signum(o1.speed - o2.speed);
        }
    }

    class DistanceComparator implements Comparator<Record> {
        @Override
        public int compare(Record o1, Record o2) {
            // distance in increasing order
            return (int)signum(o1.distance - o2.distance);
        }
    }

    static final String[] puckData = {
    "17.47,0.00;17.46,17.46;17.44,34.89;17.42,52.31;17.40,69.72;" +
    "17.39,87.10;17.37,104.47;17.35,121.82;17.33,139.15;17.32,156.47;" +
    "17.30,173.77;17.28,191.05;17.26,208.32;17.25,225.56;17.23,242.79;" +
    "17.21,260.01;17.20,277.20;17.18,294.38;17.16,311.54;17.14,328.68;" +
    "17.13,345.81;17.11,362.92;17.09,380.01;17.08,397.09;17.06,414.15;" +
    "17.04,431.19;17.02,448.21;17.01,465.22;16.99,482.21;16.97,499.18;" +
    "16.96,516.14;16.94,533.08;16.92,550.00;16.91,566.90;16.89,583.79;" +
    "16.87,600.66;16.85,617.52;16.84,634.36;16.82,651.18;16.80,667.98;" +
    "16.79,684.77;16.77,701.54;16.75,718.29;16.74,735.03;16.72,751.75;" +
    "16.70,768.45;16.69,785.14;16.67,801.81;16.65,818.47;16.64,835.10;" +
    "16.62,851.72;16.60,868.33;16.59,884.91;16.57,901.48;16.55,918.04;" +
    "16.54,934.57;16.52,951.10;16.50,967.60;16.49,984.09;16.47,1000.56;" +
    "16.45,1017.01;16.44,1033.45;16.42,1049.87",

    "16.42,0.00;16.40,16.40;16.39,32.79;16.37,49.16;16.36,65.52;16.34,81.86;" +
    "16.32,98.18;16.31,114.49;16.29,130.78;16.27,147.05;16.26,163.31;" +
    "16.24,179.55;16.22,195.77;16.21,211.98;16.19,228.17;16.18,244.35;" +
    "16.16,260.51;16.14,276.65;16.13,292.78;16.11,308.89;16.10,324.99;" +
    "16.08,341.07;16.06,357.13;16.05,373.18;16.03,389.21;16.02,405.22;" +
    "16.00,421.22;15.98,437.20;15.97,453.17;15.95,469.12;15.94,485.06;" +
    "15.92,500.98;15.90,516.88;15.89,532.77;15.87,548.64;15.86,564.49;" +
    "15.84,580.33;15.82,596.16;15.81,611.97;15.79,627.76;15.78,643.53;" +
    "15.76,659.30;15.74,675.04;15.73,690.77;15.71,706.48;15.70,722.18;" +
    "15.68,737.86;15.67,753.53;15.65,769.18;15.64,784.81;15.62,800.43;" +
    "15.60,816.04;15.59,831.63;15.57,847.20;15.56,862.76;15.54,878.30;" +
    "15.53,893.82;15.51,909.33;15.49,924.83;15.48,940.31;15.46,955.77",

    "15.46,77.45;15.44,92.89;15.43,108.32;15.41,123.73;15.40,139.13;15.38,154.51;" +
    "15.37,169.88;15.35,185.23;15.34,200.56;15.32,215.88;15.30,231.19;15.29,246.48;" +
    "15.27,261.75;15.26,277.01;15.24,292.25;15.23,307.48;15.21,322.70;15.20,337.89;" +
    "15.18,353.08;15.17,368.24;15.15,383.40;15.14,398.53;15.12,413.66;15.11,428.76;" +
    "15.09,443.85;15.08,458.93;15.06,473.99;15.05,489.04;15.03,504.07;15.02,519.09;" +
    "15.00,534.09;14.99,549.08;14.97,564.05;14.96,579.00;14.94,593.95;14.93,608.87;" +
    "14.91,623.78;14.90,638.68;14.88,653.56;14.87,668.43;14.85,683.28;14.84,698.12;" +
    "14.82,712.94",

    "14.82,89.12;14.80,103.92;14.79,118.71;14.77,133.48;14.76,148.24;14.74,162.98;" +
    "14.73,177.71;14.71,192.43;14.70,207.12;14.68,221.81;14.67,236.48;14.65,251.13;" +
    "14.64,265.77;14.63,280.40;14.61,295.01;14.60,309.60;14.58,324.18;14.57,338.75;" +
    "14.55,353.30;14.54,367.84;14.52,382.36;14.51,396.87;14.49,411.37;14.48,425.85;" +
    "14.47,440.31;14.45,454.76;14.44,469.20;14.42,483.62;14.41,498.03;14.39,512.42;" +
    "14.38,526.80;14.36,541.16;14.35,555.51;14.34,569.85;14.32,584.17;14.31,598.48;" +
    "14.29,612.77;14.28,627.05;14.26,641.31;14.25,655.56;14.24,669.80;14.22,684.02;" +
    "14.21,698.22;14.19,712.42;14.18,726.60;14.16,740.76;14.15,754.91;14.14,769.05;" +
    "14.12,783.17;14.11,797.28",

    "14.11,458.59;14.10,472.68;14.08,486.77;14.07,500.83;14.05,514.89;14.04,528.93;" +
    "14.03,542.95;14.01,556.96;14.00,570.96;13.98,584.94;13.97,598.91;13.96,612.87;" +
    "13.94,626.81;13.93,640.74;13.91,654.65;13.90,668.55;13.89,682.44;13.87,696.31;" +
    "13.86,710.17;13.84,724.01;13.83,737.84",

    "13.83,222.98;13.82,236.80;13.80,250.60;13.79,264.39;13.78,278.17;13.76,291.93;" +
    "13.75,305.68;13.74,319.42;13.72,333.14;13.71,346.85;13.69,360.54;13.68,374.22;" +
    "13.67,387.89;13.65,401.54;13.64,415.18;13.63,428.81;13.61,442.42;13.60,456.02;" +
    "13.59,469.60;13.57,483.18;13.56,496.73;13.54,510.28;13.53,523.81;13.52,537.33;" +
    "13.50,550.83;13.49,564.32;13.48,577.80;13.46,591.26;13.45,604.71;13.44,618.15;" +
    "13.42,631.57;13.41,644.98;13.40,658.37;13.38,671.76;13.37,685.13;13.36,698.48;" +
    "13.34,711.83;13.33,725.15;13.32,738.47;13.30,751.77;13.29,765.06;13.28,778.34;" +
    "13.26,791.60"};


    static final String hockeyistData =
            "5.30,2.69;\n" +
            "5.19,7.87;\n" +
            "5.09,12.96;\n" +
            "4.98,17.94;\n" +
            "4.88,22.83;\n" +
            "4.79,27.62;\n" +
            "4.69,32.31;\n" +
            "4.60,36.90;\n" +
            "4.51,41.41;\n" +
            "4.42,45.82;\n" +
            "4.33,50.15;\n" +
            "4.24,54.39;\n" +
            "4.16,58.55;\n" +
            "4.07,62.62;\n" +
            "3.99,66.61;\n" +
            "3.91,70.52;\n" +
            "3.83,74.35;\n" +
            "3.76,78.11;\n" +
            "3.68,81.79;\n" +
            "3.61,85.40;\n" +
            "3.54,88.93;\n" +
            "3.46,92.40;\n" +
            "3.40,95.79;\n" +
            "3.33,99.12;\n" +
            "3.26,102.38;\n" +
            "3.20,105.58;\n" +
            "3.13,108.71;\n" +
            "3.07,111.78;\n" +
            "3.01,114.79;\n" +
            "2.95,117.73;\n" +
            "2.89,120.62;\n" +
            "2.83,123.45;\n" +
            "2.77,126.23;\n" +
            "2.72,128.95;\n" +
            "2.66,131.61;\n" +
            "2.61,134.22;\n" +
            "2.56,136.78;\n" +
            "2.51,139.29;\n" +
            "2.46,141.74;\n" +
            "2.41,144.15;\n" +
            "2.36,146.51;\n" +
            "2.31,148.83;\n" +
            "2.27,151.09;\n" +
            "2.22,153.31;\n" +
            "2.18,155.49;\n" +
            "2.13,157.62;\n" +
            "2.09,159.72;\n" +
            "2.05,161.76;\n" +
            "2.01,163.77;\n" +
            "1.97,165.74;\n" +
            "1.93,167.67;\n" +
            "1.89,169.56;\n" +
            "1.85,171.41;\n" +
            "1.82,173.23;\n" +
            "1.78,175.00;\n" +
            "1.74,176.75;\n" +
            "1.71,178.46;\n" +
            "1.67,180.13;\n" +
            "1.64,181.77;\n" +
            "1.61,183.38;\n" +
            "1.58,184.95;\n" +
            "1.54,186.50;\n" +
            "1.51,188.01;\n" +
            "1.48,189.49;\n" +
            "1.45,190.95;\n" +
            "1.42,192.37;\n" +
            "1.40,193.77;\n" +
            "1.37,195.14;\n" +
            "1.34,196.48;\n" +
            "1.31,197.79;\n" +
            "1.29,199.08;\n" +
            "1.26,200.34;\n" +
            "1.24,201.58;\n" +
            "1.21,202.79;\n" +
            "1.19,203.97;\n" +
            "1.16,205.14;\n" +
            "1.14,206.28;\n" +
            "1.12,207.40;\n" +
            "1.10,208.49;\n" +
            "1.07,209.57;\n" +
            "1.05,210.62;\n" +
            "1.03,211.65;\n" +
            "1.01,212.66;\n" +
            "0.99,213.65;\n" +
            "0.97,214.62;\n" +
            "0.95,215.57;\n" +
            "0.93,216.50;\n" +
            "0.91,217.41;\n" +
            "0.89,218.31;\n" +
            "0.88,219.19;\n" +
            "0.86,220.05;\n" +
            "0.84,220.89;\n" +
            "0.83,221.71;\n" +
            "0.81,222.52;\n" +
            "0.79,223.32;\n" +
            "0.78,224.09;\n" +
            "0.76,224.85;\n" +
            "0.75,225.60;\n" +
            "0.73,226.33;\n" +
            "0.72,227.05;\n" +
            "0.70,227.75;\n" +
            "0.69,228.44;\n" +
            "0.67,229.11;\n" +
            "0.66,229.77;\n" +
            "0.65,230.42;\n" +
            "0.63,231.06;\n" +
            "0.62,231.68;\n" +
            "0.61,232.29;\n" +
            "0.60,232.89;\n" +
            "0.59,233.47;\n" +
            "0.57,234.04;\n" +
            "0.56,234.61;\n" +
            "0.55,235.16;\n" +
            "0.54,235.70;\n" +
            "0.53,236.23;\n" +
            "0.52,236.75;\n" +
            "0.51,237.25;\n" +
            "0.50,237.75;\n" +
            "0.49,238.24;\n" +
            "0.48,238.72;\n" +
            "0.47,239.19;\n" +
            "0.46,239.65;\n" +
            "0.45,240.10;\n" +
            "0.44,240.54;\n" +
            "0.43,240.97;\n" +
            "0.42,241.40;\n" +
            "0.42,241.81;\n" +
            "0.41,242.22;\n" +
            "0.40,242.62;\n" +
            "0.39,243.01;\n" +
            "0.38,243.39;\n" +
            "0.38,243.77;\n" +
            "0.37,244.13;\n" +
            "0.36,244.49;\n" +
            "0.35,244.85;\n" +
            "0.35,245.19;\n" +
            "0.34,245.53;\n" +
            "0.33,245.87;\n" +
            "0.33,246.19;\n" +
            "0.32,246.51;\n" +
            "0.31,246.82;\n" +
            "0.31,247.13;\n" +
            "0.30,247.43;\n" +
            "0.29,247.73;\n" +
            "0.29,248.02;\n" +
            "0.28,248.30;\n" +
            "0.28,248.58;\n" +
            "0.27,248.85;\n" +
            "0.27,249.11;\n" +
            "0.26,249.37;\n" +
            "0.26,249.63;\n" +
            "0.25,249.88;\n" +
            "0.25,250.13;\n" +
            "0.24,250.37;\n" +
            "0.24,250.60;\n" +
            "0.23,250.83;\n" +
            "0.23,251.06;\n" +
            "0.22,251.28;\n" +
            "0.22,251.50;\n" +
            "0.21,251.71;\n" +
            "0.21,251.92;\n" +
            "0.20,252.13;\n" +
            "0.20,252.33;\n" +
            "0.20,252.52;\n" +
            "0.19,252.72;\n" +
            "0.19,252.91;\n" +
            "0.19,253.09;\n" +
            "0.18,253.27;\n" +
            "0.18,253.45;\n" +
            "0.17,253.62;\n" +
            "0.17,253.80;\n" +
            "0.17,253.96;\n" +
            "0.16,254.13;\n" +
            "0.16,254.29;\n" +
            "0.16,254.45;\n" +
            "0.15,254.60;\n" +
            "0.15,254.75;\n" +
            "0.15,254.90;\n" +
            "0.15,255.04;\n" +
            "0.14,255.19;\n" +
            "0.14,255.33;\n" +
            "0.14,255.46;\n" +
            "0.13,255.60;\n" +
            "0.13,255.73;\n" +
            "0.13,255.86;\n" +
            "0.13,255.98;\n" +
            "0.12,256.11;\n" +
            "0.12,256.23;\n" +
            "0.12,256.35;\n" +
            "0.12,256.46;\n" +
            "0.11,256.58;\n" +
            "0.11,256.69;\n" +
            "0.11,256.80;\n" +
            "0.11,256.90;\n" +
            "0.11,257.01;\n" +
            "0.10,257.11;\n" +
            "0.10,257.21;\n" +
            "0.10,257.31;\n" +
            "0.10,257.41;\n" +
            "0.10,257.51;\n" +
            "0.09,257.60;\n" +
            "0.09,257.69;\n" +
            "0.09,257.78;\n" +
            "0.09,257.87;\n" +
            "0.09,257.95;\n" +
            "0.08,258.04;\n" +
            "0.08,258.12;\n" +
            "0.08,258.20;\n" +
            "0.08,258.28;\n" +
            "0.08,258.36;\n" +
            "0.08,258.43;\n" +
            "0.07,258.51;\n" +
            "0.07,258.58;\n" +
            "0.07,258.65;\n" +
            "0.07,258.72;\n" +
            "0.07,258.79;\n" +
            "0.07,258.86;\n" +
            "0.07,258.92;\n" +
            "0.06,258.99;\n" +
            "0.06,259.05;\n" +
            "0.06,259.11;\n" +
            "0.06,259.18;\n" +
            "0.06,259.24;\n" +
            "0.06,259.29;\n" +
            "0.06,259.35;\n" +
            "0.06,259.41;\n" +
            "0.06,259.46;\n" +
            "0.05,259.52;\n" +
            "0.05,259.57;\n" +
            "0.05,259.62;\n" +
            "0.05,259.67;\n" +
            "0.05,259.72;\n" +
            "0.05,259.77;\n" +
            "0.05,259.82;\n" +
            "0.05,259.87;\n" +
            "0.05,259.91;\n" +
            "0.05,259.96;\n" +
            "0.04,260.00;\n" +
            "0.04,260.04;\n" +
            "0.04,260.09;\n" +
            "0.04,260.13;\n" +
            "0.04,260.17;\n" +
            "0.04,260.21;\n" +
            "0.04,260.25;\n" +
            "0.04,260.29;\n" +
            "0.04,260.32;\n" +
            "0.04,260.36;\n" +
            "0.04,260.40;\n" +
            "0.04,260.43;\n" +
            "0.03,260.47;\n" +
            "0.03,260.50;\n" +
            "0.03,260.53;\n" +
            "0.03,260.57;\n" +
            "0.03,260.60;\n" +
            "0.03,260.63;\n" +
            "0.03,260.66;\n" +
            "0.03,260.69;\n" +
            "0.03,260.72;\n" +
            "0.03,260.75;\n" +
            "0.03,260.78;\n" +
            "0.03,260.80;\n" +
            "0.03,260.83;\n" +
            "0.03,260.86;\n" +
            "0.03,260.88;\n" +
            "0.03,260.91;\n" +
            "0.03,260.93;\n" +
            "0.02,260.96;\n" +
            "0.02,260.98;\n" +
            "0.02,261.01;\n" +
            "0.02,261.03;\n" +
            "0.02,261.05;\n" +
            "0.02,261.07;\n" +
            "0.02,261.10;\n" +
            "0.02,261.12;\n" +
            "0.02,261.14;\n" +
            "0.02,261.16;\n" +
            "0.02,261.18;\n" +
            "0.02,261.20;\n" +
            "0.02,261.22;\n" +
            "0.02,261.24;\n" +
            "0.02,261.25;\n" +
            "0.02,261.27;\n" +
            "0.02,261.29;\n" +
            "0.02,261.31;\n" +
            "0.02,261.33;\n" +
            "0.02,261.34;\n" +
            "0.02,261.36;\n" +
            "0.02,261.37;\n" +
            "0.02,261.39;\n" +
            "0.02,261.41;\n" +
            "0.02,261.42;\n" +
            "0.01,261.44;\n" +
            "0.01,261.45;\n" +
            "0.01,261.46;\n" +
            "0.01,261.48;\n" +
            "0.01,261.49;\n" +
            "0.01,261.51;\n" +
            "0.01,261.52;\n" +
            "0.01,261.53;\n" +
            "0.01,261.54;\n" +
            "0.01,261.56;\n" +
            "0.01,261.57;\n" +
            "0.01,261.58;\n" +
            "0.01,261.59;\n" +
            "0.01,261.60;\n" +
            "0.01,261.61;\n" +
            "0.01,261.63;\n" +
            "0.01,261.64;\n" +
            "0.01,261.65;\n" +
            "0.01,261.66;\n" +
            "0.01,261.67;\n" +
            "0.01,261.68;\n" +
            "0.01,261.69;\n" +
            "0.01,261.70;\n" +
            "0.01,261.71;\n" +
            "0.01,261.71";



}
