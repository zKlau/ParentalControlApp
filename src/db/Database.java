package db;

import javax.xml.transform.Result;
import java.sql.*;

public class Database {

    public static void main(String[] args) throws SQLException {
        Connection con = null;
        String url = "jdbc:mariadb://localhost:3306/parentalcontrolapp";
        String user = "root";
        String pwd = "";

        con = DriverManager.getConnection(url,user,pwd);
        Statement stm = con.createStatement();

        ResultSet rs = stm.executeQuery("Select * from users");

        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("name");
            String email = rs.getString("score");

            System.out.println(id + ": " + username + " - " + email);
        }

        System.out.println("Succesfully connected to database");

    }
}
