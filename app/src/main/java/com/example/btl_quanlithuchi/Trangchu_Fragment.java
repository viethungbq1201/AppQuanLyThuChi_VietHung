package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.text.DecimalFormat;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_chu, container, false);

        dbHelper = new DBHelper(getContext());
        currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());

        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        pieChart = view.findViewById(R.id.pieChart);
        txtBalance = view.findViewById(R.id.txtBalance);
        rc_view_1 = view.findViewById(R.id.rc_view_1);
        rc_view_1.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMonthSpinner();
        loadAllData();
        updateTotalBalance();

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
                months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Chọn "Tất cả" mặc định
        spinnerMonth.setSelection(0);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (selected.equals("Tất cả")) {
                    updateTotalBalance();
                    loadAllData();
                    loadPieChartAll();
                } else {
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

        DecimalFormat numberFormat = new DecimalFormat("#,###");

        int balanceColor;
        if (balance >= 0) {
            balanceColor = ContextCompat.getColor(getContext(), R.color.color_income);
        } else {
            balanceColor = ContextCompat.getColor(getContext(), R.color.color_expense);
        }

        txtBalance.setText("Số dư: " + numberFormat.format(balance) + " đ");
        txtBalance.setTextColor(balanceColor);
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

        DecimalFormat numberFormat = new DecimalFormat("#,###");

        int balanceColor;
        if (balance >= 0) {
            balanceColor = ContextCompat.getColor(getContext(), R.color.color_income);
        } else {
            balanceColor = ContextCompat.getColor(getContext(), R.color.color_expense);
        }

        txtBalance.setText("Tháng " + monthYear + ": " + numberFormat.format(balance) + " đ");
        txtBalance.setTextColor(balanceColor);

        // Load biểu đồ cho tháng
        loadPieChartForMonth(monthYear);
    }

    private void loadPieChartAll() {
        int totalIncome = dbHelper.getTotalIncome();
        int totalExpense = dbHelper.getTotalExpense();

        List<PieEntry> entries = new ArrayList<>();
        if (totalIncome > 0)
            entries.add(new PieEntry(totalIncome, "Tổng thu"));
        if (totalExpense > 0)
            entries.add(new PieEntry(totalExpense, "Tổng chi"));

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có dữ liệu");
            pieChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        int incomeColor = ContextCompat.getColor(getContext(), R.color.chart_income);
        int expenseColor = ContextCompat.getColor(getContext(), R.color.chart_expense);
        int textColor = ContextCompat.getColor(getContext(), R.color.chart_text);

        dataSet.setColors(incomeColor, expenseColor);
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Cấu hình biểu đồ
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.setCenterText("");
        pieChart.setDrawCenterText(false);
        pieChart.getDescription().setEnabled(false);

        // Cấu hình legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(textColor);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);

        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

    private void loadPieChartForMonth(String monthYear) {
        int income = dbHelper.getTotalIncomeByMonth(monthYear);
        int expense = dbHelper.getTotalExpenseByMonth(monthYear);

        List<PieEntry> entries = new ArrayList<>();
        if (income > 0)
            entries.add(new PieEntry(income, "Thu"));
        if (expense > 0)
            entries.add(new PieEntry(expense, "Chi"));

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Không có dữ liệu tháng này");
            pieChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        int incomeColor = ContextCompat.getColor(getContext(), R.color.chart_income);
        int expenseColor = ContextCompat.getColor(getContext(), R.color.chart_expense);
        int textColor = ContextCompat.getColor(getContext(), R.color.chart_text);

        dataSet.setColors(incomeColor, expenseColor);
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Cấu hình biểu đồ
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setHoleColor(ContextCompat.getColor(getContext(), R.color.chart_hole));
        pieChart.setTransparentCircleColor(ContextCompat.getColor(getContext(), R.color.chart_transparent_circle));

        pieChart.setCenterText("Tháng " + monthYear + "\n🟢 Thu\n🔴 Chi");
        pieChart.setCenterTextSize(12f);
        pieChart.setCenterTextColor(textColor);
        pieChart.getDescription().setEnabled(false);

        // Cấu hình legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(textColor);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);

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
                    months);
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