package Events;

public class EventInfo {
    private int id;
    private int user_id;
    private String event_name;
    private int time;
    private boolean repeat;

    public EventInfo(int id, int user_id, String event_name, int time, boolean repeat) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.repeat = repeat;
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

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
