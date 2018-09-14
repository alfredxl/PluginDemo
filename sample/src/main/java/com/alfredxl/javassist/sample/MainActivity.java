package com.alfredxl.javassist.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setText("javassist_Hello World!");
    }

    @PointAnnotation(className = "com.alfredxl.javassist.sample.InsertClass", methodName = "showText")
    private void setText(String text) {
        ((TextView) findViewById(R.id.textView)).setText(text);
    }
}
