package Testutil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public class DatumHelper {

    /**
     * In deze method worden alle datumhelper onderdelen (bijv. sysdate, morgen of jaar) vervangen door werkelijke data.
     * <p>
     * Voorbeelden:
     * *{sysdate-1d} (geeft de datum van vandaag met eventuele afwijking ten opzichte van vandaag, met d, m of j voor dag, maand en jaar en een + of - ervoor).
     * *{sysdate+3m-1d+1j} (er kunnen meerdere modifiers gebruikt worden).
     * *{jaar} (geeft huidig jaar, tenzij een jaar modifier gebruikt wordt, -1j of +1j)
     *
     * @param tekst   De te controleren foutmelding (met of zonder datumhelper onderdelen)
     * @param formaat het formaat zoals de datum teruggegeven moet worden (if null, dan "dd-MM-yyyy")
     * @return de String met alle eventuele datumhelper onderdelen vervangen.
     */
    public static String vervangDatumhelperOnderdelenInString(String tekst, String formaat) {
        // Alle datumhelper onderdelen vervangen door werkelijke data (bijv. sysdate, vandaag, gisteren, morgen, eergister, overmorgen of jaar)
        if (formaat == null) {
            formaat = "dd-MM-yyyy";
        }
        while (tekst.contains("*{")) {
            String tekstVervang = StringUtils.substring(tekst, tekst.indexOf('*'), tekst.indexOf('}') + 1);
            if (tekstVervang.contains("sysdate")) {
                tekst = tekst.replace(tekstVervang, DatumBereken.berekenDatum(tekstVervang, formaat));
            } else if (tekstVervang.contains("jaar")) {
                if (tekstVervang.toLowerCase().contains("+") || tekstVervang.contains("-")) {
                    String modifier = tekstVervang.toUpperCase().replace("*{JAAR", "").replace("}", "").toLowerCase();
                    tekst = tekst.replace(tekstVervang, DatumBereken.berekenDatum("*{sysdate" + modifier + "}", "yyyy"));
                } else {
                    tekst = tekst.replace(tekstVervang, DatumBereken.berekenDatum("*{sysdate}", "yyyy"));
                }
            } else {
                System.out.println(String.format("Onderdeel %s in het tekst is geen bekend keyword waarmee een datum opgehaald kan worden.", tekstVervang));
                break;
            }
        }
        return tekst;
    }

    /**
     * Met deze methode worden meerdere data in een stringlist van een *{sysdate} omgezet naar een dd-MM-yyyy formaat.
     *
     * @param stringList een stringlist met 1 of meerdere *{sysdate} data.
     * @return een stringlist zonder data in *{sysdate} formaat
     */
    public List<String> vervangDatumOnderdelenInStringList(List<String> stringList) {
        List<String> nieuweList = new ArrayList<>();
        for (String kolomnaam : stringList) {
            String nieuweNaam = vervangDatumhelperOnderdelenInString(kolomnaam, null);
            nieuweList.add(nieuweNaam);
        }
        return nieuweList;
    }

    /**
     * Normaal formaat is dd-MM-yyyy voor Nederlandse begrippen.
     *
     * @param datum de datum in een String, in een bepaald formaat, bijv. yyyy-MM-dd.
     * @return de datum in formaat dd-MM-yyyy.
     */
    public static String zetDatumNaarNormaalFormaat(String datum) {
        return zetDatumInSpecifiekFormaat(datum, "dd-MM-yyyy");
    }

    /**
     * Met deze methode wordt een String met een datum in een bepaald formaat omgezet in een ander gewenst formaat.
     * <p>
     * De datum kan ook aangeleverd worden als EPOCH of als *{sysdate} formaat. Wanneer het wel een datum betreft, dat wordt met een "tester" nagegaan wat het formaat van de datum is.
     * <p>
     * Wanneer de datum uit de database komt, dan zit er een spatie tussen de datum en de tijd, het gaat om de datum, dus de tijd wordt er afgesloopt.
     *
     * @param datum          de datum in een String, in een bepaald formaat, bijv. dd-MM-yyyy.
     * @param gewenstFormaat Het gewenste formaat van de datum, bijv. yyyy of yyyy-MM-dd
     * @return een String met de datum in het gewenste formaat (mag ook alleen het jaar zijn, met yyyy)
     */
    public static String zetDatumInSpecifiekFormaat(String datum, String gewenstFormaat) {
        if (datum == null || gewenstFormaat == null) {
            return datum;
        }
        if (datum.contains(" ") && !datum.contains("*{")) { //  dan is het inclusief tijd, de tijd weghalen.
            datum = datum.split(" ", 2)[0];
        }
        if (datum.length() > 8 && StringHelper.stringIsCompleetNumeriek(datum)) { // Dit kan alleen maar EPOCH zijn. Een compleet numerieke string van meer dan 8 tekens. Dus niet ddMMyyyy of variant daarop.
            datum = datumVanEpochNaarNormaalFormaat(datum);
            return datumVanFormaatNaarAnderFormaat(datum, "dd-MM-yyyy", gewenstFormaat);
        } else if (datum.contains("*{")) {
            return vervangDatumhelperOnderdelenInString(datum, gewenstFormaat);
        } else {
            String formaatVanDatum = haalDatumFormaatUitDatumString(datum);
            return datumVanFormaatNaarAnderFormaat(datum, formaatVanDatum, gewenstFormaat);
        }
    }

    /**
     * Een methode om een datum in EPOCH formaat om te zetten naar een bruikbaar formaat (dd-MM-yyyy).
     *
     * @param epochString de datum uit de database (komt in EPOCH formaat binnen)
     * @return een datum in het formaat dd-MM-yyyy.
     */
    public static String datumVanEpochNaarNormaalFormaat(final String epochString) {
        if (epochString != null && StringHelper.stringIsCompleetNumeriek(epochString)) {
            Date date = new Date(Long.parseLong(epochString));
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.format(date);
        } else {
            return zetDatumNaarNormaalFormaat(epochString); // De datum is null, wel een waarde geven ter voorkoming van een NullPointerException
        }
    }

    /**
     * Een methode om een datum (in onbepaald formaat) om te zetten naar een epoch formaat.
     *
     * @param datumString de datum uit de database (komt in normaal binnen)
     * @return een EPOCH in Long formaat
     */
    public static String datumVanNormaalNaarEpochFormaat(final String datumString) {
        String standaarddatum = DatumHelper.zetDatumInSpecifiekFormaat(datumString,"yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String[] splitDatum = standaarddatum.split("-");
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Integer.parseInt(splitDatum[0]),Integer.parseInt(splitDatum[1])-1,Integer.parseInt(splitDatum[2]));
        return Long.toString(cal.getTimeInMillis());
    }



    /**
     * Met deze methode wordt een datum van het ene formaat naar het andere formaat gezet. Deze is voor wanneer je het formaat van de datum op voorhand al weet.
     *
     * @param datum           de datum in formaat "huidigeFormaat"
     * @param huidigeFormaat  het formaat waarin de datum wordt aangeleverd
     * @param gewensteFormaat het gewenste formaat van de datum
     * @return de datum in het gewenste formaat.
     */
    private static String datumVanFormaatNaarAnderFormaat(final String datum, final String huidigeFormaat, final String gewensteFormaat) {
        if (huidigeFormaat.equals(gewensteFormaat)) {
            return datum;
        }
        DateTimeFormatter initieelFormat = DateTimeFormat.forPattern(huidigeFormaat);
        DateTime dateTimeInInitieelFormaat = initieelFormat.parseDateTime(datum);
        DateTimeFormatter doelFormat = DateTimeFormat.forPattern(gewensteFormaat);
        return doelFormat.print(dateTimeInInitieelFormaat); // print in doelformaat
    }

    /**
     * Een methode/tester om te kijken in welk formaat de aangeleverde datum staat.
     *
     * @param datum De aangeleverde datum.
     * @return Het formaat van de aangeleverde datum.
     */
    private static String haalDatumFormaatUitDatumString(final String datum) {
        if (datum != null) {
            final String[] formats = {"yyyy-MM-dd", "dd-MM-yyyy", "dd-MMM-yyyy", "yyyy-MMM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd/MMM/yyyy", "yyyy/MMM/dd", "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-ddHH:mm:ss", "MM/dd/yyyyHH:mm:ss",
                    "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss", "yyyy:MM:ddHH:mm:ss", "yyyyMMdd"};
            for (String huidigFormat : formats) {
                DateTimeFormatter testformat = DateTimeFormat.forPattern(huidigFormat);
                try {
                    testformat.parseDateTime(datum);
                    return huidigFormat;
                } catch (IllegalArgumentException e) {
                    // Hier geen code, want het is enkel een tester.
                }
            }
            throw new RuntimeException("Datumformaat niet gevonden voor datum: " + datum);
        } else {
            throw new RuntimeException("Er is geen datum is opgegeven.");
        }
    }

    public static String aantalDagenTenOpzichteVanDatum(String datum, String plusOfMin, int aantalDagen) {
        DateTimeFormatter datumFormaat = DateTimeFormat.forPattern(haalDatumFormaatUitDatumString(datum));
        DateTime dateTime = datumFormaat.parseDateTime(datum);
        String nieuwedatum;
        if (plusOfMin.toLowerCase().equals("plus") || plusOfMin.equals("+")) {
            nieuwedatum = String.valueOf(dateTime.plusDays(aantalDagen));
        } else {
            nieuwedatum = String.valueOf(dateTime.minusDays(aantalDagen));
        }
        return zetDatumNaarNormaalFormaat(nieuwedatum);
    }
}
