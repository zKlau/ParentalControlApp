package db;
import java.sql.*;

public class Database {
    Connection con;
    Statement stm;

    public Database() {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:data.db");

            // Create tables
            Statement stm = con.createStatement();
            stm.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Users (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    NAME TEXT NOT NULL,
                    IP TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS Processes (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    USER_ID INTEGER NOT NULL,
                    PROCESS_NAME TEXT NOT NULL,
                    TOTAL_TIME INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (USER_ID) REFERENCES Users(ID)
                );
                CREATE TABLE IF NOT EXISTS TimeLimits (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PROCESS_ID INTEGER NOT NULL,
                    TIME_LIMIT INTEGER NOT NULL,
                    FOREIGN KEY (PROCESS_ID) REFERENCES Processes(ID)
                );
            """);
            System.out.println("Database successfully created");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successfully connected to database");
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
            insertQuery.executeUpdate();
            System.out.println("Process added: " + name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("UPDATE Processes SET TOTAL_TIME=TOTAL_TIME+2 WHERE ID = "+process_id);
            checkQuery.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeLimit(int process_id, int time_limit) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= "+ process_id);
            ResultSet rs = checkQuery.executeQuery();
            if (rs.next()) {
                PreparedStatement insertQuery = con.prepareStatement ("UPDATE Timelimits SET TIME_LIMIT = "+time_limit + " WHERE PROCESS_ID="+process_id);
                insertQuery.executeUpdate();
                return;
            }
            System.out.println("Process timelimit doesnt exist");
            PreparedStatement insertQuery = con.prepareStatement ("INSERT INTO Timelimits (PROCESS_ID, TIME_LIMIT) VALUES ("+process_id+","+time_limit+")");
            insertQuery.executeUpdate();
            System.out.println("Time limit added for PID " + process_id);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTimeLimit(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Timelimits  WHERE PROCESS_ID= "+ process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TIME_LIMIT");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    public int getTime(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("Select * from Processes WHERE ID= "+ process_id);
            ResultSet rs = checkQuery.executeQuery();
            if(rs.next()) {
                return rs.getInt("TOTAL_TIME");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    public void removeProcess(int process_id) {
        try {
            PreparedStatement checkQuery = con.prepareStatement("DELETE FROM Processes WHERE ID="+process_id);
            ResultSet rs = checkQuery.executeQuery();

            PreparedStatement checkQuery2 = con.prepareStatement("DELETE FROM TimeLimits WHERE PROCESS_ID="+process_id);
            ResultSet rs2 = checkQuery.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
