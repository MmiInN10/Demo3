package com.live2d.demo.schedule;

import android.os.Bundle;
import android.util.Log;
import com.live2d.demo.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.live2d.demo.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.FirebaseApp;
import com.google.android.material.navigation.NavigationBarView;

public class ScheduleActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "ScheduleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        // 뷰 바인딩 초기화 후 레이아웃 설정
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 하단 바 설정
        setBottomNavigationView();

        // 처음 실행 시 홈 화면 표시
        if (savedInstanceState == null) {
            binding.bottomNavigationView.setSelectedItemId(R.id.fragment_home);
        }
    }

    private void setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.fragment_home) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, new HomeFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.fragment_calendar) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, new CalendarFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.fragment_settings) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, new SettingFragment())
                            .commit();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
}
