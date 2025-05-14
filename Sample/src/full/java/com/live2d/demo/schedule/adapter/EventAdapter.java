package com.live2d.demo.schedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.live2d.demo.R;
import com.google.api.services.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener onItemClick;

    public EventAdapter(List<Event> eventList, OnEventClickListener onItemClick) {
        this.eventList = eventList;
        this.onItemClick = onItemClick;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.titleTextView.setText(event.getSummary() != null ? event.getSummary() : "(제목 없음)");

        // 시작/종료 시간 설정
        Date start = new Date(event.getStart().getDateTime() != null ?
                event.getStart().getDateTime().getValue() : event.getStart().getDate().getValue());
        Date end = new Date(event.getEnd().getDateTime() != null ?
                event.getEnd().getDateTime().getValue() : event.getEnd().getDate().getValue());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String startStr = formatter.format(start);
        String endStr = formatter.format(end);

        holder.timeTextView.setText(startStr + " ~ " + endStr);

        // 클릭 이벤트 연결
        holder.itemView.setOnClickListener(v -> onItemClick.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView timeTextView;

        public EventViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.event_title);
            timeTextView = view.findViewById(R.id.event_time);
        }
    }
}
