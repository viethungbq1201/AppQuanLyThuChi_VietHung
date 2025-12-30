package com.example.btl_quanlithuchi;

import static android.text.TextUtils.isEmpty;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.*;

public class Trangchi_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private InfomationAdapter adapter;
    private DBHelper dbHelper;
    private Spinner spinnerMonth;
    private TextView tvTotal;
    private String currentMonth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.trang_chi, container, false);

        requestNotificationPermission();

        dbHelper = new DBHelper(getContext());
        currentMonth = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());

        recyclerView = view.findViewById(R.id.rc_view_3);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        tvTotal = view.findViewById(R.id.tvTotal);

        setupMonthSpinner();
        loadDataForMonth(currentMonth);

        FloatingActionButton fab = view.findViewById(R.id.add_wallet_entry_fab);
        fab.setOnClickListener(v -> showAddEntryDialog());

        return view;
    }

    // =========================
    // SPINNER THÁNG
    // =========================
    private void setupMonthSpinner() {
        List<String> months = dbHelper.getMonthsWithData();
        if (months.isEmpty()) months.add(currentMonth);

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                months
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapterSpinner);

        spinnerMonth.setSelection(months.indexOf(currentMonth));

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDataForMonth(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // =========================
    // LOAD DỮ LIỆU THEO THÁNG
    // =========================
    private void loadDataForMonth(String month) {
        List<Infomation> list = dbHelper.getInfomationsByMonth("chi", month);
        adapter = new InfomationAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);

        int total = dbHelper.getTotalExpenseByMonth(month);
        tvTotal.setText("Tổng chi tháng " + month + ": " +
                new DecimalFormat("#,###").format(total) + " đ");
        tvTotal.setTextColor(Color.parseColor("#F44336"));
    }

    // =========================
    // DIALOG THÊM CHI TIÊU
    // =========================
    private void showAddEntryDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(view).create();
        dialog.show();

        TextView textType = view.findViewById(R.id.text_type);
        if (textType != null) textType.setText("CHI");

        EditText edtCategory = view.findViewById(R.id.edit_category);
        EditText edtPrice = view.findViewById(R.id.edit_price);
        TextView tvDate = view.findViewById(R.id.tv_selected_date);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        tvDate.setText("Ngày: " + sdf.format(cal.getTime()));

        final String[] selectedDate = { sdf.format(cal.getTime()) };

        tvDate.setOnClickListener(v ->
                new DatePickerDialog(
                        getContext(),
                        (d, y, m, day) -> {
                            cal.set(y, m, day);
                            selectedDate[0] = sdf.format(cal.getTime());
                            tvDate.setText("Ngày: " + selectedDate[0]);
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            String category = DBHelper.capitalizeCategory(edtCategory.getText().toString());
            String priceInput = edtPrice.getText().toString();

            if (isEmpty(category) || isEmpty(priceInput)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int price = dbHelper.parsePriceFromString(priceInput);
            if (price <= 0) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            Infomation inf = new Infomation();
            inf.setCategory(category);
            inf.setPrice(price);
            inf.setType("chi");
            inf.setDate(selectedDate[0]);
            inf.setTimestamp(dbHelper.convertDateToTimestamp(selectedDate[0]));

            dbHelper.insertInfomation(inf);

            loadDataForMonth(spinnerMonth.getSelectedItem().toString());
            dialog.dismiss();

            checkBalanceAndNotify();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
    }

    // =========================
    // KIỂM TRA SỐ DƯ
    // =========================
    private void checkBalanceAndNotify() {
        if (dbHelper.getTotalIncome() - dbHelper.getTotalExpense() < 0) {
            sendNotification("⚠️ Số dư đã âm! Bạn nghèo rồi.");
        }
    }

    // =========================
    // NOTIFICATION
    // =========================
    private void sendNotification(String message) {
        NotificationManager nm =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "warning_balance";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Cảnh báo số dư",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }

        nm.notify(
                1,
                new NotificationCompat.Builder(getContext(), channelId)
                        .setSmallIcon(R.drawable.ic_warning)
                        .setContentTitle("Cảnh báo số dư")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
        );
    }

    // =========================
    // XIN QUYỀN NOTIFICATION
    // =========================
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }
    }
}
