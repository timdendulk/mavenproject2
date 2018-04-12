package Testutil;

import database.AbstractRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.fail;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public class LijstVergelijker {
	
    public static void vergelijkTreeMaps(Map<String, String> verwachteLijst, Map<String, String> gevondenLijst) {
        System.out.println("\nVERWACHTE WAARDES: " + verwachteLijst);
        System.out.println("\nGEVONDEN WAARDES: " + gevondenLijst + "\n");

        List<TestResult> results = new ArrayList<>();
        String message;
        for (Map.Entry<String, String> verwachtItem : verwachteLijst.entrySet()) {
            String veld = verwachtItem.getKey();
            String verwachteWaarde = verwachtItem.getValue();
            if (!(verwachteWaarde == null) && !"".equals(verwachteWaarde)) { // alleen de opgevraagde onderdelen ([leeg] of een waarde) worden vergeleken
                String gevondenWaarde = gevondenLijst.get(veld);
                if ("[gevuld]".equals(verwachteWaarde) && gevondenWaarde != null) {
                    // Wanneer voor bijv. bron_id verwacht wordt dat deze niet leeg is en de gevondenWaarde is niet leeg (null), dan de gevondenWaarde vullen met "[gevuld]". Voor betere vergelijking.
                    gevondenWaarde = "[gevuld]";
                    System.out.println(String.format("Voor veld '%s' is een waarde verwacht en ook een waarde gevonden ([gevuld]).", veld));
                } else if (gevondenWaarde == null || "".equals(gevondenWaarde)) {
                    gevondenWaarde = "[leeg]";
                    if ("[leeg]".equals(verwachteWaarde)) {
                        System.out.println(String.format("Voor veld '%s' wordt geen waarde verwacht en is er ook geen gevonden ([leeg]).", veld));
                    } else {
                        System.out.println(String.format("Voor veld '%s' is de verwachte waarde '%s', maar is geen waarde gevonden.", veld, verwachteWaarde));
                    }
                } else {
                    System.out.println(String.format("Voor veld '%s' is de verwachte waarde '%s' en de gevonden waarde '%s'.", veld, verwachteWaarde, gevondenWaarde));
                }

                if (gevondenWaarde.equals(verwachteWaarde)) {
                    results.add(new TestResult(true, "Ging goed: " + veld));
                } else {
                    if ("[leeg]".equals(verwachteWaarde)) {
                        message = String.format("Voor veld '%s' werd geen de waarde verwacht, maar '%s' werd gevonden.", veld, gevondenWaarde);
                    } else {
                        message = String.format("Voor veld '%s' werd de waarde '%s' verwacht, maar werd '%s' gevonden.", veld, verwachteWaarde, gevondenWaarde);
                    }
                    results.add(new TestResult(false, message));
                }
            }
        }
        testResultaatVerwerker(results);
    }

    public static void vergelijkGevondenEnVerwachteDatabaseObjecten(AbstractRecord<?> verwacht, AbstractRecord<?> gevonden) {
        if (gevonden==null || verwacht==null) {
            throw new RuntimeException("Controleren van de database inhoud kan niet uitgevoerd worden, het gevonden of verwachte object is niet gevuld.");
        }
        vergelijkTreeMaps(verwacht.maakVergelijkbareLijst(), gevonden.maakVergelijkbareLijst());
    }

    public static void vergelijkStringLists(List<String> verwachteLijst, List<String> gevondenLijst) {
        System.out.println("\nVERWACHTE WAARDES: " + verwachteLijst);
        System.out.println("\nGEVONDEN WAARDES: " + gevondenLijst + "\n");

        List<TestResult> results = new ArrayList<TestResult>();
        boolean lijstenZijnExactGelijk = verwachteLijst.equals(gevondenLijst);
        if (!lijstenZijnExactGelijk) { // dan 1 voor 1 controleren
            for (String verwachtItem : verwachteLijst) {
                if (gevondenLijst.contains(verwachtItem)) {
                    results.add(new TestResult(true, "item gevonden."));
                } else {
                    results.add(new TestResult(false, String.format("Verwacht onderdeel '%s' niet gevonden.", verwachtItem)));
                }
            }
        } else {
            results.add(new TestResult(true, "Lijsten zijn gelijk."));
        }

        testResultaatVerwerker(results);
    }
	
		
    /**
	* Nadat alle resultaten zijn verzamelt, wordt met deze methode het uiteindelijke resultaat teruggegeven (assertion).
	*
	* @param results De lijst met verzamelde resultaten.
	*/
    private static void testResultaatVerwerker(List<TestResult> results) {
        String message;
        int failureCount = 0;
        System.out.println();
        System.out.println("Testresultaten: ");
        for (TestResult result : results) {
            if (!result.success) {
                failureCount++;
                System.out.println(result.message);
            }
        }
        message = String.format("Er zijn '%s' testen uitgevoerd en '%s' gefaald. Zie logging voor meer informatie.", results.size(), failureCount);
        if (failureCount > 0) {
            fail(message); //junit laten weten dat de test is gefaald;
        } else {
            System.out.println(message);
        }
        System.out.println();
    }

    /**
     * Dit object wordt gebruikt waar meerdere onderdelen gecontroleerd worden en daarna pas een testresultaat teruggegeven wordt.
     */
    public static class TestResult {
        boolean success;
        String message;

        TestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
