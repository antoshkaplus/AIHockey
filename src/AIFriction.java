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
    private ArrayList<Record> hockeyistFriction = new ArrayList<Record>();
    private ArrayList<Record> puckFriction = new ArrayList<Record>();

    private DistanceComparator distanceComparator = new DistanceComparator();
    private SpeedComparator speedComparator = new SpeedComparator();

    // getters use it... don't use it anywhere else
    private Record bufferRecord = new Record();
    private static AIFriction instance = null;

    private AIFriction() {
        String[] speedDistance = hockeyistData.split(";");
        for (String s : speedDistance) {
            int i = s.indexOf(',');
            double speed = Double.parseDouble(s.substring(0, i));
            double distance = Double.parseDouble(s.substring(i+1));
            hockeyistFriction.add(new Record(distance, speed));
        }
        hockeyistFriction.add(new Record(hockeyistFriction.get(hockeyistFriction.size()-1).distance, 0));
    }

    public static AIFriction getInstance() {
        if (instance == null) {
            instance = new AIFriction();
        }
        return instance;
    }

    RecordTicks getRecordBySpeed(List<Record> friction, double speed) {
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
    RecordTicks getRecordByDistance(List<Record> friction, double distance) {
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

    // returns speed and ticks
    SpeedTicks hockeyistAfterDistance(double distance, double startingSpeed) {
        RecordTicks r = getRecordBySpeed(hockeyistFriction, startingSpeed);
        // add current distance to one that we looking
        double t = r.tick;
        r = getRecordByDistance(hockeyistFriction, r.record.distance + distance);
        return new SpeedTicks(r.record.speed, r.tick - t);
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

    static final String data =
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
    "16.45,1017.01;16.44,1033.45;16.42,1049.87;";

    //17.47,0.00;17.46,17.46;17.44,34.89;17.42,52.32;17.40,69.72;17.39,87.10;17.37,104.47;17.35,121.83;17.33,139.16;17.32,156.48;17.30,173.78;17.28,191.06;17.26,208.32;17.25,225.57;17.23,242.80;17.21,260.01;17.20,277.21;17.18,294.39;17.16,311.55;17.14,328.69;17.13,345.82;17.11,362.93;17.09,380.02;17.08,397.10;17.06,414.16;17.04,431.20;17.02,448.22;17.01,465.23;16.99,482.22;16.97,499.20;16.96,516.15;16.94,533.09;16.92,550.02;16.91,566.92;16.89,583.81;16.87,600.68;16.86,617.54;16.84,634.38;16.82,651.20;16.80,668.00;16.79,684.79;16.77,701.56;16.75,718.32;16.74,735.05;16.72,751.77;16.70,768.48;16.69,785.17;16.67,801.84;16.65,818.49;16.64,835.13;16.62,851.75;16.60,868.35;16.59,884.94;16.57,901.51;16.55,918.06;16.54,934.60;16.52,951.12;16.50,967.63;16.49,984.12;16.47,1000.59;16.46,1017.04;16.44,1033.48;16.42,1049.91;

    //17.72,0.00;17.71,17.71;17.69,35.39;17.67,53.06;17.65,70.72;17.63,88.35;17.62,105.97;17.60,123.57;17.58,141.15;17.56,158.71;17.55,176.26;17.53,193.79;17.51,211.30;17.49,228.80;17.48,246.27;17.46,263.73;17.44,281.17;17.42,298.60;17.41,316.01;17.39,333.39;17.37,350.77;17.35,368.12;17.34,385.46;17.32,402.78;17.30,420.08;17.29,437.37;17.27,454.64;17.25,471.89;17.23,489.12;17.22,506.34;17.20,523.54;17.18,540.72;17.16,557.88;17.15,575.03;17.13,592.16;17.11,609.27;17.10,626.37;17.08,643.45;17.06,660.51;17.05,677.56;17.03,694.58;17.01,711.60;16.99,728.59;16.98,745.57;16.96,762.53;16.94,779.47;16.93,796.40;16.91,813.31;16.89,830.20;16.88,847.07;16.86,863.93;16.84,880.77;16.82,897.60;16.81,914.41;16.79,931.20;16.77,947.97;16.76,964.73;16.74,981.47;16.72,998.19;16.71,1014.90;16.69,1031.59;16.67,1048.27;16.66,1064.92;

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
            "0.01,261.71;";



}
