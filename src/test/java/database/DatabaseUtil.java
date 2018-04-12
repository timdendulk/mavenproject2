package database;

import Other.OmgevingConfig;
import Testutil.BestandHelper;
import Testutil.PropertiesHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.fail;

/**
 * Created by tjitte.bouma on 09-01-2017.
 * <p>
 * Manages a database. Executes sql scripts.
 */
public class DatabaseUtil implements Database {

    private static Properties config;
    private static Map<String, Database> instances = new HashMap<>();

    public static synchronized Database getInstance(String configFile) {
        Database instance = instances.get(configFile);

        if (instance == null) {
            instance = new DatabaseUtil(configFile);
            instances.put(configFile, instance);
        }
        return instance;
    }

    /**
     * Aan de hand van een db-config properties bestand, in de folder "resources/properties/database/" met de naam "db_<databaseafkorting>.<omgeving>.properties". Voor omgeving zie OmgevingConfig.
     *
     * In het bestand staan de volgende onderdelen:
     * database.url=jdbc:oracle:thin:@<databaseLocatie>/<databasenaam>
     * database.username=<gebruikersnaam>     * database.class=oracle.jdbc.OracleDriver

     * database.password=<wachtwoord>
     * database.name=<databasenaam>
     *
     * Hier is de oracle jdbc dependency voor nodig.
     *
     * Zie ook het gebruik in "getConnection()".
     *
     * Eventueel kan een dergelijk properties-bestand ook met een methode worden samengesteld, als deze maar bovenstaande properties erin staan.
     *
     * @param configFile De naam van het config-bestand.
     */
    private DatabaseUtil(String configFile) {
        String omgeving = OmgevingConfig.getOmgeving();
        if (!BestandHelper.bestandBestaat("properties/" + configFile + ".properties")) { // wanneer er geen specifieke database properties zijn, dan de generieke database/dev_db.properties gebruiken
            config = maakConfigVoorSqliteDatabase();
        } else {
            config = PropertiesHelper.loadProperties(configFile);
        }
        String driverClass = config.getProperty("database.class");
        try {
            Class.forName(driverClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Kon de database driver niet starten.", e);
        }
    }

    private Properties maakConfigVoorSqliteDatabase() {
        config = new Properties();
        String databaseBestandslocatie = BestandHelper.getRootPath() + "/src/test/java/cuke/common/database/sqlite/leningDb";
        config.setProperty("database.url", "jdbc:sqlite:" + databaseBestandslocatie);
        config.setProperty("database.class", "org.sqlite.JDBC");
        return config;
    }

    private Properties maakConfigPropertiesMetDbProperties(String configFile) {
        // haal de gegevens/onderdelen op uit dev_db.properties
        Properties dbProp = PropertiesHelper.loadProperties("database/dev_db");
        String omgeving = OmgevingConfig.getOmgeving();
        String databaseNaam = configFile.replace("database/db_","").replace("." + omgeving,"");
        String urlBegin = dbProp.getProperty("generiekdeel.dev.database.url");
        String dbnummer = dbProp.getProperty(omgeving + ".dbnummer");
        if (dbnummer==null) {
            fail("Er is geen property in properties/database/dev_db.properties gevonden met " + omgeving + ".dbnummer. Deze en de overige properties die daar staan zijn nodig om de database goed te bepalen.");
        }
        String urlEinde = dbProp.getProperty(omgeving + ".urleinde");
        // maak de config properties met de samengestelde gegevens/onderdelen.
        config = new Properties();
        config.setProperty("database.url", urlBegin + databaseNaam.toLowerCase() + urlEinde);
        config.setProperty("database.class", dbProp.getProperty("database.class"));
        config.setProperty("database.username", databaseNaam.toUpperCase() + "DEV" + dbnummer.toUpperCase() + "_OWNER");
        config.setProperty("database.password", databaseNaam.toLowerCase() + "dev" + dbnummer.toLowerCase() + "_owner");
        config.setProperty("database.name", databaseNaam.toLowerCase() + urlEinde);
        return config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getProperty("database.url"), config.getProperty("database.username"), config.getProperty("database.password"));
    }
}
