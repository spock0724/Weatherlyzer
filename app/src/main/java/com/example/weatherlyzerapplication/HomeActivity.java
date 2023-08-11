package com.example.weatherlyzerapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.ComponentActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.ArrayList;


public class HomeActivity extends ComponentActivity {

    private Button profileButton;
    private Button addeventButton;
    private TextView tempTextView;
    private TextView windSpeedTextView;
    private TextView rainPercentageTextView;
    private TextView locationNameTextView;
    private ImageView weatherIconImageView;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;


    private boolean weatherDataFetched = false;


    //Addevent stuff under
    private long userId;
    private ListView eventListView;
    private ArrayList<Event> eventList = new ArrayList<>();
    private EventListAdapter eventListAdapter;

    //private ArrayAdapter<Event> eventAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        eventListView = findViewById(R.id.eventListView);
        eventListAdapter = new EventListAdapter(this, eventList);
        eventListView.setAdapter(eventListAdapter);

        // TODO Add a click listener to the ListView
        /*
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = eventList.get(position);
                String eventId = selectedEvent.getEventId();

                // Show a delete confirmation AlertDialog with eventId as the tag
                showDeleteConfirmationDialog(eventId);
            }
        });
         */


        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = eventList.get(position);
                String eventId = selectedEvent.getEventId();

                Intent intent = new Intent(HomeActivity.this, ViewEventActivity.class);
                intent.putExtra("event_id", eventId);
                startActivity(intent);
            }
        });

        //Addeventstuff under
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getLongExtra("user_id", -1); // -1 is the default value if the user ID is not provided
        }
        eventList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(this, eventList);

        // Set the adapter to the ListView
        eventListView = findViewById(R.id.eventListView);
        eventListView.setAdapter(eventListAdapter);

        // Fetch events from the database and add them to the eventList
        fetchEventsFromDatabase();

        setupViews();
        setupButtons();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!weatherDataFetched) {
                    fetchWeatherData(location.getLatitude(), location.getLongitude());
                    weatherDataFetched = true; // Set the flag to true after the first fetch
                }
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }


            @Override
            public void onProviderEnabled(String provider) {
            }


            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        String greetingMessage;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greetingMessage = "Good Morning!";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greetingMessage = "Good Afternoon!";
        } else {
            greetingMessage = "Good Evening!";
        }
        TextView greetingTextView = findViewById(R.id.greetingMessage);
        greetingTextView.setText(greetingMessage);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }


    private void setupViews() {
        tempTextView = findViewById(R.id.temp);
        windSpeedTextView = findViewById(R.id.windSpeed);
        rainPercentageTextView = findViewById(R.id.rainPercentage);
        locationNameTextView = findViewById(R.id.eventLocation);
        weatherIconImageView = findViewById(R.id.weatherIcon);
    }


    private void setupButtons() {
        profileButton = findViewById(R.id.profileButton);
        addeventButton = findViewById(R.id.addeventButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        addeventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
                intent.putExtra("user_id", userId); // Pass the user ID
                startActivityForResult(intent, 1);
            }
        });
    }


    private void showDeleteConfirmationDialog(final String eventId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed the delete, so delete the event from the list and the database
                deleteEvent(eventId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void deleteEvent(String eventId) {
        // Remove the event from the list
        int eventIndex = findEventIndexById(eventId);
        if (eventIndex != -1) {
            eventList.remove(eventIndex);
            eventListAdapter.notifyDataSetChanged();
        }

        // Delete the event from the Firebase database
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // Return if the user is not logged in
        }

        String userId = currentUser.getUid();

        // Get a reference to the events node in the Firebase Database for the current user
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("events");

        // Remove the event with the given eventId from the database
        eventsRef.child(eventId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Event deleted successfully from the database
                    Log.d("DeleteEvent", "Event deleted successfully.");
                } else {
                    // Handle error if event deletion from the database fails
                    Log.e("DeleteEventError", "Error deleting event: " + task.getException());
                }
            }
        });
    }


    private int findEventIndexById(String eventId) {
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getEventId().equals(eventId)) {
                return i;
            }
        }
        return -1;
    }


    // Sort the eventList based on their start time in ascending order
    /*
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return Long.compare(event1.getStartTimeMillis(), event2.getStartTimeMillis());
            }
        });

        // Notify the adapter that the data has changed, and the ListView will reorder the events
        eventListAdapter.notifyDataSetChanged();
    }
     */


    private void fetchEventsFromDatabase() {
        // Get the current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // Return if the user is not logged in
        }

        String userId = currentUser.getUid();

        // Get a reference to the events node in the Firebase Database for the current user
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("events");

        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear(); // Clear the existing list before adding new events

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    // Get the event ID from the Firebase snapshot key
                    String eventId = eventSnapshot.getKey();

                    // Retrieve the event details from the snapshot value
                    Event event = eventSnapshot.getValue(Event.class);

                    // Set the event ID in the Event object
                    event.setEventId(eventId);

                    if (event != null) {
                        eventList.add(event);
                    }
                }

                // Notify the adapter that the data has changed
                eventListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO Handle database read error
            }
        });

    }


    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = "aa77259b5b6b4c988ee212953230408";
        String apiUrl = "http://api.weatherapi.com/v1/current.json?key=" + apiKey +
                "&q=" + latitude + "," + longitude +
                "&units=imperial"; //imperial(US)units in the API request

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

                    if (jsonObject.has("current")) {
                        JSONObject current = jsonObject.getJSONObject("current");
                        double temperatureFahrenheit = current.getDouble("temp_f");
                        double windSpeedMph = current.getDouble("wind_mph");
                        double rainInches = current.getDouble("precip_in");
// TODO fix
//                      String weatherConditionCode = current.getString("condition:text");
                        int weatherConditionCode = current.getJSONObject("condition").getInt("code");

                        JSONObject location = jsonObject.getJSONObject("location");
                        String cityName = location.getString("name");
// TODO fix
                        Drawable weatherIcon = getWeatherIcon(weatherConditionCode);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempTextView.setText(temperatureFahrenheit + "°F");
                                windSpeedTextView.setText(windSpeedMph + " mph");
                                rainPercentageTextView.setText(rainInches + " in/hr");
                                locationNameTextView.setText("Current Location: " + cityName);
// TODO fix
                                weatherIconImageView.setImageDrawable(weatherIcon);

                                displayAttireRecommendation(temperatureFahrenheit, rainInches, weatherConditionCode);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempTextView.setText("Weather data not available.");
                                windSpeedTextView.setText("");
                                rainPercentageTextView.setText("");
                            }
                        });
                    }

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


    //TODO make more comprehensive
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


    //Addevent stuff below
    public void addEventToList(Event event) {
        eventList.add(event);
        eventListAdapter.notifyDataSetChanged();
    }


    private String getAttireMessage(int weatherConditionCode, double temperatureFahrenheit, double rainInches) {
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
                if(temperatureFahrenheit<73) {
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
                if (rainInches > 0.1) {
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
                if (temperatureFahrenheit < 32) {
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


    private void displayAttireRecommendation(double temperatureFahrenheit, double rainInches, int weatherConditionCode ) {
        String tempMsg;
        String rainMsg;
        String codeMsg;

        if (temperatureFahrenheit > 100) {
            tempMsg = "Extreme heat! It's";
        } else if (temperatureFahrenheit >= 70) {
            tempMsg = "It's warm and";
        } else if (temperatureFahrenheit >= 50) {
            tempMsg = "It's a bit cool and";
        } else if (temperatureFahrenheit >= 33) {
            tempMsg = "It's cold and";
        } else if (temperatureFahrenheit >= 0) {
            tempMsg = "Extreme Freezing! It's";
        } else {
            tempMsg = "";
        }

        if(rainInches>0.1){
            rainMsg = " Rain expected. ";
        } else {
            rainMsg = " ";
        }

        codeMsg = getAttireMessage(weatherConditionCode, temperatureFahrenheit, rainInches);


        TextView attireRecommendationTextView = findViewById(R.id.attireRecommendation);
        attireRecommendationTextView.setText(tempMsg + rainMsg + codeMsg);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            long eventId = data.getLongExtra("event_id", -1);
            String title = data.getStringExtra("event_title");
            String placeId = data.getStringExtra("event_place_id");
            long startTimeMillis = data.getLongExtra("event_start_time", 0);

            Event event = new Event(title, placeId, startTimeMillis, this);

            addEventToList(event);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                Log.d("LocationPermission", "Location permission denied.");
            }
        }
    }
}
