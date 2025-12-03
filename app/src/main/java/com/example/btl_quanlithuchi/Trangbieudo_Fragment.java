package com.example.btl_quanlithuchi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Trangbieudo_Fragment extends Fragment {

    private InfomationAdapter adapter;
    private DBHelper dbHelper;
    private LineChart lineChart;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.trang_bieu_do, container, false);
        loadBarChartTitle(view);
        loadLineChartDate(view);
        return view;
    }

    private void loadBarChartTitle(View view) {
        BarChart barChart = view.findViewById(R.id.barChart);
        DBHelper dbHelper = new DBHelper(getContext());
        Map<String, Float> dataMap = dbHelper.getTotalExpenseByCategory();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Float> entry : dataMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi theo loại");
        dataSet.setColor(Color.parseColor("#42A5F5")); // Màu xanh dương

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);

        // Trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);

        // Tắt trục phải
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Hiệu ứng
        barChart.animateY(1000);
        barChart.invalidate();

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

    }


    private void loadLineChartDate(View view) {
        lineChart = view.findViewById(R.id.lineChart);
        DBHelper dbHelper = new DBHelper(requireContext());
        DBHelper.LineChartData chartData = dbHelper.getLineChartDataByDate();

        LineDataSet netDataSet = new LineDataSet(chartData.netEntries, "Tổng thu - chi");
        netDataSet.setColor(Color.parseColor("#26A69A"));
        netDataSet.setCircleColor(Color.parseColor("#26A69A"));
        netDataSet.setLineWidth(2f);

        LineData lineData = new LineData(netDataSet);
        lineChart.setData(lineData);

        // Trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(chartData.dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.animateX(1000);
        lineChart.invalidate();
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
            loadBarChartTitle(getView());
            loadLineChartDate(getView());
        }
    }

}
