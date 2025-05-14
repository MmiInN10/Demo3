package com.live2d.demo.schedule;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.live2d.demo.R;

import androidx.fragment.app.Fragment;


public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fragment_home 레이아웃을 inflate해서 반환
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
