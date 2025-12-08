package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trangnote_Fragment extends Fragment {

    private RecyclerView rvNotes;
    private NoteAdapter noteAdapter;
    private DBHelper dbHelper;
    private FloatingActionButton fabAddNote;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_note, container, false);

        dbHelper = new DBHelper(getContext());
        rvNotes = view.findViewById(R.id.rvNotes);
        fabAddNote = view.findViewById(R.id.fabAddNote);

        // Setup RecyclerView
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Note> noteList = dbHelper.getAllNotes();
        noteAdapter = new NoteAdapter(getContext(), noteList);
        rvNotes.setAdapter(noteAdapter);

        // Nút thêm ghi chú
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNoteDialog();
            }
        });

        return view;
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etContent = view.findViewById(R.id.etContent);
        CheckBox cbHasCheckbox = view.findViewById(R.id.cbHasCheckbox);
        CheckBox cbIsChecked = view.findViewById(R.id.cbIsChecked);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Ẩn checkbox "Đã hoàn thành" ban đầu
        cbIsChecked.setVisibility(View.GONE);

        cbHasCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cbIsChecked.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getContext(), "Vui lòng nhập nội dung ghi chú", Toast.LENGTH_SHORT).show();
                    return;
                }

                Note note = new Note();
                note.setContent(content);
                note.setHasCheckbox(cbHasCheckbox.isChecked());
                note.setChecked(cbIsChecked.isChecked());
                note.setCreatedDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                dbHelper.insertNote(note);

                // Cập nhật danh sách
                List<Note> updatedList = dbHelper.getAllNotes();
                noteAdapter.setData(updatedList);

                Toast.makeText(getContext(), "Đã thêm ghi chú", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null && noteAdapter != null) {
            List<Note> updatedList = dbHelper.getAllNotes();
            noteAdapter.setData(updatedList);
        }
    }
}