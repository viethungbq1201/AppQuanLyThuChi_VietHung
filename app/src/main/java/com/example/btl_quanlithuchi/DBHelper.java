package com.example.btl_quanlithuchi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.btl_quanlithuchi.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "WalletDB";
    private static final int DB_VERSION = 4; // Tăng version để cập nhật bảng Notes
    private static final String TAG = "DBHelper";

    // --- KHAI BÁO CÁC CỘT CHO BẢNG NOTES (Lấy từ file 2) ---
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IS_CHECKBOX = "is_checkbox";
    private static final String COLUMN_IS_CHECKED = "is_checked";
    private static final String COLUMN_IS_GROUP = "is_group";
    private static final String COLUMN_GROUP_NAME = "group_name";
    private static final String COLUMN_GROUP_ID = "group_id";
    private static final String COLUMN_POSITION = "position";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng thông tin thu chi (Giữ nguyên từ file 1)
        String createInfoTable = "CREATE TABLE Infomations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " +
                "date TEXT, " +
                "timestamp INTEGER, " +
                "price INTEGER, " +
                "type TEXT)";
        db.execSQL(createInfoTable);

        // 2. Bảng ghi chú (Sử dụng cấu trúc chi tiết từ file 2)
        String createNoteTable = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_IS_CHECKBOX + " INTEGER DEFAULT 0,"
                + COLUMN_IS_CHECKED + " INTEGER DEFAULT 0,"
                + COLUMN_IS_GROUP + " INTEGER DEFAULT 0,"
                + COLUMN_GROUP_NAME + " TEXT,"
                + COLUMN_GROUP_ID + " INTEGER DEFAULT -1,"
                + COLUMN_POSITION + " INTEGER)";
        db.execSQL(createNoteTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý nâng cấp cho bảng Infomations (Giữ nguyên logic cũ)
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE Infomations ADD COLUMN timestamp INTEGER DEFAULT 0");
                updateTimestampForOldData(db);
            } catch (Exception e) {
                Log.e(TAG, "Column timestamp might already exist");
            }
        }

        // Xử lý nâng cấp cho bảng Notes
        // Vì cấu trúc bảng Notes thay đổi nhiều, ta sẽ xóa bảng cũ và tạo lại để tránh lỗi
        // Lưu ý: Dữ liệu ghi chú cũ sẽ mất nếu version < 4
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            // Cũng xóa bảng Notes cũ nếu tên khác (file 1 đặt tên là Notes viết hoa)
            db.execSQL("DROP TABLE IF EXISTS Notes");

            // Tạo lại bảng Notes mới
            String createNoteTable = "CREATE TABLE " + TABLE_NOTES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CONTENT + " TEXT,"
                    + COLUMN_IS_CHECKBOX + " INTEGER DEFAULT 0,"
                    + COLUMN_IS_CHECKED + " INTEGER DEFAULT 0,"
                    + COLUMN_IS_GROUP + " INTEGER DEFAULT 0,"
                    + COLUMN_GROUP_NAME + " TEXT,"
                    + COLUMN_GROUP_ID + " INTEGER DEFAULT -1,"
                    + COLUMN_POSITION + " INTEGER)";
            db.execSQL(createNoteTable);
        }
    }

    // Hàm phụ trợ để cập nhật timestamp (tách ra cho gọn)
    private void updateTimestampForOldData(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT id, date FROM Infomations", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String dateStr = cursor.getString(1);
            long timestamp;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(dateStr);
                timestamp = date.getTime();
            } catch (Exception e) {
                timestamp = System.currentTimeMillis();
            }
            ContentValues values = new ContentValues();
            values.put("timestamp", timestamp);
            db.update("Infomations", values, "id = ?", new String[]{String.valueOf(id)});
        }
        cursor.close();
    }


    // ====================================================================
    // PHẦN 1: CÁC PHƯƠNG THỨC CHO THU CHI (INFOMATION) - TỪ FILE 1
    // ====================================================================

    public void insertInfomation(Infomation inf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", inf.getTitle());
        values.put("category", inf.getCategory());
        values.put("date", inf.getDate());
        values.put("price", inf.getPrice());
        values.put("type", inf.getType());
        values.put("timestamp", inf.getTimestamp());
        db.insert("Infomations", null, values);
        db.close();
    }

    public List<Infomation> getInfomationsByType(String type) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations ORDER BY timestamp DESC", null);
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? ORDER BY timestamp DESC", new String[]{type});
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

    public List<Infomation> getInfomationsByMonth(String type, String monthYear) {
        List<Infomation> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (type.equals("all")) {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE substr(date, 4, 7) = ? ORDER BY timestamp DESC", new String[]{monthYear});
        } else {
            cursor = db.rawQuery("SELECT * FROM Infomations WHERE type = ? AND substr(date, 4, 7) = ? ORDER BY timestamp DESC", new String[]{type, monthYear});
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

    public void updateInfomation(Infomation info) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", info.getTitle());
        values.put("category", info.getCategory());
        values.put("price", info.getPrice());
        values.put("date", info.getDate());
        values.put("timestamp", info.getTimestamp());
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
        return getTotalAmount("thu");
    }

    public int getTotalExpense() {
        return getTotalAmount("chi");
    }

    private int getTotalAmount(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = ?", new String[]{type});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public int getTotalIncomeByMonth(String monthYear) {
        return getTotalAmountByMonth("thu", monthYear);
    }

    public int getTotalExpenseByMonth(String monthYear) {
        return getTotalAmountByMonth("chi", monthYear);
    }

    private int getTotalAmountByMonth(String type, String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price) FROM Infomations WHERE type = ? AND substr(date, 4, 7) = ?", new String[]{type, monthYear});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

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

    // Tiện ích: Chuyển đổi ngày thành timestamp
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

    // Tiện ích: Parse giá tiền
    public int parsePriceFromString(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) return 0;
        priceStr = priceStr.trim().toLowerCase();
        try {
            priceStr = priceStr.replaceAll("[^0-9.ktr]", "");
            if (priceStr.endsWith("k")) {
                String numberPart = priceStr.substring(0, priceStr.length() - 1);
                return (int) (Double.parseDouble(numberPart) * 1000);
            } else if (priceStr.endsWith("tr")) {
                String numberPart = priceStr.substring(0, priceStr.length() - 2);
                return (int) (Double.parseDouble(numberPart) * 1000000);
            } else {
                priceStr = priceStr.replace(".", "").replace(",", "");
                return Integer.parseInt(priceStr);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Phương thức chuẩn hóa tên danh mục: viết hoa chữ cái đầu mỗi từ
    public static String capitalizeCategory(String category) {
        if (category == null || category.isEmpty()) {
            return category;
        }

        // Loại bỏ khoảng trắng đầu/cuối
        category = category.trim();

        // Viết hoa chữ cái đầu, viết thường các chữ còn lại
        return category.substring(0, 1).toUpperCase() +
                category.substring(1).toLowerCase();
    }

    // ====================================================================
    // PHẦN 2: CÁC PHƯƠNG THỨC CHO GHI CHÚ (NOTES) - TỪ FILE 2
    // ====================================================================

    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("content", note.getContent()); // Sửa lại tên cột theo đúng DB của bạn
        values.put("is_checkbox", note.isCheckbox() ? 1 : 0);
        values.put("is_checked", note.isChecked() ? 1 : 0);
        values.put("is_group", note.isGroup() ? 1 : 0);
        values.put("group_name", note.getGroupName());
        values.put("group_id", note.getGroupId());
        values.put("position", note.getPosition());

        // Quan trọng: insert trả về ID của dòng mới, hoặc -1 nếu lỗi
        long id = db.insert("notes", null, values); // Sửa "notes" thành tên bảng của bạn
        db.close();
        return id;
    }

    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_ID, COLUMN_CONTENT, COLUMN_IS_CHECKBOX,
                        COLUMN_IS_CHECKED, COLUMN_IS_GROUP, COLUMN_GROUP_NAME,
                        COLUMN_GROUP_ID, COLUMN_POSITION},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            note.setId(cursor.getInt(0));
            note.setContent(cursor.getString(1));
            note.setCheckbox(cursor.getInt(2) == 1);
            note.setChecked(cursor.getInt(3) == 1);
            note.setGroup(cursor.getInt(4) == 1);
            note.setGroupName(cursor.getString(5));
            note.setGroupId(cursor.getInt(6));
            note.setPosition(cursor.getInt(7));
        }

        if (cursor != null) cursor.close();
        return note;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        // Sắp xếp theo vị trí
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_POSITION + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(0));
                note.setContent(cursor.getString(1));
                note.setCheckbox(cursor.getInt(2) == 1);
                note.setChecked(cursor.getInt(3) == 1);
                note.setGroup(cursor.getInt(4) == 1);
                note.setGroupName(cursor.getString(5));
                note.setGroupId(cursor.getInt(6));
                note.setPosition(cursor.getInt(7));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_IS_CHECKBOX, note.isCheckbox() ? 1 : 0);
        values.put(COLUMN_IS_CHECKED, note.isChecked() ? 1 : 0);
        values.put(COLUMN_IS_GROUP, note.isGroup() ? 1 : 0);
        values.put(COLUMN_GROUP_NAME, note.getGroupName());
        values.put(COLUMN_GROUP_ID, note.getGroupId());
        values.put(COLUMN_POSITION, note.getPosition());

        int rows = db.update(TABLE_NOTES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
        return rows;
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateNotePosition(int id, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POSITION, position);
        db.update(TABLE_NOTES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getNextGroupId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(" + COLUMN_GROUP_ID + ") FROM " + TABLE_NOTES;
        Cursor cursor = db.rawQuery(query, null);

        int maxId = 0;
        if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
            maxId = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return maxId + 1;
    }
}