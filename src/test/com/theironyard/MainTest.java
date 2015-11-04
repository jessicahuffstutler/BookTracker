package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jessicahuffstutler on 11/3/15.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        Main.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE users");
        stmt.execute("DROP TABLE books");
        conn.close();
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        endConnection(conn);

        assertTrue(user != null);
    }

    @Test
    public void testBook() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertBook(conn, 1, "Title1", "Author1", "fiction", "23");
        Book book = Main.selectBook(conn, 1);
        endConnection(conn);

        assertTrue(book != null);
    }

    @Test
    public void testBooks() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertUser(conn, "Bob", "");
        Main.insertBook(conn, 1, "Title1", "Author1", "fiction", "23");
        Main.insertBook(conn, 2, "Title2", "Author2", "fiction", "52");
        Main.insertBook(conn, 1, "Title1", "Author1", "fiction", "23");
        ArrayList<Book> books = Main.selectBooks(conn, 1);
        endConnection(conn);
        System.out.println(books.size());
        assertTrue(books.size() == 2);
    }

}