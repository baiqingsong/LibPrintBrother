package com.dawn.libdownload;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dawn.print_brother.PrintBrotherFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrintBrotherFactory.getInstance(this).initPrintBrother();
        TextView tvPrint = findViewById(R.id.tv_print);
        tvPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Uri path = Uri.parse("content://media/external/images/media/24");
                PrintBrotherFactory.getInstance(MainActivity.this).printImage(path);
            }
        });
    }
}