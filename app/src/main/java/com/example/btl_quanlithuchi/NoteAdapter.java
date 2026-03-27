package com.example.btl_quanlithuchi;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_CHECKBOX = 1;
    private static final int TYPE_GROUP_HEADER = 2;

    public List<Note> notes = new ArrayList<>();
    private Context context;
    private OnNoteListener listener;

    private int currentFocusedPosition = -1;

    public void setFocusPosition(int pos) {
        this.currentFocusedPosition = pos;
    }

    public interface OnNoteListener {
        void onNoteUpdated(Note note);
        void onNoteDeleted(int id);
        void onNoteAdded(Note note);
        void onNoteAddedAfter(int position, int groupId);
        void onRequestSyncDatabase();
        void onScrollToPosition(int pos);
    }

    public NoteAdapter(Context context, OnNoteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // =========================
    // BASIC
    // =========================
    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        Note n = notes.get(position);
        if (n.isGroup()) return TYPE_GROUP_HEADER;
        return n.isCheckbox() ? TYPE_CHECKBOX : TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (type == TYPE_GROUP_HEADER) {
            return new GroupVH(inflater.inflate(R.layout.item_note_group_header, parent, false));
        } else if (type == TYPE_CHECKBOX) {
            return new CheckboxVH(inflater.inflate(R.layout.item_note_checkbox, parent, false));
        }
        return new TextVH(inflater.inflate(R.layout.item_note_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextVH) ((TextVH) holder).bind(position);
        else if (holder instanceof CheckboxVH) ((CheckboxVH) holder).bind(position);
        else ((GroupVH) holder).bind(position);
    }

    public void setNotes(List<Note> list) {
        notes = new ArrayList<>(list);
        sort();
        notifyDataSetChanged();
    }

    // =========================
    // TEXT
    // =========================
    class TextVH extends RecyclerView.ViewHolder {
        LinearLayout root, actions;
        EditText et;
        ImageView delete, convert;
        TextView ok;
        boolean isUpdating = false;

        TextVH(View v) {
            super(v);
            root = v.findViewById(R.id.root_layout);
            actions = v.findViewById(R.id.layout_actions);
            et = v.findViewById(R.id.et_note_content);
            delete = v.findViewById(R.id.btn_delete);
            convert = v.findViewById(R.id.btn_switch_note_type);
            ok = v.findViewById(R.id.btn_ok);

            root.setOnClickListener(v1 -> focus());

            et.setOnFocusChangeListener((v12, hasFocus) -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (hasFocus) {
                    currentFocusedPosition = pos;
                    actions.setVisibility(View.VISIBLE);
                } else {
                    actions.setVisibility(View.GONE);
                    save(pos, et.getText().toString());
                }
            });

            ok.setOnClickListener(view -> clearFocus());
            delete.setOnClickListener(view -> deleteItem(getAdapterPosition()));

            convert.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    convertToChecklist(pos, et.getText().toString());
                }
            });
        }

        void bind(int pos) {
            Note n = notes.get(pos);

            isUpdating = true;
            et.setText(n.getContent());
            isUpdating = false;

            applyLayout(root, n, pos);

            if (pos == currentFocusedPosition) {
                focus();
            } else {
                actions.setVisibility(View.GONE);
            }
        }

        void focus() {
            et.requestFocus();
            actions.setVisibility(View.VISIBLE);
            et.post(() -> showKeyboard(et));
        }

        void clearFocus() {
            et.clearFocus();
            currentFocusedPosition = -1;
            hideKeyboard(itemView);
        }
    }

    // =========================
    // CHECKBOX
    // =========================
    class CheckboxVH extends RecyclerView.ViewHolder {
        LinearLayout root, actions;
        CheckBox cb;
        EditText et;
        ImageView btnAdd, btnConvert, btnDelete;
        TextView btnOk;

        CheckboxVH(View v) {
            super(v);
            root = v.findViewById(R.id.root_layout);
            actions = v.findViewById(R.id.layout_actions);
            cb = v.findViewById(R.id.checkbox_note);
            et = v.findViewById(R.id.et_checkbox_content);
            btnAdd = v.findViewById(R.id.btn_add_checkbox);
            btnConvert = v.findViewById(R.id.btn_switch_note_type);
            btnDelete = v.findViewById(R.id.btn_delete);
            btnOk = v.findViewById(R.id.btn_ok);

            // Enter key → add new checkbox in same group
            et.setOnEditorActionListener((v1, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // Save current content first
                        save(pos, et.getText().toString());
                        Note n = notes.get(pos);
                        listener.onNoteAddedAfter(pos, n.getGroupId());
                        return true;
                    }
                }
                return false;
            });

            // Focus change → save content & show/hide actions
            et.setOnFocusChangeListener((v12, hasFocus) -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (hasFocus) {
                    currentFocusedPosition = pos;
                    actions.setVisibility(View.VISIBLE);
                } else {
                    actions.setVisibility(View.GONE);
                    save(pos, et.getText().toString());
                }
            });

            root.setOnClickListener(v1 -> focus());
            btnOk.setOnClickListener(view -> clearFocus());
            btnDelete.setOnClickListener(view -> deleteItem(getAdapterPosition()));
            btnAdd.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    save(pos, et.getText().toString());
                    Note n = notes.get(pos);
                    listener.onNoteAddedAfter(pos, n.getGroupId());
                }
            });
        }

        void bind(int position) {
            Note n = notes.get(position);

            et.setText(n.getContent());
            cb.setChecked(n.isChecked());

            cb.setOnClickListener(view -> {
                int p = getAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;

                Note note = notes.get(p);
                note.setChecked(cb.isChecked());
                updateUI(note);
                listener.onNoteUpdated(note);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sort();
                    notifyDataSetChanged();
                }, 300);
            });

            updateUI(n);
            applyLayout(root, n, position);

            // Auto-focus newly created checkbox
            if (position == currentFocusedPosition) {
                focus();
            } else {
                actions.setVisibility(View.GONE);
            }
        }

        void focus() {
            et.requestFocus();
            actions.setVisibility(View.VISIBLE);
            et.post(() -> {
                et.setSelection(et.getText().length());
                showKeyboard(et);
            });
        }

        void clearFocus() {
            et.clearFocus();
            currentFocusedPosition = -1;
            hideKeyboard(itemView);
        }

        void updateUI(Note n) {
            if (n.isChecked()) {
                root.setAlpha(0.45f);
                et.setPaintFlags(et.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                root.setAlpha(1f);
                et.setPaintFlags(et.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

    // =========================
    // GROUP
    // =========================
    class GroupVH extends RecyclerView.ViewHolder {
        EditText title;
        TextView progress;
        CheckBox cb;
        ImageView revert;
        LinearLayout root;

        GroupVH(View v) {
            super(v);
            root = v.findViewById(R.id.root_layout);
            title = v.findViewById(R.id.et_group_title);
            progress = v.findViewById(R.id.tv_group_progress);
            cb = v.findViewById(R.id.cb_group);
            revert = v.findViewById(R.id.iv_revert);

            cb.setOnClickListener(v1 -> toggleGroup(getAdapterPosition()));
            revert.setOnClickListener(v1 -> revertToNote(getAdapterPosition()));
            
            title.setOnFocusChangeListener((v1, hasFocus) -> {
                if (!hasFocus) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Note n = notes.get(pos);
                        n.setContent(title.getText().toString());
                        listener.onNoteUpdated(n);
                    }
                }
            });
        }

        void bind(int pos) {
            Note n = notes.get(pos);
            title.setText(n.getContent());
            progress.setText(getProgress(n.getGroupId()));
            cb.setChecked(isAllChecked(n.getGroupId()));
            
            applyLayout(root, n, pos);
        }
    }

    // =========================
    // CORE FIX (LAYOUT)
    // =========================
    private void applyLayout(View root, Note n, int pos) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();

        int dp8 = (int) (8 * context.getResources().getDisplayMetrics().density);
        int dp16 = (int) (16 * context.getResources().getDisplayMetrics().density);

        if (n.getGroupId() != -1) {
            if (n.isGroup()) {
                params.topMargin = dp16; 
                params.bottomMargin = 0;
                root.setElevation(dp8);
                root.setBackgroundResource(R.drawable.bg_group_top);
            } else if (isLast(pos)) {
                params.topMargin = 0;
                params.bottomMargin = dp16;
                root.setElevation(dp8);
                root.setBackgroundResource(R.drawable.bg_group_bottom);
            } else {
                params.topMargin = 0;
                params.bottomMargin = 0;
                root.setElevation(dp8);
                root.setBackgroundResource(R.drawable.bg_group_middle);
            }
        } else {
            params.topMargin = dp8;
            params.bottomMargin = dp8;
            root.setElevation(dp8);
            root.setBackgroundResource(R.drawable.bg_card_full);
        }

        root.setLayoutParams(params);
    }

    // =========================
    // LOGIC
    // =========================
    public List<Note> getNotesListInternal() {
        return notes;
    }

    public void addNoteToTop(Note note) {
        notes.add(0, note);
        updatePositions();
        currentFocusedPosition = 0; // Auto-focus the new note
        notifyItemInserted(0);
        notifyItemRangeChanged(0, notes.size());
    }

    private void updatePositions() {
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).setPosition(i);
        }
    }

    private void save(int pos, String content) {
        if (pos < 0 || pos >= notes.size()) return;

        Note n = notes.get(pos);
        if (!content.equals(n.getContent())) {
            n.setContent(content);
            listener.onNoteUpdated(n);
        }
    }

    public void deleteItem(int pos) {
        if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= notes.size()) return;

        int id = notes.get(pos).getId();
        notes.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, notes.size() - pos);
        listener.onNoteDeleted(id);
    }

    public void deleteGroup(int groupId) {
        if (groupId == -1) return;
        List<Note> toRemove = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.getGroupId() == groupId) {
                toRemove.add(n);
                positions.add(i);
            }
        }

        // Remove from list (backwards to keep indices valid)
        for (int i = positions.size() - 1; i >= 0; i--) {
            int pos = positions.get(i);
            int id = notes.get(pos).getId();
            notes.remove(pos);
            listener.onNoteDeleted(id);
        }
        
        notifyDataSetChanged();
    }

    private void convertToChecklist(int pos, String text) {
        Note n = notes.get(pos);
        n.setCheckbox(false);
        n.setGroup(true);
        n.setGroupId(generateGroupId());
        n.setContent(""); // Empty content — placeholder hint will show

        String[] lines = text.split("\n");
        List<Note> children = new ArrayList<>();
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                Note child = new Note(trimmed);
                child.setCheckbox(true);
                child.setGroup(false);
                child.setGroupId(n.getGroupId());
                children.add(child);
            }
        }
        
        // Always add an extra empty checkbox at the end for immediate input
        Note emptyChild = new Note("");
        emptyChild.setCheckbox(true);
        emptyChild.setGroup(false);
        emptyChild.setGroupId(n.getGroupId());
        children.add(emptyChild);

        notes.addAll(pos + 1, children);
        updatePositions();

        // Auto-focus the last (empty) checkbox
        currentFocusedPosition = pos + children.size();

        notifyItemChanged(pos);
        notifyItemRangeInserted(pos + 1, children.size());
        listener.onRequestSyncDatabase();
        listener.onScrollToPosition(pos + children.size());
    }

    private void revertToNote(int headerPos) {
        if (headerPos == RecyclerView.NO_POSITION) return;
        Note header = notes.get(headerPos);
        int groupId = header.getGroupId();
        
        StringBuilder sb = new StringBuilder();
        int count = 0;
        
        Iterator<Note> iterator = notes.iterator();
        while (iterator.hasNext()) {
            Note n = iterator.next();
            if (n.getGroupId() == groupId && !n.isGroup()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(n.getContent());
                listener.onNoteDeleted(n.getId()); 
                iterator.remove();
                count++;
            }
        }
        
        header.setGroup(false);
        header.setCheckbox(false);
        header.setGroupId(-1);
        header.setContent(sb.toString().trim()); 
        listener.onNoteUpdated(header);
        
        updatePositions();
        notifyItemChanged(headerPos);
        notifyItemRangeRemoved(headerPos + 1, count);
        listener.onRequestSyncDatabase();
    }

    private void toggleGroup(int pos) {
        if (pos == RecyclerView.NO_POSITION) return;

        int groupId = notes.get(pos).getGroupId();
        boolean checked = isAllChecked(groupId);

        for (Note n : notes) {
            if (n.getGroupId() == groupId && !n.isGroup()) {
                n.setChecked(!checked);
                listener.onNoteUpdated(n);
            }
        }
        notifyDataSetChanged();
    }

    private int generateGroupId() {
        int max = 0;
        for (Note n : notes) if (n.getGroupId() > max) max = n.getGroupId();
        return max + 1;
    }

    private String getProgress(int gid) {
        int t = 0, c = 0;
        for (Note n : notes) {
            if (n.getGroupId() == gid && !n.isGroup()) {
                t++;
                if (n.isChecked()) c++;
            }
        }
        return c + "/" + t;
    }

    private boolean isAllChecked(int gid) {
        boolean has = false;
        for (Note n : notes) {
            if (n.getGroupId() == gid && !n.isGroup()) {
                has = true;
                if (!n.isChecked()) return false;
            }
        }
        return has;
    }

    private boolean isLast(int pos) {
        if (pos == notes.size() - 1) return true;
        return notes.get(pos).getGroupId() != notes.get(pos + 1).getGroupId();
    }

    private void sort() {
        Collections.sort(notes, Comparator.comparingInt(Note::getPosition));
    }

    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}