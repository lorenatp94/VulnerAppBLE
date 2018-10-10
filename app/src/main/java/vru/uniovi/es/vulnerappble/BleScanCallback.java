package vru.uniovi.es.vulnerappble;


import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import java.util.List;

import static vru.uniovi.es.vulnerappble.ClientActivity.addScanResult;
import static vru.uniovi.es.vulnerappble.ClientActivity.ClientTAG;


public class BleScanCallback extends ScanCallback {
    //Callback para manejar los resultados del escaneo

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        addScanResult(result);

    }
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            addScanResult(result);
        }
    }
    @Override
    public void onScanFailed(int errorCode) {
        Log.e(ClientTAG, "BLE Scan Failed with code " + errorCode);
    }

}//BleScanCallBack


