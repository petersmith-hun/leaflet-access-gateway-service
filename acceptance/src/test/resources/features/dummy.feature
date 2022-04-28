Feature: Dummy feature
  This is just a dummy feature for testing Cucumber integration

  Scenario: Calling health check
    When calling the health check endpoint
    Then application responds with HTTP status OK
