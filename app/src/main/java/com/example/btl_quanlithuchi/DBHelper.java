package com.example.btl_quanlithuchi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "WalletDB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE Infomations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " +
                "date TEXT, " +
                "price INTEGER, " +
                "type TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Infomations");
        onCreate(db);
    }

    // Thêm dữ liệu
    public void insertInfomation(Infomation inf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", inf.getTitle());
        values.put("category", inf.getCategory());
        values.put("date", inf.getDate());
        values.put("price", inf.getPrice());
        values.put("type", inf.getType());

        db.insert("Infomations", null, values);
        db.close();
    }

    // Lấy tất cả dữ liệu
    public List<Infomation> getInfomationsByType(String type) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations ORDER BY date DESC", null);
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? ORDER BY date DESC", new String[]{type});
        }

        if (cursor.moveToFirst()) {
            do {
                Infomation inf = new Infomation();
                inf.setId(cursor.getInt(0));
                inf.setTitle(cursor.getString(1));
                inf.setCategory(cursor.getString(2));
                inf.setDate(cursor.getString(3));
                inf.setPrice(cursor.getInt(4));
                inf.setType(cursor.getString(5));
                list.add(inf);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public void updateInfomation(Infomation info) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", info.getTitle());
        values.put("category", info.getCategory());
        values.put("price", info.getPrice());
        values.put("date", info.getDate());
        values.put("type", info.getType());

        db.update("Infomations", values, "id = ?", new String[]{String.valueOf(info.getId())});
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
        cursor.close();
        return total;
    }

    public int getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = 'chi'", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public Map<String, Float> getTotalExpenseByCategory() {
        Map<String, Float> result = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT category, SUM(price) as total " +
                        "FROM Infomations " +
                        "WHERE type = 'chi' " +
                        "GROUP BY category", null
        );

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                float total = cursor.getFloat(cursor.getColumnIndexOrThrow("total"));
                result.put(category, total);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return result;
    }


    public class LineChartData {
        public List<Entry> netEntries; // Tổng (thu - chi)
        public List<String> dateLabels;

        public LineChartData(List<Entry> netEntries, List<String> dateLabels) {
            this.netEntries = netEntries;
            this.dateLabels = dateLabels;
        }
    }


    public LineChartData getLineChartDataByDate() {
        List<Entry> netEntries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();

        String currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT substr(date, 1, 10) as day_only, " +
                        "SUM(CASE WHEN type = 'thu' THEN price ELSE 0 END) as total_thu, " +
                        "SUM(CASE WHEN type = 'chi' THEN price ELSE 0 END) as total_chi " +
                        "FROM Infomations " +
                        "WHERE substr(date, 4, 7) = ? " +     // chỉ lấy trong tháng hiện tại
                        "GROUP BY day_only " +
                        "ORDER BY day_only ASC",
                new String[]{currentMonthYear});

        int index = 0;

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            float thu = cursor.getFloat(1);
            float chi = cursor.getFloat(2);

            float net = thu - chi;

            dateLabels.add(date);
            netEntries.add(new Entry(index, net));
            index++;
        }

        cursor.close();
        db.close();

        return new LineChartData(netEntries, dateLabels);
    }
}
