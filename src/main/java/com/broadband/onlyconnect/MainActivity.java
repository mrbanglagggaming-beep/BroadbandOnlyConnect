package com.broadband.onlyconnect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private TextView tvStatus;
    private Button btnConnectHome, btnConnectOffice;

    // আপনার বাসা এবং অফিসের WiFi details এখানে দিন
    private static final String HOME_SSID = "Your_Home_WiFi_Name";
    private static final String OFFICE_SSID = "Your_Office_WiFi_Name";
    private static final String HOME_PASSWORD = "your_home_wifi_password";
    private static final String OFFICE_PASSWORD = "your_office_wifi_password";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        checkPermissions();
        setupWifiManager();
        setupBroadbandOnlyNetwork();
        checkCurrentConnection();
    }

    private void initializeViews() {
        tvStatus = findViewById(R.id.tvStatus);
        btnConnectHome = findViewById(R.id.btnConnectHome);
        btnConnectOffice = findViewById(R.id.btnConnectOffice);

        btnConnectHome.setOnClickListener(v -> connectToHomeBroadband());
        btnConnectOffice.setOnClickListener(v -> connectToOfficeBroadband());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for WiFi operations", 
                             Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupWifiManager() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void setupBroadbandOnlyNetwork() {
        NetworkRequest.Builder request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        connectivityManager.bindProcessToNetwork(network);
                    }
                    updateStatus("✅ ব্রডব্যান্ড কানেক্টেড - মোবাইল ডাটা ব্লকড");
                    Toast.makeText(MainActivity.this, 
                                 "ব্রডব্যান্ড কানেক্টেড! মোবাইল ডাটা ব্লকড", 
                                 Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    updateStatus("❌ ব্রডব্যান্ড ডিসকানেক্ট - মোবাইল ডাটা ব্যবহার হচ্ছেনা");
                    Toast.makeText(MainActivity.this, 
                                 "ব্রডব্যান্ড ডিসকানেক্ট! মোবাইল ডাটা ব্যবহার হচ্ছেনা", 
                                 Toast.LENGTH_LONG).show();
                });
            }
        };

        connectivityManager.requestNetwork(request.build(), networkCallback);
    }

    private void connectToHomeBroadband() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required for WiFi connection", 
                         Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            try { 
                Thread.sleep(1000); 
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
        connectToWifi(HOME_SSID, HOME_PASSWORD, "বাসার ব্রডব্যান্ড");
    }

    private void connectToOfficeBroadband() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission required for WiFi connection", 
                         Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            try { 
                Thread.sleep(1000); 
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
        connectToWifi(OFFICE_SSID, OFFICE_PASSWORD, "অফিসের ব্রডব্যান্ড");
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void connectToWifi(String ssid, String password, String networkType) {
        try {
            // প্রথমে পুরানো কনফিগারেশন রিমুভ করুন
            removeExistingNetwork(ssid);
            
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            
            // Security settings for WPA2
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            int netId = wifiManager.addNetwork(wifiConfig);
            if (netId != -1) {
                boolean success = wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                
                if (success) {
                    updateStatus("✅ " + networkType + " এ কানেক্টেড");
                    Toast.makeText(this, networkType + " এ কানেক্টেড!", Toast.LENGTH_SHORT).show();
                    
                    // Re-setup network monitoring
                    new android.os.Handler().postDelayed(() -> {
                        setupBroadbandOnlyNetwork();
                    }, 2000);
                    
                } else {
                    updateStatus("❌ " + networkType + " কানেক্ট করতে সমস্যা");
                    Toast.makeText(this, networkType + " কানেক্ট করতে সমস্যা!", Toast.LENGTH_SHORT).show();
                }
            } else {
                updateStatus("❌ নেটওয়ার্ক এড করতে সমস্যা");
                Toast.makeText(this, "নেটওয়ার্ক এড করতে সমস্যা!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void removeExistingNetwork(String ssid) {
        try {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                for (WifiConfiguration config : configuredNetworks) {
                    if (config.SSID != null && config.SSID.equals("\"" + ssid + "\"")) {
                        wifiManager.removeNetwork(config.networkId);
                        break;
                    }
                }
                wifiManager.saveConfiguration();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCurrentConnection() {
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String currentSSID = wifiInfo.getSSID();
                if (currentSSID != null && !currentSSID.equals("<unknown ssid>") && 
                    !currentSSID.equals("0x") && currentSSID.length() > 2) {
                    updateStatus("✅ কানেক্টেড: " + currentSSID.replace("\"", ""));
                } else {
                    updateStatus("🔍 WiFi চালু - ব্রডব্যান্ড নেটওয়ার্ক সিলেক্ট করুন");
                }
            }
        } else {
            updateStatus("❌ WiFi বন্ধ - ব্রডব্যান্ড ব্যবহার করতে WiFi চালু করুন");
        }
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> tvStatus.setText("স্ট্যাটাস: " + message));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up network callbacks if needed
    }
}
