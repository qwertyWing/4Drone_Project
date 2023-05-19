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

import androidx.fragment.app.Fragment;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.login_member;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class Fragment_Home extends Fragment {

    private ImageView Image_pullup, Image_squrt, Image_pushup;
    //선 그래프
    private BarChart barChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            login_member mem = (login_member) bundle.getSerializable("mem");
            // mem 객체를 활용하여 작업 수행
            Toast.makeText(getActivity(), "ID: " + mem.getId(), Toast.LENGTH_SHORT).show();
        }
        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist


        BarChart barChart = view.findViewById(R.id.chart);



        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"vdsvsdvsvsdvds", "Label2", "Label3","Label4", "Label5", "Label6","label7"})); // 라벨 값 설정

        BarData barData = new BarData(); // 차트에 담길 데이터

        entry_chart.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(2, 2));
        entry_chart.add(new BarEntry(3, 3));
        entry_chart.add(new BarEntry(4, 4));
        entry_chart.add(new BarEntry(5, 2));
        entry_chart.add(new BarEntry(6, 8));


        BarDataSet barDataSet = new BarDataSet(entry_chart, "dataset"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.


        barDataSet.setColor(Color.BLUE); // 해당 BarDataSet 색 설정 :: 각 막대 과 관련된 세팅은 여기서 설정한다.

        barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
        barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.

        barChart.invalidate(); // 차트 업데이트
        barChart.setTouchEnabled(false); // 차트 터치 불가능하게

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
}
