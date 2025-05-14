package com.live2d.demo.schedule;

import java.text.ParseException;
import com.live2d.demo.R;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.ParseException;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.live2d.demo.schedule.utils.GoogleCalendarHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private TextView tvSelectedDate, tvSelectedStartTime, tvSelectedEndTime, tvStartTime, tvEndTime, tvAlarmTime;
    private EditText etTitle;
    private ImageView btnUpdate, ivDateForward;
    private Switch switchTime, switchAlarm;
    private LinearLayout timePickerLayout, alarmSettingLayout;
    private int selectedDaysBefore = 0;
    private String selectedStartTime = "10:00", selectedEndTime = "11:00";
    private int selectedAlarmHour = 9, selectedAlarmMinute = 0;
    private boolean isAlarmSettingVisible = false;
    private String calendarEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // UI 초기화
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        etTitle = findViewById(R.id.editTextEventTitle);
        ivDateForward = findViewById(R.id.ivDateForward);
        switchTime = findViewById(R.id.switch_time);
        btnUpdate = findViewById(R.id.btn_confirm);
        timePickerLayout = findViewById(R.id.time_picker_layout);
        tvSelectedStartTime = findViewById(R.id.tv_selected_start_time);
        tvSelectedEndTime = findViewById(R.id.tv_selected_end_time);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        alarmSettingLayout = findViewById(R.id.alarm_setting_layout);
        tvAlarmTime = findViewById(R.id.tv_alarm_time);

        calendarEventId = getIntent().getStringExtra("eventId");
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String alarmHourKey = "alarm_hour_" + title + "_" + date;
        String alarmMinuteKey = "alarm_minute_" + title + "_" + date;

        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");
        final String prefsKey = "alarm_days_" + title + "_" + date;
        final int savedDaysBefore = PreferenceManager.getDefaultSharedPreferences(this).getInt(prefsKey, 0);
        selectedDaysBefore = savedDaysBefore;

        // UI 설정
        etTitle.setText(title);
        tvSelectedDate.setText(date);
        if (startTime != null && endTime != null) {
            switchTime.setChecked(true);
            timePickerLayout.setVisibility(View.VISIBLE);
            tvSelectedStartTime.setText(startTime);
            tvSelectedEndTime.setText(endTime);
            selectedStartTime = startTime;
            selectedEndTime = endTime;
        } else {
            switchTime.setChecked(false);
            timePickerLayout.setVisibility(View.GONE);
        }

        // 알림 설정
        int alarmHour = PreferenceManager.getDefaultSharedPreferences(this).getInt(alarmHourKey, -1);
        int alarmMinute = PreferenceManager.getDefaultSharedPreferences(this).getInt(alarmMinuteKey, -1);
        if (alarmHour != -1 && alarmMinute != -1) {
            selectedAlarmHour = alarmHour;
            selectedAlarmMinute = alarmMinute;
            tvAlarmTime.setText(String.format("%02d:%02d", alarmHour, alarmMinute));
            tvAlarmTime.setVisibility(View.VISIBLE);
            isAlarmSettingVisible = true;
            alarmSettingLayout.setVisibility(View.VISIBLE);
        } else {
            tvAlarmTime.setText("알림 시간 없음");
            tvAlarmTime.setVisibility(View.VISIBLE);
        }

        // 알림 시간 텍스트 클릭 시 수정 가능
        tvAlarmTime.setOnClickListener(v -> showAlarmTimePickerBottomSheet());

        tvStartTime.setOnClickListener(v -> showMaterialTimePicker(true));
        tvEndTime.setOnClickListener(v -> showMaterialTimePicker(false));
        ivDateForward.setOnClickListener(v -> showDatePickerDialog());

        btnUpdate.setOnClickListener(v -> updateEvent());

        findViewById(R.id.ivAlarmForward).setOnClickListener(v -> {
            isAlarmSettingVisible = !isAlarmSettingVisible;
            alarmSettingLayout.setVisibility(isAlarmSettingVisible ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.ivCancel).setOnClickListener(v -> finish());

        findViewById(R.id.ivDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("일정 삭제")
                    .setMessage("일정을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (calendarEventId != null) {
                            GoogleCalendarHelper helper = new GoogleCalendarHelper(this);
                            helper.deleteEvent(calendarEventId);
                            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }
    private void updateEvent() {
        String inputTitle = etTitle.getText().toString().isEmpty() ? "(제목 없음)" : etTitle.getText().toString();
        String inputDate = tvSelectedDate.getText().toString();
        boolean useTime = switchTime.isChecked();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        SimpleDateFormat formatter = useTime
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            start.setTime(formatter.parse(useTime ? inputDate + " " + selectedStartTime : inputDate));
            end.setTime(formatter.parse(useTime ? inputDate + " " + selectedEndTime : inputDate));
            if (!useTime) end.add(Calendar.DATE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Event event = new Event().setSummary(inputTitle);
        if (useTime) {
            event.setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(start.getTime())));
            event.setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(end.getTime())));
        } else {
            event.setStart(new EventDateTime().setDate(new com.google.api.client.util.DateTime(true, start.getTimeInMillis(), null)));
            event.setEnd(new EventDateTime().setDate(new com.google.api.client.util.DateTime(true, end.getTimeInMillis(), null)));
        }

        if (calendarEventId != null) {
            GoogleCalendarHelper helper = new GoogleCalendarHelper(this);
            helper.updateEvent(calendarEventId, event);

            // 알림 설정
            Calendar alarmCalendar = Calendar.getInstance();
            try {
                alarmCalendar.setTime(formatter.parse(inputDate));
                alarmCalendar.add(Calendar.DATE, -selectedDaysBefore);
                alarmCalendar.set(Calendar.HOUR_OF_DAY, selectedAlarmHour);
                alarmCalendar.set(Calendar.MINUTE, selectedAlarmMinute);
                alarmCalendar.set(Calendar.SECOND, 0);

                AlarmManagerHelper.setAlarm(this, inputTitle, alarmCalendar.getTimeInMillis());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt("alarm_hour_" + inputTitle + "_" + inputDate, selectedAlarmHour)
                .putInt("alarm_minute_" + inputTitle + "_" + inputDate, selectedAlarmMinute)
                .apply();
    }

    private void showAlarmTimePickerBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottomsheet_alarm_time_picker, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);

        NumberPicker daysBeforePicker = bottomSheetView.findViewById(R.id.number_picker_days_before);
        daysBeforePicker.setMinValue(0);
        daysBeforePicker.setMaxValue(30);

        TimePicker timePicker = bottomSheetView.findViewById(R.id.time_picker);
        Button btnConfirm = bottomSheetView.findViewById(R.id.btn_confirm_time);

        timePicker.setIs24HourView(true);

        btnConfirm.setOnClickListener(v -> {
            selectedDaysBefore = daysBeforePicker.getValue();
            selectedAlarmHour = timePicker.getHour();
            selectedAlarmMinute = timePicker.getMinute();
            tvAlarmTime.setText(String.format("%02d:%02d", selectedAlarmHour, selectedAlarmMinute));

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt("alarm_hour_" + etTitle.getText() + "_" + tvSelectedDate.getText(), selectedAlarmHour)
                    .putInt("alarm_minute_" + etTitle.getText() + "_" + tvSelectedDate.getText(), selectedAlarmMinute)
                    .apply();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText(formattedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showMaterialTimePicker(final boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
            String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
            if (isStart) {
                tvSelectedStartTime.setText(formattedTime);
                selectedStartTime = formattedTime;
            } else {
                tvSelectedEndTime.setText(formattedTime);
                selectedEndTime = formattedTime;
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
}
