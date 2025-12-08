package com.example.btl_quanlithuchi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class Trangnote_Fragment extends Fragment implements NoteAdapter.OnNoteListener {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private FloatingActionButton fabAdd;
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_note, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_notes);
        fabAdd = view.findViewById(R.id.fab_add_note);


        dbHelper = new DBHelper(getContext());

        setupRecyclerView();
        loadNotes();

        fabAdd.setOnClickListener(v -> addNewNote());

        return view;
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter(getContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Giúp bàn phím đẩy view lên mượt hơn
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null); // Tắt animation để tránh giật focus
        recyclerView.setAdapter(noteAdapter);

        // Kéo thả sắp xếp
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();

                // Chỉ cho phép sắp xếp các item cùng loại (cùng tick hoặc cùng chưa tick)
                if (noteAdapter.notes.get(fromPos).isChecked() == noteAdapter.notes.get(toPos).isChecked()) {
                    Collections.swap(noteAdapter.notes, fromPos, toPos);
                    noteAdapter.notifyItemMoved(fromPos, toPos);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                onRequestSyncDatabase(); // Lưu vị trí sau khi thả
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes = dbHelper.getAllNotes();
            new Handler(Looper.getMainLooper()).post(() -> {
                noteAdapter.setNotes(notes);
            });
        }).start();
    }

    private void addNewNote() {
        Note newNote = new Note("");
        newNote.setPosition(0); // Quan trọng: Đặt vị trí là 0 để lên đầu

        long id = dbHelper.addNote(newNote);
        if (id != -1) {
            newNote.setId((int) id);

            // Gọi hàm mới trong Adapter để chèn lên đầu
            noteAdapter.addNoteToTop(newNote);

            // Cuộn lên đầu trang
            recyclerView.scrollToPosition(0);

            // Vì ta chèn vào đầu, toàn bộ các note cũ phải lùi position +1
            // Gọi sync để cập nhật lại position trong Database cho chuẩn
            // (Chạy ngầm để không giật UI)
            new Thread(() -> {
                List<Note> allNotes = noteAdapter.getNotesListInternal();
                for (Note n : allNotes) {
                    dbHelper.updateNotePosition(n.getId(), n.getPosition());
                }
            }).start();
        }
    }

    // --- CÁC HÀM INTERFACE ---

    @Override
    public void onNoteUpdated(Note note) { dbHelper.updateNote(note); }

    @Override
    public void onNoteDeleted(int noteId) { dbHelper.deleteNote(noteId); }

    @Override
    public void onNoteAdded(Note note) {
        long id = dbHelper.addNote(note);
        note.setId((int) id);
    }

    @Override
    public void onRequestSyncDatabase() {
        List<Note> allNotes = noteAdapter.getNotesListInternal();
        for (int i = 0; i < allNotes.size(); i++) {
            Note note = allNotes.get(i);
            note.setPosition(i); // Đảm bảo vị trí đúng

            if (note.getId() == 0) {
                long id = dbHelper.addNote(note);
                note.setId((int) id);
            } else {
                dbHelper.updateNote(note);
            }
        }
    }

    @Override
    public void onScrollToPosition(int position) {
        // Cuộn tới vị trí chỉ định
        recyclerView.smoothScrollToPosition(position);
    }
}