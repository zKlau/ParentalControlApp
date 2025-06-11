package Processes;

/**
 * The {@code ProcessInfo} class represents a process associated with a user in the Parental Control App.
 * It encapsulates process details such as the process ID, user ID, process name, total tracked time, and time limit.
 * <p>
 * This class provides multiple constructors for flexible initialization and standard getter/setter methods.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class ProcessInfo {
    /**
     * The unique identifier for the process.
     */
    private int id;

    /**
     * The ID of the user associated with this process.
     */
    private int user_id;

    /**
     * The name or path of the process.
     */
    private String process_name;

    /**
     * The total tracked time for this process (in seconds or application-specific units).
     */
    private int total_time;

    /**
     * The time limit for this process (in seconds or application-specific units).
     */
    private int time_limit;

    /**
     * Constructs a {@code ProcessInfo} with the specified ID, user ID, process name, and total time.
     *
     * @param id           The process ID.
     * @param user_id      The user ID.
     * @param process_name The process name.
     * @param total_time   The total tracked time.
     */
    public ProcessInfo(int id, int user_id, String process_name, int total_time) {
        this.id = id;
        this.user_id = user_id;
        this.process_name = process_name;
        this.total_time = total_time;
    }

    /**
     * Constructs a {@code ProcessInfo} with all fields specified.
     *
     * @param id           The process ID.
     * @param user_id      The user ID.
     * @param process_name The process name.
     * @param total_time   The total tracked time.
     * @param time_limit   The time limit for the process.
     */
    public ProcessInfo(int id, int user_id, String process_name, int total_time, int time_limit) {
        this.id = id;
        this.user_id = user_id;
        this.process_name = process_name;
        this.total_time = total_time;
        this.time_limit = time_limit;
    }

    /**
     * Constructs a {@code ProcessInfo} with the specified ID, user ID, and process name.
     * The total time is initialized to 0.
     *
     * @param id           The process ID.
     * @param user_id      The user ID.
     * @param process_name The process name.
     */
    public ProcessInfo(int id, int user_id, String process_name) {
        this(id, user_id, process_name, 0);
    }

    /**
     * Default constructor initializing fields to default values.
     */
    public ProcessInfo() {
        this.id = -1;
        this.user_id = -1;
        this.process_name = "";
        this.total_time = 0;
        this.time_limit = 0;
    }

    /**
     * Returns the time limit for this process.
     *
     * @return The time limit.
     */
    public int getTime_limit() {
        return time_limit;
    }

    /**
     * Returns the process ID.
     *
     * @return The process ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the process ID.
     *
     * @param id The process ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user ID associated with this process.
     *
     * @return The user ID.
     */
    public int getUser_id() {
        return user_id;
    }

    /**
     * Sets the user ID associated with this process.
     *
     * @param user_id The user ID.
     */
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    /**
     * Returns the process name.
     *
     * @return The process name.
     */
    public String getProcess_name() {
        return process_name;
    }

    /**
     * Sets the process name.
     *
     * @param process_name The process name.
     */
    public void setProcess_name(String process_name) {
        this.process_name = process_name;
    }

    /**
     * Returns the total tracked time for this process.
     *
     * @return The total time.
     */
    public int getTotal_time() {
        return total_time;
    }

    /**
     * Sets the total tracked time for this process.
     *
     * @param total_time The total time.
     */
    public void setTotal_time(int total_time) {
        this.total_time = total_time;
    }

    /**
     * Sets the time limit for this process.
     *
     * @param i The time limit.
     */
    public void setTime_limit(int i) {
        this.time_limit = i;
    }
}
