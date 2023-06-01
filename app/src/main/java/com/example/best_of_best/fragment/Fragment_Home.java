package com.example.best_of_best.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.db_daily;
import com.example.best_of_best.db_java.login_member;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Fragment_Home extends Fragment {

    private ImageView Image_pullup, Image_squrt, Image_pushup;
    //선 그래프
    private BarChart barChart;
    private BarDataSet barDataSet;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private Queue<String> q = new LinkedList<>();
    private Map<String, String> map2 = new HashMap<>();
    private static BarData barData = new BarData();
    private static float index = 0;

    private login_member mem;

    private int[] ColorArray = new int[]{Color.RED,Color.YELLOW,Color.GRAY};
    private String[] labels = {"Push_up", "Pull_up", "Sqrt"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mem = (login_member) bundle.getSerializable("mem");
            // mem 객체를 활용하여 작업 수행
//            Toast.makeText(getActivity(), "ID : " + mem.getId(), Toast.LENGTH_SHORT).show();
        }



        barChart = view.findViewById(R.id.chart);
        ArrayList<BarEntry> entry_chart = new ArrayList<>();
        System.out.println(mem.getId());

        barChart.clear();
        barData.clearValues();
        XAxis xAxis = barChart.getXAxis();

//        barDataSet.clear();
//        firebase_now_Data();
//        String now = LocalDate.now().toString();
        mRootRef.child("daily").child(mem.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                for(DataSnapshot ds : snapshot1.getChildren()){
                    String str = ds.getKey();
//                    firebase_now_Data(str);
                    q.offer(str);
                    System.out.println(str + ", " +q + ", " + q.size());
//                    System.out.println(str + ", " + q.size());
                    if (q.size() >= 6) {
                        q.poll();
                    }
                }

                int q_size = q.size();
                for(int i=q_size; i<=5; i++){
//                    entry_chart.add(new BarEntry(index, 0));
                    q.offer("0");
                }

                xAxis.setValueFormatter(new IndexAxisValueFormatter(q)); // 라벨 값 설정


                while(!q.isEmpty()){
                    String now = q.poll();

                    mRootRef.child("daily").child(mem.getId()).child(now).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){

                                db_daily Db_daily = ds.getValue(db_daily.class);

                                map2.put(Db_daily.getEvent(), Db_daily.getCount());
//                                if(Db_daily.getEvent() == )

                            }
                            System.out.println(map2);

                            float value[] = new float[3];
                            value[0] = map2.containsKey("Push_up") ? Float.parseFloat(map2.get("Push_up")) : 0;
                            value[1] = map2.containsKey("Pull_up") ? Float.parseFloat(map2.get("Pull_up")) : 0;
                            value[2] = map2.containsKey("Sqrt") ? Float.parseFloat(map2.get("Sqrt")) : 0;
                            map2.clear();
                            entry_chart.add(new BarEntry(index, value));

                            if(index == 5){


                                barChart.setTouchEnabled(false); // 차트 터치 불가능하게
                                barDataSet = new BarDataSet(entry_chart, "Weekly momentum"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.

                                barDataSet.setColors(ColorArray); // 해당 BarDataSet 색 설정 :: 각 막대 과 관련된 세팅은 여기서 설정한다.
                                barDataSet.setStackLabels(labels);

                                barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다
                                barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.

                                barChart.invalidate(); // 차트 업데이트
                                barChart.setTouchEnabled(false); // 차트 터치 불가능하게
                            }
                            index++;
//                            System.out.println(map);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }


//                entry_chart.add(new BarEntry(0, 8));
//                entry_chart.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.
//                entry_chart.add(new BarEntry(2, 2));
//                entry_chart.add(new BarEntry(3, 3));
//                entry_chart.add(new BarEntry(4, 4));
//                entry_chart.add(new BarEntry(5, 2));


//                BarDataSet barDataSet = new BarDataSet(entry_chart, "dataset"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.
//
//
//                barDataSet.setColor(Color.BLUE); // 해당 BarDataSet 색 설정 :: 각 막대 과 관련된 세팅은 여기서 설정한다.
//
//                barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
//                barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.
//
//                barChart.invalidate(); // 차트 업데이트
//                barChart.setTouchEnabled(false); // 차트 터치 불가능하게

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        index = 0;

        Image_pullup = view.findViewById(R.id.Image_pullup);
        Image_squrt = view.findViewById(R.id.Image_squrt);
        Image_pushup = view.findViewById(R.id.Image_pushup);

        Image_pullup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Image_pullup_url = "https://youtu.be/nWhS28U6bCY";
                openUrlInBrowser(Image_pullup_url);
            }
        });

        Image_squrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Image_squrt_url = "https://youtu.be/vQNFiMi0m9M";
                openUrlInBrowser(Image_squrt_url);
            }
        });

        Image_pushup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Image_pushup_url = "https://youtu.be/aoH7qNedO8k";
                openUrlInBrowser(Image_pushup_url);
            }
        });
        return view;
    }

    public static Fragment_Home newInstance(login_member mem) {
        Fragment_Home fragment = new Fragment_Home();
        Bundle args = new Bundle();
        args.putSerializable("mem", mem);
        fragment.setArguments(args);
        return fragment;
    }
    private void openUrlInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

//    private void firebase_now_Data(String now){
//        mRootRef.child("daily").child(mem.getId()).child(now).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot ds : snapshot.getChildren()){
//
//                    db_daily Db_daily = ds.getValue(db_daily.class);
//
//                    map2.put(Db_daily.getEvent(), Db_daily.getCount());
////                    System.out.println(Db_daily.getEvent());
////                    System.out.println(Db_daily.getCount());
//                }
////                System.out.println("Sdfsdfsdfsdfsdf");
//                map.put(now, map2);
//                System.out.println(map);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}
