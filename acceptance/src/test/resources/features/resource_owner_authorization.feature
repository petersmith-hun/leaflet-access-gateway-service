Feature: OAuth2 Resource Owner (Password Grant) authorization flow tests

  @PositiveScenario
  Scenario: Successfully authorizing a user via password grant flow with automatic scope negotiation

    Given a client identified by dummy_front_app_1 tries authorization
      And the client authenticates with its client secret frontapp4455
      And the user picks the username test-user-1@ac-leaflet.local
      And the user uses the password testpw01
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:comments:own read:users:own write:comments:own write:users:own
      And the returned token is a Bearer type token

  @PositiveScenario
  Scenario: Successfully authorizing a user via password grant flow with decreased scope

    Given a client identified by dummy_front_app_1 tries authorization
      And the client authenticates with its client secret frontapp4455
      And the user picks the username test-user-1@ac-leaflet.local
      And the user uses the password testpw01
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience
      And the client requests access for scope read:users:own write:comments:own write:users:own

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:users:own write:comments:own write:users:own
      And the returned token is a Bearer type token

  @PositiveScenario
  Scenario: Successfully authorizing an editor user to the admin system

    Given a client identified by mock_lms tries authorization
      And the client authenticates with its client secret mocklms1234
      And the user picks the username test-editor-pwgrant@ac-leaflet.local
      And the user uses the password testpw01
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience

     When the client requests a token

     Then the application responds with HTTP status OK
      And the response contains a token
      And the returned token expires in 30 seconds
      And the returned token gives access to scope read:comments:own read:users:own write:comments:own write:users:own read:categories read:comments read:documents read:entries read:tags write:categories write:comments write:documents write:entries write:tags
      And the returned token is a Bearer type token

  @NegativeScenario
  Scenario: Failed authorization due to missing username

    Given a client identified by dummy_front_app_1 tries authorization
      And the client authenticates with its client secret frontapp4455
      And the user uses the password testpw01
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Failed authorization due to missing password

    Given a client identified by dummy_front_app_1 tries authorization
      And the client authenticates with its client secret frontapp4455
      And the user picks the username test-user-1@ac-leaflet.local
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Failed authorization due to invalid credentials

    Given a client identified by dummy_front_app_1 tries authorization
      And the client authenticates with its client secret frontapp4455
      And the user picks the username non-existing-user@ac-leaflet.local
      And the user uses the password invalidpw
      And the client chooses password grant type
      And the client requests access to a service identified by the dummy:acceptance:svc:mockleaflet:test audience

     When the client requests a token

     Then the application responds with HTTP status UNAUTHORIZED
