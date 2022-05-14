Feature: OAuth2 Authorization Code authorization flow tests

  @PositiveScenario
  Scenario: Successful authorization of a local user

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-1234
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the user is redirected to the specified redirection
      And the response contains the authorization code
      And the response contains the sent state

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the client chooses authorization_code grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:thirdsvc:test audience
      And the client requests access for scope write:admin read:admin
      And the client uses the previously claimed authorization code
      And the client uses the specified redirect URI

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope write:admin read:admin
      And the returned token is a Bearer type token

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the previously issued token is prepared for introspection

     When the client requests introspection

     Then the application responds with HTTP status OK
      And the introspected token is active
      And the introspected token expires in about 30 seconds
      And the introspected token belongs to dummy_test_app_1 client
      And the introspected token belongs to the Administrator user with ID 1

  @PositiveScenario
  Scenario: Successful admin system authorization for an admin user

    Given the authorization is requested by application mock_lms
      And the response type is set to code
      And the application requests redirection to http://localhost:9291/mock/admin
      And the sent state value is state-9876
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the user is redirected to the specified redirection
      And the response contains the authorization code
      And the response contains the sent state

    Given a client identified by mock_lms tries authorization
      And the client authenticates with its client secret mocklms1234
      And the client chooses authorization_code grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience
      And the client uses the previously claimed authorization code
      And the client uses the specified redirect URI

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:users:own write:comments:own write:users:own read:categories read:comments read:documents read:entries read:tags read:users write:categories write:comments write:documents write:entries write:tags read:admin write:admin write:users
      And the returned token is a Bearer type token

  @Ignore # TODO re-enable after LFLT-468
  @PositiveScenario
  Scenario: Successful admin system authorization for an editor user

    Given the authorization is requested by application mock_lms
      And the response type is set to code
      And the application requests redirection to http://localhost:9291/mock/admin
      And the sent state value is state-9876
      And the user is signed in as test-editor-2@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the user is redirected to the specified redirection
      And the response contains the authorization code
      And the response contains the sent state

    Given a client identified by mock_lms tries authorization
      And the client authenticates with its client secret mocklms1234
      And the client chooses authorization_code grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience
      And the client uses the previously claimed authorization code
      And the client uses the specified redirect URI

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:users:own write:comments:own write:users:own read:categories read:comments read:documents read:entries read:tags write:categories write:comments write:documents write:entries write:tags
      And the returned token is a Bearer type token

  @NegativeScenario
  Scenario: Rejected admin system authorization for a non-elevated user

    Given the authorization is requested by application mock_lms
      And the response type is set to code
      And the application requests redirection to http://localhost:9291/mock/admin
      And the sent state value is state-9876
      And the user is signed in as test-user-1@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN

  @NegativeScenario
  Scenario: Rejected admin system authorization for too broad requested scope

    Given the authorization is requested by application mock_lms
      And the response type is set to code
      And the application requests redirection to http://localhost:9291/mock/admin
      And the sent state value is state-8888
      And the client requests access for scope write:schedule read:schedule
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is Requested scope is broader than the user's authority range.

  @NegativeScenario
  Scenario: Rejected UI application authorization on invalid redirection

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:7777/possibly/an/attacker
      And the sent state value is state-2222
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is Specified redirection URI [http://localhost:7777/possibly/an/attacker] is not registered

  @Ignore # TODO re-enable after LFLT-469
  @NegativeScenario
  Scenario: Rejected UI application authorization for mismatching scope

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-5555
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the user is redirected to the specified redirection
      And the response contains the authorization code
      And the response contains the sent state

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the client chooses authorization_code grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:othersvc:test audience
      And the client requests access for scope read:schedule
      And the client uses the previously claimed authorization code
      And the client uses the specified redirect URI

     When the client requests a token

     Then the application responds with HTTP status FORBIDDEN

  @NegativeScenario
  Scenario: Rejected UI application authorization for application not using code response type

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to invalid
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-5555
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is Unsupported response type [invalid]

  @NegativeScenario
  Scenario: Rejected UI application authorization for application not specifying response type

    Given the authorization is requested by application dummy_test_app_1
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-5555
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is A mandatory field [response_type] is missing from request

  @NegativeScenario
  Scenario: Rejected authorization for a service not being permitted to use authorization code flow

    Given the authorization is requested by application dummy_test_service_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-5555
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is Client application is not permitted to use authorization code flow.

  @NegativeScenario
  Scenario: Rejected UI application authorization for application not specifying redirection

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the sent state value is state-5555
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is A mandatory field [redirect_uri] is missing from request

  @NegativeScenario
  Scenario: Rejected UI application authorization for application not specifying state

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is A mandatory field [state] is missing from request

  @NegativeScenario
  Scenario: Rejected UI application authorization for application not being authenticated

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-1234

     When the authorization is requested

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Rejected authorization for unregistered UI application

    Given the authorization is requested by application unregistered_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-1234
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is OAuth client by ID [unregistered_app_1] is not registered

  @LongScenario
  @NegativeScenario
  Scenario: Rejected UI application authorization on expired authorization code

    Given the authorization is requested by application dummy_test_app_1
      And the response type is set to code
      And the application requests redirection to http://localhost:9290/mock/external-app/dummy-callback
      And the sent state value is state-1234
      And the user is signed in as test-admin@ac-leaflet.local with password testpw01

     When the authorization is requested

     Then the user is redirected to the specified redirection
      And the response contains the authorization code
      And the response contains the sent state

    Given a client identified by dummy_test_app_1 tries authorization
      And the client authenticates with its client secret dummyapplicationpw5678
      And the client chooses authorization_code grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:thirdsvc:test audience
      And the client uses the previously claimed authorization code
      And the client uses the specified redirect URI
      And the client waits for 61 seconds

     When the client requests a token

     Then the application responds with HTTP status FORBIDDEN
      And the rejection message is Authorization has already expired.
