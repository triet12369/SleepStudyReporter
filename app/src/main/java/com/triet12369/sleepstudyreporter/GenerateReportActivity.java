package com.triet12369.sleepstudyreporter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class GenerateReportActivity extends AppCompatActivity {
    public static String filename;
    private static final String TAG = "GenerateReport";

    private ArrayList<String[]> data= new ArrayList<String[]>();
    private LineGraphSeries[] mSeries=  new LineGraphSeries[3];
    private double graph2LastXValue = 5d;

    private Handler mHandler = new Handler();
    private ProgressDialog progress;
    private boolean taskFinish = false;
    boolean isDemo = false;
    int index = 0;
    //variable for counting two successive up-down events
    int clickCount = 0;
    //variable for storing the time of first click
    long startTime;
    //variable for calculating the total time
    long duration;
    //constant for defining the time duration between the click that can be considered as double-tap
    static final int MAX_DURATION = 500;


    TextView    textMaxHr, textMaxHrVal, textMaxSp, textMaxSpVal,
                textAvgHr, textAvgHrVal, textAvgSp, textAvgSpVal,
                textMinHr, textMinHrVal, textMinSp, textMinSpVal,
                textNumDipsSp, textNumDipsSpVal, textNumDipsAHI, textNumDipsAHIVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);
        //Check for permissions/////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        ///////////////////////////////////////////////////////////////////////////

        //Initialize texts
        textMaxHr = (TextView) findViewById(R.id.textMaxHr);
        textMaxHrVal = (TextView) findViewById(R.id.textMaxHrVal);
        textMaxSp = (TextView) findViewById(R.id.textMaxSp);
        textMaxSpVal = (TextView) findViewById(R.id.textMaxSpVal);
        textMinHr = (TextView) findViewById(R.id.textMinHr);
        textMinHrVal = (TextView) findViewById(R.id.textMinHrVal);
        textMinSp = (TextView) findViewById(R.id.textMinSp);
        textMinSpVal = (TextView) findViewById(R.id.textMinSpVal);
        textAvgHr = (TextView) findViewById(R.id.textAvgHr);
        textAvgHrVal = (TextView) findViewById(R.id.textAvgHrVal);
        textAvgSp = (TextView) findViewById(R.id.textAvgSp);
        textAvgSpVal = (TextView) findViewById(R.id.textAvgSpVal);
        textNumDipsSp = (TextView) findViewById(R.id.textNumDipsSp);
        textNumDipsSpVal = (TextView) findViewById(R.id.textNumDipsSpVal);
        textNumDipsAHI = (TextView) findViewById(R.id.textNumDipsAHI);
        textNumDipsAHIVal = (TextView) findViewById(R.id.textNumDipsAHIVal);
        ///////////////////////////////////////////////////////////////////////////
        if (filename.equals("testdata.csv")) {
            Log.d(TAG, filename);
            isDemo = true;
        }
        new readFromFilesTask().execute();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    //Graphview
                    if (isDemo) {
                        GraphView graph = (GraphView) findViewById(R.id.graph_report);
                        mSeries[0] = new LineGraphSeries<>(dataParser(0));
                        graph.addSeries(mSeries[0]);
                        graph.getViewport().setYAxisBoundsManual(true);
                        graph.getViewport().setXAxisBoundsManual(false);
                        graph.getViewport().setScalable(true);
                        graph.getViewport().setScrollable(true);
                        graph.getViewport().setYAxisBoundsManual(true);
                        mSeries[0].setTitle("Nasal Respiration");
                        graph.getLegendRenderer().setVisible(true);
                        graph.getViewport().setMinX(11900);
                        graph.getViewport().setMaxX(12100);
                        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                        mSeries[0].setColor(Color.RED);
                    } else {
                        GraphView graph = (GraphView) findViewById(R.id.graph_report);
                        mSeries[0] = new LineGraphSeries<>(dataParser(0));
                        mSeries[1] = new LineGraphSeries<>(dataParser(1));
                        mSeries[2] = new LineGraphSeries<>(dataParser(2));
                        graph.addSeries(mSeries[index]);
                        graph.getViewport().setYAxisBoundsManual(false);
                        graph.getViewport().setXAxisBoundsManual(false);
                        graph.getViewport().setScalable(true);
                        graph.getViewport().setScrollable(true);
                        mSeries[0].setTitle("Heart Rate");
                        mSeries[0].setColor(Color.RED);
                        View view = (View) findViewById(R.id.graph_report);
                        view.setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                switch(event.getAction() & MotionEvent.ACTION_MASK) {
                                    case MotionEvent.ACTION_DOWN:
                                        startTime = System.currentTimeMillis();
                                        clickCount++;
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        long time = System.currentTimeMillis() - startTime;
                                        duration=  duration + time;
                                        if(clickCount == 2)
                                        {
                                            if(duration<= MAX_DURATION)
                                            {
                                                GraphView graph = (GraphView) findViewById(R.id.graph_report);
                                                graph.removeAllSeries();
                                                //mSeries[index] = new LineGraphSeries<>(dataParser(index));
                                                graph.addSeries(mSeries[index]);
                                                graph.getLegendRenderer().setVisible(true);
                                                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                                                switch (index) {
                                                    case 0:
                                                        mSeries[0].setTitle("Heart Rate");
                                                        mSeries[0].setColor(Color.RED);
                                                        break;
                                                    case 1:
                                                        mSeries[1].setTitle("SpO2");
                                                        mSeries[1].setColor(Color.GREEN);
                                                        break;
                                                    case 2:
                                                        mSeries[2].setTitle("Nasal Respiration");
                                                        mSeries[2].setColor(Color.BLUE);
                                                        break;
                                                }
                                                index += 1;
                                                Log.d(TAG, Integer.toString(index));
                                                if (index > 2){
                                                    index = 0;
                                                }
                                            }
                                            clickCount = 0;
                                            duration = 0;
                                            break;
                                        }
                                }
                                return true;
                            }
                        });
                    }
                    ////////////////////////////////////////
                    new dataProcessing().execute();
                }
            }
        };

    }

    private void readFromFile() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/SleepStudy-retrieved");
        String filePath = dir + File.separator + filename;
        File f = new File(filePath);
        CSVReader csvReader = null;
        String[] line;
        //Pattern pattern = Pattern.compile(Pattern.quote(","));
        try {
            //Create CSVWriter for writing to Employee.csv
            csvReader = new CSVReader(new FileReader(f));

            while ((line = csvReader.readNext()) != null) {
                data.add(line);
            }
            Log.d(TAG, "readFromFile: " + data.size());

        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                //closing the writer
                csvReader.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
    private DataPoint[] dataParser(int index) {
        int size = data.size();
        double x, y;
        String[] line;
        DataPoint[] values = new DataPoint[size];
        for (int i = 0; i < size; i++) {
            line = data.get(i);
            y = Double.parseDouble(line[index]);
            x = Integer.parseInt(line[line.length -1]);
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    private class readFromFilesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onProgressUpdate(Void... progress) {

        }
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(GenerateReportActivity.this, "", "Reading data...", true);
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.dismiss();
            mHandler.sendEmptyMessage(0);
        }
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Async: doInBackground");
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File (root.getAbsolutePath() + "/SleepStudy-retrieved");
            String filePath = dir + File.separator + filename;
            File f = new File(filePath);
            CSVReader csvReader = null;
            String[] line;
            //Pattern pattern = Pattern.compile(Pattern.quote(","));
            try {
                //Create CSVWriter for writing to Employee.csv
                csvReader = new CSVReader(new FileReader(f));

                while ((line = csvReader.readNext()) != null) {
                    data.add(line);
                }
                Log.d(TAG, "readFromFile: " + data.size());

            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                try {
                    //closing the writer
                    csvReader.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            return null;
        }
    }
    private class dataProcessing extends AsyncTask<Void, String, Void> {
        int[] minMaxHr = new int[2];
        int[] minMaxSp = new int[2];
        int dips;
        double[] avg = new double[2];
        double ahi = 0;
        String status = " (Normal)";
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progress.setMessage(Arrays.toString(values));

        }
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(GenerateReportActivity.this, "", "Processing...", true);
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            textMaxHrVal.setText(Integer.toString(minMaxHr[0]));
            textMaxSpVal.setText(Integer.toString(minMaxSp[0]) + '%');
            textMinHrVal.setText(Integer.toString(minMaxHr[1]));
            textMinSpVal.setText(Integer.toString(minMaxSp[1]) + '%');
            textAvgHrVal.setText(String.format(Locale.ENGLISH, "%1$.2f", avg[0]));
            textAvgSpVal.setText(String.format(Locale.ENGLISH, "%1$.2f", avg[1]) + '%');
            textNumDipsSpVal.setText(Integer.toString(dips));
            textNumDipsAHIVal.setText(String.format(Locale.ENGLISH, "%1$.2f", ahi) + status);
            progress.dismiss();
        }
        @Override
        protected Void doInBackground(Void... params) {

            SignalProcessingModule processing = new SignalProcessingModule(data, isDemo);
            publishProgress("Processing...");
            Log.d(TAG, "isDemo: " + String.valueOf(isDemo));
            if (isDemo) {
                ahi = processing.demo();
                if (ahi <= 5) {
                    status = " (Normal)";
                } else if (ahi > 5 && ahi <= 15) {
                    status = "(Mild sleep apnea)";
                } else if (ahi > 15 && ahi <= 30) {
                    status = "(Moderate sleep apnea)";
                } else if (ahi >= 30) {
                    status = " (Severe sleep apnea)";
                }
                minMaxHr[0] = 0;
                minMaxHr[1] = 0;
                minMaxSp[0] = 0;
                minMaxSp[1] = 0;
                dips = 0;
                avg[0] = 0;
                avg[1] = 0;
                return null;
            } else {
                processing.initialize();
                publishProgress("Processing...Heart rate");
                minMaxHr = processing.getMinMaxHr();
                publishProgress("Processing...SpO2");
                minMaxSp = processing.getMinMaxSp();
                dips = processing.getNumberOfDips(1);
                publishProgress("Processing...Averages");
                avg = processing.getAverage();
                return null;
            }
        }
    }


}
