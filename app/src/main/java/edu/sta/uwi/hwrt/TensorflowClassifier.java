package edu.sta.uwi.hwrt;


import android.content.res.AssetManager;

import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import edu.sta.uwi.hwrt.Utils;

public class TensorflowClassifier implements Classifier {

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

    private static HashMap<String, String> readLabels(AssetManager am, String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));

        String line;
        //List<String> labels = new ArrayList<>();
        HashMap<String, String> map = new HashMap<String, String>();
        while ((line = br.readLine()) != null) {
            String [] parts = line.split(":",2);
            if(parts.length >= 2) {
                String key = parts[0];
                String val = parts[1];
                map.put(key,val);
            } else {
                System.out.println("ignoring line:" + line);
            }
        }
        br.close();
        return map;
    }

    private static String [] getchars(HashMap<String, String> map) {
        String [] chars = new String[map.size()];
        for (String key: map.keySet()){
            int idx = Integer.parseInt(map.get(key));
            chars[idx] = key;
        }
        return chars;
    }

    public static TensorflowClassifier create(AssetManager assetManager, String name,
                                              String modelPath, String labelFile, int inputSize, String inputName, String outputName,
                                              boolean feedKeepProb) throws IOException {
        //intialize a classifier
        TensorflowClassifier c = new TensorflowClassifier();

        //store its name, input and output labels
        c.name = name;

        c.inputName = inputName;
        c.outputName = outputName;

        //read labels for label file
        c.map = readLabels(assetManager, labelFile);
        c.chars = getchars(c.map);

        //set its model path and where the raw asset files are
        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        int numClasses = 57;

        //how big is the input?
        c.inputSize = inputSize;

        // Pre-allocate buffer.
        c.outputNames = new String[] { outputName };

        c.outputName = outputName;
        c.output = new float[numClasses];

        c.feedKeepProb = feedKeepProb;

        return c;
    }

    @Override
    public String name() {
        return name;
    }

    public String recogize(final int[] pixels) {
        tfHelper.feed(inputName, pixels, 1,1);

        if (feedKeepProb) {
            tfHelper.feed("keep_prob", new float[] { 1 });
        }
        tfHelper.run(outputNames);

        tfHelper.fetch(outputName, output);

        Utils util = new Utils();
        int p = util.weightedPick(output);
        //System.out.println(output);
        String ans;
        //if(p < chars.length) {
        ans = chars[p];
        //}
        //Classification ans = new Classification();
//        for (int i = 0; i < output.length; ++i) {
//            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
//                ans.update(output[i], labels.get(i));
////                System.out.println(output[i]);
////                System.out.println(labels.get(i));
//            }
//        }

        return ans;
    }


}
