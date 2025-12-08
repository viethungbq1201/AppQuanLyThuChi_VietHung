package com.example.btl_quanlithuchi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class InfomationAdapterTrangchu extends RecyclerView.Adapter<InfomationAdapterTrangchu.ViewHolder> {
    private List<Infomation> list;
    private final Context context;

    public InfomationAdapterTrangchu(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_trangchu, parent, false);
        return new ViewHolder(v);
    }

    private int getImageForTitle(String category) {
        if (category == null) return R.drawable.ic_money;

        String lowerTitle = category.toLowerCase();

        if (lowerTitle.contains("gửi xe 1") || lowerTitle.contains("gửi xe 2") ||
                lowerTitle.contains("gửi xe 3") || lowerTitle.contains("xăng")) {
            return R.drawable.ic_vehicle;
        }

        if (lowerTitle.contains("xe thái bình") || lowerTitle.contains("bee") || lowerTitle.contains("grab")) {
            return R.drawable.ic_transport;
        }

        if (lowerTitle.contains("ăn sáng") || lowerTitle.contains("ăn trưa") || lowerTitle.contains("ăn tối") ||
                lowerTitle.contains("phở") || lowerTitle.contains("bún riêu") || lowerTitle.contains("cơm rang") ||
                lowerTitle.contains("nem nướng") || lowerTitle.contains("cơm")) {
            return R.drawable.ic_food;
        }

        if (lowerTitle.contains("nạp điện thoại")) {
            return R.drawable.ic_phone;
        }

        if (lowerTitle.contains("lương")) {
            return R.drawable.ic_salary;
        }

        if (lowerTitle.contains("siêu thị") || lowerTitle.contains("st")) {
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
}