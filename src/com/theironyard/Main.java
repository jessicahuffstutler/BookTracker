package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, User> users = new HashMap<>();

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    String password = session.attribute("password");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("password", password);
                    m.put("books", users.get(username).books);
                    return new ModelAndView(m, "logged-in.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    Session session = request.session();
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");
                    session.attribute("username", username);
                    session.attribute("password", password);
                    if (users.get(username) == null) {
                        User currentUser = new User();
                        currentUser.username = username;
                        currentUser.password = password;
                        users.put(username, currentUser);
                        response.redirect("/");
                    }
                    else if (username.equals(users.get(username).username) && password.equals(users.get(username).password)) {
                        response.redirect("/");
                    }
                    else {
                        return "ERROR";
                    }
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

                    Book book = new Book();
                    book.id = users.get(username).books.size() + 1; //starts at 0, adding 1 to show human counting
                    book.title = request.queryParams("title");
                    book.author = request.queryParams("author");
                    book.qty = request.queryParams("qty");
                    book.type = request.queryParams("type");
                    users.get(username).books.add(book);
                    response.redirect("/");
                    return "";
                })
        );

        Spark.get(
                "/book",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);
                    Book book = users.get(username).books.get(idNum - 1);
                    m.put("book", book);

                    return new ModelAndView(m, "book.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.get(
                "/edit-book",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);
                    Book book = users.get(username).books.get(idNum - 1);
                    m.put("book", book);

                    return new ModelAndView(m, "edit-book.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/edit-book",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    String id = request.queryParams("id");
                    String title = request.queryParams("title");
                    String author = request.queryParams("author");
                    String qty = request.queryParams("qty");
                    String type = request.queryParams("type");
                    try {
                        int idNum = Integer.valueOf(id);
                        ArrayList<Book> books = users.get(username).books;
                        Book book = books.get(idNum - 1);
                        book.title = title;
                        book.author = author;
                        book.qty = qty;
                        book.type = type;
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-book",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    String id = request.queryParams("id");
                    try {
                        int idNum = Integer.valueOf(id);
                        users.get(username).books.remove(idNum - 1);
                        for (int i = 0; i < users.get(username).books.size(); i++) {
                            users.get(username).books.get(i).id = i + 1; //renumbering the books after you delete one of them
                        }
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );


    }
}

//doesn't maintain login on the book & edit-book pages.
//edit doesn't submit