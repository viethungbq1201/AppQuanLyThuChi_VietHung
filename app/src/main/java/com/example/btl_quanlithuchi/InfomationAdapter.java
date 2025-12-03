package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfomationAdapter extends RecyclerView.Adapter<InfomationAdapter.ViewHolder> {
    private  List<Infomation> list;
    private final Context context;
    private final DBHelper dbHelper;

    public InfomationAdapter(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, date, price;
        ImageButton btnedit, btndelete;
        ImageView anh;



        public ViewHolder(View view) {
            super(view);
            anh = view.findViewById(R.id.anh);
            category = view.findViewById(R.id.danhmuc);
            date = view.findViewById(R.id.thoigian);
            price = view.findViewById(R.id.gia);
            btnedit = view.findViewById(R.id.bt_edit);
            btndelete = view.findViewById(R.id.btn_delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new ViewHolder(v);
    }

    private int getImageForTitle(String category) {
        if (category == null) return R.drawable.ic_money; // Mặc định

        String lowerTitle = category.toLowerCase();

        // Nhóm xe
        if (lowerTitle.contains("gửi xe 1") || lowerTitle.contains("gửi xe 2") ||
                lowerTitle.contains("gửi xe 3") || lowerTitle.contains("xăng") ||
                lowerTitle.contains("sửa xe")) {
            return R.drawable.ic_vehicle;
        }

        // Nhóm di chuyển
        if (lowerTitle.contains("xe thái bình") || lowerTitle.contains("bee") || lowerTitle.contains("grab") || lowerTitle.contains("be")) {
            return R.drawable.ic_transport;
        }

        // Nhóm ăn uống
        if (lowerTitle.contains("ăn sáng") || lowerTitle.contains("ăn trưa") || lowerTitle.contains("ăn tối") ||
                lowerTitle.contains("phở") || lowerTitle.contains("bún riêu") || lowerTitle.contains("cơm rang") ||
                lowerTitle.contains("nem nướng") || lowerTitle.contains("cơm") || lowerTitle.contains("mì")) {
            return R.drawable.ic_food;
        }

        // Nhóm điện thoại
        if (lowerTitle.contains("nạp điện thoại") || lowerTitle.contains("sửa điện thoại")) {
            return R.drawable.ic_phone;
        }

        // Nhóm lương
        if (lowerTitle.contains("lương") || lowerTitle.contains("bố mẹ") || lowerTitle.contains("c trang") || lowerTitle.contains("tip")) {
            return R.drawable.ic_salary;
        }

        // Nhóm shopping
        if (lowerTitle.contains("siêu thị") || lowerTitle.contains("st") || lowerTitle.contains("thuốc")) {
            return R.drawable.ic_shopping;
        }

        // Nhóm quà tặng
        if (lowerTitle.contains("quà tặng")) {
            return R.drawable.ic_gift;
        }

        // Mặc định
        return R.drawable.ic_money;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Infomation t = list.get(position);
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedPrice = formatter.format(t.getPrice());

        int imageRes = getImageForTitle(t.getCategory());
        holder.anh.setImageResource(imageRes);
        holder.category.setText(t.getCategory());
        holder.date.setText(t.getDate());
        if (t.getType().equalsIgnoreCase("thu")) {
            holder.price.setText("+ " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.price.setText("- " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#F44336"));
        }


        holder.btnedit.setOnClickListener(v -> showEditDialog(t, holder.getAdapterPosition()));

        holder.btndelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa mục này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        Infomation item = list.get(position);

                        dbHelper.deleteInfomation(item.getId());

                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());

                        Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Infomation> newList) {
        newList.sort((a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                Date dateA = sdf.parse(a.getDate());
                Date dateB = sdf.parse(b.getDate());
                return dateB.compareTo(dateA); // Mới nhất trước
            } catch (Exception e) {
                return 0;
            }
        });

        this.list = newList;
        notifyDataSetChanged();
    }

    private void showEditDialog(Infomation info, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Spinner spinner = view.findViewById(R.id.spinner);
        EditText edtCategory = view.findViewById(R.id.edit_category);
        EditText edtPrice = view.findViewById(R.id.edit_price);
        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Khởi tạo spinner

        String[] options = {"Nhập loại tiền thu chi", "Gửi xe 1", "Gửi xe 2", "Gửi xe 3", "Xăng", "Nạp đt", "Xe TB"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set dữ liệu cũ
        edtCategory.setText(info.getCategory());
        edtPrice.setText(String.valueOf(info.getPrice()));
        //spinner.setSelection(getSpinnerPosition(info.getTitle(), options));



        btnOk.setOnClickListener(v -> {
            String newTitle = "";
            String newCategory = edtCategory.getText().toString();
            String newPriceStr = edtPrice.getText().toString();

            if (TextUtils.isEmpty(newCategory) || TextUtils.isEmpty(newPriceStr)) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ dữ liệu!", Toast.LENGTH_SHORT).show();
                return;
            }


            int newPrice = Integer.parseInt(newPriceStr);
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // Cập nhật object
            info.setTitle(newTitle);
            info.setCategory(newCategory);
            info.setPrice(newPrice);
            info.setDate(currentDate);

            dbHelper.updateInfomation(info);
            notifyItemChanged(position);

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private int getSpinnerPosition(String value, String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(value)) return i;
        }
        return 0;
    }



}
