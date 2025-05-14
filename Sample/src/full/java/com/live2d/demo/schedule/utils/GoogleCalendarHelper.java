package com.live2d.demo.schedule.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GoogleCalendarHelper {

    private GoogleAccountCredential mCredential;
    private Calendar calendarService;
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/calendar"};

    public GoogleCalendarHelper(Context context) {
        initGoogleCalendar(context);
    }

    private void initGoogleCalendar(Context context) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(), java.util.Arrays.asList(SCOPES)
        );
        String accountName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("accountName", null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
        }

        try {
            calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    mCredential
            ).setApplicationName("My Calendar App").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertEvent(String title, DateTime startDateTime, DateTime endDateTime, boolean isAllDay, InsertEventCallback callback) {
        new Thread(() -> {
            try {
                Event event = new Event();
                event.setSummary(title);
                event.setReminders(null);

                EventDateTime start = new EventDateTime();
                if (isAllDay) {
                    start.setDate(startDateTime);
                } else {
                    start.setDateTime(startDateTime);
                }
                start.setTimeZone("Asia/Seoul");

                EventDateTime end = new EventDateTime();
                if (isAllDay) {
                    end.setDate(endDateTime);
                } else {
                    end.setDateTime(endDateTime);
                }
                end.setTimeZone("Asia/Seoul");

                event.setStart(start);
                event.setEnd(end);

                calendarService.events().insert("primary", event).execute();

                if (callback != null) callback.onSuccess();

            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                if (callback != null) callback.onFailure(e);
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) callback.onFailure(e);
            }
        }).start();
    }

    public interface InsertEventCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void updateEvent(String eventId, Event updatedEvent) {
        new Thread(() -> {
            try {
                calendarService.events().update("primary", eventId, updatedEvent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void deleteEvent(String eventId) {
        new Thread(() -> {
            try {
                calendarService.events().delete("primary", eventId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return new Date(); // 실패 시 현재 시간 반환
        }
    }
}
