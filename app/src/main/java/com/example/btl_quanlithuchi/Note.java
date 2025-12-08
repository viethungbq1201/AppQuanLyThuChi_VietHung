package com.example.btl_quanlithuchi;

public class Note {
    private int id;
    private String content;
    private boolean hasCheckbox;
    private boolean isChecked;
    private String createdDate;

    public Note() {
    }

    public Note(String content, boolean hasCheckbox, String createdDate) {
        this.content = content;
        this.hasCheckbox = hasCheckbox;
        this.createdDate = createdDate;
        this.isChecked = false;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasCheckbox() {
        return hasCheckbox;
    }

    public void setHasCheckbox(boolean hasCheckbox) {
        this.hasCheckbox = hasCheckbox;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}