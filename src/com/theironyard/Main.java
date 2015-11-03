package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        ArrayList<Book> books = new ArrayList<>();
        HashMap<String, User> users = new HashMap<>();

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("books", books);

                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
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
                    User currentUser = users.get(username);
                    if (currentUser == null) {
                        currentUser = new User();
                        currentUser.password = password;
                        users.put(username, currentUser);
                    }
                    else if (!password.equals(currentUser.password)) {
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("username", username);
                    session.attribute("password", password);

                    for(Book book : books){
                        book.authorized = book.username.equals(username);
                    }

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

                    Book book = new Book();

                    book.id = books.size() + 1; //starts at 0, adding 1 to show human counting
                    book.title = request.queryParams("title");
                    book.author = request.queryParams("author");
                    book.qty = request.queryParams("qty");
                    book.type = request.queryParams("type");
                    book.username = session.attribute("username");
                    book.authorized = true;
                    books.add(book);
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
                    Book book = books.get(idNum - 1);
                    m.put("book", book);

                    return new ModelAndView(m, "book.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.get(
                "/edit-book",
                ((request, response) -> {
                    HashMap m = new HashMap();

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);
                    Book book = books.get(idNum - 1);
                    m.put("book", book);
                    m.put("id", id);

                    return new ModelAndView(m, "edit-book.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/edit-book",
                ((request, response) -> {
                    String id = request.queryParams("id");
                    Session session = request.session();
                    try {
                        int idNum = Integer.valueOf(id);
                        Book book = books.get(idNum - 1);
                        String titleString = request.queryParams("title");
                        if(titleString != null && !"".equals(titleString)){
                            book.title = titleString;
                        }
                        String authorString = request.queryParams("author");

                        if(authorString != null && !"".equals(authorString)){
                            book.author = authorString;
                        }
                        String qtyString = request.queryParams("qty");
                        if(qtyString != null && !"".equals(qtyString)){
                            book.qty= qtyString;
                        }
                        String typeString = request.queryParams("type");
                        if(typeString != null && !"".equals(typeString)){
                            book.type = typeString;
                        }
                        book.username = session.attribute("username");
                        book.authorized = true;
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-book",
                ((request, response) -> {
                    String id = request.queryParams("id");
                    try {
                        int idNum = Integer.valueOf(id);
                        books.remove(idNum - 1);
                        for (int i = 0; i < books.size(); i++) {
                            books.get(i).id = i + 1; //renumbering the books after you delete one of them
                        }
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );


    }
}

//edit doesn't submit