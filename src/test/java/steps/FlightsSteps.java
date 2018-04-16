package steps;

import Impl.Implementatie;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class FlightsSteps {
    private Implementatie impl = new Implementatie();

    @Given("^I am on the homepage$")
    public void iAmOnTheHomepage() {
        impl.gaNaarDeWebsite();

    }

    @And("^i click on the flights page$")
    public void iClickOnTheFlightsPage()  {
        impl.clickonbutton("CSS SELECTOR NOG INVULLEN HIER");
        //even wachten tot de website weer online is om de CSS selector te vinden
    }

    @And("^i click on search$")
    public void iClickOnSearch() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^i see the results for that <city>, <destination> and <date>$")
    public void iSeeTheResultsForThatCityDestinationAndDate() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^i enter a <city>, a <destination> and a <date>$")
    public void iEnterACityADestinationAndADate() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
