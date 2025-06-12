package Events;

import java.util.Calendar;

/**
 * The {@code EventInfo} class represents an event associated with a user in the Parental Control App.
 * It encapsulates event details such as the event's ID, user ID, name, scheduled time, and recurrence options.
 * <p>
 * This class provides constructors for various initialization scenarios and standard getter/setter methods.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class EventInfo {
    /**
     * The unique identifier for the event.
     */
    private int id;

    /**
     * The ID of the user associated with this event.
     */
    private int user_id;

    /**
     * The name or description of the event.
     */
    private String event_name;

    /**
     * The scheduled time for the event (in seconds or application-specific units).
     */
    private int time;

    /**
     * Indicates whether the event should trigger before a certain time.
     */
    private boolean before_at;

    /**
     * Indicates whether the event should repeat.
     */
    private boolean repeat;

    /**
     * Represents the creation time of the event.
     */
    private long created_at;
    /**
     * Default constructor initializing fields to default values.
     */
    public EventInfo() {
        this.id = -1;
        this.user_id = -1;
        this.event_name = "";
        this.time = 0;
        this.repeat = false;
        this.before_at = false;
        this.created_at = System.currentTimeMillis();
    }

    /**
     * Constructs an {@code EventInfo} with the specified parameters (without before_at).
     *
     * @param id         The event ID.
     * @param user_id    The user ID.
     * @param event_name The event name.
     * @param time       The scheduled time.
     * @param repeat     Whether the event repeats.
     */
    public EventInfo(int id, int user_id, String event_name, int time, boolean repeat) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.repeat = repeat;
        Calendar c = Calendar.getInstance();
        this.created_at = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    /**
     * Constructs an {@code EventInfo} with all parameters.
     *
     * @param id         The event ID.
     * @param user_id    The user ID.
     * @param event_name The event name.
     * @param time       The scheduled time.
     * @param before_at  Whether the event triggers before a certain time.
     * @param repeat     Whether the event repeats.
     */
    public EventInfo(int id, int user_id, String event_name, int time, boolean before_at, boolean repeat) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.before_at = before_at;
        this.repeat = repeat;
        this.created_at = System.currentTimeMillis();
    }
    /**
     * Constructs an {@code EventInfo} with all parameters.
     *
     * @param id         The event ID.
     * @param user_id    The user ID.
     * @param event_name The event name.
     * @param time       The scheduled time.
     * @param before_at  Whether the event triggers before a certain time.
     * @param repeat     Whether the event repeats.
     * @param created_at The event creation time
     */
    public EventInfo(int id, int user_id, String event_name, int time, boolean before_at, boolean repeat,long created_at) {
        this.id = id;
        this.user_id = user_id;
        this.event_name = event_name;
        this.time = time;
        this.before_at = before_at;
        this.repeat = repeat;
        this.created_at = created_at;
    }
    /**
     * Returns whether the event triggers before a certain time.
     *
     * @return {@code true} if before_at is set; {@code false} otherwise.
     */
    public boolean isBefore_at() {
        return before_at;
    }

    /**
     * Sets whether the event triggers before a certain time.
     *
     * @param before_at {@code true} to trigger before; {@code false} otherwise.
     */
    public void setBefore_at(boolean before_at) {
        this.before_at = before_at;
    }

    /**
     * Returns the event creation time.
     *
     * @return The event creation time.
     */
    public long getCreated_at() {
        return created_at;
    }
    /**
     * Sets the event creation time .
     *
     * @param created_at The event creation time.
     */
    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    /**
     * Returns the event ID.
     *
     * @return The event ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the event ID.
     *
     * @param id The event ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user ID associated with this event.
     *
     * @return The user ID.
     */
    public int getUser_id() {
        return user_id;
    }

    /**
     * Sets the user ID associated with this event.
     *
     * @param user_id The user ID.
     */
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    /**
     * Returns the event name.
     *
     * @return The event name.
     */
    public String getEvent_name() {
        return event_name;
    }

    /**
     * Sets the event name.
     *
     * @param event_name The event name.
     */
    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    /**
     * Returns the scheduled time for the event.
     *
     * @return The scheduled time.
     */
    public int getTime() {
        return time;
    }

    /**
     * Sets the scheduled time for the event.
     *
     * @param time The scheduled time.
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * Returns whether the event repeats.
     *
     * @return {@code true} if the event repeats; {@code false} otherwise.
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * Sets whether the event repeats.
     *
     * @param repeat {@code true} if the event should repeat; {@code false} otherwise.
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
