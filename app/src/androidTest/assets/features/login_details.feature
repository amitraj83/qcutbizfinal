Feature: Enter login details

  Scenario Outline: Successful login
    Given I have a SigninActivity
    When I entered username "<email>"
    When I entered password "<password>"
    And I click Signin button
    Then I should see shop status "<result>"
    Examples:
      | email          | password | result |
      | butta@gmail.com | 12345678 | ONLINE |
