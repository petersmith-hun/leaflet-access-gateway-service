Feature: OAuth2 Client Credentials authorization flow tests

  @PositiveScenario
  Scenario Outline: Successfully authorizing a registered client

    Given a client identified by <clientID> tries authorization
      And the client authenticates with its client secret <clientSecret>
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the <audience> audience
      And the client requests access for scope <scope>

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope <scope>
      And the returned token is a Bearer type token

    Examples:
      | clientID             | clientSecret       | audience                            | scope                        |
      | dummy_test_service_1 | dummyservicepw1234 | dummy:acceptance:svc:othersvc:test  | write:schedule               |
      | dummy_test_service_1 | dummyservicepw1234 | dummy:acceptance:svc:othersvc:test  | write:schedule read:schedule |
      | dummy_test_service_2 | dummyservicepw9876 | dummy:acceptance:svc:fourthsvc:test | read:accounts write:accounts |
      | dummy_test_service_2 | dummyservicepw9876 | dummy:acceptance:svc:fourthsvc:test | write:accounts               |
      | dummy_test_service_2 | dummyservicepw9876 | dummy:acceptance:svc:thirdsvc:test  | read:admin                   |

  @PositiveScenario
  Scenario: Successfully authorizing a registered application

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:dummysvc:test audience
      And the client requests access for scope read:entries write:entries

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:entries write:entries
      And the returned token is a Bearer type token

  @NegativeScenario
  Scenario Outline: Rejected authorization scenarios

    Given a client identified by <clientID> tries authorization
      And the client authenticates with its client secret <clientSecret>
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the <audience> audience
      And the client requests access for scope <scope>

     When the client requests a token

     Then the application responds with HTTP status BAD_REQUEST
      And the OAuth error code is <errorCode>
      And the rejection message is <errorMessage>

    Examples:
      | clientID             | clientSecret           | audience                           | scope                        | errorCode           | errorMessage                                                                                                               |
      | dummy_test_service_1 | dummyservicepw1234     | dummy:acceptance:svc:thirdsvc:test | read:admin                   | unauthorized_client | Target client [testsvc3] does not allow access for source client [testsvc1]                                                |
      | dummy_test_app_1     | dummyapplicationpw5678 | dummy:acceptance:svc:othersvc:test | read:schedule write:schedule | invalid_scope       | Target client [testsvc2] does not allow the requested scope [[read:schedule, write:schedule]] for source client [testapp1] |
      | dummy_test_service_1 | dummyservicepw1234     | non:existing:audience              | read:all                     | unauthorized_client | Requested access for non-registered OAuth client [non:existing:audience]                                                   |

  @NegativeScenario
  Scenario: Rejected authorization for registered client, caused by invalid client secret

    Given a client identified by dummy_test_service_1 tries authorization
      And the client authenticates with its client secret invalid_secret
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience
      And the client requests access for scope write:schedule

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Rejected authorization for unregistered client

    Given a client identified by unregistered_client tries authorization
      And the client authenticates with its client secret secret1
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience
      And the client requests access for scope write:schedule

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Rejected authorization for registered client, caused by missing grant type

    Given a client identified by dummy_test_service_1 tries authorization
      And the client authenticates with its client secret dummyservicepw1234
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience
      And the client requests access for scope write:schedule

     When the client requests a token

     Then the application responds with HTTP status BAD_REQUEST
      And the rejection message is Unsupported grant type [null]

  @NegativeScenario
  Scenario: Rejected authorization for registered client, caused by missing audience

    Given a client identified by dummy_test_service_1 tries authorization
      And the client authenticates with its client secret dummyservicepw1234
      And the client chooses client_credentials grant type
      And the client requests access for scope write:schedule

     When the client requests a token

     Then the application responds with HTTP status BAD_REQUEST
      And the rejection message is Value for required authorization parameter [audience] is missing

  @NegativeScenario
  Scenario: Rejected authorization for registered client, caused by missing scope

    Given a client identified by dummy_test_service_1 tries authorization
      And the client authenticates with its client secret dummyservicepw1234
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience

     When the client requests a token

     Then the application responds with HTTP status BAD_REQUEST
      And the rejection message is Value for required authorization parameter [scope] is missing

  @NegativeScenario
  Scenario: Unauthorized token request if client is not authenticated

    Given a client identified by dummy_test_service_1 tries authorization
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience
      And the client requests access for scope write:schedule

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED

  @LongScenario
  @NegativeScenario
  Scenario: Token introspection returns inactive flag for expired token

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the client chooses client_credentials grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:dummysvc:test audience
      And the client requests access for scope read:entries write:entries

     When the client requests a token

     Then the application responds with HTTP status OK

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the previously issued token is prepared for introspection

     When the client requests introspection

     Then the application responds with HTTP status OK
      And the introspected token is active

    Given the client waits for 31 seconds

     When the client requests introspection

     Then the application responds with HTTP status OK
      And the introspected token is expired
