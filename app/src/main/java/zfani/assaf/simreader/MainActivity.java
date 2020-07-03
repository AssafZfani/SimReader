package zfani.assaf.simreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @SuppressLint({"HardwareIds", "MissingPermission"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread paramThread, @NonNull Throwable paramThrowable) {
                writeToFile("crash", paramThrowable.getMessage());
                System.exit(2);
            }
        });
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return;
        }
        String networkOperatorName, gid, imsi;
        int mcc = 0, mnc = 0;

        networkOperatorName = telephonyManager.getNetworkOperatorName();
        String networkOperator = telephonyManager.getNetworkOperator();

        if (!TextUtils.isEmpty(networkOperator)) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));
        }
        gid = telephonyManager.getSimSerialNumber();
        imsi = telephonyManager.getSubscriberId();
        writeToFile("data", "Network Operator Name - " + networkOperatorName + "\n" +
                "mcc - " + mcc + "\n" +
                "mnc - " + mnc + "\n" +
                "gid - " + gid + "\n" +
                "imsi - " + imsi);
        if (shouldSetAirplaneMode(networkOperatorName, mcc, mnc, gid, imsi)) {
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", true);
            sendBroadcast(intent);
        }
        finish();
    }

    private boolean shouldSetAirplaneMode(String networkOperatorName, int mcc, int mnc, String gid, String imsi) {
        if (networkOperatorName == null || networkOperatorName.trim().isEmpty() || networkOperatorName.toLowerCase().contains("android")) {
            return false;
        }
        if (networkOperatorName.toLowerCase().contains("cellcom")) {
            return mcc != 425 || mnc != 2 || gid == null || !gid.equalsIgnoreCase("2F");
        } else if (networkOperatorName.toLowerCase().contains("pelephone")) {
            return gid == null || !gid.equalsIgnoreCase("26") || ((mcc != 425 || mnc != 3) && (mcc != 204 || mnc != 4));
        } else if (networkOperatorName.toLowerCase().contains("hot mobile")) {
            return gid == null || !gid.equalsIgnoreCase("26") || ((mcc != 425 || mnc != 7) && (mcc != 208 || mnc != 9));
        } else if (networkOperatorName.toLowerCase().contains("partner")) {
            return mcc != 425 || mnc != 1 || imsi == null || !imsi.equalsIgnoreCase("42501034");
        } else if (networkOperatorName.toLowerCase().contains("golan telecom")) {
            return gid == null || !gid.equalsIgnoreCase("2F") || ((mcc != 425 || mnc != 8) && (mcc != 206 || mnc != 1));
        }
        return true;
    }

    private void writeToFile(String fileName, String whatToWrite) {
        File root = new File(getFilesDir(), "assaf");
        boolean dirCreated = true;
        if (!root.exists()) {
            dirCreated = root.mkdir();
        }
        File crashFile = new File(root, fileName);
        try {
            if (dirCreated) {
                FileWriter writer = new FileWriter(crashFile);
                writer.append(whatToWrite);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
