package database;

import Testutil.Wacht;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public class GenericDAO<T extends AbstractRecord> {

    private Database database;
    private Class<T> clazz;
    private T slaveInstance;

    public GenericDAO(Database database, Class<T> clazz) {
        this.database = database;
        this.clazz = clazz;

        try {
            this.slaveInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private List<T> _haalRecords(String sql) {
        Statement st = null;
        List<T> result = new ArrayList<T>();
        try {
            st = database.getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                T instance = clazz.newInstance();
                instance = (T) instance.readFieldsFromDb(rs, instance);
                result.add(instance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't execute query '" + sql + "'.", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException re) {
            System.out.println(re.toString());
        } finally {
            try {
                if (st != null) {
                    st.getConnection().close();
                    st.close();
                    boolean stIsGesloten = st.isClosed();
                    if (!stIsGesloten) {
                        System.out.println(String.format("Connection is gesloten is: %s!", stIsGesloten));
                        //assertTrue("Connection is niet (juist) gesloten.", stIsGesloten);
                    }
                } else {
                    System.out.println("Connection was al null voor het sluiten ervan.");
                }

            } catch (SQLException e) {
                //System.out.println("Couldn't close connection.");
            }
        }
        return result;
    }

    public List<T> haalRecords(String... nameValues) {
        String sql = slaveInstance.createQuery(nameValues);
        return _haalRecords(sql);
    }

    public void wachtEnControleerOpBepaaldAantalRecordsInDatabase(String aantalRecordsVerwacht, String messageBijFalen, String... nameValues) {
        String sql = slaveInstance.createCountQuery(nameValues);
        int wachtTijd = 200;
        int maxPogingen = 40;
        int ophaalPoging = 1;
        // maxPogingen x wachtTijd wachten
        String aantalRecordsGevonden = haalCountResultaatVanCountQuery(sql);
        while (ophaalPoging <= maxPogingen && !aantalRecordsGevonden.equals(aantalRecordsVerwacht)) {
            if (ophaalPoging==1 || ophaalPoging%10==0) { // iets minder vaak een regel loggen
                System.out.println(String.format("Bezig met wachten op %s records in de database, %s gevonden (%se poging, totale wachttijd %d sec).", aantalRecordsVerwacht, aantalRecordsGevonden, ophaalPoging, (ophaalPoging * wachtTijd)/1000));
            }
            Wacht.milliseconden(wachtTijd);
            aantalRecordsGevonden = haalCountResultaatVanCountQuery(sql);
            ophaalPoging++;
        }
        assertEquals(messageBijFalen, aantalRecordsVerwacht,  aantalRecordsGevonden);
    }

    private String haalCountResultaatVanCountQuery(String sql) {
        try {
            Statement st = database.getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            final ResultSetMetaData rsmd = rs.getMetaData();
            String kolomnaam =  rsmd.getColumnName(1).toLowerCase(); // Eigenlijk altijd aantal, omdat die in createCountQuery als kolomnaam wordt meegegeven
            rs.next(); // deze is nodig om het eerste resultaat te selecteren.
            return rs.getString(kolomnaam); // het cijfer van het aantal gevonden records.
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Het ophalen van het countresultaat ging niet goed.");
        }
    }

    public T haalRecord(String... nameValues) {
        int wachtTijd = 200;
        //int maxPogingen = 150;
        int maxPogingen = 40;
        int connectiePoging = 1;
        List<T> resultList = haalRecords(nameValues);
        // maxPogingen x wachtTijd wachten
        while (connectiePoging <= maxPogingen && resultList.size() == 0) {
            if (connectiePoging==1 || connectiePoging%10==0) { // iets minder vaak een regel loggen
                System.out.println(String.format("Bezig met wachten op bepaalde informatie in de database (%se poging, totale wachttijd %d sec).", connectiePoging, (connectiePoging * wachtTijd)/1000));
            }
            Wacht.milliseconden(wachtTijd);
            resultList = haalRecords(nameValues);
            connectiePoging++;
        }
        if (connectiePoging > maxPogingen){ // Wanneer het gefaald is, dan is connectiePoging 1 hoger dan maxPogingen
            throw new RuntimeException("Ophalen informatie uit de database niet gelukt binnen de wachttijd van " + (connectiePoging * wachtTijd)/1000 + " seconden.");
        } else if  (resultList.size() > 1) {
            throw new RuntimeException("Meer dan 1 record in de database gevonden, werkelijk aantal: " + resultList.size());
        }
        System.out.println("De gezochte data is gevonden na " + connectiePoging + " poging(en).");
        return resultList.get(0);
    }

    public List<T> haalRecordsSql(String sql) {
        return _haalRecords(sql);
    }

    public T haalRecordSql(String sql) {
        List<T> resultList = _haalRecords(sql);
        if (resultList.size() != 1) {
            throw new RuntimeException("Verwachtte 1 record in de database, maar vond er " + resultList.size());
        }
        return resultList.get(0);
    }

    public void voerSqlQueryUit(Database database, String sql) {
        try {
            Statement st = database.getConnection().createStatement();
            st.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
