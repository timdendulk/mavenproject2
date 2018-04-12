package Other;

import database.Database;
import database.DatabaseUtil;
import Testutil.Environment;
import Testutil.PropertiesHelper;

import java.util.Properties;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public abstract class OmgevingConfig {

    private static String omgeving;
    private static String browser;
    private static Database klantDatabase;

    public static String getOmgeving() {
        if (omgeving==null) { bepaalOmgeving(); }
        return omgeving;
    }

    public static String getBrowser() {
        if (browser==null) { bepaalBrowser(); }
        return browser;
    }

    public static Database getKlantDatabase() {
        if (klantDatabase==null) {
            klantDatabase = DatabaseUtil.getInstance(null);
        }
        return klantDatabase;
    }

    /**
     * Met deze methode wordt de omgeving bepaald.
     */
    private static void bepaalOmgeving() {
        String computernaam = Environment.getComputerName();
        omgeving = computernaam;
        // Nu is de omgeving nog altijd de computernaam.
        // Deze kan anders geprogrammeerd worden, door bijv. in een properties bestand een computernaam een omgeving te geven.
        // Gebruik hiervoor PropertiesHelper.loadPropertiesNoEnv in testutil
        System.out.println("Computernaam is: " + Environment.getComputerName() + ", dan is de omgeving: " + omgeving);
    }

    private static void bepaalBrowser() {
        Properties browserProp = PropertiesHelper.loadProperties("browser");
        browser = browserProp.getProperty("browser");
    }

}
