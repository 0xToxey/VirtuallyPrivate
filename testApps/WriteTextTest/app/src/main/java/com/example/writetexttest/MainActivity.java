package com.example.writetexttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private Button saveBtn, displayBtn;
    private TextView content;
    private EditText textInput;

    private static String fileName = "test.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveBtn = findViewById(R.id.saveBTN);
        displayBtn = findViewById(R.id.displayBTN);
        content = findViewById(R.id.fileContent);
        textInput = findViewById(R.id.textInput);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFile(fileName, textInput.getText().toString());
            }
        });

        displayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content.setText(readFile(fileName));
            }
        });

    }

    public void saveFile(String file, String text) {
        try {
            FileOutputStream fos = openFileOutput(file, Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
            Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getFileStreamPath(file).toString(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception err) {
            err.printStackTrace();
            Toast.makeText(this, "Error Saving data!", Toast.LENGTH_SHORT).show();
        }
    }

    public String readFile(String file) {
        String text = "";

        try {
            FileInputStream fis = openFileInput(file);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            text = new String(buffer);
        }
        catch (Exception err) {
            err.printStackTrace();
            Toast.makeText(this, "Error Reading data!", Toast.LENGTH_SHORT).show();
        }

        return text;
    }

}