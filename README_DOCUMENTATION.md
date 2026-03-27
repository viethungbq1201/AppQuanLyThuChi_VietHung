# Ứng dụng Quản lý Thu chi - Tài liệu Toàn diện / Wallet Management App - Comprehensive Documentation

Tài liệu này cung cấp cái nhìn chi tiết nhất về cả tính năng và kỹ thuật triển khai của ứng dụng. / This document provides the most detailed overview of both features and technical implementation of the app.

---

## 1. Tổng quan & Kiến trúc / Overview & Architecture

### Ngôn ngữ & Công nghệ / Languages & Technology
- **Ngôn ngữ / Language**: Java
- **Cơ sở dữ liệu / Database**: SQLite (`DBHelper.java`)
- **Giao diện / UI**: Fragment-based navigation, ViewPager2, MPAndroidChart, Material Design.

---

## 2. Thành phần Cốt lõi / Core Components

### 2.1. MainActivity.java
Hành động chính điều phối giao diện và quản lý Theme. / Primary activity coordinating the UI and Theme management.
- **EN**:
    - **`onCreate()`**: Restores theme from `SharedPreferences` and sets up `ViewPager2` with `TabLayoutMediator`.
    - **`toggleTheme()`**: Switches between Light and Dark mode using `AppCompatDelegate.setDefaultNightMode()` and `recreate()`.
    - **`addControl()`**: Sets up `ViewPagerAdapter` for the 4 main tabs: Home, Income, Expense, Note.
- **VN**:
    - **`onCreate()`**: Khôi phục theme từ `SharedPreferences` và thiết lập `ViewPager2`.
    - **`toggleTheme()`**: Chuyển đổi giữa chế độ Sáng và Tối, sử dụng `recreate()` để áp dụng theme mới ngay lập tức.
    - **`addControl()`**: Thiết lập bộ nạp cho 4 tab chính: Home, Income, Expense, Note.

### 2.2. DBHelper.java
Trung tâm xử lý dữ liệu SQLite. / The SQLite data processing center.
- **EN**:
    - **`parsePriceFromString(String)`**: Intelligent Vietnamese price parsing. Converts "50k" -> 50,000; "1.2tr" -> 1,200,000; removes non-numeric characters.
    - **`getInfomationsByMonth(String, String)`**: Filters transactions using SQL `substr(date, 4, 7)` to match "MM/yyyy" format.
    - **`getDefaultPriceForCategory(String)`**: Contains default pricing rules: Salary (5M), Parking (3k-5k), Breakfast (30k)...
    - **`updateNotePosition(int, int)`**: Updates the database position to maintain order after drag-and-drop.
- **VN**:
    - **`parsePriceFromString(String)`**: Logic xử lý tiền tiếng Việt thông minh. Chuyển "50k" -> 50,000; "1.2tr" -> 1,200,000.
    - **`getInfomationsByMonth(String, String)`**: Lọc giao dịch bằng SQL `substr(date, 4, 7)` theo định dạng "MM/yyyy".
    - **`getDefaultPriceForCategory(String)`**: Bộ quy tắc giá mặc định: Lương (5tr), Gửi xe (3k-5k), Ăn sáng (30k)...
    - **`updateNotePosition(int, int)`**: Cập nhật vị trí để duy trì thứ tự sắp xếp sau khi kéo thả.

---

## 3. Giao diện & Các Tab / UI & Fragments

### 3.1. Trangchu_Fragment.java (Dashboard)
- **EN**:
    - **`setupPieChart()`**: Uses `PieEntry` to load data. Sets colors (Green for Income, Red for Expense) and entry animations.
    - **`loadData()`**: Calculates balance (Income - Expense) and populates the Recent Transactions list.
- **VN**:
    - **`setupPieChart()`**: Sử dụng `PieEntry` để nạp dữ liệu. Thiết lập màu sắc (Xanh cho Thu, Đỏ cho Chi).
    - **`loadData()`**: Tính toán số dư (Tổng Thu - Tổng Chi) và cập nhật danh sách Giao dịch gần đây.

### 3.2. Trangthu & Trangchi (Income & Expense)
- **EN**:
    - **`showAddEntryDialog()`**: Inflates `R.layout.dialog_add` for manual data entry with category suggestions.
    - **`checkBalanceAndNotify()`**: Instant balance check. If negative, triggers a high-priority `NotificationChannel` with the message "You are poor now".
- **VN**:
    - **`showAddEntryDialog()`**: Hiển thị Dialog thêm mới với các gợi ý danh mục và giá tiền.
    - **`checkBalanceAndNotify()`**: Kiểm tra số dư tức thì. Nếu âm, sẽ đẩy thông báo `NotificationChannel` với nội dung "Bạn nghèo rồi".

### 3.3. Trangnote_Fragment.java (Notes & Checklist)
- **EN**:
    - **`setupRecyclerView()`**: Implements `ItemTouchHelper` for:
        - `onMove`: Swaps items and calls `syncPositions()` to save order to DB.
        - `onSwiped`: Deletes item or shows confirmation for deleting an entire group.
    - **`addNewNote()`**: Spam prevention: If the top note is empty, it focuses it instead of creating a new one.
- **VN**:
    - **`setupRecyclerView()`**: Sử dụng `ItemTouchHelper` để:
        - `onMove`: Hoán đổi vị trí và gọi `syncPositions()` để lưu vào DB.
        - `onSwiped`: Xoá mục hoặc xác nhận xoá cả nhóm.
    - **`addNewNote()`**: Chống spam: Nếu ghi chú đầu tiên đang trống, hệ thống sẽ focus vào đó thay vì tạo mới.

---

## 4. Bộ nạp dữ liệu / Data Adapters

### 4.1. InfomationAdapter.java
- **EN**: Mapping keywords to appropriate icons (e.g., "phở" -> `ic_food`) and handling transaction editing/deletion.
- **VN**: Ánh xạ từ khóa sang icon (ví dụ: "phở" -> `ic_food`) và xử lý việc sửa/xoá giao dịch qua Dialog.

### 4.2. NoteAdapter.java (Advanced Checklist System)
- **EN**:
    - **Multi-View Types**: Handles regular text, checkbox items, and group headers.
    - **`applyLayout()`**: Creates the visual "group" effect using specialized backgrounds (`bg_group_top`, `bg_group_middle`, `bg_group_bottom`).
    - **`convertToChecklist()`**: Splits multi-line text into individual checkbox items linked by a `group_id`.
    - **`revertToNote()`**: Reassembles checkbox contents back into a single multi-line text note.
- **VN**:
    - **ViewType đa dạng**: Xử lý văn bản thường, mục checklist và tiêu đề nhóm.
    - **`applyLayout()`**: Tạo hiệu ứng thị giác "nhóm" bằng các loại background bo góc chuyên biệt.
    - **`convertToChecklist()`**: Chia nhỏ văn bản thành các mục checklist có chung `group_id`.
    - **`revertToNote()`**: Ghép nội dung checklist lại thành một ghi chú văn bản duy nhất.

---

## 5. Điểm nhấn Kỹ thuật / Technical Highlights

1. **Keyboard Management**: Uses `InputMethodManager` to ensure the keyboard appears instantly when clicking "Add". / Sử dụng `InputMethodManager` để hiện bàn phím ngay lập tức khi nhấn "Thêm".
2. **Background Threading**: Heavy DB operations (loading, deleting all) are moved to `new Thread()` to keep the UI smooth. / Các thao tác DB nặng (load, xoá tất cả) được chạy trong luồng phụ (`Thread`) để giao diện mượt mà.
3. **Currency Formatting**: Uses `DecimalFormat("#,###")` for localized currency presentation like "1.500.000 đ". / Sử dụng định dạng tiền tệ chuyên nghiệp dạng "1.500.000 đ".
4. **Visual Grouping**: Elevation and corner-radius logic that visually binds related checklist items together. / Logic xử lý đổ bóng và bo góc giúp các mục checklist có cảm giác gắn kết thành một khối.
