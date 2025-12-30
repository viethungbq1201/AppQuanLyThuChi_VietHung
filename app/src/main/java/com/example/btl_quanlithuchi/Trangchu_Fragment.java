package com.example.btl_quanlithuchi;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trangchu_Fragment extends Fragment {

    private RecyclerView rc_view_1;
    private InfomationAdapterTrangchu adapter;
    private DBHelper dbHelper;
    private Spinner spinnerMonth;
    private PieChart pieChart;
    private TextView txtBalance;
    private String currentMonthYear;

    private FloatingActionButton fabVoiceInput;
    private VoiceInputHelper voiceHelper;
    private Dialog voiceDialog;
    private String lastRecognizedText = "";

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

        fabVoiceInput = view.findViewById(R.id.fab_voice_input);

        setupMonthSpinner();
        loadAllData();
        updateTotalBalance();

        voiceHelper = new VoiceInputHelper(getContext(), getActivity());
        voiceHelper.setListener(new VoiceInputHelper.VoiceListener() {
            @Override
            public void onVoiceResult(String text) {
                lastRecognizedText = text;
                updateVoiceDialogResult(text);
            }

            @Override
            public void onVoiceError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                updateVoiceDialogStatus("Lỗi: " + message, false);
            }

            @Override
            public void onListeningStarted() {
                updateVoiceDialogStatus("Đang nghe... nói ngay", true);
            }

            @Override
            public void onListeningStopped() {}
        });

        fabVoiceInput.setOnClickListener(v -> showVoiceDialog());

        return view;
    }

    private void showVoiceDialog() {
        voiceDialog = new Dialog(getContext());
        voiceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        voiceDialog.setContentView(R.layout.dialog_voice);
        voiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        voiceDialog.setCancelable(false);

        ImageButton btnCancel = voiceDialog.findViewById(R.id.btn_cancel);
        ImageView ivMic = voiceDialog.findViewById(R.id.iv_mic);
        ImageView ivWave = voiceDialog.findViewById(R.id.iv_wave);
        TextView tvStatus = voiceDialog.findViewById(R.id.tv_status);
        TextView tvResult = voiceDialog.findViewById(R.id.tv_result);
        Button btnStart = voiceDialog.findViewById(R.id.btn_start);
        Button btnOk = voiceDialog.findViewById(R.id.btn_ok);
        Button btnReplay = voiceDialog.findViewById(R.id.btn_replay);

        // Áp dụng theme
        int cardBg = ContextCompat.getColor(getContext(), R.color.dialog_background);
        int textPrimary = ContextCompat.getColor(getContext(), R.color.text_primary);
        int primaryColor = ContextCompat.getColor(getContext(), R.color.color_primary);
        int resultBg = ContextCompat.getColor(getContext(), R.color.result_background);

        voiceDialog.findViewById(R.id.card_view).setBackgroundColor(cardBg);
        tvStatus.setTextColor(textPrimary);
        tvResult.setTextColor(textPrimary);
        tvResult.setBackgroundColor(resultBg);
        ivMic.setColorFilter(primaryColor);
        ivWave.setColorFilter(primaryColor);

        // Reset state
        lastRecognizedText = "";
        tvResult.setText("");
        ivWave.setVisibility(View.GONE);
        btnOk.setVisibility(View.GONE);
        btnReplay.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText("BẮT ĐẦU NÓI");
        tvStatus.setText("Nhấn nút để bắt đầu nói");

        btnCancel.setOnClickListener(v -> voiceDialog.dismiss());
        btnStart.setOnClickListener(v -> voiceHelper.startListening());
        btnOk.setOnClickListener(v -> {
            processVoiceCommand(lastRecognizedText);
            voiceDialog.dismiss();
        });
        btnReplay.setOnClickListener(v -> voiceHelper.startListening());

        voiceDialog.show();
    }

    private void updateVoiceDialogStatus(String status, boolean isListening) {
        if (voiceDialog != null && voiceDialog.isShowing()) {
            TextView tvStatus = voiceDialog.findViewById(R.id.tv_status);
            ImageView ivWave = voiceDialog.findViewById(R.id.iv_wave);
            Button btnStart = voiceDialog.findViewById(R.id.btn_start);

            int listeningColor = ContextCompat.getColor(getContext(), R.color.listening_color);
            int normalColor = ContextCompat.getColor(getContext(), R.color.text_primary);

            tvStatus.setText(status);
            tvStatus.setTextColor(isListening ? listeningColor : normalColor);
            ivWave.setVisibility(isListening ? View.VISIBLE : View.GONE);
            btnStart.setVisibility(isListening ? View.GONE : View.VISIBLE);

            if (isListening) {
                ivWave.animate()
                        .scaleX(1.2f).scaleY(1.2f).setDuration(500)
                        .withEndAction(() -> ivWave.animate()
                                .scaleX(1.0f).scaleY(1.0f).setDuration(500).start())
                        .start();
            }
        }
    }

    private void updateVoiceDialogResult(String text) {
        if (voiceDialog != null && voiceDialog.isShowing()) {
            TextView tvResult = voiceDialog.findViewById(R.id.tv_result);
            Button btnOk = voiceDialog.findViewById(R.id.btn_ok);
            Button btnReplay = voiceDialog.findViewById(R.id.btn_replay);
            Button btnStart = voiceDialog.findViewById(R.id.btn_start);

            tvResult.setText(text);
            btnOk.setVisibility(View.VISIBLE);
            btnReplay.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);
            updateVoiceDialogStatus("Đã nhận diện xong!", false);
            highlightKeywords(text, tvResult);
        }
    }

    private void highlightKeywords(String text, TextView textView) {
        SpannableString spannable = new SpannableString(text);
        String lowerText = text.toLowerCase();

        int amountColor = ContextCompat.getColor(getContext(), R.color.amount_highlight);
        int incomeColor = ContextCompat.getColor(getContext(), R.color.income_highlight);
        int expenseColor = ContextCompat.getColor(getContext(), R.color.expense_highlight);

        Pattern pattern = Pattern.compile("(\\d+)\\s*(k|tr|nghìn|triệu|ngàn)");
        Matcher matcher = pattern.matcher(lowerText);
        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(amountColor),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (lowerText.contains("thu") || lowerText.contains("nhận") ||
                lowerText.contains("lương") || lowerText.contains("tiếp")) {
            int start = findFirstOccurrence(lowerText, "thu", "nhận", "lương", "tiếp");
            if (start != -1) spannable.setSpan(new ForegroundColorSpan(incomeColor),
                    start, start + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (lowerText.contains("chi") || lowerText.contains("mua") ||
                lowerText.contains("trả") || lowerText.contains("tiêu")) {
            int start = findFirstOccurrence(lowerText, "chi", "mua", "trả", "tiêu");
            if (start != -1) spannable.setSpan(new ForegroundColorSpan(expenseColor),
                    start, start + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannable);
    }

    private int findFirstOccurrence(String text, String... keywords) {
        for (String keyword : keywords) {
            int index = text.indexOf(keyword);
            if (index != -1) return index;
        }
        return -1;
    }

    private void processVoiceCommand(String voiceText) {
        String type = "chi";
        int amount = 0;
        String category = "Khác";
        String lowerText = voiceText.toLowerCase();

        if (lowerText.contains("thu") || lowerText.contains("nhận") ||
                lowerText.contains("lương") || lowerText.contains("tiếp")) {
            type = "thu";
        }

        Pattern pattern = Pattern.compile("(\\d+)\\s*(k|tr|nghìn|triệu|ngàn)");
        Matcher matcher = pattern.matcher(lowerText);
        if (matcher.find()) {
            try {
                int num = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                amount = unit.contains("tr") || unit.contains("triệu") ? num * 1000000 : num * 1000;
            } catch (Exception e) { amount = 0; }
        }

        if (lowerText.contains("ăn") || lowerText.contains("cơm") || lowerText.contains("bún")) {
            category = "Ăn uống";
        } else if (lowerText.contains("xăng") || lowerText.contains("xe")) {
            category = "Xăng xe";
        } else if (lowerText.contains("điện thoại") || lowerText.contains("nạp")) {
            category = "Điện thoại";
        } else if (lowerText.contains("lương")) {
            category = "Lương";
        } else if (lowerText.contains("mua sắm") || lowerText.contains("siêu thị")) {
            category = "Mua sắm";
        } else if (lowerText.contains("nhà") || lowerText.contains("trọ")) {
            category = "Tiền nhà";
        }

        showConfirmationDialog(type, amount, category, voiceText);
    }

    private void showConfirmationDialog(String type, int amount, String category, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận thêm giao dịch");

        String message = String.format(
                "Loại: %s\nDanh mục: %s\nSố tiền: %s đ\nMô tả: %s\n\nBạn có chắc muốn thêm?",
                type.equals("thu") ? "THU NHẬP" : "CHI TIÊU",
                category,
                new DecimalFormat("#,###").format(amount),
                description
        );

        builder.setMessage(message)
                .setPositiveButton("THÊM NGAY", (dialog, which) -> saveTransaction(type, amount, category, description))
                .setNegativeButton("CHỈNH SỬA", (dialog, which) -> showEditDialog(type, amount, category, description))
                .setNeutralButton("HỦY", null)
                .show();
    }

    private void saveTransaction(String type, int amount, String category, String description) {
        if (amount == 0) {
            Toast.makeText(getContext(), "Không thể xác định số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        Infomation info = new Infomation();
        info.setTitle(description);
        info.setCategory(category);
        info.setPrice(amount);
        info.setType(type);
        info.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

        dbHelper.insertInfomation(info);

        String selected = (String) spinnerMonth.getSelectedItem();
        if (selected != null) {
            if (selected.equals("Tất cả")) {
                loadAllData();
                loadPieChartAll();
            } else {
                loadDataForMonth(selected);
            }
        }

        int successColor = ContextCompat.getColor(getContext(), R.color.success_color);
        fabVoiceInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(successColor));

        new Handler().postDelayed(() -> {
            int primaryColor = ContextCompat.getColor(getContext(), R.color.color_primary);
            fabVoiceInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        }, 1000);

        Toast.makeText(getContext(), String.format("Đã thêm %s %s đ",
                type.equals("thu") ? "thu" : "chi", new DecimalFormat("#,###").format(amount)), Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(String type, int amount, String category, String description) {
        // Implement your edit dialog here
        Toast.makeText(getContext(), "Mở chỉnh sửa", Toast.LENGTH_SHORT).show();
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
        if (totalIncome > 0) entries.add(new PieEntry(totalIncome, "Tổng thu"));
        if (totalExpense > 0) entries.add(new PieEntry(totalExpense, "Tổng chi"));

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
        if (income > 0) entries.add(new PieEntry(income, "Thu"));
        if (expense > 0) entries.add(new PieEntry(expense, "Chi"));

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (voiceHelper != null) voiceHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (voiceHelper != null) voiceHelper.handlePermissionResult(requestCode, permissions, grantResults);
    }
}