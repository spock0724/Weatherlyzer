package com.example.weatherlyzerapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventTitleEditText;
    private TextView locationTextView;

    private String title;
    private EditText eventLocationEditText;
    private EditText eventDateEditText;
    private Button saveEventButton;
    private Button exitButton;
    private Button autocompleteButton;

    private long selectedDateTimeMillis;
    private Event event;
    private Double latitude;
    private Double longitude;

    private Place selectedPlace;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getSupportActionBar().hide();
        setContentView(R.layout.addevent);
        setupViews();
        setupButtons();

        String apiKey = "AIzaSyCfQ6LNv7Nv0b4gXiQJu5ufE22_nT6Pvvk";
        Places.initialize(getApplicationContext(), apiKey);

        // Initialize the event object
        event = new Event("", "", 0, getApplicationContext());
        //event.setOnPlaceFetchCompleteListener(this);

        // Call deleteExpiredEvents every 60000 (maybe thats too often we'll see)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                deleteExpiredEvents();
                handler.postDelayed(this, 60000); // 1 minute = 60000
            }
        }, 60000); //   60000 =1mins
    }

    private void setupViews() {
        eventTitleEditText = findViewById(R.id.eventTitle);
        //eventLocationEditText = findViewById(R.id.autocomplete_fragment);

        eventDateEditText = findViewById(R.id.dateStart);
        locationTextView = findViewById(R.id.locationTextView);
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
                String placeId = ""; //
                latitude = selectedPlace.getLatLng().latitude;
                longitude = selectedPlace.getLatLng().longitude;

                // Initialize placeId variable

                if (selectedPlace != null) {
                    placeId = selectedPlace.getId(); // Get the placeId from the selected Place object
                }

                if (!title.isEmpty() && !placeId.isEmpty()) {
                    saveEventToDatabase(title, placeId, latitude, longitude, selectedDateTimeMillis); // Pass placeId instead of Place
                    finish();
                } else {
                    Toast.makeText(AddEventActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        autocompleteButton = findViewById(R.id.autocompleteButton);

        autocompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity();
            }
        });
    }

    private void deleteExpiredEvents() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("events");

            // Get the current time in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            eventsRef.orderByChild("startTimeMillis").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        if (event != null && event.getStartTimeMillis() < currentTimeMillis) {
                            // Delete the event if its start time is in the past
                            eventSnapshot.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors if needed
                }
            });
        }
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
                        calendar.set(year, monthOfYear, dayOfMonth);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);
                                        selectedDateTimeMillis = calendar.getTimeInMillis();
                                        eventDateEditText.setText(calendar.getTime().toString());
                                    }
                                }, hour, minute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Handle the result of the autocomplete activity
                if (data != null) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    selectedPlace = place; // Assign the selected place to selectedPlace

                    // Get the latitude and longitude from selectedPlace and set them in the event
                    if (selectedPlace != null && selectedPlace.getLatLng() != null) {
                        double latitude = selectedPlace.getLatLng().latitude;
                        double longitude = selectedPlace.getLatLng().longitude;
                        event.setLatitude(latitude);
                        event.setLongitude(longitude);
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle errors that occurred in the autocomplete activity
                Status status = Autocomplete.getStatusFromIntent(data);
                if (status != null) {
                    Log.e("AutocompleteError", status.getStatusMessage());
                }
            }
        }
    }

    /*
    @Override
    public void onPlaceSelected(Place place) {
        // Handle the selected place here.
        Log.d("PlaceSelected", "Place selected: " + place.getName());
        eventLocationEditText.setText(place.getName());
        // Update the location TextView with the latitude and longitude
        LatLng latLng = place.getLatLng();
        if (latLng != null) {
            String latitudeLongitude = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
            locationTextView.setText(latitudeLongitude);
        }
    }

     */
    /*
    @Override
    public void onPlaceFetchComplete(String locationName) {
        locationTextView.setText(locationName);
    }


    @Override
    public void onError(Status status) {
        // Handle any errors that occurred during the autocomplete process here.
        Log.e("AutocompleteError", status.getStatusMessage());
    }
    */


    /*
    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startAutocompleteLauncher.launch(intent);
    }
    */

    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void saveEventToDatabase(String title, String placeId, Double latitude, Double longitude, long startTimeMillis) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference eventsRef = database.getReference().child("users").child(userId).child("events");

            String eventId = eventsRef.push().getKey();

            // Use selectedPlace object to get the city name
            String cityName = selectedPlace != null ? selectedPlace.getName() : "";

            Event event = new Event(title, placeId, startTimeMillis, getApplicationContext());

            // Update the location field with the city name
            event.setLocationName(cityName);

            // Get the latitude and longitude from selectedPlace and set them in the event
            if (selectedPlace != null && selectedPlace.getLatLng() != null) {
                latitude = selectedPlace.getLatLng().latitude;
                longitude = selectedPlace.getLatLng().longitude;
                event.setLatitude(latitude);
                event.setLongitude(longitude);
            }
            eventsRef.child(eventId).setValue(event);
        }
    }
}
