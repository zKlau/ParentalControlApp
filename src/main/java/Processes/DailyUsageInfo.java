package Processes;

import java.time.LocalDate;

/**
 * The {@code DailyUsageInfo} class represents daily usage statistics for a user in the Parental Control App.
 * It encapsulates the user ID, date, and the total time spent for a given day.
 * <p>
 * This class provides multiple constructors for flexible initialization and standard getter/setter methods.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class DailyUsageInfo {
    /**
     * The ID of the user associated with this daily usage record.
     */
    int userId;

    /**
     * The date for which the usage is recorded.
     */
    private LocalDate date;

    /**
     * The total time spent by the user on the given date (in seconds or application-specific units).
     */
    private int timeSpent;

    /**
     * Constructs a {@code DailyUsageInfo} with the specified user ID, date, and time spent.
     *
     * @param userId    The user ID.
     * @param date      The date of usage.
     * @param timeSpent The total time spent.
     */
    public DailyUsageInfo(int userId, LocalDate date, int timeSpent) {
        this(date,timeSpent);
        this.userId = userId;
    }

    /**
     * Constructs a {@code DailyUsageInfo} with the specified user ID, date (as a string), and time spent.
     *
     * @param userId    The user ID.
     * @param date      The date of usage as a string (ISO format).
     * @param timeSpent The total time spent.
     */
    public DailyUsageInfo(int userId, String date, int timeSpent) {
        this(date,timeSpent);
        this.userId = userId;
    }

    /**
     * Constructs a {@code DailyUsageInfo} with the specified date (as a string) and time spent.
     *
     * @param date      The date of usage as a string (ISO format).
     * @param timeSpent The total time spent.
     */
    public DailyUsageInfo(String date, int timeSpent) {
        this.date = LocalDate.parse(date);
        this.timeSpent = timeSpent;
    }

    /**
     * Constructs a {@code DailyUsageInfo} with the specified date and time spent.
     *
     * @param date      The date of usage.
     * @param timeSpent The total time spent.
     */
    public DailyUsageInfo(LocalDate date, int timeSpent) {
        this.date = date;
        this.timeSpent = timeSpent;
    }

    /**
     * Returns the user ID associated with this daily usage record.
     *
     * @return The user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID associated with this daily usage record.
     *
     * @param userId The user ID.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the date for which the usage is recorded.
     *
     * @return The date of usage.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the date for which the usage is recorded.
     *
     * @param date The date of usage.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Returns the total time spent by the user on the given date.
     *
     * @return The total time spent.
     */
    public int getTimeSpent() {
        return timeSpent;
    }

    /**
     * Sets the total time spent by the user on the given date.
     *
     * @param timeSpent The total time spent.
     */
    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }
}
