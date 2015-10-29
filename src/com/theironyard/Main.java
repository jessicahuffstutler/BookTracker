package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        ArrayList<Book> books = new ArrayList();

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    String password = session.attribute("password");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("password", password);
                    m.put("books", books);
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");
                    Session session = request.session();
                    session.attribute("username", username);
                    session.attribute("password", password);
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/input-books",
                ((request, response) -> {
                    Book book = new Book();
                    book.id = books.size() + 1; //starts at 0, adding 1 to show human counting
                    book.title = request.queryParams("title");
                    book.author = request.queryParams("author");
                    book.qty = request.queryParams("qty");
                    book.type = request.queryParams("type");
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
    }
}
