package com.live2d.demo.schedule;

import com.live2d.demo.R;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.SignInButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Collections;

public class SettingFragment extends Fragment {

    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    private static final int REQUEST_ACCOUNT_PICKER = 1001;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // 사용자 이름 표시
        Context context = requireContext();
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("userName", "사용자");

        TextView greetingTextView = view.findViewById(R.id.textViewGreeting);
        greetingTextView.setText(userName + "님, 안녕하세요!");

        // 이름 수정 아이콘 클릭
        ImageView editIcon = view.findViewById(R.id.imageViewEdit);
        editIcon.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("이름 수정");

            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(userName);
            builder.setView(input);

            builder.setPositiveButton("확인", (dialog, which) -> {
                String newName = input.getText().toString();
                greetingTextView.setText(newName + "님, 안녕하세요!");
                prefs.edit().putString("userName", newName).apply();
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // Google 인증 초기화
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList(CalendarScopes.CALENDAR_READONLY)
        ).setBackOff(new ExponentialBackOff());

        SignInButton signInButton = view.findViewById(R.id.btnGoogleSignIn);
        signInButton.setOnClickListener(v -> signInWithGoogle());

        return view;
    }

    private void signInWithGoogle() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);

                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit().putString("accountName", accountName).apply();

                // 커스텀 토스트 표시
                LayoutInflater inflater = (LayoutInflater) requireContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View toastView = inflater.inflate(R.layout.custom_toast, null);

                TextView toastText = toastView.findViewById(R.id.toast_text);
                toastText.setText("계정 연동 완료");

                Toast toast = new Toast(requireContext());
                toast.setView(toastView);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public GoogleAccountCredential getCredential() {
        return mCredential;
    }
}
