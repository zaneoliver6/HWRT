package edu.sta.uwi.hwrt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Bundle;
import android.graphics.PorterDuff.Mode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AnnotateImg extends AppCompatActivity {

    private Button doneButton;
    private String imgPath;
    private String text;
    private File imgFile;
    private File xmlFile;
    private File txtFile;
    private Bitmap bitmapMaster;
    private Canvas canvasMaster;
    private Bitmap bitmapDrawingPane;
    private Canvas canvasDrawingPane;
    private projectPt startPt;
    private Bitmap myBitMap;
    ImageView imageResult, imageDrawingPane;
    private List<annotation> annotations = new ArrayList<>();

    final Context context = this;
    //private Button button;
    private String  result;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_img);
        imageResult = (ImageView) findViewById(R.id.result);
        imageDrawingPane = (ImageView) findViewById(R.id.drawingpane);

        Intent intent = getIntent();
        imgPath = intent.getStringExtra("picture");
        text = intent.getStringExtra("text");

        loadImage(imgPath);

        imageResult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startPt = projectXY((ImageView)view, bitmapMaster,x,y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawOnRectProjectedBitMap((ImageView)view,bitmapMaster,x,y);
                        break;
                    case MotionEvent.ACTION_UP:
                        drawOnRectProjectedBitMap((ImageView)view,bitmapMaster,x,y);
                        finalizeDrawing();
                        getLabel();
                        projectPt end = projectXY((ImageView)view,bitmapMaster,x,y);
                        annotation ann = new annotation(startPt,end,result);
                        annotations.add(ann);
                        break;
                }
                return true;
            }
        });

        doneButton = (Button) findViewById(R.id.btn_feedback);
        assert doneButton != null;
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                sendFeedback();
            }
        });
    }


    private void getLabel() {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.activity_annotate_img, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                result = userInput.getText().toString();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });


        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void createXMl(){
        String imgname = imgFile.getName();
        imgname = imgname.substring(0, imgname.lastIndexOf('.'));
        imgname += ".xml";

        xmlFile = new File(Environment.getExternalStorageDirectory() + "/" + imgname);
        try {
            xmlFile.createNewFile();
        } catch (IOException e) {
            Log.e("IOException", "exception in createNewFile() method: ");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(xmlFile);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "can't create File output stream: ");
        }

        XmlSerializer serializer = Xml.newSerializer();
        try {

            serializer.setOutput(fos,"UTF-8");
            //serializer.startDocument(null,Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null,"annotation");
                serializer.startTag(null,"folder");
                serializer.text("receipts");
                serializer.endTag(null,"folder");

                serializer.startTag(null,"filename");
                serializer.text(imgFile.getName());
                serializer.endTag(null,"filename");

                serializer.startTag(null,"size");
                    serializer.startTag(null,"width");
                    serializer.text("480");
                    serializer.endTag(null,"width");
                    serializer.startTag(null,"height");
                    serializer.text("640");
                    serializer.endTag(null,"height");
                serializer.endTag(null,"size");

                serializer.startTag(null,"segmentation");
                serializer.text("0");
                serializer.endTag(null,"segmentation");

                for(annotation a : annotations) {
                    serializer.startTag(null, "object");
                        serializer.startTag(null,"name");
                        serializer.text(a.ann);
                        serializer.endTag(null,"name");
                        serializer.startTag(null,"bndbox");
                            serializer.startTag(null,"xmin");
                            serializer.text(String.valueOf(a.start.x));
                            serializer.endTag(null,"xmin");

                            serializer.startTag(null,"ymin");
                            serializer.text(String.valueOf(a.start.y));
                            serializer.endTag(null,"ymin");

                            serializer.startTag(null,"xmax");
                            serializer.text(String.valueOf(a.end.x));
                            serializer.endTag(null,"xmax");

                            serializer.startTag(null,"ymax");
                            serializer.text(String.valueOf(a.end.y));
                            serializer.endTag(null,"ymax");
                        serializer.endTag(null,"bndbox");
                    serializer.endTag(null,"object");
                }
            serializer.endTag(null,"annotation");
            serializer.endDocument();
            serializer.flush();
            fos.close();
        } catch (Exception e) {
            Log.e("Exception", "error occrred while creating xml file");
        }
    }


    private void createTxtFile(){
        txtFile = new File(Environment.getExternalStorageDirectory() + "/" + "text.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(txtFile);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "can't create File output stream: ");
        }

        try {
            fos.write(text.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void sendFeedback(){
        createXMl();
        createTxtFile();

        FileUploader xmlUploader = new FileUploader(xmlFile);
        xmlUploader.uploadFile();

        FileUploader imgUploader = new FileUploader(imgFile);
        imgUploader.uploadFile();

        FileUploader txtUploader = new FileUploader(txtFile);
        txtUploader.uploadFile();
    }

    private void loadImage(String fileStr) {
        Bitmap tempBitmap;

        imgFile = new File(fileStr);
        if(imgFile.exists()) {
            tempBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            Config config;
            if(tempBitmap.getConfig() != null) {
                config = tempBitmap.getConfig();
            } else {
                config = Config.ARGB_8888;
            }

            bitmapMaster = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(),config);

            canvasMaster = new Canvas(bitmapMaster);
            canvasMaster.drawBitmap(tempBitmap,0,0,null);
            imageResult.setImageBitmap(bitmapMaster);

            bitmapDrawingPane = Bitmap.createBitmap(tempBitmap.getWidth(),tempBitmap.getHeight(),Config.ARGB_8888);
            canvasDrawingPane = new Canvas(bitmapDrawingPane);
            imageDrawingPane.setImageBitmap(bitmapDrawingPane);
        }
    }

    class annotation {
        projectPt start;
        projectPt end;
        String ann;

        annotation(projectPt start, projectPt end, String ann) {
            start = start;
            end = end;
            ann = ann;
        }
    }

    class projectPt{
        int x;
        int y;

        projectPt(int tx, int ty){
            x = tx;
            y = ty;
        }
    }

    private projectPt projectXY(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return null;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            return new projectPt(projectedX, projectedY);
        }
    }

    private void drawOnRectProjectedBitMap(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            //clear canvasDrawingPane
            canvasDrawingPane.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(3);
            canvasDrawingPane.drawRect(startPt.x, startPt.y, projectedX, projectedY, paint);
            imageDrawingPane.invalidate();
        }
    }

    private void finalizeDrawing(){
        canvasMaster.drawBitmap(bitmapDrawingPane, 0, 0, null);
    }

}
