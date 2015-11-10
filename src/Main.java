import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static Connection conn;
    public static void main(String [ ] args) {
        LoginWindow app = new LoginWindow();
        app.setVisible(true);

//        try {
//            String dbpath = "jdbc:mysql://siarhei.by/bsu?user=bsu&password=bsuk&useUnicode=true&characterEncoding=UTF8";
//            Main.conn = DriverManager.getConnection(dbpath);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        main2();
    }

    public static void main2() {


        MainWindow win = new MainWindow();
        win.setVisible(true);
    }
}
