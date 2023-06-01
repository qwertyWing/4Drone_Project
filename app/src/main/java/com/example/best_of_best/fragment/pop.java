package com.example.best_of_best.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.best_of_best.MainActivity;
import com.example.best_of_best.R;
import com.example.best_of_best.db_java.login_member;

public class pop extends Activity {

    EditText tset_edit;
    String user_id = "";
    String user_event = "";
    private EditText pop_event, pop_set, pop_count;
    private login_member mem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pop);

        //UI 객체생성
        tset_edit = (EditText)findViewById(R.id.pop_event);

        pop_event = findViewById(R.id.pop_event);
        pop_set = findViewById(R.id.pop_set);
        pop_count = findViewById(R.id.pop_count);


        //데이터 가져오기
        Intent intent = getIntent();
        user_id = intent.getStringExtra("id");
        user_event = intent.getStringExtra("event");
        tset_edit.setText(user_event);
        mem = (login_member) intent.getSerializableExtra("object");
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기

//        System.out.println(pop_event.getText() + ", " + pop_set.getText() + ", " + pop_count.getText());
//        String pop_ev = String.valueOf(pop_event.getText());
        try{

            String str = pop_event.getText().toString();
            int set = Integer.parseInt(pop_set.getText().toString());
            int count = Integer.parseInt(pop_count.getText().toString());

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("id", user_id);
            intent.putExtra("event", pop_event.getText().toString());
            intent.putExtra("set",  pop_set.getText().toString());
            intent.putExtra("count", pop_count.getText().toString());
            intent.putExtra("object", mem);
            startActivity(intent);
            finish();
        }catch (NumberFormatException  e){
            Toast.makeText(getApplicationContext(), "잘못된 입력입니다", Toast.LENGTH_SHORT).show();
        }
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        intent.putExtra("id", user_id);
//        intent.putExtra("event", String.valueOf(pop_event.getText()));
//        intent.putExtra("set", String.valueOf(pop_set.getText()));
//        intent.putExtra("count", String.valueOf(pop_count.getText()));
//        startActivity(intent);

//        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}