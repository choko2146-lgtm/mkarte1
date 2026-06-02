package com.example.mkarte1;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.ui.camera.CameraActivity;
import com.example.mkarte1.ui.calendar.CalendarActivity;
import com.example.mkarte1.ui.customer.CustomerListActivity;
import com.example.mkarte1.ui.customer.CustomerRegisterActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonCamera).setOnClickListener(v ->
                startActivity(new Intent(this, CameraActivity.class)));
        findViewById(R.id.buttonCustomerRegister).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerRegisterActivity.class)));
        findViewById(R.id.buttonCustomerList).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerListActivity.class)));
        findViewById(R.id.buttonCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class)));
        findViewById(R.id.buttonPhotoList).setOnClickListener(v ->
                startActivity(new Intent(this, PhotoListActivity.class)));
    }
}
