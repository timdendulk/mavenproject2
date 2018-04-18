package steps;

import Impl.Implementatie;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

public class FlightsSteps {
    private Implementatie impl = new Implementatie();

    @Given("^I am on the homepage$")
    public void iAmOnTheHomepage() {
        impl.gaNaarDeWebsite();

    }

    @And("^i click on the flights page$")
    public void iClickOnTheFlightsPage() {
//        impl.clickonbutton("CSS SELECTOR NOG INVULLEN HIER");
        //even wachten tot de website weer online is om de CSS selector te vinden
    }

    @And("^i click on search$")
    public void iClickOnSearch()  {
    }

    @When("^i enter a city$")
    public void iEnterACity()  {
    }

    @Then("^i see the results for that city$")
    public void iSeeTheResultsForThatCity() {
        Assert.assertTrue("test",true);
    }
}
