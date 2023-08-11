package com.example.weatherlyzerapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

public class ViewEventActivity extends AppCompatActivity {

    private TextView eventNameTextView;
    private TextView locationTextView;
    private TextView startTimeTextView;
    private TextView tempTextView;

    private TextView lowTempTextView;
    private TextView attireRecTextView;
    private TextView highTempTextView;
    private TextView windSpeedTextView;
    private TextView rainPercentageTextView;

    private ImageView weatherIconImageView;

    private Button backButton;
    private Button deleteButton;

    private Drawable weatherIcon;
    private Event event;
    private String eventId;
    private DatabaseReference eventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getSupportActionBar().hide();
        setContentView(R.layout.viewevent);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        eventId = getIntent().getStringExtra("event_id");
        String userId = currentUser.getUid();
        eventRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("events").child(eventId);
        Log.d("ViewEventActivity", "Received Event ID: " + eventId);

        eventNameTextView = findViewById(R.id.eventNameTextView);
        locationTextView = findViewById(R.id.eventLocation);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        lowTempTextView = findViewById(R.id.lowTemp);
        highTempTextView = findViewById(R.id.highTemp);
        windSpeedTextView = findViewById(R.id.windSpeed);
        rainPercentageTextView = findViewById(R.id.rainPercentage);
        attireRecTextView = findViewById(R.id.attireReco);

        weatherIconImageView = findViewById(R.id.weatherIcon);
        backButton = findViewById(R.id.backButton2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Button deleteButton = findViewById(R.id.deleteEventButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
            }
        });

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("ViewEventActivity", "Event data snapshot key: " + dataSnapshot.getKey());
                    Log.d("ViewEventActivity", "Event data snapshot value: " + dataSnapshot.getValue());

                    Event event = dataSnapshot.getValue(Event.class);
                    if (event != null) {
                        Log.d("ViewEventActivity", "Event data from Firebase: " + event.toString());
                        eventNameTextView.setText(event.getTitle());
                        startTimeTextView.setText(event.getStartTimeAsString());
                        //temp under
                        String eventDate = event.getStartTimeAsStringForecast();

                        fetchAndDisplayForecast(event.getLatitude(), event.getLongitude(), eventDate);

                    }
                } else {
                    Log.d("ViewEventActivity", "Event not found in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ViewEventActivity", "Database error: " + databaseError.getMessage());
            }
        });

    /*
        // Get the event details from the intent
        Event event = getIntent().getParcelableExtra("event");

        if (event != null) {
            eventNameTextView.setText(event.getTitle());
            locationTextView.setText(event.getLocationName());
            startTimeTextView.setText(event.getStartTimeAsString());

            // Fetch and display forecast data using the event's location
            fetchAndDisplayForecast(event.getLatitude(), event.getLongitude());
        }
     */
    }

    private void fetchAndDisplayForecast(double latitude, double longitude, String eventDate) {
        Log.d("fetchAndDisplayForecast", "Latitude: " + latitude + ", Longitude: " + longitude);
        String apiKey = "aa77259b5b6b4c988ee212953230408";
        String dt = eventDate; // Extract yyyy-MM-dd format date
        //String hour = eventHour; // Use the event's start hour
        //String dt = "2023-08-14"; // Example date in yyyy-MM-dd format
        String hour = "12"; // Example hour in 24-hour format

        String apiUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + apiKey +
                "&q=" + latitude + "," + longitude +
                "&dt=" + dt +
                "&hour=" + hour +
                "&units=imperial"; // Specify imperial(US) units in the API request

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufferedReader.close();
                    connection.disconnect();

                    // Log the raw JSON response for debugging
                    Log.d("WeatherData", "Raw JSON Response: " + response.toString());

                    JSONObject jsonObject = new JSONObject(response.toString());

                    // Get forecastday array
                    JSONArray forecastdayArray = jsonObject.getJSONObject("forecast").getJSONArray("forecastday");

                    // Get first forecastday object
                    JSONObject firstForecastDay = forecastdayArray.getJSONObject(0);

                    // Get day object within forecastday
                    JSONObject dayObject = firstForecastDay.getJSONObject("day");

                    double maxTempFahrenheit = dayObject.getDouble("maxtemp_f");
                    double minTempFahrenheit = dayObject.getDouble("mintemp_f");
                    double avgTempFahrenheit = dayObject.getDouble("avgtemp_f");
                    double maxWindMph = dayObject.getDouble("maxwind_mph");
                    double totalPrecipInches = dayObject.getDouble("totalprecip_in");

                    int avgHumidity = dayObject.getInt("avghumidity");
                    String weatherConditionText = dayObject.getJSONObject("condition").getString("text");
                    String weatherConditionIcon = dayObject.getJSONObject("condition").getString("icon");
                    int weatherConditionCode = dayObject.getJSONObject("condition").getInt("code");

                    JSONObject location = jsonObject.getJSONObject("location");
                    String cityName = location.getString("name");

                    Drawable weatherIcon = getWeatherIcon(weatherConditionCode);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("WeatherData", "Updating UI with forecast data");
                            highTempTextView.setText("H:" + maxTempFahrenheit + "°F");
                            lowTempTextView.setText("L:" + minTempFahrenheit + "°F");
                            windSpeedTextView.setText(maxWindMph + " mph");
                            rainPercentageTextView.setText(totalPrecipInches + " in/hr");
                            locationTextView.setText("Location Area: " + cityName);

                            displayAttireRecommendation(avgTempFahrenheit,totalPrecipInches,weatherConditionCode);
                            weatherIconImageView.setImageDrawable(weatherIcon);

                            Log.d("WeatherData", "UI updated with forecast data");
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Log.e("WeatherData", "Error fetching weather data: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tempTextView.setText("Error fetching weather data.");
                            windSpeedTextView.setText("");
                            rainPercentageTextView.setText("");
                        }
                    });
                }
            }
        }).start();
    }

    private void deleteEvent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("events");

            eventsRef.child(eventId).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Event deleted successfully, close the activity
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            Toast.makeText(ViewEventActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getAttireMessage(int weatherConditionCode, double avgTempFahrenheit, double totalPrecipInches) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        switch (weatherConditionCode) {
            case 1000:
                //sunny/clear (the only one that diffenent based on time
                if(hourOfDay >= 0 && hourOfDay < 18){
                    return "sunny. Dress lightly.";
                } else {
                    return "a clear night. Enjoy the stars!";
                }
            case 1003:
                if(avgTempFahrenheit<73) {
                    return "partly cloudy. You may need a light jacket.";
                } else {
                    return "partly cloudy.";
                }
            case 1006:
            case 1009:
                // Weather is cloudy or overcast
                return "cloudy outside. Don't forget an umbrella!";
            case 1063:
            case 1066:
            case 1069:
            case 1072:
            case 1180:
            case 1183:
            case 1186:
            case 1189:
            case 1192:
            case 1195:
                // Rainy weather conditions
                if (totalPrecipInches > 0.1) {
                    return "rain is expected. Don't forget your raincoat or umbrella!";
                } else {
                    return "light rain is possible. You may want to bring an umbrella.";
                }
            case 1114:
            case 1117:
            case 1135:
            case 1147:
                // Foggy or freezing fog conditions
                return "foggy. Drive carefully and wear warm clothing.";
            case 1210:
            case 1213:
            case 1216:
            case 1219:
            case 1222:
            case 1225:
                // Snowy conditions
                if (avgTempFahrenheit < 32) {
                    return "snowing and freezing outside. Dress warmly!";
                } else {
                    return "snow is expected. Don't forget your winter boots and warm clothing.";
                }
            case 1198:
            case 1201:
            case 1204:
            case 1207:
            case 1237:
            case 1240:
            case 1243:
            case 1246:
            case 1249:
            case 1252:
            case 1255:
            case 1258:
            case 1261:
            case 1264:
            case 1273:
            case 1276:
            case 1279:
            case 1282:
                // TODO ADD Other weather conditions(codes in assests folder from api site)
                return "Check the weather forecast for specific attire recommendations.";
            default:
                return "";
        }
    }

    private void displayAttireRecommendation(double avgTempFahrenheit, double totalPrecipInches, int weatherConditionCode ) {
        String tempMsg;
        String rainMsg;
        String codeMsg;

        if (avgTempFahrenheit > 100) {
            tempMsg = "There will be Extreme heat! It's";
        } else if (avgTempFahrenheit >= 70) {
            tempMsg = "It's going to be warm and";
        } else if (avgTempFahrenheit >= 50) {
            tempMsg = "It's going to be a bit cool and";
        } else if (avgTempFahrenheit >= 33) {
            tempMsg = "It's going to be cold and";
        } else if (avgTempFahrenheit >= 0) {
            tempMsg = "There will be Extreme Freezing! It's";
        } else {
            tempMsg = "";
        }

        if(totalPrecipInches>0.1){
            rainMsg = " Rain is expected. ";
        } else {
            rainMsg = " ";
        }

        codeMsg = getAttireMessage(weatherConditionCode, avgTempFahrenheit, totalPrecipInches);
        TextView attireRecTextView = findViewById(R.id.attireReco);
        String fullMessage = tempMsg + rainMsg + codeMsg;
        attireRecTextView.setText(fullMessage);
    }

    private Drawable getWeatherIcon(int weatherConditionCode) {
        switch (weatherConditionCode) {
            case 1000:
                // Weather is clear
                return getResources().getDrawable(R.drawable.weather_clear_icon);
            case 1003:
                // Weather is partly cloudy
                return getResources().getDrawable(R.drawable.weather_partly_cloudy_icon);
            case 1006:
                // Weather is cloudy
                return getResources().getDrawable(R.drawable.weather_cloudy_icon);
            case 1135:
                // Weather is foggy
                return getResources().getDrawable(R.drawable.weather_fog_icon);
            // Add more cases for other weather conditions as needed
            default:
                // Default weather icon if the condition code is not recognized
                return getResources().getDrawable(R.drawable.weather_default_icon);
        }
    }
}