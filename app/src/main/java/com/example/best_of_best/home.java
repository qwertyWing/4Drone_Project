package com.example.best_of_best;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.best_of_best.db_java.login_member;
import com.example.best_of_best.fragment.Fragment_Home;
import com.example.best_of_best.fragment.Fragment_Search;
import com.example.best_of_best.fragment.Fragment_myPage;
import com.google.android.material.navigation.NavigationBarView;

public class home extends AppCompatActivity {

    private Fragment_Home homeFragment;
    private Fragment_Search fragment_search;
    private Fragment_myPage myPageFragment;

    private long backpressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment);

        Intent intent = getIntent();
        login_member mem = (login_member) intent.getSerializableExtra("object");
        Toast.makeText(getApplicationContext(), "dfff"+mem.getId(), Toast.LENGTH_SHORT).show();

        homeFragment = Fragment_Home.newInstance(mem);
        fragment_search = Fragment_Search.newInstance(mem);
        myPageFragment = Fragment_myPage.newInstance(mem);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, homeFragment).commit();


        NavigationBarView navigationBarView = findViewById(R.id.bottomNavi);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId()){
                    case R.id.home:
//                        Fragment_Home homeFragment = Fragment_Home.newInstance(mem);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, homeFragment).commit();
                        return true;
                    case R.id.search:
//                        Fragment_Search fragment_search = Fragment_Search.newInstance(mem);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, fragment_search).commit();
                        return true;
                    case R.id.myPage:
//                        Fragment_myPage myPageFragment = Fragment_myPage.newInstance(mem);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, myPageFragment).commit();

                        return true;
                }
                return false;
            }
        });
    }

    public void onBackPressed(){
        if (System.currentTimeMillis() > backpressedTime + 2000) {
            backpressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
            finish();
        }
    }

}