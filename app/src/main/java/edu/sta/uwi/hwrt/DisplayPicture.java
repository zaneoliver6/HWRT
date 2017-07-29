package edu.sta.uwi.hwrt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.Image;
import android.os.Environment;
import android.os.Trace;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.security.spec.ECField;
import java.util.Arrays;
import java.util.Random;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplayPicture extends AppCompatActivity {

    private Bitmap myBitMap;
    private boolean useUpdatedModels = false;
    private Bitmap gray;
    private Button scanReceiptButton;
    private Rect rect = new Rect();
    private Classifier mClassifier;
    private Detector objectDetector;
    //private static final int PIXEL_WIDTH = 28;
    private File imgFile;
    private String rnnModel = "rnn_optimized_model_mobile.pb";
    private String rnnMapfile = "stringmap.txt";
    private boolean useAssets = true;
    private String odModel = "output_inference_graph.pb";
    private String odModelMap = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        //Get taken picture
        Intent intent = getIntent();
        String fileStr = intent.getStringExtra("picture");
        useUpdatedModels = intent.getBooleanExtra("useNewModels", false);

        //since the object detection model is not working this part of the code only updates the paths for the rnnModel.
        if(useUpdatedModels) {
            rnnModel = Environment.getExternalStorageDirectory().getAbsolutePath() + rnnModel;
            rnnMapfile = Environment.getExternalStorageDirectory().getAbsolutePath() + rnnMapfile;
            useAssets = false;
        }

        //load Image character detector model
        //loadModel2();

        //load rnn model
        loadModel();

        //set click listener to scan button
        scanReceiptButton = (Button) findViewById(R.id.btn_analyze);
        assert scanReceiptButton != null;
        scanReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                scanReceipt();
            }
        });


        imgFile = new File(fileStr);
        if(imgFile.exists()) {
            myBitMap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myBitMap = ARGBBitmap(myBitMap);
            ImageView imgView = (ImageView) findViewById(R.id.displayPic);
            imgView.setImageBitmap(myBitMap);
            //imgFile.delete();
        }
    }

    protected void sendText(String imgPath, String text) {
        Intent intent = new Intent(this,DisplayText.class);
        intent.putExtra("picture", imgPath);
        intent.putExtra("text",text);
        startActivity(intent);
    }

    private void scanReceipt() {
        //int [] int_image = arrayFromBitmap(myBitMap);

        //Bitmap myBMap = ARGBBitmap(myBitMap);
        //String out = objectDetector.predict(myBitMap);
        //Classification res = mClassifier.recogize(int_image);
        //scanBitmap();

        ArrayList<Integer> chars = new ArrayList<Integer>();
        for(int i = 0; i < 56; i++) {
            Random rnd = new Random();
            int n = rnd.nextInt(56) + 0;
            if(chars.contains(n)) {
                i--;
                continue;
            }
            chars.add(n);
        }

        System.out.println(chars);


        String preds = "";
        for (int j = 0; j < chars.size();j++) {
            int [] input = new int[1];
            input[0] = chars.get(j).intValue();
            String rs = mClassifier.predict(input);

            if(rs.toString().contains("\n")) {
                System.out.println("Hello rs:" + rs);
                preds += " \n ";
            } else {
                preds += rs;
            }
        }

        System.out.println(preds);
        sendText(imgFile.getAbsolutePath(),preds);
    }


//    private void scanBitmap() {
//    }
//
//
    private Bitmap ARGBBitmap(Bitmap img) {
        System.out.println(img);
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }

    public static int[] arrayFromBitmap(Bitmap source){
        int width = source.getWidth();
        int height = source.getHeight();
//        int [][] result = new int[width][height];
        int[] pixels = new int[width*height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
//        int pixelsIndex = 0;
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                result[i][j] = pixels[pixelsIndex];
//                pixelsIndex++;
//            }
//        }
        return pixels;
    }

//    private float[] getPixelData(Bitmap bitmap) {
//        int[] pixels = new int[PIXEL_WIDTH * PIXEL_WIDTH];
//        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
//        float[] retPixels = new float[pixels.length];
//        for(int i=0; i < pixels.length; ++i) {
//            //set 0 for white and 255 for black pixel
//            int pix = pixels[i];
//            int b = pix & 0xff;
//            retPixels[i] = (float) ((0xff - b)/255.0);
//        }
//        return retPixels;
//    }

    private void loadModel() {
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   mClassifier = TensorflowClassifier.create(getAssets(),"TensorFlow", rnnModel, rnnMapfile, "input_data:0", "probs",useAssets,false);
               } catch (final Exception e) {
                   throw new RuntimeException("Error initializing Classifiers!", e);
               }
           }
       }).start();
    }

    private void loadModel2() {
        final String [] outputnames = new String[] {"detection_boxes", "detection_scores", "detection_classes", "num_detections"};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    objectDetector = TensorflowObjectDetection.create(getAssets(),"TFOD", odModel, odModelMap, "image_input:0", outputnames, useAssets,false);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing Model!", e);
                }
            }
        }).start();
    }

//
//    private Bitmap toGrayScale() {
//        int width, height;
//        height = myBitMap.getHeight();
//        width = myBitMap.getWidth();
//
//        Bitmap bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//        final float factor = 255f;
//        final float redBri = 0.2126f;
//        final float greenBri = 0.2126f;
//        final float blueBri = 0.0722f;
//
//        int length = width * height;
//        int[] inpixels = new int[length];
//        int[] oupixels = new int[length];
//
//        myBitMap.getPixels(inpixels, 0, width, 0, 0, width, height);
//
//        int point = 0;
//        for(int pix: inpixels){
//            int R = (pix >> 16) & 0xFF;
//            int G = (pix >> 8) & 0xFF;
//            int B = pix & 0xFF;
//
//            float lum = (redBri * R / factor) + (greenBri * G / factor) + (blueBri * B / factor);
//
//            if (lum > 0.25) {
//                oupixels[point] = 0xFFFFFFFF;
//            }else{
//                oupixels[point] = 0xFF000000;
//            }
//            point++;
//        }
//        bmpGray.setPixels(oupixels, 0, width, 0, 0, width, height);
//
////        int[] pixels = new int[bmpGray.getWidth() * bmpGray.getHeight()];
////        bmpGray.getPixels(pixels,0,bmpGray.getWidth(),0,0,bmpGray.getWidth(),bmpGray.getHeight());
////        int[] retPixels = new int[pixels.length];
////        for(int i=0; i < pixels.length; ++i) {
////            //set 0 for white and 255 for black pixel
////            int pix = pixels[i];
////            int b = pix & 0xff;
////            retPixels[i] = ((0xff - b)/255);
////        }
////
////        bmpGray.setPixels(retPixels, 0, width, 0, 0, width, height);
//
//        return bmpGray;
//    }



}
