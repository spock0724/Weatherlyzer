package com.example.weatherlyzerapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Calendar;

public class AddEventActivity extends ComponentActivity {

    private EditText eventTitleEditText;
    private EditText eventLocationEditText;
    private EditText eventDateEditText;
    private Button saveEventButton;
    private Button exitButton;

    private long selectedDateTimeMillis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addevent);
        setupViews();
        setupButtons();
    }

    private void setupViews() {
        eventTitleEditText = findViewById(R.id.eventTitle);
        eventLocationEditText = findViewById(R.id.location);
        eventDateEditText = findViewById(R.id.dateStart);
    }

    private void setupButtons() {
        exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        eventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
        saveEventButton = findViewById(R.id.completeEvent);
        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = eventTitleEditText.getText().toString();
                String location = eventLocationEditText.getText().toString();

                if (!title.isEmpty() && !location.isEmpty() && selectedDateTimeMillis != 0) {
                    saveEventToDatabase(title, location, selectedDateTimeMillis); // Save the event to the database
                    finish();
                }
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date to the Calendar object
                        calendar.set(year, monthOfYear, dayOfMonth);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        // Set the selected time to the Calendar object
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        // Convert to milliseconds
                                        selectedDateTimeMillis = calendar.getTimeInMillis();

                                        // Display/send
                                        eventDateEditText.setText(calendar.getTime().toString());
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveEventToDatabase(String title, String location, long startTimeMillis) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference eventsRef = database.getReference().child("users").child(userId).child("events");

            // Generate a new unique key as the event ID
            String eventId = eventsRef.push().getKey();

            Event event = new Event(title, location, startTimeMillis);

            eventsRef.child(eventId).setValue(event);
        }
    }
}