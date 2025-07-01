import Processes.Managers.UsageManager;
import Processes.UserInfo;
import db.Database;

public class Main {
    public static UsageManager usage;
    public static Database db = Database.getInstance();

    public static void main(String[] args) {
        usage = new UsageManager(db);
        UserInfo usr1 = new UserInfo("name",0);
        usage.dailyUsage(usr1);


    }
}