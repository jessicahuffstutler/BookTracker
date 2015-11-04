package com.theironyard;

/**
 * Created by jessicahuffstutler on 10/29/15.
 */
public class Book {
    int id;
    String title;
    String author;
    String type;
    String qty; //needs to be int, but this works for now

    boolean authorized;
    String username;

    public Book() {

    }

    public Book(int id, String title, String author, String type, String qty) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.type = type;
        this.qty = qty;
    }
}
