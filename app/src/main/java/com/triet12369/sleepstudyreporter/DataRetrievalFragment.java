package com.triet12369.sleepstudyreporter;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;


public class DataRetrievalFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "DataRet Fragment";

    TextView text_debug, text_debug2;
    Button buttonConnect, buttonViewFiles;
    Handler bluetoothIn;
    ProgressDialog progress;
    ListView listfiles;

    private SharedPreferences mSharedPreference;
    private String mHistoryInterval;
    private String mDetectMethod;

    private BluetoothDevice device;
    final int handlerState = 0;
    private BluetoothSocket btSocket = null;
    private static StringBuilder recDataString = new StringBuilder();

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static String address; //"20:16:05:24:64:80"



    private final Handler mHandler = new Handler();
    private LineGraphSeries mSeries, mSeries2;
    private double graph2LastXValue = 5d;


    private int sizeOfData;

    private Pattern pattern;
    private String[] data;

    int handlerControl = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View rootView = inflater.inflate(R.layout.fragment_data_retrieval, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Data Retrieval");
        text_debug = (TextView) getView().findViewById(R.id.text_debug);
        text_debug.setText("Standby");
        //text_debug2 = (TextView) getView().findViewById(R.id.text_debug2);

        buttonConnect = (Button) getView().findViewById(R.id.buttonConnect);
        buttonConnect.setText("Connect");
        buttonViewFiles = (Button) getView().findViewById(R.id.buttonViewFiles);
        buttonViewFiles.setText("Refresh");
        buttonConnect.setOnClickListener(this);
        buttonViewFiles.setOnClickListener(this);


        listfiles = (ListView) getView().findViewById(R.id.lvfiles);


        progress = new ProgressDialog(getActivity());
        //request permission for SD write
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        // handles data retrieval
        bluetoothIn = new Handler() {
            public void handleMessage(Message msg) {
                StringBuilder temp = new StringBuilder();
                if (msg.what == handlerControl) {
                    int check = 1;
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    //text_debug.setText(Integer.toString(recDataString.length()));
//                    if (recDataString.charAt(recDataString.length()-1) != ']' || recDataString.charAt(recDataString.length()-1) != '$') {
//                        for (int i=0; i < recDataString.length(); i++) {
//                            if (recDataString.charAt(i) == '[') {
//                                check = 1;
//                            }
//                            if (recDataString.charAt(i) == ']' && check == 1) {
//                                check = 0;
//                                pattern = Pattern.compile(Pattern.quote("#"));
//                                //data = pattern.split(temp);
//                                Data_array.add(pattern.split(temp));
//                                //text_debug.setText(Arrays.toString(data));
//                                text_debug.setText(Integer.toString(Data_array.size()));
//                                //text_debug2.setText(Integer.toString(count));
//                                recDataString.delete(0, i);
//                                temp.delete(0, temp.length());
//                            }
//                            if (check == 1 && recDataString.charAt(i) != '[') {
//                                temp.append(recDataString.charAt(i));
//                            }
//                        }
//
//                    }
                }
            }
        };

        final Runnable dataRead = new Runnable() {
            @Override
            public void run() {
                handlerControl = 1;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress = ProgressDialog.show(getActivity(), "", "Saving data...", true);
                    }
                });
                StringBuilder temp = new StringBuilder();
                int check = 0;
                Date date = Calendar.getInstance().getTime();
                DateFormat formatter = new SimpleDateFormat("d_MMM-hh_mm_ss");
                final String filename = formatter.format(date) + ".csv";
                //read data from recDataString
                    for (int i=0; i < recDataString.length(); i++) {
                        if (recDataString.charAt(i) == '[') {
                            check = 1;
                        }
                        if (recDataString.charAt(i) == ']' && check == 1) {
                            check = 0;
                            pattern = Pattern.compile(Pattern.quote("#"));
                            data = pattern.split(temp);
                            writeToFile(filename, data);
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    text_debug2.setText(Arrays.toString(data));
//                                }
//                            });
                            temp.delete(0, temp.length());
                        }
                        if (check == 1 && recDataString.charAt(i) != '[') {
                            temp.append(recDataString.charAt(i));
                        }
                    }
                    recDataString.delete(0, recDataString.length());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        generateReportForm(filename);
                    }
                });
                handlerControl = 0;
            }
        };
        Runnable dataRecListener = new Runnable() {
            @Override
            public void run() {
                int size_temp = sizeOfData;
                sizeOfData = recDataString.length();
                if ((size_temp - sizeOfData == 0) && sizeOfData != 0 && handlerControl == 0) {
                    progress.dismiss();
                    new Thread(dataRead).start();
                } else if ((sizeOfData > 0) && !progress.isShowing())  {
                    progress = ProgressDialog.show(getActivity(), "", "Receiving data...", true);
                }
                mHandler.postDelayed(this, 100);
            }
        }; mHandler.postDelayed(dataRecListener, 100);

        //list view functions
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/SleepStudy-retrieved");
        File[] filelist = dir.listFiles();
        Arrays.sort(filelist);
        ArrayList<String> theNamesOfFiles = new ArrayList<String>();
        for (int i = 0; i < filelist.length; i++) {
            theNamesOfFiles.add(filelist[i].getName());        }

        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), R.layout.listfiles_adapter_view, theNamesOfFiles);
        listfiles.setAdapter(adapter);
        listfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), adapter.getItem(position), Toast.LENGTH_SHORT).show();
                generateReportForm(adapter.getItem(position));
            }
        });

    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (btSocket != null){
                btSocket.close();
            }
        } catch (IOException e) {

        }
        mHandler.removeCallbacksAndMessages(null);
    }
    public void onClick (View view) {
        switch (view.getId()){
            case R.id.buttonViewFiles:
                Log.d(TAG, "Button: View Files");
                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/SleepStudy-retrieved");
                File[] filelist = dir.listFiles();
                Arrays.sort(filelist);
                ArrayList<String> theNamesOfFiles = new ArrayList<String>();
                for (int i = 0; i < filelist.length; i++) {
                    theNamesOfFiles.add(filelist[i].getName());
                }
                StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), R.layout.listfiles_adapter_view, theNamesOfFiles);
                listfiles.setAdapter(adapter);
                break;
            case R.id.buttonConnect:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                text_debug.setText("Attempting to connect");
                                text_debug.setTextColor(getResources().getColorStateList(R.color.darkYellow));
                            }
                        });
                        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

                        if (address == null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "No address found, defaulting.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            address = "98:D3:32:20:66:6E";
                            device = btAdapter.getRemoteDevice(address);
                        } else {
                            device = btAdapter.getRemoteDevice(address);
                        }
                        if (btAdapter.isEnabled()){
                            try {
                                btSocket = createBluetoothSocket(device);
                            } catch (IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        text_debug.setText("Connection Failed");
                                        text_debug.setTextColor(getResources().getColorStateList(R.color.darkRed));
                                    }
                                });
                            }
                            // Establish the Bluetooth socket connection.
                            try
                            {
                                btSocket.connect();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        text_debug.setText("Ready");
                                        text_debug.setTextColor(getResources().getColorStateList(R.color.darkGreen));
                                    }
                                });
                            } catch (IOException e) {
                                try
                                {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    text_debug.setText("Connection Failed");
                                                    text_debug.setTextColor(getResources().getColorStateList(R.color.darkRed));
                                                }
                                            });
                                        }
                                    });
                                    btSocket.close();
                                } catch (IOException e2)
                                {
                                }
                            }
                            ConnectedThread mConnectedThread = new ConnectedThread(btSocket);
                            mConnectedThread.start();
                        }
                    }
                }).start();
                break;
            case R.id.list:
                break;
            case R.id.buttonAppend:
                break;
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final StringBuilder temp = new StringBuilder();

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[30];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    if (mmInStream.available() > 0){
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        // Send the obtained bytes to the UI Activity via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getActivity(), "Connection Failure", Toast.LENGTH_LONG).show();

            }
        }

    }
    private void writeToFile(String filename, String[] data) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/SleepStudy-retrieved");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = dir + File.separator + filename;
        File f = new File(filePath);
        CSVWriter csvWriter = null;
        try {
            //Create CSVWriter for writing to Employee.csv
            csvWriter = new CSVWriter(new FileWriter(filePath, true));
            //row1
            //String[] row = new String[]{data[0],data[1],data[2]};
            Log.d(TAG, "Writing to: " + filePath + " with: " +Arrays.toString(data));
            csvWriter.writeNext(data);
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                //closing the writer
                csvWriter.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
    private void generateReportForm(final String filename){
        new AlertDialog.Builder(getActivity())
                .setTitle("Generate report")
                .setMessage("Are you sure you want to generate report for "+ filename + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentGenerateReport = new Intent(getActivity(), GenerateReportActivity.class);
                        GenerateReportActivity.filename = filename;
                        startActivity(intentGenerateReport);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}




