package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfomationAdapter extends RecyclerView.Adapter<InfomationAdapter.ViewHolder> {
    private List<Infomation> list;
    private final Context context;
    private final DBHelper dbHelper;

    public InfomationAdapter(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, date, price;
        ImageView anh;

        public ViewHolder(View view) {
            super(view);
            anh = view.findViewById(R.id.anh);
            category = view.findViewById(R.id.danhmuc);
            date = view.findViewById(R.id.thoigian);
            price = view.findViewById(R.id.gia);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new ViewHolder(v);
    }

    private int getImageForTitle(String category) {
        if (category == null) return R.drawable.ic_money;

        String lowerTitle = category.toLowerCase();

        if (lowerTitle.contains("gửi xe 1") || lowerTitle.contains("gửi xe 2") ||
                lowerTitle.contains("gửi xe 3") || lowerTitle.contains("xăng") ||
                lowerTitle.contains("sửa xe")) {
            return R.drawable.ic_vehicle;
        }

        if (lowerTitle.contains("xe thái bình") || lowerTitle.contains("bee") ||
                lowerTitle.contains("grab") || lowerTitle.contains("be")) {
            return R.drawable.ic_transport;
        }

        if (lowerTitle.contains("ăn sáng") || lowerTitle.contains("ăn trưa") || lowerTitle.contains("ăn tối") ||
                lowerTitle.contains("phở") || lowerTitle.contains("bún riêu") || lowerTitle.contains("cơm rang") ||
                lowerTitle.contains("nem nướng") || lowerTitle.contains("cơm") || lowerTitle.contains("mì")) {
            return R.drawable.ic_food;
        }

        if (lowerTitle.contains("nạp điện thoại") || lowerTitle.contains("sửa điện thoại")) {
            return R.drawable.ic_phone;
        }

        if (lowerTitle.contains("lương") || lowerTitle.contains("bố mẹ") ||
                lowerTitle.contains("c trang") || lowerTitle.contains("tip")) {
            return R.drawable.ic_salary;
        }

        if (lowerTitle.contains("siêu thị") || lowerTitle.contains("st") || lowerTitle.contains("thuốc")) {
            return R.drawable.ic_shopping;
        }

        if (lowerTitle.contains("quà tặng")) {
            return R.drawable.ic_gift;
        }

        return R.drawable.ic_money;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Infomation t = list.get(position);

        // DEBUG: Kiểm tra giá trị
        if (t.getPrice() == 0) {
            // Nếu giá trị là 0, có thể do lỗi parse
            System.out.println("DEBUG: Item at position " + position + " has price = 0");
        }

        // Sử dụng DecimalFormat thay vì NumberFormat
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(t.getPrice());

        int imageRes = getImageForTitle(t.getCategory());
        holder.anh.setImageResource(imageRes);
        holder.category.setText(t.getCategory());

        // Hiển thị ngày tháng
        String displayDate = t.getDate();
        if (displayDate.length() > 10) {
            displayDate = displayDate.substring(0, 10); // Chỉ lấy dd/MM/yyyy
        }
        holder.date.setText(displayDate);

        // Hiển thị số tiền
        if (t.getType().equalsIgnoreCase("thu")) {
            holder.price.setText("+ " + formattedPrice + " đ");
            holder.price.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.price.setText("- " + formattedPrice + " đ");
            holder.price.setTextColor(Color.parseColor("#F44336"));
        }

        // Click để sửa
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(t, holder.getAdapterPosition());
            }
        });

        // Long click để xóa
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(t, holder.getAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Infomation> newList) {
        // Sắp xếp theo timestamp (mới nhất trước)
        newList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        this.list = newList;
        notifyDataSetChanged();
    }

    private void showEditDialog(Infomation info, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView textType = view.findViewById(R.id.text_type);
        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        Spinner spinner = view.findViewById(R.id.spinner);
        EditText edtCategory = view.findViewById(R.id.edit_category);
        EditText edtPrice = view.findViewById(R.id.edit_price);
        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Set loại (thu/chi)
        textType.setText(info.getType().equalsIgnoreCase("thu") ? "SỬA THU" : "SỬA CHI");

        // Khởi tạo spinner
        String[] options = info.getType().equalsIgnoreCase("thu") ?
                new String[]{"Nhập loại tiền thu chi", "Lương", "Bố mẹ", "C Trang", "Nạp điện thoại"} :
                new String[]{"Nhập loại tiền thu chi", "Gửi xe 1", "Gửi xe 2", "Gửi xe 3", "Xăng", "Nạp điện thoại", "Xe Thái Bình"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set dữ liệu cũ - HIỂN THỊ SỐ TIỀN ĐẦY ĐỦ
        edtCategory.setText(info.getCategory());

        // Hiển thị số tiền dạng có dấu phẩy
        DecimalFormat priceFormat = new DecimalFormat("#,###");
        edtPrice.setText(priceFormat.format(info.getPrice()));

        spinner.setSelection(getSpinnerPosition(info.getCategory(), options));

        // Set ngày hiện tại của giao dịch
        String currentDateTime = info.getDate();
        tvSelectedDate.setText("Ngày: " + currentDateTime);

        // Parse ngày từ chuỗi
        final String[] selectedDateTime = { currentDateTime };
        final Calendar calendar = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(currentDateTime);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (Exception e) {
            // Nếu không parse được, dùng ngày hiện tại
            calendar.setTime(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            selectedDateTime[0] = sdf.format(calendar.getTime());
            tvSelectedDate.setText("Ngày: " + selectedDateTime[0]);
        }

        // Sự kiện chọn ngày - THÊM CẢ THỜI GIAN
        tvSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị dialog chọn ngày
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                                // Cập nhật ngày
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                // Hiển thị dialog chọn thời gian
                                showTimePickerDialog(calendar, tvSelectedDate, selectedDateTime);
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            }
        });

        // Sự kiện spinner
        final boolean[] isSpinnerInitialized = {false};
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v1, int position, long id) {
                if (!isSpinnerInitialized[0]) {
                    isSpinnerInitialized[0] = true;
                    return;
                }

                if (position > 0) {
                    edtCategory.setText(options[position]);
                } else {
                    edtCategory.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Trong phương thức showEditDialog của InfomationAdapter
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = "";
                String newCategory = edtCategory.getText().toString();
                String newPriceStr = edtPrice.getText().toString();

                if (TextUtils.isEmpty(newCategory) || TextUtils.isEmpty(newPriceStr)) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ dữ liệu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sử dụng phương thức parse từ DBHelper
                int newPrice = dbHelper.parsePriceFromString(newPriceStr);

                if (newPrice <= 0) {
                    Toast.makeText(context, "Số tiền không hợp lệ! Vui lòng nhập số (vd: 10000, 10k, 0.5tr)", Toast.LENGTH_LONG).show();
                    return;
                }

                // Cập nhật object
                info.setTitle(newTitle);
                info.setCategory(newCategory);
                info.setPrice(newPrice);
                info.setDate(selectedDateTime[0]);
                info.setTimestamp(dbHelper.convertDateToTimestamp(selectedDateTime[0]));

                dbHelper.updateInfomation(info);
                notifyItemChanged(position);

                Toast.makeText(context, "Đã cập nhật thành công: " + newCategory + " - " + newPrice + " đ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showTimePickerDialog(final Calendar calendar, final TextView tvSelectedDate, final String[] selectedDateTime) {
        // Hiển thị dialog chọn thời gian
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        selectedDateTime[0] = sdf.format(calendar.getTime());
                        tvSelectedDate.setText("Ngày: " + selectedDateTime[0]);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDeleteDialog(Infomation info, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa mục này?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteInfomation(info.getId());
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int getSpinnerPosition(String value, String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(value)) return i;
        }
        return 0;
    }
}