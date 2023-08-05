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

        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = eventTitleEditText.getText().toString();
                String location = eventLocationEditText.getText().toString();

                if (!title.isEmpty() && !location.isEmpty() && selectedDateTimeMillis != 0) {
                    addEventToCalendar(title, location, selectedDateTimeMillis);
                }
            }
        });
    }

    private void setupViews() {
        eventTitleEditText = findViewById(R.id.eventTitle);
        eventLocationEditText = findViewById(R.id.location);
        eventDateEditText = findViewById(R.id.dateStart);
        saveEventButton = findViewById(R.id.completeEvent);
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
    }
//TODO add events to database by userid
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

                                        // Convert
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
//TODO fix cal data output for weather
    private void addEventToCalendar(String title, String location, long startTimeMillis) {

        Event event = new Event(title, location, startTimeMillis);

        // Update tin HomeActivity

        Intent intent = new Intent();
        intent.putExtra("event_location", location);
        intent.putExtra("event_title", title);
        intent.putExtra("event_start_time", startTimeMillis);
        setResult(RESULT_OK, intent);
        //may delete and do back to home if dont work
        finish();
    }
}
