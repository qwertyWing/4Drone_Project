package com.example.best_of_best.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.best_of_best.R;
import com.example.best_of_best.login;
import com.example.best_of_best.db_java.login_member;

public class Fragment_myPage extends Fragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        TextView mp_1 = (TextView) view.findViewById(R.id.mP_1);
        TextView mp_2 = (TextView) view.findViewById(R.id.mP_2);
        TextView mp_3 = (TextView) view.findViewById(R.id.mP_3);
        TextView mp_4 = (TextView) view.findViewById(R.id.mP_4);

        Bundle bundle = getArguments();
        if (bundle != null) {
            login_member mem = (login_member) bundle.getSerializable("mem");

            mp_1.setText("이름 : " + mem.getName());
            mp_2.setText("이메일 : " + mem.getEmail());
            mp_3.setText("생년월일 : " + mem.getBirth());
            mp_4.setText("비밀번호 : " + mem.getPw());
            // 가져온 값들을 활용하여 작업 수행
            Toast.makeText(getActivity(), "Name: " + mem.getName() + ", ID: " + mem.getId(), Toast.LENGTH_SHORT).show();
        }

        Button btn = (Button) view.findViewById(R.id.logout);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), login.class));
                getActivity().finish();
            }
        });

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Hello World", Toast.LENGTH_SHORT).show();
//            }
//        });

        return view;
    }
    public static Fragment_myPage newInstance(login_member mem) {
        Fragment_myPage fragment = new Fragment_myPage();
        Bundle args = new Bundle();
        args.putSerializable("mem", mem);
        fragment.setArguments(args);

        return fragment;
    }
}
