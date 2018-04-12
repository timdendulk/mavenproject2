package steps;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.PendingException;
import static org.junit.Assert.assertEquals;
import Impl.Implementatie;
import Testutil.BestandHelper;
import Testutil.Wacht;
import java.io.File;
import java.util.List;
import Impl.Implementatie;
import Impl.BrowserHelper;

public class ExampleSteps {

    //Implementatie implementatie;
    Implementatie implementatie = new Implementatie();

    @Given("^I am on the website$")
    public void iAmOnTheWebsite()  {
        //implementatie = new Implementatie();
        implementatie.gaNaarCukeWorkshopPagina();
    }

    @When("^i click on the cars button$")
    public void iClickOnTheCarsButton() {
        implementatie.clickonbutton("a[href*=\"#CARTRAWLER\"]");
    }

    @Then("^I should arrive at the cars input screen and be able to input details$")
    public void iShouldArriveAtTheCarsInputScreenAndBeAbleToInputDetails() {
        implementatie.checkiftrue("#s2id_autogen6");
        implementatie.clickonbutton("#s2id_autogen6 > a > span.select2-chosen");
        implementatie.typetext("#select2-drop > div > input");
        implementatie.clickindropdown("//*[@id=\"select2-drop\"]/ul/li[2]/div/text()");
    }
}
