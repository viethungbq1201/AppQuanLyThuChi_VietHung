package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trangnote_Fragment extends Fragment implements NoteAdapter.OnNoteListener {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private FloatingActionButton fabAdd;
    private Button btnDeleteAll;
    private ImageView btnVoiceInput;
    private DBHelper dbHelper;
    private VoiceInputHelper voiceInputHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trang_note, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_notes);
        fabAdd = view.findViewById(R.id.fab_add_note);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all_notes);
        btnVoiceInput = view.findViewById(R.id.btn_voice_input_note);

        dbHelper = new DBHelper(getContext());
        voiceInputHelper = new VoiceInputHelper(getContext(), this);
        
        // Hide voice input if not available
        if (!voiceInputHelper.isSpeechAvailable()) {
            btnVoiceInput.setVisibility(View.GONE);
        }

        voiceInputHelper.setListener(new VoiceInputHelper.VoiceListener() {
            @Override
            public void onVoiceResult(String text) {
                addNewNoteWithContent(text);
            }

            @Override
            public void onVoiceError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onListeningStarted() {
                Toast.makeText(getContext(), "Đang nghe...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onListeningStopped() {
                // Done listening
            }
        });

        setupRecyclerView();
        loadNotes();

        fabAdd.setOnClickListener(v -> addNewNote());
        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());
        btnVoiceInput.setOnClickListener(v -> voiceInputHelper.startListening());

        return view;
    }

    private void showDeleteAllConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tất cả?")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả ghi chú?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAllNotes())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAllNotes() {
        new Thread(() -> {
            List<Note> allNotes = noteAdapter.getNotesListInternal();
            for (Note n : allNotes) {
                dbHelper.deleteNote(n.getId());
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                noteAdapter.setNotes(new ArrayList<>());
                Toast.makeText(getContext(), "Đã xóa tất cả ghi chú", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void addNewNoteWithContent(String content) {
        Note newNote = new Note(content);
        newNote.setPosition(0);

        long id = dbHelper.addNote(newNote);
        if (id != -1) {
            newNote.setId((int) id);
            noteAdapter.addNoteToTop(newNote);
            recyclerView.scrollToPosition(0);
            syncPositions();
        }
    }

    private void syncPositions() {
        new Thread(() -> {
            List<Note> allNotes = noteAdapter.getNotesListInternal();
            for (Note n : allNotes) {
                dbHelper.updateNotePosition(n.getId(), n.getPosition());
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        voiceInputHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        voiceInputHelper.handlePermissionResult(requestCode, permissions, grantResults);
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter(getContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Giúp bàn phím đẩy view lên mượt hơn
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null); // Tắt animation để tránh giật focus
        recyclerView.setAdapter(noteAdapter);

        // Kéo thả sắp xếp và Vuốt để xóa
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Note n = noteAdapter.notes.get(position);
                
                if (n.isGroup()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Xóa nhóm?")
                            .setMessage("Bạn có muốn xóa toàn bộ nhóm này không?")
                            .setPositiveButton("Xóa", (dialog, which) -> noteAdapter.deleteGroup(n.getGroupId()))
                            .setNegativeButton("Hủy", (dialog, which) -> noteAdapter.notifyItemChanged(position))
                            .show();
                } else {
                    noteAdapter.deleteItem(position);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                onRequestSyncDatabase(); // Lưu vị trí sau khi thả hoặc trước khi thoát fragment
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
    public void onNoteAddedAfter(int position, int groupId) {
        Note newNote = new Note("");
        newNote.setCheckbox(true);
        newNote.setGroupId(groupId);
        newNote.setPosition(position + 1);

        long id = dbHelper.addNote(newNote);
        if (id != -1) {
            newNote.setId((int) id);
            noteAdapter.notes.add(position + 1, newNote);
            noteAdapter.notifyItemInserted(position + 1);
            noteAdapter.notifyItemRangeChanged(position + 1, noteAdapter.getItemCount() - position - 1);
            
            // Sync positions and scroll
            onRequestSyncDatabase();
            
            // Focus the new item after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                recyclerView.scrollToPosition(position + 1);
            }, 100);
        }
    }

    @Override
    public void onRequestSyncDatabase() {
        if (noteAdapter == null || dbHelper == null) return;
        List<Note> allNotes = noteAdapter.getNotesListInternal();
        for (int i = 0; i < allNotes.size(); i++) {
            Note note = allNotes.get(i);
            note.setPosition(i); // Đảm bảo vị trí đúng

            if (note.getId() <= 0) {
                long id = dbHelper.addNote(note);
                if(id > 0) note.setId((int) id);
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