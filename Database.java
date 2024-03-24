import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;
import java.sql.ResultSet;

public class Database {
    private Connection connection;
    private Statement statement;

    public Database() {
        try {
            // Membuat objek Properties
            Properties props = new Properties();
            props.put("user", "root");  // Menambahkan nama pengguna ke properti
            props.put("password", "");   // Menambahkan kata sandi ke properti

            // Membuat koneksi dengan menggunakan URL database dan properti
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_mahasiswa", props);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Metode untuk menjalankan query SELECT dan mengembalikan ResultSet
    public ResultSet selectQuery(String sql) {
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public int insertUpdateDeleteQuery(String sql) {
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Statement getStatement() {
        return statement;
    }
}