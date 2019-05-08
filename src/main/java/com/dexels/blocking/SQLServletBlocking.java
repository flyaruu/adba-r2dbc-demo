package com.dexels.blocking;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SQLServletBlocking extends HttpServlet {

	private static final long serialVersionUID = 7511084274322957420L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://postgres:5432/dvdrental"
        			, "postgres", "mysecretpassword");
        		Statement statement = connection.createStatement();
        		ResultSet resultSet = statement.executeQuery("SELECT * FROM film")) {
            while (resultSet.next()) {
            	resp.getWriter().write(resultSet.getString("title")+"\n");
            }
        } catch (SQLException e) {
			throw new ServletException("whoops", e);
        }
	}

}
