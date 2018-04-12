package Testutil;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by Tjitte Bouma on 1/17/2017.
 */
public class BestandHelper {

    /**
     * Met deze methode wordt een bestand uit de testfiles folder (of subfolder) uitgelezen en in een string gezet.
     * @return scriptpath
     */
    public static String haalTestBestand(String filename) {
        String scriptPath = getScriptPath();
        String content;
        try {
            Scanner scanner = new Scanner(new File(scriptPath + filename), "UTF-8");
            if (scanner.hasNext()) {
                content = scanner.useDelimiter("\\A").next();
            } else {
                content = "";
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open " + scriptPath + filename, e);
        }
        return content;
    }

    /**
     * Onderstaande methode is voor het ophalen van het lokale pad t/m src/test/resources/. Werkt op unix en windows.
     * @return scriptpath De locatie van de "resources" folder.
     */
    public static String getScriptPath() {
        String currentPath = getRootPath();
        return currentPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    }

    /**
     * Deze methode geeft het pad van de hoofdmap van het project (dit maakt het omgeving onafhankelijk, maakt niet uit waar je het project neer zet.
     * @return de locatie van de hoofdmap van het project
     */
    public static String getRootPath() {
        String currentPath = null;
        try {
            currentPath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            System.out.println("Could not open current path.");
        }
        return currentPath;
    }

    /**
     * Met deze method wordt geprobeerd een bestand op te halen, wanneer dit een FileNotFoundException geeft, dan bestaat het bestand schijnbaar niet.
     * @param bestandslocatie de locatie van het bestand (achter /testfiles/)
     * @return false wanneer het bestand niet bestaat.
     */
    public static boolean bestandBestaat(String bestandslocatie) {
        String scriptPath = getScriptPath();
        try {
            Scanner scanner = new Scanner(new File(scriptPath + bestandslocatie));
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public static String vervangVariabelenInString(String dynamischBestandStr, TreeMap<String, String> parameters) {
        while (dynamischBestandStr.contains("*{")) {
            String tekstVervangIncl = StringUtils.substring(dynamischBestandStr, dynamischBestandStr.indexOf('*'), dynamischBestandStr.indexOf('}') + 1);
            String tekstVervangExcl = tekstVervangIncl.replace("*{", "").replace("}", "");
            if (parameters!=null && parameters.size() > 0) {
                for (Map.Entry<String, String> element : parameters.entrySet()) {
                    if (tekstVervangExcl.equals(element.getKey())) {
                        dynamischBestandStr = dynamischBestandStr.replace(tekstVervangIncl, element.getValue());
                        break;
                    }
                }
            }
            if (tekstVervangIncl.contains("sysdate")) {
                dynamischBestandStr = dynamischBestandStr.replace(tekstVervangIncl, DatumBereken.berekenDatum(tekstVervangIncl, "dd-MM-yyyy"));
            }
        }
        return dynamischBestandStr;
    }

    /**
     * Deze methode geeft de bestandsnaam terug.
     * @param bestandslocatie de locatie van het bestand
     * @return de bestandsnaam inclusief extensie
     */
    public static String haalBestandsnaamUitBestandslocatie(String bestandslocatie) {
        String[] bestandParts = bestandslocatie.split("/");
        return bestandParts[bestandParts.length-1];
    }

    public static InputStream haalBestandAlsInputStream(String bestandslocatie)  {
        String geheelPadMetBestandsnaam = getScriptPath() + "testfiles" + File.separator + bestandslocatie;
        try {
            return new FileInputStream(new File(geheelPadMetBestandsnaam));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Ophalen bestand mislukt.", e);
        }
    }

    public static void maakLokaleFolderAlsDezeNietBestaat(String folderlocatie, String foldernaam) {
        boolean folderBestaat = BestandHelper.bestandBestaat(folderlocatie + File.separator  + foldernaam);
        if (!folderBestaat) {
            new File(BestandHelper.getScriptPath() + folderlocatie + File.separator +foldernaam).mkdir();
        }
    }
}
