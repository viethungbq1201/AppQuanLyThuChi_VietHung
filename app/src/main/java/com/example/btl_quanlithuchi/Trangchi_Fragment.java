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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trangchi_Fragment extends Fragment {

    private RecyclerView rc_view_3;
    private InfomationAdapter adapter;
    private DBHelper dbHelper;
    private Spinner spinnerMonth;
    private String currentMonthYear;
    private TextView tvTotal;
    private static final String TAG = "Trangchi_Fragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.trang_chi, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        dbHelper = new DBHelper(getContext());

        // DEBUG: Kiểm tra dữ liệu
        dbHelper.debugAllData();

        // Lấy tháng hiện tại
        currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());

        // Setup spinner tháng
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        setupMonthSpinner();

        // Setup RecyclerView
        rc_view_3 = view.findViewById(R.id.rc_view_3);
        rc_view_3.setLayoutManager(new LinearLayoutManager(getContext()));

        // TextView tổng
        tvTotal = view.findViewById(R.id.tvTotal);

        // Load dữ liệu tháng hiện tại
        loadDataForMonth(currentMonthYear);

        FloatingActionButton fab = view.findViewById(R.id.add_wallet_entry_fab);
        fab.setOnClickListener(v -> {
            showAddEntryDialog();
        });

        return view;
    }

    private void setupMonthSpinner() {
        List<String> months = dbHelper.getMonthsWithData();
        if (months.isEmpty()) {
            months.add(currentMonthYear);
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                months
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Chọn tháng hiện tại
        int position = months.indexOf(currentMonthYear);
        if (position >= 0) {
            spinnerMonth.setSelection(position);
        }

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = (String) parent.getItemAtPosition(position);
                loadDataForMonth(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadDataForMonth(String monthYear) {
        List<Infomation> list = dbHelper.getInfomationsByMonth("chi", monthYear);
        adapter = new InfomationAdapter(getContext(), list);
        rc_view_3.setAdapter(adapter);

        // Hiển thị tổng chi của tháng
        if (tvTotal != null) {
            int total = dbHelper.getTotalExpenseByMonth(monthYear);
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvTotal.setText("Tổng chi tháng " + monthYear + ": " + formatter.format(total) + " đ");
            tvTotal.setTextColor(Color.parseColor("#F44336"));
        }
    }

    private void showAddEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add, null);
        builder.setView(view);

        TextView textType = view.findViewById(R.id.text_type);
        textType.setText("CHI");

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        Spinner spinner = view.findViewById(R.id.spinner);
        String[] options = {"Nhập loại tiền thu chi", "Gửi xe 1", "Gửi xe 2", "Gửi xe 3", "Xăng", "Nạp điện thoại", "Xe Thái Bình"};
        String[] defaultPrices = {"", "3k", "5k", "10k", "50k", "50k", "155k"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String currentDate = sdfDateOnly.format(calendar.getTime());
        String currentDateTime = sdfFull.format(calendar.getTime());
        tvSelectedDate.setText("Ngày: " + currentDateTime);

        final String[] selectedDateTime = { currentDateTime };

        final boolean[] isSpinnerInitialized = {false};

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v1, int position, long id) {
                EditText editCategory = view.findViewById(R.id.edit_category);
                EditText editPrice = view.findViewById(R.id.edit_price);

                if (!isSpinnerInitialized[0]) {
                    isSpinnerInitialized[0] = true;
                    return;
                }
                if (position > 0) {
                    editCategory.setText(options[position]);
                    editPrice.setText(defaultPrices[position]);
                } else {
                    editCategory.setText("");
                    editPrice.setText("");
                }
                editPrice.requestFocus();
                editPrice.setSelection(editPrice.getText().length());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        tvSelectedDate.setOnClickListener(v1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth, hour, minute, second);
                        selectedDateTime[0] = sdfFull.format(calendar.getTime());
                        tvSelectedDate.setText("Ngày: " + sdfFull.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnOk.setOnClickListener(v -> {
            DBHelper dbHelper = new DBHelper(getContext());
            Infomation inf = new Infomation();

            EditText editCategory = view.findViewById(R.id.edit_category);
            EditText editPrice = view.findViewById(R.id.edit_price);

            String category = editCategory.getText().toString();
            String priceInput = editPrice.getText().toString();

            if (isEmpty(category)) {
                Toast.makeText(getContext(), "Vui lòng nhập danh mục!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEmpty(priceInput)) {
                Toast.makeText(getContext(), "Vui lòng nhập giá tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sử dụng phương thức parse từ DBHelper
            int price = dbHelper.parsePriceFromString(priceInput);

            if (price <= 0) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ! Vui lòng nhập số (vd: 10000, 10k, 0.5tr)", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Adding new entry - Category: " + category + ", Price: " + price + ", PriceInput: " + priceInput);

            inf.setTitle("");
            inf.setCategory(category);
            inf.setPrice(price);
            inf.setType("chi");
            inf.setDate(selectedDateTime[0]);
            inf.setTimestamp(dbHelper.convertDateToTimestamp(selectedDateTime[0]));

            dbHelper.insertInfomation(inf);

            // Cập nhật dữ liệu
            String selectedMonth = (String) spinnerMonth.getSelectedItem();
            if (selectedMonth != null) {
                loadDataForMonth(selectedMonth);
            }

            dialog.dismiss();
            Toast.makeText(getContext(), "Đã thêm thành công: " + category + " - " + price + " đ", Toast.LENGTH_SHORT).show();

            // Kiểm tra số dư âm
            int income = dbHelper.getTotalIncome();
            int expense = dbHelper.getTotalExpense();
            int balance = income - expense;
            if (balance < 0) {
                sendNotification("⚠️ Số dư đã âm! Bạn nghèo rồi.");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void sendNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "sodu_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Số dư cảnh báo", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Cảnh báo số dư")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (dbHelper != null && adapter != null) {
            String selectedMonth = (String) spinnerMonth.getSelectedItem();
            if (selectedMonth != null) {
                List<Infomation> updatedList = dbHelper.getInfomationsByMonth("chi", selectedMonth);
                adapter.setData(updatedList);
            }
        }
    }
}