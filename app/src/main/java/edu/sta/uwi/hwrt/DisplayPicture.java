package edu.sta.uwi.hwrt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplayPicture extends AppCompatActivity {

    private Bitmap myBitMap;
    private Bitmap gray;
    private Button scanReceiptButton;
    private Rect rect = new Rect();
    private Classifier mClassifier;
    private static final int PIXEL_WIDTH = 28;
    private ArrayList<Classification> classifications = new ArrayList<Classification>();
    private ArrayList<ConfRect> confRects =  new ArrayList<ConfRect>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        //load model
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

        //Get taken picture
        Intent intent = getIntent();
        String fileStr = intent.getStringExtra("picture");
        File imgFile = new File(fileStr);
        if(imgFile.exists()) {
            myBitMap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imgView = (ImageView) findViewById(R.id.displayPic);
            imgView.setImageBitmap(myBitMap);
            imgFile.delete();
        }
    }

    private void scanReceipt() {
        gray = toGrayScale();
        ImageView imgView = (ImageView) findViewById(R.id.displayPic);
        imgView.refreshDrawableState();
        imgView.setImageBitmap(gray);

        scanBitmap();
    }

    private void scanBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int croppedWidth, croppedHeight, offset_x,offset_y, croppedSize;
                Bitmap croppedBMP;
                Bitmap scaledBM = Bitmap.createScaledBitmap(gray,480,360,true);
                gray.recycle();
                //for(croppedWidth = 100; croppedWidth < 300; croppedWidth += 20) {
                    for(croppedSize = 30; croppedSize < 100; croppedSize += 20) {
                        for(offset_x = 0; offset_x < scaledBM.getWidth() - croppedSize; offset_x += croppedSize/4) {
                            for(offset_y = 0; offset_y < scaledBM.getHeight() - croppedSize; offset_y += croppedSize/4) {
                                croppedBMP = Bitmap.createBitmap(scaledBM,offset_x,offset_y,croppedSize,croppedSize);

                                Bitmap resizedBM = Bitmap.createScaledBitmap(croppedBMP,28,28, true);
                                float pixels[] = getPixelData(resizedBM);
                                resizedBM.recycle();
                                croppedBMP.recycle();
                                Classification res = mClassifier.recogize(pixels);
                                if(res.getLabel() != null) {
                                    if(res.getConf() >= 0.999f) {
                                        //classifications.add(res);
//                                        ConfRect confRect = ConfRect.create(offset_x,offset_y,offset_x+croppedSize,offset_y+croppedSize,res);
//                                        if(confRects.isEmpty()) {
//                                            confRects.add(confRect);
//                                        } else {
//                                            for (int i = 0; i < confRects.size(); i++) {
//                                                ConfRect cr = confRects.get(i);
//                                                if(cr.intersects(confRect.getRect())) {
//                                                    if(confRect.getClassification().getConf() > cr.getClassification().getConf()) {
//                                                        int idx = confRects.indexOf(cr);
//                                                        confRects.remove(idx);
//                                                        confRects.add(idx,confRect);
//                                                    }
//                                                } else {
//                                                    confRects.add(confRect);
//                                                }
//                                            }
//                                        }
                                        System.out.println(res.getLabel() + ", " + res.getConf());
                                    }
                                }
                            }
                        }
                    }

//                    for(ConfRect cr : confRects) {
//                        System.out.print(cr.getClassification().getLabel() + ", "  + cr.getClassification().getConf());
//                    }
                //}
                scaledBM.recycle();
                //gray.recycle();
            }
        }).start();
    }
    private float[] getPixelData(Bitmap bitmap) {
        int[] pixels = new int[PIXEL_WIDTH * PIXEL_WIDTH];
        bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        float[] retPixels = new float[pixels.length];
        for(int i=0; i < pixels.length; ++i) {
            //set 0 for white and 255 for black pixel
            int pix = pixels[i];
            int b = pix & 0xff;
            retPixels[i] = (float) ((0xff - b)/255.0);
        }
        return retPixels;
    }

    private void loadModel() {
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   mClassifier = TensorflowClassifier.create(getAssets(),"TensorFlow",
                           "optimized_tfdroid.pb", "labels.txt", PIXEL_WIDTH,
                           "input","output",false);
               } catch (final Exception e) {
                   throw new RuntimeException("Error initializing Classifiers!", e);
               }
           }
       }).start();
    }

    private Bitmap toGrayScale() {
        int width, height;
        height = myBitMap.getHeight();
        width = myBitMap.getWidth();

        Bitmap bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final float factor = 255f;
        final float redBri = 0.2126f;
        final float greenBri = 0.2126f;
        final float blueBri = 0.0722f;

        int length = width * height;
        int[] inpixels = new int[length];
        int[] oupixels = new int[length];

        myBitMap.getPixels(inpixels, 0, width, 0, 0, width, height);

        int point = 0;
        for(int pix: inpixels){
            int R = (pix >> 16) & 0xFF;
            int G = (pix >> 8) & 0xFF;
            int B = pix & 0xFF;

            float lum = (redBri * R / factor) + (greenBri * G / factor) + (blueBri * B / factor);

            if (lum > 0.25) {
                oupixels[point] = 0xFFFFFFFF;
            }else{
                oupixels[point] = 0xFF000000;
            }
            point++;
        }
        bmpGray.setPixels(oupixels, 0, width, 0, 0, width, height);

//        int[] pixels = new int[bmpGray.getWidth() * bmpGray.getHeight()];
//        bmpGray.getPixels(pixels,0,bmpGray.getWidth(),0,0,bmpGray.getWidth(),bmpGray.getHeight());
//        int[] retPixels = new int[pixels.length];
//        for(int i=0; i < pixels.length; ++i) {
//            //set 0 for white and 255 for black pixel
//            int pix = pixels[i];
//            int b = pix & 0xff;
//            retPixels[i] = ((0xff - b)/255);
//        }
//
//        bmpGray.setPixels(retPixels, 0, width, 0, 0, width, height);

        return bmpGray;
    }



}
