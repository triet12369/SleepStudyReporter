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
import java.util.regex.Pattern;

public class GenerateReportActivity extends AppCompatActivity {
    public static String filename;
    private static final String TAG = "GenerateReport";

    private ArrayList<String[]> data= new ArrayList<String[]>();
    private LineGraphSeries mSeries, mSeries2;
    private double graph2LastXValue = 5d;

    private Handler mHandler = new Handler();
    private ProgressDialog progress;
    private boolean taskFinish = false;

    TextView textMaxHr, textMaxHrVal, textMaxSp, textMaxSpVal,
            textMinHr, textMinHrVal, textMinSp, textMinSpVal;


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
        ///////////////////////////////////////////////////////////////////////////

        new readFromFilesTask().execute();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    //Graphview
                    GraphView graph = (GraphView) findViewById(R.id.graph_report);
                    mSeries = new LineGraphSeries<>(dataParser(0));
                    mSeries2 = new LineGraphSeries<>(dataParser(1));
                    graph.addSeries(mSeries);
                    graph.addSeries(mSeries2);
                    graph.getViewport().setYAxisBoundsManual(true);
                    graph.getViewport().setXAxisBoundsManual(true);
                    graph.getViewport().setScalable(true);
                    graph.getViewport().setScrollable(true);
                    graph.getViewport().setYAxisBoundsManual(true);
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(120);
                    mSeries.setTitle("Heart Rate");
                    mSeries2.setTitle("SpO2");
                    graph.getLegendRenderer().setVisible(true);
                    graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                    mSeries.setColor(Color.RED);
                    mSeries2.setColor(Color.GREEN);
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
        int x, y;
        String[] line;
        DataPoint[] values = new DataPoint[size];
        for (int i = 0; i < size; i++) {
            line = data.get(i);
            y = Integer.parseInt(line[index]);
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
    private class dataProcessing extends AsyncTask<Void, Void, Void> {
        int[] minMaxHr, minMaxSp = {0,0};
        @Override
        protected void onProgressUpdate(Void... progress) {

        }
        @Override
        protected void onPreExecute() {
            textMaxHrVal.setText(R.string.calculating);
            textMaxSpVal.setText(R.string.calculating);
            textMinHrVal.setText(R.string.calculating);
            textMinSpVal.setText(R.string.calculating);
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            textMaxHrVal.setText(Integer.toString(minMaxHr[0]));
            textMaxSpVal.setText(Integer.toString(minMaxSp[0]) + '%');
            textMinHrVal.setText(Integer.toString(minMaxHr[1]));
            textMinSpVal.setText(Integer.toString(minMaxSp[1]) + '%');
        }
        @Override
        protected Void doInBackground(Void... params) {
            SignalProcessingModule processing = new SignalProcessingModule(data);
            processing.initialize();
            minMaxHr = processing.getMinMaxHr();
            minMaxSp = processing.getMinMaxSp();
            return null;
        }
    }


}
