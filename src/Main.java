import Processes.DailyUsageInfo;
import Processes.Managers.UsageManager;
import Processes.UserInfo;
import db.Database;

public class Main {
    public static UsageManager usage;
    public static Database db = Database.getInstance();

    public static void main(String[] args) {
        usage = new UsageManager(db);
        UserInfo usr1 = new UserInfo("name",1);
        usage.dailyUsage(usr1);

        for(DailyUsageInfo f : db.getDailyUsage(usr1)) {
            System.out.println(f.getDate());
        };

        System.exit(0);

    }
}