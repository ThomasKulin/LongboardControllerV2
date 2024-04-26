package com.example.thomas.vesccontroller.Helpers.Communications;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.thomas.vesccontroller.Activities.Board_Activity;
import com.example.thomas.vesccontroller.Activities.Control_Activity;
import com.example.thomas.vesccontroller.Activities.Vesc_Settings_Activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Thomas on 2018-03-12.
 */

public class BluetoothConnectionService{

    private static final int REQUEST_ENABLE_BT = 1;

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ConnectedThread mConnectedThread;

    private boolean connected = false;


    public static void enableBtPrompt(Activity activity) {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public BluetoothConnectionService(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //------------------Handle Connection Setup-----------------------------------------------------
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final String TAG = "ConnectThread";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(deviceUUID);
                int r = 2;
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
                mmSocket = tmp;

        }
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                connected = startConnectedThread(mmSocket, mmDevice);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.d(TAG, "ConnectThread Socket closed");
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public boolean startConnectThread(BluetoothDevice device,UUID uuid){
        final String TAG = "startConnectThread";
        Log.d(TAG, "startClient: Started.");
        ConnectThread client;

        deviceUUID = uuid;
        client  = new ConnectThread(device);
        client.start();
        return connected;
        //mConnectThread = new ConnectThread(device);
        //mConnectThread.start();
    }


    //---------------------------------Connected Device Management----------------------------------

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private PacketTools.mc_values values;
        private PacketTools.mc_states states;
        private  PacketTools.mc_configuration configuration;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private final String TAG = "ConnectedThread";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            boolean messageRead = false;
            int counter = 0;
            int endMessage = 1024;
            int[] messageReceived = new int[1024];
            int lenPayload = 0;
            byte receive = 0;
            boolean currentMessage = true;

            // Keep listening to the InputStream until an exception occurs.
            while(true) {
                while (currentMessage) {
                    try {
                        receive = (byte) mmInStream.read(); //read one byte from input stream
                        connected = true;
                    } catch (IOException e) {
                        Log.e(TAG, "BluetoothCOnnectionService: InputStream was disconnected");
                        connected = false;
                        currentMessage = false;
                        break;
                    }
                    messageReceived[counter++] = (int) receive & 0xFF; // convert unsigned byte to integer to avoid overflow
                    if (counter == 3) { // messageReceived includes the payload type and size
                        switch (messageReceived[0]) {
                            case 2:
                                endMessage = messageReceived[1] + 5; //payload size +2 for size + 3 for CRC and end
                                lenPayload = messageReceived[1] & 0xFF;
                                break;
                            case 3:
                                //messagereceived[1] holds first 8 bits of length, messagereceived[2] holds the last 8 bits
                                endMessage = ((messageReceived[1] & 0xFF) << 8) | (messageReceived[2] & 0xFF);
                                lenPayload = endMessage;
                                endMessage += 6; //payload size +2 for size + 3 for CRC and end
                                break;
                            default:
                                break;
                        }
                    }

                    if (counter >= messageReceived.length) {
                        currentMessage = false;
                        break;
                    }
                    if (counter == endMessage && messageReceived[endMessage - 1] == 3) { //end of packet reached (3 is stop bit)
                        messageReceived[endMessage] = 0;
                        messageRead = true;
                        Log.d(TAG, "BluetoothConnectionService: End of message reached!");
                        currentMessage = false;
                        break;
                    }
                }
                boolean unpacked = false;
                if (messageRead) {
                    unpacked = PacketTools.unpackPayload(messageReceived, endMessage, lenPayload);
                }
                if (unpacked) {
                    switch (PacketTools.getPacketId()) {
                        case COMM_GET_VALUES: {
                            values = (PacketTools.mc_values) PacketTools.processReadPacket(); //save the values received
                            Board_Activity.updateValues(values);
                            Log.d(TAG, "BluetoothConnectionService: COMM_GET_VALUES received");
                            break;
                        }
                        case COMM_GET_STATES: {
                            states = (PacketTools.mc_states) PacketTools.processReadPacket(); //save the values received
                            Board_Activity.updateStates(states);
                            Log.d(TAG, "BluetoothConnectionService: COMM_GET_STATES received");
                            break;
                        }
                        case COMM_GET_MCCONF: {
                            configuration = (PacketTools.mc_configuration) PacketTools.processReadPacket();
                            Vesc_Settings_Activity.updateValues(configuration);
                            Log.d(TAG, "BluetoothConnectionService: COMM_GET_MCCONF received");
                            break;
                        }
                    }
                    //reset packet info to accept next packet
                    messageRead = false;
                    counter = 0;
                    endMessage = 1024;
                    lenPayload = 0;
                    receive = 0;
                    currentMessage = true;

                    for (int i = 0; i < messageReceived.length; i++) {
                        messageReceived[i] = 0;
                    }
                } else {
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes, int off, int len) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes, off, len);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        // Call this method from the main activity to shut down the connection.
        public boolean cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
            return mmSocket.isConnected();
        }
    }

    //----------------------------------------------------------------------------------------------

    private boolean startConnectedThread(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        final String TAG = "StartConnectedThread";
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
        Log.d("TAG", "THREAD IS ALIVE: "+ mConnectedThread.isAlive());
        return mConnectedThread.isAlive();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        final String TAG = "WriteConnectedThread";
        // Create temporary object
        ConnectedThread r;
        //Log.d(TAG, "write: Write Called.");
        mConnectedThread.write(out);
    }
    public void write(byte[] out, int off, int len) {
        final String TAG = "WriteConnectedThread";
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        //Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out, off, len);
    }

    //end the bluetooth data stream
    public boolean endConnection(){
        try {
            //mConnectThread.cancel();
            connected = mConnectedThread.cancel();
        }catch (Exception e){}
        return connected;
    }
    public boolean isConnected(){
        return connected;
    }
}
