package db;

import javax.xml.crypto.Data;
import javax.xml.transform.Result;
import java.sql.*;

public class Database {
    Connection con;
    Statement stm;

    public Database() {
        try {
            con = null;
            String url = "jdbc:mariadb://localhost:3306/parentalcontrolapp";
            String user = "root";
            String pwd = "";

            con = DriverManager.getConnection(url, user, pwd);
            stm = con.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Succesfully connected to database");
       /* ResultSet rs = stm.executeQuery("Select * from users");

        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("name");
            String email = rs.getString("score");

            System.out.println(id + ": " + username + " - " + email);
        }


        */
    }


    public void addProcess(String name,int user_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Processes WHERE PROCESS_NAME = '"+ name+"'");
            ResultSet rs = checkQuery.executeQuery();
            if (rs.next()) {
                System.out.println("Process already exists");
                return;
            }

            PreparedStatement insertQuery = con.prepareStatement ("INSERT INTO Processes (USER_ID,PROCESS_NAME,TOTAL_TIME) VALUES ("+user_id+",'"+name+"',0)");
            ResultSet iqs = insertQuery.executeQuery();
            System.out.println("Process added: " + name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("UPDATE Processes SET TOTAL_TIME=TOTAL_TIME+2 WHERE ID = "+process_id);
            ResultSet rs = checkQuery.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
