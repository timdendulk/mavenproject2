package steps;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.PendingException;
import static org.junit.Assert.assertEquals;
import Impl.Implementatie;
import java.io.File;
import Impl.Implementatie;
import Impl.BrowserHelper;

public class ExampleSteps {

    //Implementatie implementatie aa;
    Implementatie implementatie = new Implementatie();

    @Given("^I am on the homepage and click on cars$")
    public void iAmOnTheHomepageAndClickOnCars() {
        //implementatie = new Implementatie();
        implementatie.gaNaarCukeWorkshopPagina();
    }

    @When("^i choose all the required details$")
    public void iChooseAllTheRequiredDetails() {
        implementatie.clickonbutton("a[href*=\"#CARTRAWLER\"]");
    }

    @Then("^I should be able to view details and search result$")
    public void iShouldBeAbleToViewDetailsAndSearchResult() {
        implementatie.checkiftrue("#s2id_autogen6");
        implementatie.clickonbutton("#s2id_autogen6 > a > span.select2-chosen");
        implementatie.typetext("#select2-drop > div > input","Amsterdam");
        implementatie.clickindropdown("//*[@id=\"select2-drop\"]/ul/li[2]/div");
    }
}
