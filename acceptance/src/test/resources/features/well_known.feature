Feature: Well-known (meta-information) endpoints tests

  Scenario: Calling JWKs endpoint

    When the JWKs endpoint is called
    Then the application responds with HTTP status OK
     And the response contains one entry
     And the response contains the public RSA key for signature verification