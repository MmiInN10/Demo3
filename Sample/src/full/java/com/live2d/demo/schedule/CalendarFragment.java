package com.live2d.demo.schedule;

import com.live2d.demo.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.live2d.demo.schedule.adapter.EventAdapter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate;
    private TextView tvEvents;
    private EventAdapter adapter;
    private ImageView btnAdd;
    private final List<Event> eventList = new ArrayList<>();

    private GoogleAccountCredential mCredential;
    private final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private Calendar currentSelectedDate = Calendar.getInstance();

    private static final int REQUEST_ADD_EVENT = 2001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.eventRecyclerView);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvEvents = view.findViewById(R.id.tvEvents);
        btnAdd = view.findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddActivity.class);
            startActivityForResult(intent, REQUEST_ADD_EVENT);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                DateTime startDateTime = event.getStart().getDateTime() != null ?
                        event.getStart().getDateTime() : event.getStart().getDate();
                DateTime endDateTime = event.getEnd().getDateTime() != null ?
                        event.getEnd().getDateTime() : event.getEnd().getDate();

                Date startDate = new Date(startDateTime.getValue());
                Date endDate = new Date(endDateTime.getValue());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                String startTimeStr = timeFormat.format(startDate);
                String endTimeStr = timeFormat.format(endDate);
                String dateStr = dateFormat.format(startDate);

                Intent editIntent = new Intent(requireContext(), EditActivity.class);
                editIntent.putExtra("eventId", event.getId());
                editIntent.putExtra("title", event.getSummary() != null ? event.getSummary() : "");
                editIntent.putExtra("date", dateStr);
                editIntent.putExtra("startTime", startTimeStr);
                editIntent.putExtra("endTime", endTimeStr);
                startActivity(editIntent);
            }
        });
        recyclerView.setAdapter(adapter);

        mCredential = GoogleAccountCredential.usingOAuth2(
                requireContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        String accountName = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("accountName", null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
        }

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            currentSelectedDate = Calendar.getInstance();
            currentSelectedDate.set(date.getYear(), date.getMonth(), date.getDay());

            int dayOfWeek = currentSelectedDate.get(Calendar.DAY_OF_WEEK);
            String dayOfWeekStr;
            switch (dayOfWeek) {
                case Calendar.SUNDAY: dayOfWeekStr = "일요일"; break;
                case Calendar.MONDAY: dayOfWeekStr = "월요일"; break;
                case Calendar.TUESDAY: dayOfWeekStr = "화요일"; break;
                case Calendar.WEDNESDAY: dayOfWeekStr = "수요일"; break;
                case Calendar.THURSDAY: dayOfWeekStr = "목요일"; break;
                case Calendar.FRIDAY: dayOfWeekStr = "금요일"; break;
                case Calendar.SATURDAY: dayOfWeekStr = "토요일"; break;
                default: dayOfWeekStr = ""; break;
            }

            tvSelectedDate.setText(
                    String.format("%d년 %d월 %d일 %s", date.getYear(), date.getMonth() + 1, date.getDay(), dayOfWeekStr)
            );
            fetchEvents(currentSelectedDate);
        });

        Calendar today = Calendar.getInstance();
        currentSelectedDate = today;
        calendarView.setSelectedDate(today);
        fetchEvents(today);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_EVENT && resultCode == Activity.RESULT_OK) {
            fetchEvents(currentSelectedDate);
        }
    }

    private void fetchEvents(final Calendar date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            mCredential
                    ).setApplicationName("My Calendar App").build();

                    TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");

                    Calendar startCalendar = Calendar.getInstance(seoulTimeZone);
                    startCalendar.setTime(date.getTime());
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);

                    Calendar endCalendar = (Calendar) startCalendar.clone();
                    endCalendar.add(Calendar.DAY_OF_MONTH, 1);

                    DateTime startDateTime = new DateTime(startCalendar.getTime(), seoulTimeZone);
                    DateTime endDateTime = new DateTime(endCalendar.getTime(), seoulTimeZone);

                    List<Event> events = service.events().list("primary")
                            .setTimeMin(startDateTime)
                            .setTimeMax(endDateTime)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()
                            .getItems();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            eventList.clear();
                            eventList.addAll(events);
                            adapter.notifyDataSetChanged();
                            tvEvents.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
                } catch (GooglePlayServicesAvailabilityIOException e) {
                    Log.e("CalendarAPI", "Google Play Services 오류", e);
                } catch (UserRecoverableAuthIOException e) {
                    Log.e("CalendarAPI", "UserRecoverableAuthIOException 발생", e);
                } catch (Exception e) {
                    Log.e("CalendarAPI", "이벤트 가져오기 오류", e);
                }
            }
        }).start();
    }
}

