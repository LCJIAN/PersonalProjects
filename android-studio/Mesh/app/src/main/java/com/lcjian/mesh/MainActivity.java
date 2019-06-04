package com.lcjian.mesh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.gradle.mesh.Moderator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Moderator().start();
    }
}
