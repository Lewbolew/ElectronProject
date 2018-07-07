package info.devexchanges.bluetoothchatapp;

/**
 * Created by bohdan on 12/14/17.
 */

public class Data {
    private String machine_id;
    private String stamp_id;
    private int strikes_number;

    public void setMachine_id(String m){
        this.machine_id = m;
    }
    public void setStamp_id(String s) {
        this.stamp_id = s;
    }
    public void setDistances(int d) {
        this.strikes_number = d;
    }

    public String getMachine_id() {
        return this.machine_id;
    }
    public String getStamp_id() {
        return this.stamp_id;}
    public int getDistances() {
        return this.strikes_number;
    }
}
