package Processes;

/**
 * The {@code UserInfo} class represents a user in the Parental Control App.
 * It encapsulates user details such as the user's name, unique ID, and IP address.
 * <p>
 * This class provides constructors for flexible initialization and standard getter/setter methods.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class UserInfo {
    /**
     * The user's name.
     */
    private String name;

    /**
     * The unique identifier for the user.
     */
    private int id;

    /**
     * The IP address associated with the user.
     */
    private String ip;

    /**
     * Constructs a {@code UserInfo} with the specified name, ID, and IP address.
     *
     * @param name The user's name.
     * @param id   The user's unique ID.
     * @param ip   The user's IP address.
     */
    public UserInfo(String name, int id, String ip) {
        this.name = name;
        this.id = id;
        this.ip = ip;
    }

    /**
     * Constructs a {@code UserInfo} with the specified name and ID.
     * The IP address defaults to "127.0.0.1".
     *
     * @param name The user's name.
     * @param id   The user's unique ID.
     */
    public UserInfo(String name, int id) {
        this(name, id, "127.0.0.1");
    }

    /**
     * Returns the user's name.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name The user's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the user's unique ID.
     *
     * @return The user's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user's unique ID.
     *
     * @param id The user's ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's IP address.
     *
     * @return The user's IP address.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets the user's IP address.
     *
     * @param ip The user's IP address.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
}
