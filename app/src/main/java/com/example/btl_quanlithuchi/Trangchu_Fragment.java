package com.example.btl_quanlithuchi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Trangchu_Fragment extends Fragment {

    private RecyclerView rc_view_1;
    private InfomationAdapterTrangchu adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_chu, container, false);



        //Recycler View
        DBHelper dbHelper = new DBHelper(getContext());

        rc_view_1 = view.findViewById(R.id.rc_view_1);
        rc_view_1.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Infomation> list = dbHelper.getInfomationsByType("all");
        adapter = new InfomationAdapterTrangchu(getContext(), list);
        rc_view_1.setAdapter(adapter);
        return view;

    }


    private void loadPieChart(View view) {

        DBHelper dbHelper = new DBHelper(getContext());
        int income = dbHelper.getTotalIncome();
        int expense = dbHelper.getTotalExpense();
        int balance = income - expense;

        // Định dạng tiền tệ (có dấu phẩy ngăn cách)
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        // Hiển thị số dư
        TextView txtBalance = view.findViewById(R.id.txtBalance);
        txtBalance.setText("Số dư: "+ numberFormat.format(balance) + "VND " );
        txtBalance.setTextColor(Color.BLACK); // đổi sang đen để nổi bật trên nền trắng

        // Biểu đồ Pie
        PieChart pieChart = view.findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(income, "Thu"));
        entries.add(new PieEntry(expense, "Chi"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336")); // xanh + đỏ
        dataSet.setValueTextColor(Color.BLUE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true); // vẽ hình tròn
        pieChart.setHoleRadius(60f); // độ lớn lỗ trống giữa
        pieChart.setTransparentCircleRadius(65f); // vòng trong mờ
        //pieChart.setCenterText("VND " + numberFormat.format(balance)); // Hiện số dư ở giữa biểu đồ
        //pieChart.setCenterTextSize(16f);
        //pieChart.setCenterTextColor(Color.BLUE);
        pieChart.setCenterText("🟢 Thu   🔴 Chi");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);


        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        legend.setDrawInside(false);
//        legend.setTextColor(Color.BLACK);
//        legend.setTextSize(14f);

        pieChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        DBHelper dbHelper = new DBHelper(getContext());
        if (dbHelper != null && adapter != null) {
            List<Infomation> updatedList = dbHelper.getInfomationsByType("all");
            adapter.setData(updatedList);
        }
        if (getView() != null) {
            loadPieChart(getView());
        }
    }
}
