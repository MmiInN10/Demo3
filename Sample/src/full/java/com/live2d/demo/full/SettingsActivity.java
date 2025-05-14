package com.live2d.demo.full;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 간단한 텍스트로 설정화면 표시
        TextView textView = new TextView(this);
        textView.setText("일정관리");
        textView.setTextSize(24f);
        textView.setPadding(50, 200, 50, 100);
        setContentView(textView);
    }
}
