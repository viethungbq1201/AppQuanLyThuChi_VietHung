package com.example.btl_quanlithuchi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trangchu_Fragment extends Fragment {

    private RecyclerView rc_view_1;
    private InfomationAdapterTrangchu adapter;
    private DBHelper dbHelper;
    private Spinner spinnerMonth;
    private PieChart pieChart;
    private TextView txtBalance;
    private String currentMonthYear;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_chu, container, false);

        dbHelper = new DBHelper(getContext());

        // Lấy tháng hiện tại
        currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());

        // Setup spinner tháng
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        setupMonthSpinner();

        // Setup biểu đồ
        pieChart = view.findViewById(R.id.pieChart);
        txtBalance = view.findViewById(R.id.txtBalance);

        // Setup RecyclerView
        rc_view_1 = view.findViewById(R.id.rc_view_1);
        rc_view_1.setLayoutManager(new LinearLayoutManager(getContext()));

        // Hiển thị số dư tổng (toàn bộ thời gian)
        updateTotalBalance();

        // Load dữ liệu tất cả các giao dịch
        loadAllData();

        return view;
    }

    private void setupMonthSpinner() {
        List<String> months = dbHelper.getMonthsWithData();
        if (months.isEmpty()) {
            months.add(currentMonthYear);
        }

        // Thêm option "Tất cả"
        months.add(0, "Tất cả");

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                months
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Chọn "Tất cả" mặc định
        spinnerMonth.setSelection(0);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (selected.equals("Tất cả")) {
                    // Hiển thị tất cả dữ liệu và số dư tổng
                    updateTotalBalance();
                    loadAllData();
                    loadPieChartAll();
                } else {
                    // Hiển thị dữ liệu theo tháng
                    loadDataForMonth(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateTotalBalance() {
        int totalIncome = dbHelper.getTotalIncome();
        int totalExpense = dbHelper.getTotalExpense();
        int balance = totalIncome - totalExpense;

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        txtBalance.setText("Số dư hiện tại: " + numberFormat.format(balance) + " VND");
        txtBalance.setTextColor(balance >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
    }

    private void loadAllData() {
        List<Infomation> list = dbHelper.getInfomationsByType("all");
        adapter = new InfomationAdapterTrangchu(getContext(), list);
        rc_view_1.setAdapter(adapter);
    }

    private void loadDataForMonth(String monthYear) {
        List<Infomation> list = dbHelper.getInfomationsByMonth("all", monthYear);
        adapter = new InfomationAdapterTrangchu(getContext(), list);
        rc_view_1.setAdapter(adapter);

        // Cập nhật số dư của tháng
        int income = dbHelper.getTotalIncomeByMonth(monthYear);
        int expense = dbHelper.getTotalExpenseByMonth(monthYear);
        int balance = income - expense;

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        txtBalance.setText("Số dư tháng " + monthYear + ": " + numberFormat.format(balance) + " VND");
        txtBalance.setTextColor(balance >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        // Load biểu đồ cho tháng
        loadPieChartForMonth(monthYear);
    }

    private void loadPieChartAll() {
        int totalIncome = dbHelper.getTotalIncome();
        int totalExpense = dbHelper.getTotalExpense();

        List<PieEntry> entries = new ArrayList<>();
        if (totalIncome > 0) entries.add(new PieEntry(totalIncome, "Tổng thu"));
        if (totalExpense > 0) entries.add(new PieEntry(totalExpense, "Tổng chi"));

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có dữ liệu");
            pieChart.setNoDataTextColor(Color.GRAY);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setCenterText("🟢 Tổng thu\n🔴 Tổng chi");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate();
    }

    private void loadPieChartForMonth(String monthYear) {
        int income = dbHelper.getTotalIncomeByMonth(monthYear);
        int expense = dbHelper.getTotalExpenseByMonth(monthYear);

        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry(income, "Thu"));
        if (expense > 0) entries.add(new PieEntry(expense, "Chi"));

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có dữ liệu tháng này");
            pieChart.setNoDataTextColor(Color.GRAY);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setCenterText("🟢 Thu tháng\n🔴 Chi tháng\n" + monthYear);
        pieChart.setCenterTextSize(12f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null) {
            // Cập nhật số dư tổng
            updateTotalBalance();

            // Cập nhật spinner
            List<String> months = dbHelper.getMonthsWithData();
            if (months.isEmpty()) {
                months.add(currentMonthYear);
            }
            months.add(0, "Tất cả");

            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    months
            );
            monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMonth.setAdapter(monthAdapter);

            // Cập nhật dữ liệu
            String selected = (String) spinnerMonth.getSelectedItem();
            if (selected != null) {
                if (selected.equals("Tất cả")) {
                    loadAllData();
                    loadPieChartAll();
                } else {
                    loadDataForMonth(selected);
                }
            }
        }
    }
}