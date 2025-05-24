package Processes;

public class ProcessInfo {
    private int id;
    private int user_id;
    private String process_name;
    private int total_time;

    public ProcessInfo(int id, int user_id, String process_name, int total_time) {
        this.id = id;
        this.user_id = user_id;
        this.process_name = process_name;
        this.total_time = total_time;
    }

    public ProcessInfo(int id, int user_id, String process_name) {
        this(id,user_id,process_name,0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getProcess_name() {
        return process_name;
    }

    public void setProcess_name(String process_name) {
        this.process_name = process_name;
    }

    public int getTotal_time() {
        return total_time;
    }

    public void setTotal_time(int total_time) {
        this.total_time = total_time;
    }
}
