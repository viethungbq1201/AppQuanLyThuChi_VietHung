package com.example.btl_quanlithuchi;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private int id;
    private String content;
    private boolean isCheckbox;
    private boolean isChecked;
    private boolean isGroup;
    private String groupName;
    private int groupId;
    private int position;
    private Date createdAt;

    public Note() {}

    public Note(String content) {
        this.content = content;
        this.isCheckbox = false;
        this.isChecked = false;
        this.isGroup = false;
        this.groupId = -1;
    }

    public Note(String content, boolean isCheckbox) {
        this.content = content;
        this.isCheckbox = isCheckbox;
        this.isChecked = false;
        this.isGroup = false;
        this.groupId = -1;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCheckbox() { return isCheckbox; }
    public void setCheckbox(boolean checkbox) { isCheckbox = checkbox; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

}