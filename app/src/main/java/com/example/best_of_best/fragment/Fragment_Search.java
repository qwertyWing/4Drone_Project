package com.example.best_of_best.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.login_member;

public class Fragment_Search extends Fragment {

//    private View view;
//    private ImageButton ultra_image;
    private login_member mem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mem = (login_member) bundle.getSerializable("mem");
            // mem 객체를 활용하여 작업 수행
            Toast.makeText(getActivity(), "ID: " + mem.getId(), Toast.LENGTH_SHORT).show();
        }

//        Button btn = (Button) view.findViewById(R.id.bt_signId);
        Intent intent = new Intent(getActivity(), Fragment_mlkit_poseDetect.class);
        intent.putExtra("id", mem.getId());
        startActivity(intent);


        return view;
    }
    public static Fragment_Search newInstance(login_member mem) {
        Fragment_Search fragment = new Fragment_Search();
        Bundle args = new Bundle();
        args.putSerializable("mem", mem);
        fragment.setArguments(args);

        return fragment;
    }
}