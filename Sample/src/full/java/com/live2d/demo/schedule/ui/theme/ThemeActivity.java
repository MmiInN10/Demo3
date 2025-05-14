package com.live2d.demo.schedule.ui.theme;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.live2d.demo.R;

public class ThemeActivity extends AppCompatActivity {

    private static final int DARK_MODE = 1;
    private static final int LIGHT_MODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 색상 테마를 동적으로 적용합니다.
        applyColorScheme(LIGHT_MODE);  // 예시로 LIGHT_MODE로 설정

        // UI 구성 - XML 없이 Java 코드로 처리
        TextView textView = new TextView(this);
        textView.setText("Hello, World!");
        textView.setTextSize(18);
        setContentView(textView);
    }

    public void applyColorScheme(int mode) {
        int primaryColor, secondaryColor, tertiaryColor;

        // 다크 모드와 라이트 모드에 따른 색상 설정
        if (mode == DARK_MODE) {
            primaryColor = ContextCompat.getColor(this, R.color.purple_80);
            secondaryColor = ContextCompat.getColor(this, R.color.purple_grey_80);
            tertiaryColor = ContextCompat.getColor(this, R.color.pink_80);
        } else {
            primaryColor = ContextCompat.getColor(this, R.color.purple_40);
            secondaryColor = ContextCompat.getColor(this, R.color.purple_grey_40);
            tertiaryColor = ContextCompat.getColor(this, R.color.pink_40);
        }

        // 텍스트 색상과 배경 색상 적용
        TextView textView = new TextView(this);
        textView.setTextColor(primaryColor);
        textView.setBackgroundColor(secondaryColor);
        setContentView(textView);
    }
}
