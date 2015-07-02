package fr.fliizweb.ardsweeper.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import fr.fliizweb.ardsweeper.Screens.GameBluetooth;

public class BluetoothArduino implements GameBluetooth {

    BluetoothSocket mSocket;
    InputStream mBluetoothIn;
    OutputStream mBluetoothOut;

    private boolean connected = false;

     public BluetoothArduino() {
        Log.i("BTTEST", "Welcome to Tijuana");

        //================ BT Setup

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Device does not support Bluetooth
            Log.i("BTTEST", "Device does not support Bluetooth or is not enabled...");
        } else {

            BluetoothDevice arduino = null;

            // look for the arduino....
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.i("BTTEST", "      >> Known devices: [" + device.getName() + "]");
                if (device.getName().startsWith("itead")) {
                    arduino = device;
                    Log.i("BTTEST", "      >> @@@@@@@@@@@@@ YES");
                }
            }

            // did we find it ???
            if (arduino == null) {
                Log.i("BTTEST", "Arduino not found ; please pair it in the BT preferences.");
            } else {
                Log.i("BTTEST", "Found arduino");

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {
                    Log.i("BTTEST", "Connecting to arduino...");
                    BluetoothSocket btSocket = arduino.createRfcommSocketToServiceRecord(uuid);

                    btSocket.connect();

                    mBluetoothIn = btSocket.getInputStream();
                    mBluetoothOut = btSocket.getOutputStream();
                    mSocket = btSocket;

                    connected = true;

                    Log.i("BTTEST", "Connected to arduino");

                } catch(Exception e) {
                    Log.e("BTTEST", "Sadly it failed : " + e.getMessage());
                }
            }
        }
    }


    /**
     * Send a message to the device connected
     *
     * @param message Message to send
     */

    public void send(String message){

        byte[] rawData = (message + "\n").getBytes();

        // send by BT
        try {
            if (mSocket != null) {
                Log.i("BTTEST", "Attempting to send to arduino this message = " + message);
                mBluetoothOut.write(rawData);
                mBluetoothOut.flush();
            } else {
                Log.i("BTTEST", "Not connected yet...");
            }
        } catch(Exception e) {
            Log.e("BTTEST", "Could not write to BT");
        }
    }


    /**
     *
     * @return A string sent by the device connected
     */

    public String receive(){
        try {
            if (mSocket != null) {
                int nbBytes = mBluetoothIn.available();
                Log.i("BTTEST", String.valueOf(nbBytes) + " bytes available");

                if (nbBytes > 0) {
                    byte[] data = new byte[nbBytes];
                    mBluetoothIn.read(data, 0, nbBytes);
                    String text = new String(data);
                    Log.i("BTTEST", "String received = " + text);

                    return text;
                }
            } else {
                Log.i("BTTEST", "Not connected yet...");
            }
        } catch(Exception e) {
            Log.e("BTTEST", "Could not write to BT");
        }
        return null;
    }

    /**
     *
     * @return boolean to know if we are connected to the device
     */

    public Boolean getConnected(){
        return connected;
    }
}