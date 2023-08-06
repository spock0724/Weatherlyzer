package com.example.weatherlyzerapplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@IgnoreExtraProperties
public class Event {
    private String title;
    private String placeId;
    private long startTimeMillis;
    private Place location; // Add the Place field

    private String locationName;

    public Event() {
        // Empty constructor required by Firebase to deserialize data
    }


    public Event(String title, String placeId, long startTimeMillis, Context context) {
        this.title = title;
        this.placeId = placeId;
        this.startTimeMillis = startTimeMillis;
        this.locationName = "";
        this.createPlaceFromId(placeId, context); // Fetch and set the location asynchronously
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    // Method to fetch place details using the placeId
    private void createPlaceFromId(String placeId, Context context) {
        // Initialize the Places SDK if not already initialized.
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyCfQ6LNv7Nv0b4gXiQJu5ufE22_nT6Pvvk");
        }

        // Fetch the place details using the placeId.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields).build();
        PlacesClient placesClient = Places.createClient(context);

        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FetchPlaceResponse response = task.getResult();
                    if (response != null) {
                        location = response.getPlace(); // Set the location when fetched
                    } else {
                        // Handle error if place is null.
                        Log.e("PlaceError", "Place not found");
                    }
                } else {
                    // Handle error if task was not successful.
                    Log.e("PlaceError", "Failed to fetch place details: " + task.getException());
                }
            }
        });
    }

    // Method to get the name of the location
    public String getLocationName(){
        if (placeId != null) {
            return "testtest";//lookup id and give me the name of the location;
        } else {
            return "Location not available";
        }
    }

    @Override
    public String toString() {
        return " " + title + " - " + getLocationName();
    }

    public String getStartTimeAsString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy @ HH:mm", Locale.getDefault());
        return sdf.format(new Date(startTimeMillis));
    }
}
