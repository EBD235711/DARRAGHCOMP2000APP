package com.example.libraryapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String author;
    public String isbn;
    public String status;

    public Book(String title, String author, String isbn, String status) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.status = status;
    }
}
