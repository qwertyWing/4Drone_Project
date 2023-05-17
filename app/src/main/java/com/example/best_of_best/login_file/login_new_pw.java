package com.example.best_of_best.login_file;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.best_of_best.R;
import com.example.best_of_best.login;
import com.example.best_of_best.db_java.login_member;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class login_new_pw extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_new_pw);

        Intent intent = getIntent();
        login_member mem = (login_member) intent.getSerializableExtra("object");

        TextView view_id = findViewById(R.id.view_id);
        view_id.setText("Id : " +mem.getId());

        EditText new_fpw = findViewById(R.id.ed_fpw);
        EditText new_spw = findViewById(R.id.ed_spw);

        Button submit = findViewById(R.id.bt_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str1 = new_fpw.getText().toString();
                String str2 = new_spw.getText().toString();

                if(str1.equals("") || str2.equals("")){
                    Toast.makeText(getApplicationContext(), "새로운 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }else if(!str1.equals(str2)){
                    Toast.makeText(getApplicationContext(), "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                }else{
                    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

                    if(mem == null) Toast.makeText(getApplicationContext(), "dfff", Toast.LENGTH_SHORT).show();
                    else{
                        mem.setPw(str1);

                        Map<String, Object> update = new HashMap<>();
                        update.put("/member/"+mem.getId()+"/", mem);


                        mRootRef.updateChildren(update);
                        Toast.makeText(getApplicationContext(), "비밀번호 변경 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), login.class);

                        startActivity(intent);
//                        startActivity(new Intent(getApplicationContext(), login.class));
                        finish();
                    }

                }
            }
        });
    }

}
