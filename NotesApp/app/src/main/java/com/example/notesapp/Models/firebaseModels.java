package com.example.notesapp.Models;

public class firebaseModels {
    private String title;
    private String content;

    public firebaseModels(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public firebaseModels() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
