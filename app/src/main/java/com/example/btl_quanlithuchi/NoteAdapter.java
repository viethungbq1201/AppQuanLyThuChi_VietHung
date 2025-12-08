package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> noteList;
    private Context context;
    private DBHelper dbHelper;

    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvDate;
        CheckBox cbCheck;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tvContent);
            tvDate = view.findViewById(R.id.tvDate);
            cbCheck = view.findViewById(R.id.cbCheck);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvContent.setText(note.getContent());
        holder.tvDate.setText(note.getCreatedDate());

        if (note.hasCheckbox()) {
            holder.cbCheck.setVisibility(View.VISIBLE);
            holder.cbCheck.setChecked(note.isChecked());

            holder.cbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    note.setChecked(isChecked);
                    dbHelper.updateNoteCheckStatus(note.getId(), isChecked);
                }
            });
        } else {
            holder.cbCheck.setVisibility(View.GONE);
        }

        // Click để sửa
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNoteDialog(note, position);
            }
        });

        // Long click để xóa
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(note, position);
                return true;
            }
        });

        // Nút sửa (nếu cần)
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNoteDialog(note, position);
            }
        });

        // Nút xóa (nếu cần)
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(note, position);
            }
        });
    }

    private void showEditNoteDialog(Note note, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_note, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etContent = view.findViewById(R.id.etContent);
        CheckBox cbHasCheckbox = view.findViewById(R.id.cbHasCheckbox);
        CheckBox cbIsChecked = view.findViewById(R.id.cbIsChecked);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Hiển thị dữ liệu cũ
        etContent.setText(note.getContent());
        cbHasCheckbox.setChecked(note.hasCheckbox());
        cbIsChecked.setChecked(note.isChecked());

        // Ẩn/hiện checkbox "Đã hoàn thành" dựa trên "Có checkbox"
        cbIsChecked.setVisibility(note.hasCheckbox() ? View.VISIBLE : View.GONE);

        cbHasCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbIsChecked.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (!isChecked) {
                    cbIsChecked.setChecked(false);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(context, "Vui lòng nhập nội dung ghi chú", Toast.LENGTH_SHORT).show();
                    return;
                }

                note.setContent(content);
                note.setHasCheckbox(cbHasCheckbox.isChecked());
                note.setChecked(cbIsChecked.isChecked());
                note.setCreatedDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                dbHelper.updateNote(note);
                notifyItemChanged(position);

                Toast.makeText(context, "Đã cập nhật ghi chú", Toast.LENGTH_SHORT).show();
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

    private void showDeleteDialog(Note note, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa ghi chú này?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteNote(note.getId());
                        noteList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, noteList.size());
                        Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void setData(List<Note> newList) {
        this.noteList = newList;
        notifyDataSetChanged();
    }
}