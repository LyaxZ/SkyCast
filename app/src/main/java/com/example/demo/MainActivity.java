package com.example.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView helloText = findViewById(R.id.helloText);
        helloText.setText(R.string.hello_world);

        TextView niHaoText = findViewById(R.id.niHaoText);
        niHaoText.setText(R.string.ni_hao);
    }
}
