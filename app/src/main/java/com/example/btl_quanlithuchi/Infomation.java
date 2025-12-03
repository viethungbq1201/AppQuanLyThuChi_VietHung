package com.example.btl_quanlithuchi;

public class Infomation {

    private int id;
    private String title;
    private String category;
    private String date;
    private int price;
    private String type;

    public Infomation() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Infomation(int id, String title, String category, String date, int price, String type) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.price = price;
        this.type = type;
    }
}
