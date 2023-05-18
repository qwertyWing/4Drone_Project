package com.example.best_of_best.login_file;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.login_member;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class login_pwsearch extends AppCompatActivity {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private login_member ori_data;

    private String id;
    private String name;
    private String email;

    private String ori_id;
    private String ori_name;
    private String ori_birth;
    private String ori_pw;
    private String ori_email;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_pwsearch);

        EditText ed_pwName, ed_pwId, ed_pwmail;

        ed_pwName = findViewById(R.id.ed_pwName);
        ed_pwId = findViewById(R.id.ed_pwId);
        ed_pwmail = findViewById(R.id.ed_pwmail);
        Button bt_search = findViewById(R.id.bt_search);


        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = ed_pwName.getText().toString();
                id = ed_pwId.getText().toString();
                email = ed_pwmail.getText().toString();

                if(!name.equals("") || !id.equals("") || !email.equals("")){
                    mRootRef.child("member").addValueEventListener(postListener);
                }else{
                    Toast.makeText(getApplicationContext(), "정보를 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    ValueEventListener postListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI

            boolean found = false;

            for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                login_member mem = snapshot1.getValue(login_member.class);

                ori_id = mem.getId();
//                Toast.makeText(getApplicationContext(), ori_id + ", " + id, Toast.LENGTH_SHORT).show();
                if(id.equals(ori_id)) {
//                    Toast.makeText(getApplicationContext(), ori_id + ", " + id, Toast.LENGTH_SHORT).show();
                    ori_name = mem.getName();
                    ori_pw = mem.getPw();
                    ori_birth = mem.getBirth();
                    ori_email = mem.getEmail();

                    found = true;
                    break;
                }

            }
            if (!found) {
                Toast.makeText(getApplicationContext(), "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
            }else{
                ori_data = new login_member(ori_name, ori_id, ori_pw, ori_email, ori_birth);
                Intent intent = new Intent(getApplicationContext(), login_new_pw.class);
                intent.putExtra("object", ori_data);
                startActivity(intent);
                finish();
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        }

    };

}