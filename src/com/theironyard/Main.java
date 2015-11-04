package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS books (id IDENTITY, user_id INT, title VARCHAR, author VARCHAR, type VARCHAR, qty VARCHAR)");
    }

    public static void insertUser(Connection conn, String username, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String username) throws SQLException {
        User user = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            user = new User();
            user.id = results.getInt("id");
            user.password = results.getString("password");
        }
        return user;
    }

    public static void insertBook(Connection conn, int userId, String title, String author, String type, String qty) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO books VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setInt(1, userId);
        stmt.setString(2, title);
        stmt.setString(3, author);
        stmt.setString(4, type);
        stmt.setString(5, qty);
        stmt.execute();
    }

    public static Book selectBook(Connection conn, int id) throws SQLException {
        Book book = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books INNER JOIN users ON books.user_id = users.id WHERE books.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            book = new Book();
            book.id = results.getInt("books.id");
            book.title = results.getString("books.title");
            book.author = results.getString("books.author");
            book.type = results.getString("books.type");
            book.qty = results.getString("books.qty");
        }
        return book;
    }

    public static ArrayList<Book> selectBooks(Connection conn, int userId) throws SQLException {
        ArrayList<Book> books = new ArrayList();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books INNER JOIN users ON books.user_id = users.id WHERE books.user_id = ?");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Book book = new Book();
            book.id = results.getInt("books.id");
            book.title = results.getString("books.title");
            book.author = results.getString("books.author");
            book.type = results.getString("books.type");
            book.qty = results.getString("books.qty");
            books.add(book);
        }
        return books;
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

//        ArrayList<Book> books = new ArrayList<>();
//        HashMap<String, User> users = new HashMap<>();

//        addTestUsers(users);
//        addTestBooks(books);

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    ArrayList<Book> books = new ArrayList();

                    User user = selectUser(conn, username); //get User from database
                    if (user != null) {
                       books  = selectBooks(conn, user.id); //user id from user to get books from database
                    }

                    HashMap m = new HashMap();
                    m.put("books", books);

                    if (username == null) {
                        return new ModelAndView(m, "not-logged-in.html");
                    }
                    m.put("username", username);
                    return new ModelAndView(m, "logged-in.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");
                    if (username.isEmpty() || password.isEmpty()) {
                        Spark.halt(403);
                    }
//                    User user = users.get(username); //replaced below
                    User user = selectUser(conn, username);
                    if (user == null) {
//                        user = new User();
//                        user.password = password;
//                        users.put(username, user);
                        insertUser(conn, username, password);
                    }
                    else if (!password.equals(user.password)) {
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("username", username);
                    session.attribute("password", password);

//                    for(Book book : books){
//                        book.authorized = book.username.equals(username);
//                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/input-books",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    if (username == null) {
                        Spark.halt(403);
                    }

//                    int id = books.size() + 1;
                    String title = request.queryParams("title");
                    String author = request.queryParams("author");
                    String qty = request.queryParams("qty");
                    String type = request.queryParams("type");

                    User me = selectUser(conn, username);
                    insertBook(conn, me.id, title, author, type, qty);
//                    Book book = new Book();


//                    book.authorized = true;
//                    books.add(book);
                    response.redirect("/");
                    return "";
                })
        );

        Spark.get(
                "/book",
                ((request, response) -> {

                    HashMap m = new HashMap();

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);
                    Book book = selectBook(conn, idNum);

                    m.put("book", book);

                    return new ModelAndView(m, "book.html");
                }),
                new MustacheTemplateEngine()
        );

//        Spark.get(
//                "/edit-book",
//                ((request, response) -> {
//                    HashMap m = new HashMap();
//
//                    String id = request.queryParams("id");
//                    int idNum = Integer.valueOf(id);
//                    Book book = books.get(idNum - 1);
//                    m.put("book", book);
//                    m.put("id", id);
//
//                    return new ModelAndView(m, "edit-book.html");
//                }),
//                new MustacheTemplateEngine()
//        );
//
//        Spark.post(
//                "/edit-book",
//                ((request, response) -> {
//                    String id = request.queryParams("id");
//                    Session session = request.session();
//                    try {
//                        int idNum = Integer.valueOf(id);
//                        Book book = books.get(idNum - 1);
//                        String titleString = request.queryParams("title");
//                        if(titleString != null && !"".equals(titleString)){
//                            book.title = titleString;
//                        }
//                        String authorString = request.queryParams("author");
//
//                        if(authorString != null && !"".equals(authorString)){
//                            book.author = authorString;
//                        }
//                        String qtyString = request.queryParams("qty");
//                        if(qtyString != null && !"".equals(qtyString)){
//                            book.qty= qtyString;
//                        }
//                        String typeString = request.queryParams("type");
//                        if(typeString != null && !"".equals(typeString)){
//                            book.type = typeString;
//                        }
//                        book.username = session.attribute("username");
//                        book.authorized = true;
//                    } catch (Exception e) {
//
//                    }
//                    response.redirect("/");
//                    return "";
//                })
//        );
//
//        Spark.post(
//                "/delete-book",
//                ((request, response) -> {
//                    String id = request.queryParams("id");
//                    try {
//                        int idNum = Integer.valueOf(id);
//                        books.remove(idNum - 1);
//                        for (int i = 0; i < books.size(); i++) {
//                            books.get(i).id = i + 1;
//                        }
//                    } catch (Exception e) {
//
//                    }
//                    response.redirect("/");
//                    return "";
//                })
//        );
    }

//    static void addTestUsers(HashMap<String, User> users) {
//        users.put("Alice", new User());
//        users.put("Bob", new User());
//        users.put("Charlie", new User());
//    }
//
//    static void addTestBooks(ArrayList<Book> books) {
//        books.add(new Book(1, "Title1", "Author1", "fiction", "23"));
//        books.add(new Book(2, "Title2", "Author2", "fiction", "43"));
//        books.add(new Book(3, "Title3", "Author3", "fiction", "54"));
//        books.add(new Book(4, "Title4", "Author4", "fiction", "65"));
//    }
}