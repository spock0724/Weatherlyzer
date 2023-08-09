package com.example.weatherlyzerapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    private Context context;
    private List<Event> eventList;

    public EventListAdapter(Context context, List<Event> eventList) {
        super(context, 0, eventList);
        this.context = context;
        this.eventList = eventList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        }

        Event currentEvent = eventList.get(position);

        TextView titleTextView = itemView.findViewById(R.id.eventTitleTextView);
        TextView locationTextView = itemView.findViewById(R.id.eventLocationTextView);
        TextView dateTextView = itemView.findViewById(R.id.eventDateTextView);

        titleTextView.setText(currentEvent.getTitle());
        locationTextView.setText(currentEvent.getLocationName()); // Use getLocationName() here

        dateTextView.setText(currentEvent.getStartTimeAsString());

        String formattedStartTime = currentEvent.getStartTimeAsString();

        dateTextView.setText(formattedStartTime);

        int paddingInDp = 8; // You can adjust this value to increase or decrease the space between events (NOT the words!!)
        int paddingInPx = (int) (paddingInDp * context.getResources().getDisplayMetrics().density);
        itemView.setPadding(0, 0, 0, paddingInPx);

        return itemView;
    }
}
