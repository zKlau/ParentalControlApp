package Processes;

public class UserInfo {
    private String name;
    private int id;
    private String ip;

    public UserInfo(String name, int id, String ip) {
        this.name = name;
        this.id = id;
        this.ip = ip;
    }
    public UserInfo(String name, int id) {
        this(name,id,"127.0.0.1");
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
