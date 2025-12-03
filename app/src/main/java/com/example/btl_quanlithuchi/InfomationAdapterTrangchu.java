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

public class InfomationAdapterTrangchu extends RecyclerView.Adapter<InfomationAdapterTrangchu.ViewHolder> {
    private  List<Infomation> list;
    private final Context context;
    private final DBHelper dbHelper;

    public InfomationAdapterTrangchu(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, date, price;
        ImageView anh;



        public ViewHolder(View view) {
            super(view);
            anh =  view.findViewById(R.id.anh);
            category = view.findViewById(R.id.danhmuc);
            date = view.findViewById(R.id.thoigian);
            price = view.findViewById(R.id.gia);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_trangchu, parent, false);
        return new ViewHolder(v);
    }


    private int getImageForTitle(String category) {
        if (category == null) return R.drawable.ic_money; // Mặc định

        String lowerTitle = category.toLowerCase();

        // Nhóm xe
        if (lowerTitle.contains("gửi xe 1") || lowerTitle.contains("gửi xe 2") ||
                lowerTitle.contains("gửi xe 3") || lowerTitle.contains("xăng")) {
            return R.drawable.ic_vehicle;
        }

        // Nhóm di chuyển
        if (lowerTitle.contains("xe thái bình") || lowerTitle.contains("bee") || lowerTitle.contains("grab")) {
            return R.drawable.ic_transport;
        }

        // Nhóm ăn uống
        if (lowerTitle.contains("ăn sáng") || lowerTitle.contains("ăn trưa") || lowerTitle.contains("ăn tối") ||
                lowerTitle.contains("phở") || lowerTitle.contains("bún riêu") || lowerTitle.contains("cơm rang") ||
                lowerTitle.contains("nem nướng") || lowerTitle.contains("cơm")) {
            return R.drawable.ic_food;
        }

        // Nhóm điện thoại
        if (lowerTitle.contains("nạp điện thoại")) {
            return R.drawable.ic_phone;
        }

        // Nhóm lương
        if (lowerTitle.contains("lương")) {
            return R.drawable.ic_salary;
        }

        // Nhóm shopping
        if (lowerTitle.contains("siêu thị") || lowerTitle.contains("st")) {
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

}
