package com.example.btl_quanlithuchi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "WalletDB";
    private static final int DB_VERSION = 3; // Tăng version lên 3
    private static final String TAG = "DBHelper"; // Thêm TAG cho log


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng thông tin thu chi - THÊM CỘT timestamp
        String query = "CREATE TABLE Infomations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " +
                "date TEXT, " +
                "timestamp INTEGER, " + // Thêm cột timestamp để sắp xếp
                "price INTEGER, " +
                "type TEXT)";
        db.execSQL(query);

        // Bảng ghi chú
        String createNoteTable = "CREATE TABLE Notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "content TEXT, " +
                "has_checkbox INTEGER DEFAULT 0, " +
                "is_checked INTEGER DEFAULT 0, " +
                "created_date TEXT, " +
                "created_timestamp INTEGER)"; // Thêm timestamp
        db.execSQL(createNoteTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Thêm cột timestamp nếu nâng cấp từ version cũ
            db.execSQL("ALTER TABLE Infomations ADD COLUMN timestamp INTEGER DEFAULT 0");

            // Cập nhật timestamp cho các bản ghi cũ
            Cursor cursor = db.rawQuery("SELECT id, date FROM Infomations", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String dateStr = cursor.getString(1);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(dateStr);
                    long timestamp = date.getTime();

                    ContentValues values = new ContentValues();
                    values.put("timestamp", timestamp);
                    db.update("Infomations", values, "id = ?", new String[]{String.valueOf(id)});
                } catch (Exception e) {
                    // Nếu không parse được, dùng timestamp hiện tại
                    ContentValues values = new ContentValues();
                    values.put("timestamp", System.currentTimeMillis());
                    db.update("Infomations", values, "id = ?", new String[]{String.valueOf(id)});
                }
            }
            cursor.close();
        }
    }


    // ========== CÁC PHƯƠNG THỨC CHO THU CHI ==========

    // Thêm dữ liệu
    public void insertInfomation(Infomation inf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", inf.getTitle());
        values.put("category", inf.getCategory());
        values.put("date", inf.getDate());
        values.put("price", inf.getPrice());
        values.put("type", inf.getType());
        values.put("timestamp", inf.getTimestamp()); // Thêm timestamp

        db.insert("Infomations", null, values);
        db.close();
    }

    // Lấy tất cả dữ liệu (giữ nguyên)
    public List<Infomation> getInfomationsByType(String type) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations ORDER BY timestamp DESC", null);
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? ORDER BY timestamp DESC", new String[]{type});
        }

        Log.d(TAG, "Getting infomations by type: " + type + ", count: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                Infomation inf = new Infomation();
                inf.setId(cursor.getInt(0));
                inf.setTitle(cursor.getString(1));
                inf.setCategory(cursor.getString(2));
                inf.setDate(cursor.getString(3));
                inf.setTimestamp(cursor.getLong(4));
                inf.setPrice(cursor.getInt(5));
                inf.setType(cursor.getString(6));
                list.add(inf);

                // DEBUG: Log từng item
                Log.d(TAG, "Loaded item - ID: " + inf.getId() + ", Price: " + inf.getPrice() + ", Category: " + inf.getCategory());
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Lấy dữ liệu theo tháng - SẮP XẾP THEO TIMESTAMP
    public List<Infomation> getInfomationsByMonth(String type, String monthYear) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE substr(date, 4, 7) = ? ORDER BY timestamp DESC",
                    new String[]{monthYear});
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? AND substr(date, 4, 7) = ? ORDER BY timestamp DESC",
                    new String[]{type, monthYear});
        }

        if (cursor.moveToFirst()) {
            do {
                Infomation inf = new Infomation();
                inf.setId(cursor.getInt(0));
                inf.setTitle(cursor.getString(1));
                inf.setCategory(cursor.getString(2));
                inf.setDate(cursor.getString(3));
                inf.setTimestamp(cursor.getLong(4));
                inf.setPrice(cursor.getInt(5));
                inf.setType(cursor.getString(6));
                list.add(inf);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }


    // Update thông tin với timestamp
    public void updateInfomation(Infomation info) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", info.getTitle());
        values.put("category", info.getCategory());
        values.put("price", info.getPrice());
        values.put("date", info.getDate());
        values.put("timestamp", info.getTimestamp());
        values.put("type", info.getType());

        int rows = db.update("Infomations", values, "id = ?", new String[]{String.valueOf(info.getId())});
        Log.d(TAG, "Updated infomation - ID: " + info.getId() + ", Rows affected: " + rows + ", New Price: " + info.getPrice());

        db.close();
    }

    public void deleteInfomation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Infomations", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = 'thu'", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        Log.d(TAG, "Total income: " + total);
        cursor.close();
        return total;
    }

    // Lấy tổng chi
    public int getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = 'chi'", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        Log.d(TAG, "Total expense: " + total);
        cursor.close();
        return total;
    }

    // Lấy tổng thu theo tháng
    public int getTotalIncomeByMonth(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = 'thu' AND substr(date, 4, 7) = ?",
                new String[]{monthYear});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    // Lấy tổng chi theo tháng
    public int getTotalExpenseByMonth(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = 'chi' AND substr(date, 4, 7) = ?",
                new String[]{monthYear});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    // Lấy danh sách các tháng có dữ liệu
    public List<String> getMonthsWithData() {
        List<String> months = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT substr(date, 4, 7) as month FROM Infomations ORDER BY month DESC", null);

        if (cursor.moveToFirst()) {
            do {
                months.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return months;
    }

    // Chuyển đổi ngày thành timestamp
    public long convertDateToTimestamp(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(dateStr);
                return date.getTime();
            } catch (ParseException e2) {
                return System.currentTimeMillis();
            }
        }
    }

    // PHƯƠNG THỨC ĐỂ PARSE GIÁ TIỀN TỪ CHUỖI
    public int parsePriceFromString(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0;
        }

        priceStr = priceStr.trim().toLowerCase();

        try {
            // Loại bỏ tất cả ký tự không phải số, dấu chấm, dấu phẩy, k, tr
            priceStr = priceStr.replaceAll("[^0-9.ktr]", "");

            if (priceStr.endsWith("k")) {
                // Xử lý đơn vị "k" (ngàn)
                String numberPart = priceStr.substring(0, priceStr.length() - 1);
                double value;

                if (numberPart.contains(".")) {
                    value = Double.parseDouble(numberPart);
                } else {
                    value = Integer.parseInt(numberPart);
                }

                return (int) (value * 1000);

            } else if (priceStr.endsWith("tr")) {
                // Xử lý đơn vị "tr" (triệu)
                String numberPart = priceStr.substring(0, priceStr.length() - 2);
                double value;

                if (numberPart.contains(".")) {
                    value = Double.parseDouble(numberPart);
                } else {
                    value = Integer.parseInt(numberPart);
                }

                return (int) (value * 1000000);

            } else {
                // Chỉ có số, không có đơn vị
                // Loại bỏ dấu chấm và dấu phẩy phân cách hàng nghìn
                priceStr = priceStr.replace(".", "").replace(",", "");
                return Integer.parseInt(priceStr);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing price: " + priceStr + ", error: " + e.getMessage());
            return 0;
        }
    }

    // DEBUG: Hiển thị tất cả dữ liệu trong database
    public void debugAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Infomations", null);

        Log.d(TAG, "=== DEBUG ALL DATA ===");
        Log.d(TAG, "Total records: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String category = cursor.getString(2);
                String date = cursor.getString(3);
                long timestamp = cursor.getLong(4);
                int price = cursor.getInt(5);
                String type = cursor.getString(6);

                Log.d(TAG, String.format("ID: %d, Category: %s, Price: %d, Type: %s, Date: %s",
                        id, category, price, type, date));
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "=== END DEBUG ===");

        cursor.close();
        db.close();
    }

    // ========== CÁC PHƯƠNG THỨC CHO GHI CHÚ ==========

    // Thêm ghi chú
    public void insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", note.getContent());
        values.put("has_checkbox", note.hasCheckbox() ? 1 : 0);
        values.put("is_checked", note.isChecked() ? 1 : 0);
        values.put("created_date", note.getCreatedDate());
        db.insert("Notes", null, values);
        db.close();
    }

    // Lấy tất cả ghi chú
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Notes ORDER BY created_date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(0));
                note.setContent(cursor.getString(1));
                note.setHasCheckbox(cursor.getInt(2) == 1);
                note.setChecked(cursor.getInt(3) == 1);
                note.setCreatedDate(cursor.getString(4));
                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    // Cập nhật ghi chú
    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", note.getContent());
        values.put("has_checkbox", note.hasCheckbox() ? 1 : 0);
        values.put("is_checked", note.isChecked() ? 1 : 0);
        values.put("created_date", note.getCreatedDate());

        db.update("Notes", values, "id = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    // Xóa ghi chú
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Notes", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Cập nhật trạng thái checkbox
    public void updateNoteCheckStatus(int id, boolean isChecked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_checked", isChecked ? 1 : 0);
        db.update("Notes", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Trong DBHelper.java, thêm phương thức debug
    public void debugInfomationData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, category, price, type FROM Infomations", null);

        System.out.println("=== DEBUG DATABASE ===");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String category = cursor.getString(1);
                int price = cursor.getInt(2);
                String type = cursor.getString(3);
                System.out.println("ID: " + id + ", Category: " + category + ", Price: " + price + ", Type: " + type);
            } while (cursor.moveToNext());
        }
        System.out.println("=== END DEBUG ===");

        cursor.close();
        db.close();
    }



}