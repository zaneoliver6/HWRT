package edu.sta.uwi.hwrt;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class TensorflowObjectDetection implements Detector {

    private TensorFlowInferenceInterface tfHelper;
    private static final float THRESHOLD = 0.1f;
    private String name;
    private String inputName;
    private String outputName;
    private int inputSize;
    private boolean feedKeepProb;

    private List<String> labels;
    private HashMap<String, String> map;
    private float[] output;
    private String[] outputNames;
    private String [] chars;
    private int imageMean = 128;
    private int imageStd = 128;


    public static TensorflowObjectDetection create(AssetManager assetManager, String name, String modelPath, String labelFile, String inputName, String[] outputNames, boolean useAssets , boolean feedKeepProb) throws IOException {
        //intialize a classifier
        TensorflowObjectDetection c = new TensorflowObjectDetection();

        //store its name, input and output labels
        c.name = name;

        c.inputName = inputName;
        //c.outputName = outputName;

        //read labels for label file
//        c.map = readLabels(assetManager, labelFile,useAssets);
//        c.chars = getchars(c.map);

        //set its model path and where the raw asset files are
        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        int numClasses = 62;

        //how big is the input?
        //c.inputSize = inputSize;

        // Pre-allocate buffer.
        c.outputNames =  outputNames;

        //c.outputName = outputName;
        c.output = new float[numClasses];

        c.feedKeepProb = feedKeepProb;

        return c;
    }

    @Override
    public String predict(final Bitmap bm) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float [] floatValues =  new float[width*height*3];
        int [] intValues = new int[width*height];

        bm.getPixels(intValues, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            floatValues[i * 3 + 0] = ((intValues[i] & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((intValues[i] >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = (((intValues[i] >> 16) & 0xFF) - imageMean) / imageStd;
        }

        tfHelper.feed(inputName, floatValues, 1,bm.getWidth(),bm.getHeight(),3);
        tfHelper.run(outputNames);


        String ans="";

        return ans;
    }


}
