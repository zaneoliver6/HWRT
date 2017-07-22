package edu.sta.uwi.hwrt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class DisplayText extends AppCompatActivity {

    private Button doneButton;
    private EditText textView;
    private String imgPath;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text);
        textView = (EditText) findViewById(R.id.myTextView);
        textView.setSingleLine(false);
        textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        textView.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        Intent intent = getIntent();
        imgPath = intent.getStringExtra("picture");
        text = intent.getStringExtra("text");
        textView.setText(String.format(text));


        doneButton = (Button) findViewById(R.id.btn_done);
        assert doneButton != null;
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                text = textView.getText().toString();
                gotoAnnotate();
            }
        });
    }

    protected void gotoAnnotate() {
        Intent intent = new Intent(this,AnnotateImg.class);
        intent.putExtra("picture", imgPath);
        intent.putExtra("text",text);
        startActivity(intent);
    }
}
