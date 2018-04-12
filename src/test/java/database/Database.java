package database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public interface Database {
    Connection getConnection() throws SQLException;
}
