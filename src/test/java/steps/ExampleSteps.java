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

    //Implementatie implementatie;
    Implementatie implementatie = new Implementatie();

    @Given("^I am on the homepage and click on cars$")
    public void iAmOnTheHomepageAndClickOnCars() {
        //implementatie = new Implementatie();
        implementatie.gaNaarCukeWorkshopPagina();
        implementatie.clickonbutton("//*[@id=\"body-section\"]/div[1]/div/div/div[1]/div/ul/li[4]/a");
    }

    @When("^i choose all the required details$")
    public void iChooseAllTheRequiredDetails() {
        implementatie.checkiftrue("#s2id_autogen6");
        implementatie.clickonbutton("//*[@id=\"s2id_autogen6\"]/a");
        implementatie.typetext("//*[@id=\"select2-drop\"]/div/input","Amsterdam");
        //onderstaande werkt niet, kan specifieke 'Amsterdam downtown' element niet vinden
        implementatie.clickindropdown("\"//*[@id=\\\"select2-drop\\\"]/ul/li[2]/div/span\"");
    }

    @Then("^I should be able to view details and search result$")
    public void iShouldBeAbleToViewDetailsAndSearchResult() {

    }
}
