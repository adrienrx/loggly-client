package net.sparkeek.myapplication;

import android.os.Bundle;

import com.github.adrienrx.loggly.LogglyClient;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LogglyClient test = new LogglyClient("test");
        test.log("test");
    }
}