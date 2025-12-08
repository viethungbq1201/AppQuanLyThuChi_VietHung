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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "WalletDB";
    private static final int DB_VERSION = 2; // Tăng version lên 2

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng thông tin thu chi
        String query = "CREATE TABLE Infomations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " +
                "date TEXT, " +
                "price INTEGER, " +
                "type TEXT)";
        db.execSQL(query);

        // Bảng ghi chú mới
        String createNoteTable = "CREATE TABLE Notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "content TEXT, " +
                "has_checkbox INTEGER DEFAULT 0, " +
                "is_checked INTEGER DEFAULT 0, " +
                "created_date TEXT)";
        db.execSQL(createNoteTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Thêm bảng Notes nếu nâng cấp từ version cũ
            String createNoteTable = "CREATE TABLE IF NOT EXISTS Notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "content TEXT, " +
                    "has_checkbox INTEGER DEFAULT 0, " +
                    "is_checked INTEGER DEFAULT 0, " +
                    "created_date TEXT)";
            db.execSQL(createNoteTable);
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

        db.insert("Infomations", null, values);
        db.close();
    }

    // Lấy tất cả dữ liệu (giữ nguyên)
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

    // Lấy dữ liệu theo tháng
    public List<Infomation> getInfomationsByMonth(String type, String monthYear) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE substr(date, 4, 7) = ? ORDER BY date DESC",
                    new String[]{monthYear});
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? AND substr(date, 4, 7) = ? ORDER BY date DESC",
                    new String[]{type, monthYear});
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



}