package database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public abstract class AbstractRecord<T> {

    public abstract String tableName();

    public abstract TreeMap<String, String> maakVergelijkbareLijst();

    private Map<String, Method> _map(String prefix) {
        Class clazz = this.getClass();
        Map<String, Method> result = new HashMap<>();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            String fullName = method.getName();
            if (fullName.startsWith(prefix)) {
                String name = fullName.substring(3);
                result.put(name, method);
            }
        }
        return result;
    }

    private Map<String, Method> mapGetters() {
        return _map("get");
    }

    private Map<String, Method> mapSetters() {
        return _map("set");
    }

    String createQuery(String... nameValue) {
        String selectPart = createQuerySelectFields() + " ";
        String fromPart = "FROM " + tableName();
        String wherePart = createQueryWherePart(nameValue);
        return selectPart + fromPart + wherePart;
    }

    String createCountQuery(String... nameValue) {
        String selectPart = "SELECT COUNT(*) aantal ";
        String fromPart = "FROM " + tableName();
        String wherePart = createQueryWherePart(nameValue);
        return selectPart + fromPart + wherePart;
    }

    private String createQuerySelectFields() {
        final StringBuilder selectPart = new StringBuilder();
        selectPart.append("SELECT ");
        for (Map.Entry<String, Method> entry : mapGetters().entrySet()) {
            selectPart.append(entry.getKey().toLowerCase());
            selectPart.append(", ");
        }
        selectPart.delete(selectPart.length() - 2, selectPart.length());
        return selectPart.toString();
    }

    private String createQueryWherePart(String... nameValue) {
        StringBuilder wherePart = new StringBuilder();
        if (nameValue.length != 0) {
            wherePart.append(" WHERE ");
        }
        for (int i = 0; i < nameValue.length; i += 2) {
            if ("".equals(nameValue[i + 1])) {
                wherePart.append(nameValue[i]);
                wherePart.append(" IS NULL");
            } else if (nameValue[i+1].contains("TO_DATE")) {
                String part = nameValue[i] + " = " + nameValue[i + 1];
                wherePart.append(part);
            } else {
                String part = nameValue[i] + " = '" + nameValue[i + 1] + "'";
                wherePart.append(part);
            }
            if ((i + 2) != nameValue.length) {
                wherePart.append(" AND ");
            }
        }
        return wherePart.toString();
    }

    T readFieldsFromDb(final ResultSet rs, final T result) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();

        final Map<String, String> columns = new HashMap<String, String>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columns.put(rsmd.getColumnName(i).toLowerCase(), rsmd.getColumnTypeName(i));
        }

        for (Map.Entry<String, Method> setter : mapSetters().entrySet()) {
            String shortName = setter.getKey();
            String fullName = "set" + shortName;
            Method method = setter.getValue();

            String columnname = shortName.toLowerCase();
            String columnType = columns.get(columnname);

            String value = null;
            try {
                if (columnType.equalsIgnoreCase("blob")) {
                    Blob blob = rs.getBlob(columnname);
                    if (blob != null) {
                        byte[] bdata = blob.getBytes(1, (int) blob.length());
                        value = new String(bdata);
                    } else {
                        value = "[leeg]";
                    }
                } else {
                    value = rs.getString(columnname);
                }
                method.invoke(result, value);
            } catch (SQLException e) {
                System.out.println("Error getting String from column " + columnname);
            } catch (InvocationTargetException | IllegalAccessException e) {
                System.out.println("Error invocing " + fullName);
            } catch (NullPointerException re) {
                System.out.println(re.toString());
            }
        }
        return result;
    }
}