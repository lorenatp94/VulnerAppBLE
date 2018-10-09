package vru.uniovi.es.vulnerappble;

public class Device {
    private String deviceName;
    private String deviceAddress;
    private String uType;
    private int rssi;

    Device(String deviceName, String deviceAddress, String uType, int rssi){
        this.deviceName=deviceName;
        this.deviceAddress=deviceAddress;
        this.uType=uType;
        this.rssi=rssi;
    }

    public String getDeviceName(){
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getuType() {
        return uType;
    }

    public int getRssi() {
        return rssi;
    }


}
