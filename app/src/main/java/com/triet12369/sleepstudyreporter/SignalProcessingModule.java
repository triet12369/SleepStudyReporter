package com.triet12369.sleepstudyreporter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Triet on 5/3/2017.
 */

public class SignalProcessingModule {
    private static final String TAG = "SignalProcessing";
    private ArrayList<String[]> data = new ArrayList<String[]>();
    public int DISCARD_BOTH_ENDS = 5;
    private int THRESHOLD_SP = 90, THRESHOLD_HR = 50;
    private int startIndex, endIndex;
    private static ArrayList<Integer> hrArray = new ArrayList<>();
    private static ArrayList<Integer> spArray = new ArrayList<>();

    public SignalProcessingModule(ArrayList<String[]> in_data) {
        data = in_data;
    }
    ////Initialize indexes
    public void initialize() {
        hrArray.clear();
        spArray.clear();
        if (DISCARD_BOTH_ENDS != 0) {
            startIndex = DISCARD_BOTH_ENDS;
            endIndex = data.size() - DISCARD_BOTH_ENDS;
        } else {
            startIndex = 0;
            endIndex = data.size();
        }
        for (int i = startIndex; i < endIndex; i++) {
            hrArray.add(Integer.parseInt(data.get(i)[0]));
            spArray.add(Integer.parseInt(data.get(i)[1]));
        }

    }

    public int[] getMinMaxHr() {
        int[] output = {0,0};
        output[0] = Collections.max(hrArray);
        output[1] = Collections.min(hrArray);
        return output;
    }
    public int[] getMinMaxSp() {
        int[] output = {0,0};
        output[0] = Collections.max(spArray);
        output[1] = Collections.min(spArray);
        return output;
    }
    public double[] getAverage() {
        double[] output = new double[2];
        output[0] = average(hrArray);
        output[1] = average(spArray);
        return output;
    }
    public int getNumberOfDips(int args) {
        switch (args){
            case 0:
                return computeDips(hrArray, THRESHOLD_HR);
            case 1:
                return computeDips(spArray, THRESHOLD_SP);
        }
        return 0;
    }


    private int computeDips(List<Integer> list, int threshold) {
        int check = 0;
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) < threshold && check == 0) {
                check = 1;
                count++;
            } else if (list.get(i) > threshold && check == 1) {
                check = 0;
            } else if (list.get(list.size()-1) < threshold && check == 1) {
                count++;
            }
        }
        return count;
    }
    private double average(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }
        long sum = 0;
        int n = list.size();
        for (int i = 0; i < n; i++) {
            sum += list.get(i);
        }
        return ((double) sum) / n;
    }


}
