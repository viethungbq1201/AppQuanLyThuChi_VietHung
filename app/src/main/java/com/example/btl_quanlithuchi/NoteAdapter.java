package com.example.btl_quanlithuchi;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_CHECKBOX = 1;

    public List<Note> notes;
    private Context context;
    private OnNoteListener onNoteListener;

    // Biến lưu vị trí đang được focus để hiện thanh công cụ
    private int currentFocusedPosition = -1;

    public interface OnNoteListener {
        void onNoteUpdated(Note note);
        void onNoteDeleted(int noteId);
        void onNoteAdded(Note note);
        void onRequestSyncDatabase();
        void onScrollToPosition(int position);
    }

    public NoteAdapter(Context context, OnNoteListener onNoteListener) {
        this.context = context;
        this.onNoteListener = onNoteListener;
        this.notes = new ArrayList<>();
    }

    public void setNotes(List<Note> notes) {
        this.notes = new ArrayList<>(notes);
        sortNotes();
        notifyDataSetChanged();
    }

    // Hàm chèn vào đầu danh sách (dùng cho nút FAB Add)
    public void addNoteToTop(Note note) {
        notes.add(0, note);
        reindexPositions();
        notifyItemInserted(0);
        requestKeyboardFocus(0);
    }

    // Hàm chèn xuống dưới dòng hiện tại (dùng cho phím Enter)
    public void addNoteBelow(Note note, int index) {
        notes.add(index, note);
        reindexPositions();
        notifyItemInserted(index);
        requestKeyboardFocus(index);
    }

    // Logic sắp xếp: Chưa tick ở trên, Đã tick ở dưới
    private void sortNotes() {
        Collections.sort(notes, (n1, n2) -> {
            if (n1.isChecked() != n2.isChecked()) {
                return n1.isChecked() ? 1 : -1;
            }
            return Integer.compare(n1.getPosition(), n2.getPosition());
        });
    }

    public List<Note> getNotesListInternal() { return notes; }

    @Override
    public int getItemCount() { return notes.size(); }

    @Override
    public int getItemViewType(int position) {
        return notes.get(position).isCheckbox() ? TYPE_CHECKBOX : TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CHECKBOX) {
            View view = inflater.inflate(R.layout.item_note_checkbox, parent, false);
            return new CheckboxViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_note_text, parent, false);
            return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(position);
        } else if (holder instanceof CheckboxViewHolder) {
            ((CheckboxViewHolder) holder).bind(position);
        }
    }

    // ==========================================
    // VIEW HOLDER TEXT (Ghi chú thường)
    // ==========================================
    class TextViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout, layoutActions;
        EditText etContent;
        ImageView btnDelete;
        TextView btnOk;

        // Cờ hiệu để tránh vòng lặp vô hạn khi setText trong TextWatcher
        private boolean isUpdating = false;

        TextViewHolder(View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);
            layoutActions = itemView.findViewById(R.id.layout_actions);
            etContent = itemView.findViewById(R.id.et_note_content);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnOk = itemView.findViewById(R.id.btn_ok);

            rootLayout.setOnClickListener(v -> {
                etContent.requestFocus();
                showKeyboard(etContent);
            });

            etContent.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    currentFocusedPosition = getAdapterPosition();
                    layoutActions.setVisibility(View.VISIBLE);
                } else {
                    layoutActions.setVisibility(View.GONE);
                    if (!isUpdating) {
                        saveContent(getAdapterPosition(), etContent.getText().toString());
                    }
                }
            });

            btnOk.setOnClickListener(v -> {
                etContent.clearFocus();
                currentFocusedPosition = -1;
                hideKeyboard(itemView);
            });
            btnDelete.setOnClickListener(v -> deleteItem(getAdapterPosition()));

            // SỬ DỤNG TEXTWATCHER ĐỂ BẮT SỰ KIỆN ENTER TRÊN ĐIỆN THOẠI
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isUpdating) return;

                    String text = s.toString();
                    if (text.contains("\n")) { // Phát hiện xuống dòng
                        isUpdating = true; // Khóa cập nhật

                        // Xóa ký tự xuống dòng
                        String cleanText = text.replace("\n", "");
                        etContent.setText(cleanText);
                        etContent.setSelection(cleanText.length());

                        // Thực hiện logic chuyển đổi thành Checkbox
                        convertTextToCheckboxList(getAdapterPosition(), cleanText);

                        isUpdating = false; // Mở khóa
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        void bind(int position) {
            Note note = notes.get(position);

            isUpdating = true; // Khóa khi bind data
            etContent.setText(note.getContent());
            isUpdating = false;

            if (position == currentFocusedPosition) {
                layoutActions.setVisibility(View.VISIBLE);
                etContent.requestFocus();
                etContent.post(() -> showKeyboard(etContent));
            } else {
                layoutActions.setVisibility(View.GONE);
            }
        }
    }

    // ==========================================
    // VIEW HOLDER CHECKBOX (Ghi chú dạng list)
    class CheckboxViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout, layoutActions;
        CheckBox checkBox;
        EditText etContent;
        ImageView btnDelete;
        TextView btnOk;

        LinearLayout layoutGroupHeader;
        EditText etGroupTitle;
        private boolean isUpdating = false;
        private boolean isUpdatingGroup = false;

        CheckboxViewHolder(View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);
            layoutActions = itemView.findViewById(R.id.layout_actions);
            layoutGroupHeader = itemView.findViewById(R.id.layout_group_header);
            etGroupTitle = itemView.findViewById(R.id.et_group_title);
            checkBox = itemView.findViewById(R.id.checkbox_note);
            etContent = itemView.findViewById(R.id.et_checkbox_content);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnOk = itemView.findViewById(R.id.btn_ok);

            rootLayout.setOnClickListener(v -> {
                etContent.requestFocus();
                showKeyboard(etContent);
            });

            etContent.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    currentFocusedPosition = getAdapterPosition();
                    layoutActions.setVisibility(View.VISIBLE);
                } else {
                    layoutActions.setVisibility(View.GONE);
                    if (!isUpdating) {
                        saveContent(getAdapterPosition(), etContent.getText().toString());
                    }
                }
            });

            btnOk.setOnClickListener(v -> {
                etContent.clearFocus();
                currentFocusedPosition = -1;
                hideKeyboard(itemView);
            });
            btnDelete.setOnClickListener(v -> deleteItem(getAdapterPosition()));

            checkBox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Note note = notes.get(pos);
                    boolean isChecked = checkBox.isChecked();
                    note.setChecked(isChecked);
                    updateAppearance(isChecked);
                    onNoteListener.onNoteUpdated(note);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        sortNotes();
                        notifyDataSetChanged();
                        if (currentFocusedPosition == pos) {
                            currentFocusedPosition = -1;
                            hideKeyboard(itemView);
                        }
                    }, 300);
                }
            });

            // SỬ DỤNG TEXTWATCHER CHO CHECKBOX (Thay vì OnKeyListener)
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isUpdating) return;

                    String text = s.toString();
                    if (text.contains("\n")) { // Phát hiện xuống dòng
                        isUpdating = true;

                        String cleanText = text.replace("\n", "");
                        etContent.setText(cleanText);
                        etContent.setSelection(cleanText.length());

                        // Lưu nội dung hiện tại
                        saveContent(getAdapterPosition(), cleanText);

                        // Tạo dòng mới
                        addNewCheckboxBelow(getAdapterPosition());

                        isUpdating = false;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        void bind(int position) {
            Note note = notes.get(position);
            checkBox.setOnClickListener(null); // Gỡ listener tạm thời

            isUpdating = true;
            etContent.setText(note.getContent());
            isUpdating = false;

            checkBox.setChecked(note.isChecked());
            updateAppearance(note.isChecked());

            boolean isFirstInGroup = false;
            // Check if this note is the first unchecked checkbox in a consecutive run
            if (!note.isChecked()) {
                if (position == 0) {
                    isFirstInGroup = true;
                } else {
                    Note prevNote = notes.get(position - 1);
                    if (prevNote.isCheckbox() != true || prevNote.isChecked()) {
                        isFirstInGroup = true;
                    }
                }
            }

            if (isFirstInGroup) {
                layoutGroupHeader.setVisibility(View.VISIBLE);
                isUpdatingGroup = true;
                etGroupTitle.setText(note.getGroupName() != null ? note.getGroupName() : "");
                isUpdatingGroup = false;
            } else {
                layoutGroupHeader.setVisibility(View.GONE);
            }

            // Gắn lại listener click
            checkBox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Note n = notes.get(pos);
                    n.setChecked(checkBox.isChecked());
                    updateAppearance(checkBox.isChecked());
                    onNoteListener.onNoteUpdated(n);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        sortNotes();
                        notifyDataSetChanged();
                    }, 300);
                }
            });

            if (position == currentFocusedPosition) {
                layoutActions.setVisibility(View.VISIBLE);
                etContent.requestFocus();
                etContent.post(() -> showKeyboard(etContent));
            } else {
                layoutActions.setVisibility(View.GONE);
            }
        }

        void updateAppearance(boolean isChecked) {
            if (isChecked) {
                etContent.setAlpha(0.5f);
                etContent.setPaintFlags(etContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                etContent.setAlpha(1.0f);
                etContent.setPaintFlags(etContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    // ==========================================
    // LOGIC XỬ LÝ (CORE)
    // ==========================================

    private void convertTextToCheckboxList(int position, String currentContent) {
        if(position < 0 || position >= notes.size()) return;

        // Biến dòng hiện tại thành checkbox nhưng giữ nguyên vị trí
        Note currentNote = notes.get(position);
        currentNote.setCheckbox(true);
        currentNote.setContent(currentContent);

        // Tạo checkbox mới để chèn xuống dưới
        Note newNote = new Note("");
        newNote.setCheckbox(true);

        // Chèn vào ngay bên dưới dòng hiện tại (position + 1)
        addNoteBelow(newNote, position + 1);

        onNoteListener.onRequestSyncDatabase();
    }

    private void addNewCheckboxBelow(int position) {
        Note newNote = new Note("");
        newNote.setCheckbox(true);

        // Chèn vào ngay bên dưới
        addNoteBelow(newNote, position + 1);

        onNoteListener.onNoteAdded(newNote);
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < notes.size()) {
            int id = notes.get(position).getId();
            notes.remove(position);
            reindexPositions();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notes.size());
            onNoteListener.onNoteDeleted(id);
        }
    }

    private void saveContent(int position, String content) {
        if (position >= 0 && position < notes.size()) {
            Note note = notes.get(position);
            if (!content.equals(note.getContent())) {
                note.setContent(content);
                onNoteListener.onNoteUpdated(note);
            }
        }
    }

    private void reindexPositions() {
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).setPosition(i);
        }
    }

    private void requestKeyboardFocus(int position) {
        currentFocusedPosition = position;
        onNoteListener.onScrollToPosition(position);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}