package com.example.best_of_best.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.login_member;

public class Fragment_Home extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            login_member mem = (login_member) bundle.getSerializable("mem");
            // mem 객체를 활용하여 작업 수행
            Toast.makeText(getActivity(), "ID: " + mem.getId(), Toast.LENGTH_SHORT).show();
        }

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public static Fragment_Home newInstance(login_member mem) {
        Fragment_Home fragment = new Fragment_Home();
        Bundle args = new Bundle();
        args.putSerializable("mem", mem);
        fragment.setArguments(args);
        return fragment;
    }

}
