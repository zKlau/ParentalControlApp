package Events;

public class EventInfo {
    private int id;
    private int user_id;
    private String event_name;
    private int time;
    private boolean before_at;
    private boolean repeat;

    public EventInfo() {
        this.id = -1;
        this.user_id = -1;
        this.event_name = "";
        this.time = 0;
        this.repeat = false;
        this.before_at = false;
    }
    public EventInfo(int id, int user_id, String event_name, int time, boolean repeat) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.repeat = repeat;
    }

    public EventInfo(int id, int user_id, String event_name, int time, boolean before_at, boolean repeat) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.before_at = before_at;
        this.repeat = repeat;
    }

    public boolean isBefore_at() {
        return before_at;
    }

    public void setBefore_at(boolean before_at) {
        this.before_at = before_at;
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
