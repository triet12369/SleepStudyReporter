package com.triet12369.sleepstudyreporter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Created by Triet on 5/3/2017.
 */

public class SignalProcessingModule {
    private static final String TAG = "SignalProcessing";
    private ArrayList<String[]> data = new ArrayList<String[]>();
    private boolean isDemo;
    public int DISCARD_BOTH_ENDS = 5;
    private int THRESHOLD_SP = 90, THRESHOLD_HR = 50;
    private int startIndex, endIndex;
    private static ArrayList<Integer> hrArray = new ArrayList<>();
    private static ArrayList<Integer> spArray = new ArrayList<>();
    private static double[][] IW = {{0.064491,-0.11759,0.031438,-0.30395,-0.071171,0.027982,-0.040499,0.048577,0.089648,-0.14169,-0.12681,-0.0325,-0.1314,0.22422,-0.063181,0.21454,0.11446,0.21169,0.0022012,0.23965,0.16491,-0.034637,0.21961,-0.0062949,0.12832,0.0066208,0.0026943,0.1442,0.17955,0.14693,-0.078664,0.16306,0.026796,0.26757,0.045897,0.30122,0.14369,-0.063567,0.31721,0.29874,0.2346,0.20916,-0.17778,0.076567,-0.017316,-0.090252,-0.052953,0.019679,-0.28682,-0.0024621,-0.20693,-0.26522,0.053281,-0.12458,-0.09616,-0.20554,0.014281,-0.13144,-0.012164,-0.13739,-0.072703,-0.16691,-0.043334,0.22489,0.2101,0.013981,0.15377,0.099891,0.16551,0.1074,0.086895,0.23187,0.234,0.20731,0.066105,0.14365,0.12562,-0.087979,0.13035,0.061961,0.091004,0.10917,-0.024524,-0.038843,-0.067861,-0.084192,0.20245,0.038268,0.15797,0.18824,0.36461,-0.10693,0.039226,-0.05656,0.10551,-0.033007,-0.022279,0.093994,0.083481,-0.048006,-0.06196,-0.16554,0.24516,0.09944,0.18153,-0.10873,-0.15507,-0.11155,0.077215,-0.041292,0.054576,-0.059184,-0.048092,0.17352,0.17315,0.03444,-0.27972,-0.18416,-0.019199,-0.0014284,-0.098589,-0.17116,-0.033818,-0.060818,-0.014706,-0.16127,-0.089746,0.024758,-0.077573,0.00013521,-0.03821,0.10995,0.040572,0.21326,0.094371,-0.06619,-0.11515,0.003257,0.16341,0.021465,-0.2182,-0.2817,-0.14453,0.0081429,-0.16044,-0.074016,0.022027,-0.058598,-0.043344,0.18988
            },
            {0.30927,0.31206,0.07872,0.19115,-0.11559,-0.090773,0.0096095,0.25383,0.1505,-0.030963,0.19437,-0.013404,-0.2085,0.012355,-0.002653,-0.028452,0.062794,0.10731,0.095931,0.018857,0.17834,-0.027277,0.23882,0.12556,0.13454,-0.03163,0.0057162,0.01073,0.11096,-0.0020924,0.012439,-0.2079,-0.037482,0.16411,0.052244,0.096865,0.16416,0.13596,-0.075,-0.13635,-0.2221,-0.17034,-0.14337,-0.08206,0.12156,0.082737,0.051253,-0.12783,-0.097347,-0.17381,0.12165,0.3157,0.10314,0.034713,-0.011443,0.23974,0.17923,0.072055,-0.077065,-0.097194,-0.053106,-0.020051,0.085884,-0.11492,-0.021943,0.0029053,-0.14022,-0.092585,0.0079422,0.19725,0.044027,-0.037016,0.011487,0.15216,-0.085051,-0.15233,0.095119,-0.10828,0.14136,0.052059,-0.13756,0.085171,-0.14197,0.12038,-0.14121,0.20659,0.10908,-0.18032,-0.10852,-0.079285,0.16658,-0.054721,0.056423,-0.14044,-0.019564,-0.08174,0.16466,0.2174,0.10949,0.098871,-0.04788,0.18905,-0.10883,0.22769,0.08939,-0.14129,0.029466,0.10156,-0.14492,-0.27629,-0.043381,0.11255,0.27056,-0.11853,-0.10964,-0.10986,-0.3308,-0.2674,-0.14445,-0.0090848,-0.034756,-0.13978,-0.23443,-0.028067,0.002498,0.017165,-0.30926,-0.16702,-0.012256,-0.005082,0.074669,0.023613,0.12103,-0.050482,0.025836,-0.097141,-0.1907,-0.18374,-0.083314,-0.18273,0.047531,-0.071219,-0.23041,-0.077548,-0.30476,-0.11451,0.0045944,-0.10067,0.057442,0.039799
            },
            {-0.097089,-0.13014,-0.024034,0.12164,-0.14844,0.11559,-0.2544,-0.028147,0.093544,0.16185,0.063837,-0.067649,-0.083735,0.093562,0.048454,0.23227,-0.09982,0.048764,0.078358,0.092746,-0.2294,0.0051406,0.14448,0.14656,-0.0102,-0.021759,-0.024573,0.055929,0.090459,-0.1845,0.021742,-0.03257,0.057413,0.028192,0.016073,-0.061145,0.14592,-0.045932,0.12088,0.19021,0.13702,-0.15454,0.0066905,0.17817,0.077439,0.15138,0.068242,-0.076413,0.089416,-0.0067762,-0.16501,-0.045879,-0.21537,-0.17268,0.13602,0.089076,-0.16375,0.16014,0.11447,-0.24614,-0.22941,-0.054349,-0.12288,0.17873,0.12103,-0.046486,-0.046906,0.090395,-0.20308,-0.10653,0.094596,0.083496,0.11845,-0.14823,-0.13324,-0.088325,0.10974,0.20359,-0.070925,0.031844,-0.035112,-0.032864,0.057165,-0.09691,0.14037,0.021273,0.23493,0.12546,0.11061,-0.075112,0.027489,0.10044,-0.063344,-0.16146,-0.090911,-0.094923,0.13453,-0.094656,-0.24366,0.16077,-0.1696,-0.20088,-0.04734,-0.1953,-0.059574,0.11133,-0.032372,0.10935,0.18031,-0.050334,0.1387,0.27298,0.085752,0.06906,0.12825,-0.012929,0.13181,0.020881,-0.071145,0.030005,0.22497,0.20861,-0.083653,-0.11475,-0.094704,0.075866,0.10706,0.13788,0.18584,-0.10857,0.091001,-0.19147,0.099076,-0.017003,0.08631,0.13036,0.20233,0.19762,0.067195,-0.16022,0.025959,0.20585,0.26238,0.26939,-0.036158,0.14795,0.12379,0.15432,-0.0020604,-0.1265
            },
            {-0.17804,0.034093,-0.14016,-0.11818,0.29398,0.15022,0.17161,0.17948,0.042884,0.10412,-0.11783,-0.0095419,0.18824,0.15253,0.15753,0.21079,0.038324,-0.14383,-0.16494,-0.073712,0.16982,0.027728,0.17155,0.094758,0.028365,0.11198,0.15864,0.0596,-0.11649,0.071993,-0.0697,0.24874,0.17167,0.06177,0.11476,0.14856,0.073305,0.21722,-0.10522,-0.041867,-0.06788,0.2404,0.1379,0.092279,0.25534,-0.086073,0.10385,-0.044851,-0.14847,0.19223,-0.12802,0.17745,-0.11974,-0.087057,0.19003,0.21501,-0.033033,-0.19707,-0.072345,-0.048305,0.00055701,0.1148,-0.072713,0.24301,-0.11054,0.0076072,-0.10829,-0.11139,0.10747,0.072523,0.058215,0.055351,0.19293,-0.12087,0.11294,-0.080104,-0.05497,0.094372,0.13126,-0.0053229,0.13798,-0.013651,0.1562,-0.045627,-0.18952,0.06524,-0.049108,-0.055216,0.042344,-0.15282,-0.050992,-0.17334,-0.10317,-0.14916,-0.082561,0.084368,-0.11143,0.14947,-0.13407,0.055357,0.10595,-0.0052309,-0.079094,0.12593,0.070866,0.1236,0.28597,0.089227,-0.092654,-0.086819,-0.05733,0.048451,0.010607,0.086436,-0.077394,0.098771,0.079719,-0.11306,0.081971,0.10192,0.19474,-0.020723,0.10677,-0.12222,0.10426,-0.074809,0.16109,0.049391,-0.022659,0.079732,0.029358,0.09343,0.028826,0.062363,0.1375,0.12302,0.27883,0.20935,0.15442,-0.090946,-0.048612,-0.051962,0.24076,0.29391,0.17934,0.24901,-0.030667,0.062196,0.040501,-0.026656
            },
            {-0.27088,-0.35665,-0.020457,0.17913,-0.097842,0.13573,-0.070286,-0.16309,-0.10624,0.16973,-0.2649,0.12422,0.20792,0.24361,0.40344,0.064967,-0.065363,0.027977,-0.22123,-0.055018,0.094057,-0.02727,-0.013153,0.0041824,-0.015405,-0.017344,-0.056347,-0.11145,0.10032,-0.21625,-0.18152,-0.11639,-0.06918,0.21909,-0.033802,-0.10048,0.10261,0.25965,0.22396,0.018254,0.19874,0.029334,0.13049,0.070104,0.19583,0.13169,0.066977,-0.11089,-0.045567,0.17261,-0.026871,-0.233,-0.14437,0.13225,-0.10399,-0.0051979,-0.13681,-0.13101,-0.2719,0.12287,0.16025,0.10334,0.14643,-0.15547,-0.17435,-0.062916,-0.093649,0.20018,0.13786,-0.054863,0.15626,-0.031219,0.016849,0.0011095,-0.073318,0.23355,-0.010893,-0.10998,0.20108,-0.0086091,0.18734,0.20163,0.10802,-0.11039,0.16817,0.08802,0.14208,0.25568,-0.10074,0.15494,0.056246,-0.11584,-0.28734,-0.14663,0.080513,0.014065,-0.028087,-0.011341,-0.019899,0.22075,0.11548,0.11588,-0.29012,-0.1157,-0.007547,0.20693,0.13517,-0.038713,-0.17435,0.19998,0.27616,0.12236,-0.30216,-0.099434,0.034659,0.24605,0.15345,0.14196,0.3149,0.20497,0.097111,-0.064938,0.13875,0.16145,0.24854,-0.069525,0.14013,0.30749,0.0016518,-0.042402,-0.26781,-0.15776,-0.075227,0.11911,0.055731,0.1209,0.2349,0.10981,0.3864,0.056442,0.15899,0.36002,0.40022,0.38288,0.23529,0.1054,0.40366,0.3043,0.23182,0.16167
            },
            {0.053606,0.11144,0.12687,0.13771,0.010159,0.056359,0.010688,0.14422,0.10279,0.12389,0.13405,0.05839,0.13963,0.14586,0.16835,0.13374,0.032562,0.091885,-0.2035,0.14854,-0.012061,-0.066948,-0.21511,0.078999,-0.025096,0.076445,0.099494,-0.065939,-0.10799,-0.14109,0.1752,0.033622,0.083262,0.078328,0.043618,-0.076948,-0.076481,0.15718,-0.19148,0.10002,-0.20252,0.13684,0.14645,0.093034,-0.16209,0.08325,0.041157,0.10513,-0.2073,-0.056553,-0.036747,0.16191,-0.11661,0.13151,0.050186,-0.020343,0.12896,0.19296,0.076392,0.10419,-0.082736,0.22402,0.025799,0.037364,0.21537,-0.10445,-0.10511,-0.12916,-0.057493,0.03392,0.20285,-0.10833,0.15399,-0.12574,0.05012,-0.021823,-0.061987,0.13445,0.22354,0.12034,-0.00483,0.21525,0.11779,-0.10946,0.14734,0.17173,0.14779,0.098881,-0.15456,0.17701,-0.054959,0.13889,-0.10798,0.10931,-0.0043285,-0.20067,-0.048998,-0.073805,-0.021181,-0.034333,-0.086629,0.1333,-0.14937,0.17963,0.048346,0.084341,-0.13798,-0.15314,-0.062749,-0.12334,0.014829,0.12232,-0.006888,-0.14368,0.01092,0.19436,-0.083017,0.17199,-0.10341,0.14729,-0.11895,0.041545,0.040009,0.15726,0.12238,0.042388,-0.10443,0.17157,-0.094717,-0.11245,-0.086626,-0.06682,-0.18482,0.159,0.046531,-0.10846,0.1198,-0.017681,-0.094456,-0.084864,0.0022061,0.06271,-0.0020753,-0.1122,-0.0378,-0.010164,-0.14192,0.034232,0.12455,0.0079691
            },
            {-0.33777,0.0098981,-0.080284,0.082173,-0.25714,0.11563,-0.17294,-0.16214,0.076304,-0.09044,0.060808,-0.033244,-0.043605,0.083749,0.12032,-0.20401,-0.080339,-0.0068186,-0.099874,-0.06794,-0.31668,-0.26984,-0.26552,-0.14074,-0.2701,-0.07211,0.12587,-0.17559,-0.043773,-0.0099305,-0.30279,0.031287,-0.19974,-0.23436,0.026593,-0.18566,0.077917,0.09553,-0.28365,-0.19594,0.043865,-0.14746,-0.076781,-0.31923,-0.19979,-0.17266,-0.2411,-0.20891,0.024098,-0.30528,0.12235,-0.10085,-0.027827,-0.0058554,-0.017074,-0.11944,-0.20326,0.0093776,0.087918,-0.094476,-0.15988,0.24941,-0.00756,0.001081,0.21937,0.012349,0.074765,0.17147,0.19521,0.17557,-0.15558,-0.13255,0.24819,-0.026094,0.034433,0.21791,0.083592,-0.10445,0.1525,0.16717,-0.074873,-0.14665,-0.12834,0.063763,-0.18003,0.035235,-0.044769,-0.0092202,0.012467,0.035245,0.14801,0.020856,-0.20959,-0.1727,-0.17918,-0.0041724,-0.16867,-0.0080395,-0.35811,0.03103,-0.16267,0.068469,-0.1428,0.094659,0.080406,0.10838,0.12855,-0.00099265,-0.073852,-0.042897,0.10555,-0.044603,0.065855,0.044915,0.009722,0.13391,-0.018741,0.077871,-0.049632,-0.04508,0.19337,-0.19134,-0.052123,0.17831,-0.016628,-0.10673,-0.15221,-0.011185,-0.069612,0.2457,-0.025922,-0.10842,-0.045775,0.10471,-0.02804,0.20921,0.0074026,-0.035603,0.16061,-0.022297,0.12868,-0.00077629,0.14795,-0.033386,0.12123,0.048851,0.22972,0.21154,0.089396,0.0085454
            },
            {-0.14894,-0.075566,-0.01479,-0.089781,0.076481,0.05139,-0.045838,0.21223,-0.11972,0.16048,-0.11762,-0.14689,0.13893,0.10598,-0.11349,-0.089514,-0.11611,0.091548,0.10559,0.1552,0.17358,-0.077155,0.15423,-0.15181,0.098362,0.095724,0.026213,0.042727,0.040519,-0.05746,0.1514,0.096302,0.18579,-0.027493,-0.075552,-0.071735,-0.10854,-0.098778,0.11707,-0.17547,-0.17327,-0.16074,-0.10099,0.15381,0.064869,-0.15887,-0.023368,-0.038134,-0.024369,-0.10599,-0.10924,-0.10638,0.030694,0.19577,0.034998,0.011012,0.15933,0.20001,-0.021207,-0.13272,0.066729,-0.01093,0.23214,0.14359,0.19972,0.014607,0.18903,-0.028569,0.10841,0.081422,0.12573,-0.13816,0.07716,0.0093247,0.0016148,0.11949,0.052258,0.10091,-0.13111,0.16918,0.17459,-0.038663,-0.030123,0.091421,-0.17455,0.095022,-0.12756,0.12535,-0.18028,-0.0089429,-0.018358,0.18284,0.043732,0.083343,-0.086987,0.11911,-0.096701,-0.041115,0.08832,-0.1013,-0.031938,-0.14952,0.022061,-0.1302,0.017078,0.157,0.21106,0.077208,0.18267,-0.12424,-0.13499,-0.021144,0.1315,0.18239,-0.13129,-0.072436,-0.1458,-0.1207,0.23596,-0.068498,-0.024824,0.082313,-0.049938,-0.096136,0.12761,0.061357,0.10897,0.12736,-0.034772,0.041489,0.029274,0.13043,0.19683,0.20393,-0.12235,-0.10058,-0.11722,-0.076687,0.040777,0.12978,-0.050949,-0.081114,-0.086044,-0.096765,0.060753,0.054435,-0.01419,0.15612,-0.15976,-0.021338
            },
            {0.17514,0.14381,-0.064127,-0.15173,0.0066182,-0.10576,0.034054,0.142,-0.044345,-0.15454,0.11078,-0.17985,-0.14798,-0.065618,0.12883,0.095902,0.14469,0.066759,-0.16671,-0.11861,-0.16634,-0.026176,0.14263,-0.14569,-0.055185,-0.14893,-0.24222,0.10135,-0.12559,0.011672,0.071369,-0.18015,-0.12565,-0.23188,-0.11815,-0.17036,0.098856,-0.13682,-0.087373,0.1891,0.18864,0.18858,-0.14918,-0.10312,-0.019683,0.12832,0.19854,-0.13225,0.15048,0.079579,-0.14103,-0.061305,-0.20136,0.0045443,-0.0091618,0.09411,0.081349,-0.073736,-0.16843,0.069233,-0.13714,-0.12899,0.011136,-0.10578,-0.031997,-0.0014642,0.0025987,-0.21577,-0.20325,-0.019613,0.089055,0.1234,-0.021409,0.072257,-0.24687,-0.2105,-0.13356,0.043786,-0.10804,-0.23616,-0.17144,-0.20928,-0.19735,-0.11798,-0.029924,-0.005107,-0.14244,0.16484,-0.0066057,0.14265,-0.062341,-0.062722,-0.17806,-0.17692,-0.13232,-0.18751,0.072693,-0.12724,0.064505,0.14276,0.062076,0.0010139,-0.17409,-0.025012,0.083966,0.1,0.06149,-0.15725,-0.14374,0.0059563,0.10632,-0.02266,-0.17602,-0.094844,0.15244,0.04453,-0.0013236,-0.17752,0.18194,0.053863,0.035801,-0.15873,0.010637,-0.066277,0.1108,-0.12956,-0.054169,-0.029047,-0.18396,0.011657,-0.0014732,0.1484,0.083787,0.1493,-0.056316,0.019576,0.068883,-0.042122,-0.064709,0.15666,0.18256,0.12168,-0.1059,-0.0026687,-0.15127,-0.072357,0.01346,0.092703,-0.12126,0.14315
            },
            {0.10737,-0.15011,-0.055284,-0.022744,-0.018511,0.15206,0.19269,-0.0040952,-0.11853,0.22077,-0.0061628,-0.079574,-0.094619,0.13782,0.039727,-0.16314,0.08827,0.18951,-0.14168,-0.091194,0.087733,-0.067282,-0.0070133,-0.11073,0.0079747,0.013187,0.035973,0.0072981,0.19045,-0.18442,-0.12005,-0.17065,-0.12315,-0.033864,-0.12925,0.14319,-0.11764,-0.15364,-0.02118,0.12795,-0.019876,-0.094471,0.039271,0.14987,-0.15971,0.15092,-0.11324,-0.1355,0.20654,0.088767,-0.056698,0.14882,0.15081,-0.11291,-0.094911,0.15821,-0.12044,0.078097,0.16395,-0.11851,0.088938,0.20173,0.11501,0.10631,0.21609,-0.023395,0.18217,-0.098512,-0.079777,-0.0091922,0.1377,0.035459,0.17953,0.118,0.19615,0.19732,-0.096971,0.17981,0.1853,-0.15341,-0.0020471,-0.079319,-0.071161,0.10562,-0.12981,0.15763,-0.04763,0.10515,-0.13186,0.007246,-0.098542,-0.10987,0.02735,-0.061163,-0.14974,0.063663,-0.027901,0.043126,-0.098293,-0.017214,0.15023,-0.10052,0.036958,-0.010826,0.14568,0.18554,0.1635,-0.021189,0.17143,-0.13561,-0.052537,-0.027606,-0.026607,0.12718,-0.029844,0.07621,0.061027,0.12983,0.11311,-0.18263,0.15612,-0.1348,0.16791,-0.19117,0.046705,-0.082685,-0.011958,-0.10803,0.069278,-0.1871,0.1586,-0.061926,-0.059652,0.098847,-0.17608,0.12532,-0.083723,0.14004,0.095454,-0.011943,-0.017688,0.11344,0.11698,0.14683,-0.054322,0.087529,-0.06381,0.12016,-0.069832,0.097073
            }};
    private static double[][] LW = {{0.27757,-0.51247,0.3964,0.12468,0.58839,0.23804,0.89526,-0.062796,0.42589,-0.44056
        },
            {0.86282,0.59855,-0.44383,-0.3658,-1.0463,0.069174,-0.18069,0.062141,-0.38285,0.30673
            }};
    private static double[][] b1 = {{-1.3654},{-1.2043},{0.81745},{0.44637},{0.21212},{0.22037},{-0.26142},{-0.82848},{1.0571},{1.3883}};
    private static double[][] b2 = {{-1.1032},{1.5354}};

    public SignalProcessingModule(ArrayList<String[]> in_data, boolean test) {
        data = in_data;
        isDemo = test;
    }
    ////Initialize indexes
    public void initialize() {
        Log.d(TAG, String.valueOf(isDemo));
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

    public double demo() {
        double[][] sigInput = new double[150][700];
        double[][] sig,ira,iri;
        double ahi;
        sig = reshape(parseArray(0), 50, 700);
        ira = reshape(parseArray(1), 50, 700);
        iri = reshape(parseArray(2), 50, 700);
        for (int i = 0; i<50; i++) {
            for (int k = 0; k<700; k++){
                sigInput[i][k] = ira[i][k];
            }
        }
        for (int i = 50; i<100; i++) {
            for (int k = 0; k<700; k++){
                sigInput[i][k] = iri[i-50][k];
            }
        }
        for (int i = 100; i<150; i++) {
            for (int k = 0; k<700; k++){
                sigInput[i][k] = sig[i-100][k];
            }
        }
        ahi = feedfoward(sigInput);
        Log.d(TAG, String.valueOf(ahi));
        return ahi;
    }
    private double[][] parseArray(int index) {
        int size = data.size();
        //Log.d(TAG, "data.size = " + size);
        double[][] tempArray = new double[size][1];
        for (int i = 0; i<data.size();i++) {
            tempArray[i][0] = Double.parseDouble(data.get(i)[index]);
        }
        return tempArray;
    }
    public static double[][] reshape(double[][] A, int m, int n) {
        int origM = A.length;
        int origN = A[0].length;
        if(origM*origN != m*n){
            throw new IllegalArgumentException("New matrix must be of same area as matix A");
        }
        double[][] B = new double[m][n];
        double[] A1D = new double[A.length * A[0].length];

        int index = 0;
        for(int i = 0;i<A.length;i++){
            for(int j = 0;j<A[0].length;j++){
                A1D[index++] = A[i][j];
            }
        }

        index = 0;
        for(int i = 0;i<n;i++){
            for(int j = 0;j<m;j++){
                B[j][i] = A1D[index++];
            }

        }
        return B;
    }
    private double feedfoward(double[][] sigInput) {
        double[][] output = new double [2][700];
        double[] xmax = new double[150];
        double[] xmin = new double[150];
        double[] input = new double[150];
        for (int m = 0; m < 700; m++) {
            for (int l = 0; l < 150; l++){
                input[l] = sigInput[l][m];
            }
            double[][] L1 = new double[10][150];
            for (int i = 0; i < 150; i++){
                xmax[i] = max(sigInput[i]);
                xmin[i] = min(sigInput[i]);
                input[i] = 2*(input[i]-xmin[i])/(xmax[i] - xmin[i]) - 1;
                for (int k = 0; k < 10; k++){
                    L1[k][i] = input[i] * IW[k][i];
                }
            }
            double[] L1a = new double[10];
            for (int i = 0; i < 10; i++) {
                L1a[i] = 2/(1 + Math.exp(-2*sum(L1[i]) + b1[i][0])) - 1;
            }
            double[][] L2 = new double[2][10];
            for (int i = 0; i < 10; i++) {
                for (int k = 0; k < 2; k++) {
                    L2[k][i] = L1a[i] * LW[k][i];
                }
            }
            output[0][m] = sum(L2[0]) + b2[0][0];
            output[1][m] = sum(L2[1]) + b2[1][0];
            for (int i = 0; i < 2; i++){
                output[i][m] = Math.round((output[i][m] + 1)/2);
                if (output[i][m] > 1){
                    output[i][m] = 1;
                } else if (output[i][m] < 0){
                    output[i][m] = 0;
                }
            }
        }
        //Log.d(TAG, "output: " + output[0][0] + output[1][0]);
        return sum(output[1])/(5+5/6.0);
    }
    private double max(double[] array) {
        double max = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }
    private double min(double[] array) {
        double min = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }
    private double sum(double[] array){
        double sum = 0;
        for (int i = 0; i < array.length; i++){
            sum += array[i];
        }
        return sum;
    }
}
