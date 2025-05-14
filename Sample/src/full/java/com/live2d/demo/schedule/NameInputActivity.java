package com.live2d.demo.schedule;

import com.live2d.demo.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;



public class NameInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_input);

        EditText etName = findViewById(R.id.etName);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString();
            if (!name.isEmpty()) {
                // SharedPreferences에 데이터 저장
                Context context = getApplicationContext();
                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstLaunch", false)
                        .putString("userName", name)
                        .apply();

                // MainActivity로 이동
                Intent intent = new Intent(NameInputActivity.this, ScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
