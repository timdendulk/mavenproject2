package Testutil;

import Other.OmgevingConfig;
import database.Database;
import database.SqlPlusClient;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

/**
 * Created by tjitte.bouma on 23-1-2017.
 */
public class SqlHelper {

    private static Map<String, SqlPlusClient> sqlPlusClients = new HashMap<>();
    private static List<Thread> threads = new ArrayList<>();
    final static AtomicBoolean hasErrors = new AtomicBoolean(false);

    /**
     * Met deze methode kan 1 enkele query afgevuurd worden op een database.
     *
     * Voorbeeld:
     *      SqlHelper.voerSqlQueryUit(OmgevingConfig.getBamDatabase(), XmlHelper.haalTestBestand("testdata/testUpdate.sql"));
     *
     * of   SqlHelper.voerSqlQueryUit(OmgevingConfig.getBamDatabase(), "UPDATE BASISVERSIEMATERIEEL SET NAAM = 'updatednaam' WHERE ID = '1'"));
     *
     * of   String query = "INSERT INTO BASISVERSIEMATERIEEL(ID, NAAM, OMSCHRIJVING, BEGINDATUM, INFRAVERSIE_NAAM) VALUES (1, 'Testnaam', 'sql met cuke', to_date(sysdate), '20170123-BD-20110325-001-BVI')";
     *      SqlHelper.voerSqlQueryUit(OmgevingConfig.getBadDatabase(), query);
     *
     * @param database een database, bijv OmgevingConfig.getBamDatabase() of OmgevingConfig.getBadDatabase()
     * @param sql een string met 1 losse query, zonder ; op het einde.
     */
    public static void voerSqlQueryUit(Database database, String sql) {
        System.out.println(sql);
        try {
            Statement st = database.getConnection().createStatement();
            st.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Uitvoeren sql query gefaald.", e);
        }
    }

    /**
     * Met deze methode wordt een sql of pl/sql met parameters in een bestand (dus losse query of een pl/sql) opgehaald en uitgevoerd op de gewenste database.
     *
     * Hierbij worden parameters meegegeven in een lijst van "var1 = x en var2 = y en var3 = z", etc.
     *
     * @param sqlLocatie de locatie van het sql script bestand, met alles wat na "testdata/" komt.
     * @param parameters de namen en waardes van de variabelen in het sql-script. Met format <variabeleNaam1>=<waarde1> en <variabeleNaam2>=<waarde2> (etcetera, oneindigish veel keer " en ").
     * @param databaseNaam de naam van de database waarop het script uitgevoerd moet worden. Hiervan moet een propertiesbestand zijn: database/db_<databasenaam>.<computernaam/omgeving>.properties.
     */
    public static void voerSqlPlusQueryUitMetParameters(String sqlLocatie, String parameters, String databaseNaam) {
        if (parameters==null || parameters.equals("")) {
            voerSqlPlusQueryUitMetEventueelAangepasteBestandsinhoud(null, sqlLocatie, databaseNaam, true);
        }
        String sqlInhoud = vervangParametersInSqlString(sqlLocatie, parameters);
        voerSqlPlusQueryUitMetEventueelAangepasteBestandsinhoud(sqlInhoud, sqlLocatie, databaseNaam, true);
    }

    /**
     * Met deze methode wordt een sql of pl/sql zonder parameters in een bestand opgehaald en uitgevoerd op de gewenste database.
     *
     * @param sqlLocatie de locatie van het sql script bestand, met alles wat na "testdata/" komt.
     * @param databaseNaam de naam van de database waarop het script uitgevoerd moet worden. Hiervan moet een propertiesbestand zijn: database/db_<databasenaam>.<computernaam/omgeving>.properties.
     */
    public static void voerSqlPlusQueryUit(String sqlLocatie, String databaseNaam) {
        voerSqlPlusQueryUitMetEventueelAangepasteBestandsinhoud(null, sqlLocatie, databaseNaam, true);
    }

    public static void voerSqlPlusQueryUitNegeerFouten(String sqlLocatie, String databaseNaam) {
        voerSqlPlusQueryUitMetEventueelAangepasteBestandsinhoud(null, sqlLocatie, databaseNaam, false);
    }

    private static void voerSqlPlusQueryUitMetEventueelAangepasteBestandsinhoud(String bestandsinhoud, String bestandsnaam, String databaseNaam, boolean stopBijErrors) {
        if (bestandsinhoud==null) { // Anders is er al een specifieke inhoud meegegeven en zijn er waarschijnlijk parameters uit het bestand vervangen.
            bestandsinhoud = BestandHelper.haalTestBestand("testdata/" + bestandsnaam);
        }
        String dbString = haalDbStringMetPropertyFiles(databaseNaam);
        SqlPlusClient sqlPlusClient;
        if (sqlPlusClients.containsKey(dbString)) {
            sqlPlusClient = sqlPlusClients.get(dbString);
        } else {
            sqlPlusClient = new SqlPlusClient(dbString, hasErrors);
            sqlPlusClients.put(dbString, sqlPlusClient);
            Thread t = new Thread(sqlPlusClient, "SqlPlus(" + dbString + ")");
            threads.add(t);
            t.start();
        }
        CountDownLatch cdl = new CountDownLatch(1);
        if (!heeftSqlPlusClientErrors()) {
            sqlPlusClient.fillBuffer("testfiles/" + bestandsnaam, bestandsinhoud, cdl);
        } else if (stopBijErrors) {
            sqlPlusClient.stop();
            throw new IllegalStateException("Fout tijdens uitvoeren script "+ bestandsnaam + ", want het vorige script bevatte fouten!");
        }
        try {
            System.out.println("Bezig met uitvoeren van " + bestandsnaam);
            cdl.await();
            System.out.println("Klaar met uitvoeren van " + bestandsnaam);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interruptie van de countdownlatch", e);
        }
    }

    private static String haalDbStringMetPropertyFiles(String databaseNaam) {
        String omgeving = OmgevingConfig.getOmgeving();
        String propVoorOmgeving = "database/db_" + databaseNaam.toLowerCase();
        if (BestandHelper.bestandBestaat("properties/" + propVoorOmgeving + "." + omgeving + ".properties")) {
            Properties dbProp = PropertiesHelper.loadProperties(propVoorOmgeving);
            String[] urlSplit = dbProp.getProperty("database.url").split("@");
            return dbProp.getProperty("database.username")
                    + "/" + dbProp.getProperty("database.password")
                    + "@" + urlSplit[urlSplit.length-1];
        } else { // alleen voor dev omgevingen
            Properties dbProp = PropertiesHelper.loadProperties("database/dev_db");
            String dbnummer = dbProp.getProperty(omgeving + ".dbnummer");
            if (dbnummer==null) {
                fail("Er is geen property in properties/database/dev_db.properties gevonden met " + omgeving + ".dbnummer. Deze en de overige properties die daar staan zijn nodig om de database goed te bepalen.");
            }
            String[] urlGenSplit = dbProp.getProperty("generiekdeel.dev.database.url").split("@");
            String urlEinde = dbProp.getProperty(omgeving + ".urleinde");
            return databaseNaam.toUpperCase() + "DEV" + dbnummer.toUpperCase() + "_OWNER"
                    + "/" + databaseNaam.toLowerCase() + "dev" + dbnummer.toLowerCase() + "_owner"
                    + "@" + urlGenSplit[urlGenSplit.length-1] + databaseNaam.toLowerCase() + urlEinde;
        }
    }

    private static String vervangParametersInSqlString(String sqlLocatie, String parameters) {
        String sqlInhoud = BestandHelper.haalTestBestand("testdata/" + sqlLocatie);
        String[] lijstInString = StringHelper.maakLijstVanOpsomming(parameters);
        TreeMap<String, String> treeMap = new TreeMap<>();
        for (String param : lijstInString) {
            String[] nameValue = param.split("=");
            treeMap.put(nameValue[0].trim(), nameValue[1].trim());
        }
        return BestandHelper.vervangVariabelenInString(sqlInhoud, treeMap);
    }

    private static boolean heeftSqlPlusClientErrors() {
        for (SqlPlusClient s :sqlPlusClients.values()) {
            if (Boolean.TRUE.equals(s.getHasErrors().get())) {
                System.out.println("Errors aanwezig voor sqlPlusClient: " + s.getName() + " De uitgevoerde code:" + s.getTotNuToeUitgevoerdeSqlCode());
                return true;
            }
        }
        return false;
    }
}
