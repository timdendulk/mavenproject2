package database.sqlite;

import Other.OmgevingConfig;
import database.GenericDAO;
import Model.databaseTabellen.Klanten;
import database.AbstractRecord;

/**
 * Created by Tjidobo on 31-3-2017.
 */
public class SqliteDbHelper {

    public static void printResultaatVanQuery() {
        GenericDAO<Klanten> klantDao = new GenericDAO<>(OmgevingConfig.getKlantDatabase(), Klanten.class);
        Klanten klant = klantDao.haalRecord("naam", "klant1");
        System.out.println(klant.getTelefoonnummer());
    }
}
