package Testutil;


import org.joda.time.DateTime;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public class DatumBereken {

    private static final String SYSTEEM_DATUM = "sysdate";

    static String berekenDatum(String datumExpressie, String format) throws NullPointerException {
        datumExpressie = datumExpressie.trim();
        datumExpressie = datumExpressie.substring(2, datumExpressie.length() - 1);
        DateTime datum = null;
        Character operand = null;
        String datumExpressieDeel = datumExpressie;

        while (datumExpressieDeel.length() > 0) {
            char c = datumExpressieDeel.charAt(0);

            if (Character.isLetter(c)) {
                DatumBewerking db = parseWoord(datumExpressieDeel);
                datum = db.doeBewerking(datum);
                datumExpressieDeel = db.getExpressie();
            } else if (Character.isDigit(c)) {
                if (operand!=null) {
                    DatumBewerking db = parseNummer(operand, datumExpressieDeel);
                    datum = db.doeBewerking(datum);
                    datumExpressieDeel = db.getExpressie();
                }
            } else if (c == '+' || c == '-') {
                operand = c;
                datumExpressieDeel = datumExpressieDeel.substring(1);
            } else if (c == ' ') {
                datumExpressieDeel = datumExpressieDeel.substring(1);
            } else {
                throw new IllegalArgumentException("Onverwacht symbool in datum expressie (" + datumExpressie + "): " + c);
            }
        }
        assert datum != null;
        return datum.toString(format);
    }

    private static DatumBewerking parseWoord(String expressie) {
        int i = 0;
        StringBuilder woordBuilder = new StringBuilder();
        while (i < expressie.length() && Character.isLetter(expressie.charAt(i))) {
            woordBuilder.append(expressie.charAt(i));
            i++;
        }
        String woord = woordBuilder.toString();
        DatumBewerking output;

        if (SYSTEEM_DATUM.equals(woord)) {
            output = new StartDatum(DateTime.now());
        } else {
            throw new IllegalArgumentException("Onbekend woord '" + woord + "' in datum expressie.");
        }

        output.setExpressie(expressie.substring(i));

        return output;
    }

    private static DatumBewerking parseNummer(char operand, String expressie) {
        int i = 0;
        StringBuilder nummer = new StringBuilder();
        while (Character.isDigit(expressie.charAt(i)) && i < expressie.length()) {
            nummer.append(expressie.charAt(i));
            i++;
        }

        if (i >= expressie.length()) {
            throw new IllegalArgumentException("Onverwacht eind van datum expressie. Nummer gevonden zonder eenheid.");
        }
        // eenheid vinden
        char c = expressie.charAt(i);
        DatumEenheid eenheid;
        if (Character.isLetter(c)) { // toegestaan: d, m of j
            eenheid = DatumEenheid.fromLetter(c); // dag, maand of jaar
            if (eenheid == null) {
                throw new IllegalArgumentException("Onverwacht eenheidsteken '" + c + "' in datum expressie.");
            }
        } else {
            throw new IllegalArgumentException("Onverwacht eenheidsteken '" + c + "' in datum expressie.");
        }
        int inummer = Integer.parseInt(nummer.toString());
        if (operand == '-') {
            inummer = -inummer;
        }

        DatumBewerking output = new TelDatum(eenheid, inummer);

        output.setExpressie(expressie.substring(i + 1));
        return output;
    }

    private enum DatumEenheid {
        DAG('d'), MAAND('m'), JAAR('j');

        private char letter;

        DatumEenheid(char letter) {
            this.letter = letter;
        }

        public static DatumEenheid fromLetter(char letter) {
            for (DatumEenheid d : values()) {
                if (letter == d.letter) {
                    return d;
                }
            }
            return null;
        }
    }

    private static abstract class DatumBewerking {
        private String expressie;

        public abstract DateTime doeBewerking(DateTime input);

        String getExpressie() {
            return expressie;
        }

        void setExpressie(String expressie) {
            this.expressie = expressie;
        }
    }

    private static class StartDatum extends DatumBewerking {
        DateTime datum;

        private StartDatum(DateTime datum) {
            this.datum = datum;
        }

        @Override
        public DateTime doeBewerking(DateTime input) {
            return datum;
        }
    }

    private static class TelDatum extends DatumBewerking {
        private DatumEenheid eenheid;
        private int operand;

        private TelDatum(DatumEenheid eenheid, int operand) {
            this.eenheid = eenheid;
            this.operand = operand;
        }

        @Override
        public DateTime doeBewerking(DateTime input) {
            DateTime output = input;

            switch (eenheid) {
                case DAG:
                    output = input.plusDays(operand);
                    break;
                case MAAND:
                    output = input.plusMonths(operand);
                    break;
                case JAAR:
                    output = input.plusYears(operand);
                    break;
            }
            return output;
        }

    }

}
