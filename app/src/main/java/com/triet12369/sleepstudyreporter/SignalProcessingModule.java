package com.triet12369.sleepstudyreporter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Triet on 5/3/2017.
 */

public class SignalProcessingModule {
    private static final String TAG = "SignalProcessing";
    private ArrayList<String[]> data = new ArrayList<String[]>();
    private int DISCARD_BOTH_ENDS = 5;
    private int THRESHOLD_SP = 90;
    private int startIndex, endIndex;
    public SignalProcessingModule(ArrayList<String[]> in_data) {
        data = in_data;
    }
    ////Initialize indexes
    public void initialize() {
        if (DISCARD_BOTH_ENDS != 0) {
            startIndex = DISCARD_BOTH_ENDS;
            endIndex = data.size() - DISCARD_BOTH_ENDS;
        } else {
            startIndex = 0;
            endIndex = data.size();
        }
    }

    public int[] getMinMaxHr() {
        ArrayList<Integer> hrArray = new ArrayList<>();
        int[] output = {0,0};

        for (int i = startIndex; i < endIndex; i++) {
            hrArray.add(Integer.parseInt(data.get(i)[0]));
        }
        output[0] = Collections.max(hrArray);
        output[1] = Collections.min(hrArray);
        return output;
    }
    public int[] getMinMaxSp() {
        ArrayList<Integer> spArray = new ArrayList<>();
        int[] output = {0,0};
        for (int i = startIndex; i < endIndex; i++) {
            spArray.add(Integer.parseInt(data.get(i)[1]));
        }
        output[0] = Collections.max(spArray);
        output[1] = Collections.min(spArray);
        return output;
    }


}
