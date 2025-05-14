package com.live2d.demo.schedule.ui.theme;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.live2d.demo.R;

public class TypographyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // 이곳에 XML을 지정하지 않고도 할 수 있으나, 다른 XML을 사용하시면 됩니다.

        // 직접 TextView 생성 및 스타일 적용
        TextView bodyLargeTextView = new TextView(this);

        // bodyLarge 스타일을 수동으로 설정
        bodyLargeTextView.setText("Hello, World!");
        bodyLargeTextView.setTextSize(16);  // sp 단위로 설정
        bodyLargeTextView.setLineSpacing(4f, 1);  // Line height 설정 (sp로 비례)
        bodyLargeTextView.setLetterSpacing(0.5f); // 문자 간격 설정 (sp로 설정)

        // 화면에 추가
        setContentView(bodyLargeTextView);
    }
}
