package seu.cse.kai.wifinavi;

/**
 * Created by Dell on 2015/5/6.
 */
public class AP {
    public String SSID;
    public int RSSI;
    public String mac;

    public AP(String _SSID, int _RSSI, String _mac) {
        SSID = _SSID;
        RSSI = _RSSI;
        mac=_mac;
    }

    @Override
    public String toString() {
        return SSID +"  MAC: "+ mac + "\r\n " + RSSI + " dBm";
    }
}
