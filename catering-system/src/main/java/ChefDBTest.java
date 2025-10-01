import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
public class ChefDBTest {



        public static void main(String[] args) {
            // Change databaseName, username and password as per your setup
            String url = "jdbc:sqlserver://localhost:1433;databaseName=HeadChefDB;encrypt=false";
            String user = "sa";         // your DB username
            String pass = "123";   // your DB password

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                System.out.println("Connected to DB successfully!");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT menu_id, menu_name FROM Menus");

                while (rs.next()) {
                    System.out.println("Menu ID: " + rs.getInt("menu_id") + ", Name: " + rs.getString("menu_name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


