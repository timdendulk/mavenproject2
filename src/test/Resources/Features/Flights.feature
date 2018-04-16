Feature: Booking a flight
@Tim
  Scenario Outline: i want to search for a flight and see the results
    Given I am on the homepage
    And i click on the flights page
    When i enter a <city>, a <destination> and a <date>
    And i click on search
    Then i see the results for that <city>, <destination> and <date>

    Examples:
    | city | destination | date |
    |  Amsterdam | Budapest | 04-10-18 |
    | New York | London | 13-09-18 |
    | Dubai | Moscow | 16-06-18 |
