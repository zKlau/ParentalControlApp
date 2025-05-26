package Processes;

public class ProcessInfo {
    private int id;
    private int user_id;
    private String process_name;
    private int total_time;
    private int time_limit;

    public ProcessInfo(int id, int user_id, String process_name, int total_time) {
        this.id = id;
        this.user_id = user_id;
        this.process_name = process_name;
        this.total_time = total_time;
    }
    public ProcessInfo(int id, int user_id, String process_name, int total_time, int time_limit) {
        this.id = id;
        this.user_id = user_id;
        this.process_name = process_name;
        this.total_time = total_time;
        this.time_limit = time_limit;
    }
    public ProcessInfo(int id, int user_id, String process_name) {
        this(id,user_id,process_name,0);
    }
    public ProcessInfo() {
        this.id = -1;
        this.user_id = -1;
        this.process_name = "";
        this.total_time = 0;
        this.time_limit = 0;
    }

    public int getTime_limit() {
        return time_limit;
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

    public void setTime_limit(int i) {
        this.time_limit = i;
    }
}
