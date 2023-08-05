package com.example.weatherlyzerapplication;

public class Event {
    private String title;
    private String location;
    private long startTimeMillis;

    public Event(String title, String location, long startTimeMillis) {
        this.title = title;
        this.location = location;
        this.startTimeMillis = startTimeMillis;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    @Override
    public String toString() {
        return " " + title + " - " + location;
    }
}
