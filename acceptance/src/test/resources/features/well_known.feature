Feature: Well-known (meta-information) endpoints tests

  Scenario: Calling JWKs endpoint

    When the JWKs endpoint is called
    Then the application responds with HTTP status OK
     And the JWKs response contains one entry
     And the JWKs response contains the public RSA key with RS256 algorithm for signature verification
     And the JWKs response contains the key id with the value of acceptance-test-public-key

  Scenario: Calling the server meta-info endpoint

    When the server meta-info endpoint is called
    Then the application responds with HTTP status OK
     And the meta-info response contains the key issuer with value http://localhost:9285/lags
     And the meta-info response contains the key authorization_endpoint with value http://localhost:9285/lags/oauth/authorize
     And the meta-info response contains the key token_endpoint with value http://localhost:9285/lags/oauth/token
     And the meta-info response contains the key jwks_uri with value http://localhost:9285/lags/.well-known/jwks
     And the meta-info response contains the key token_introspection_endpoint with value http://localhost:9285/lags/oauth/introspect
     And the meta-info response contains the key grant_types_supported with values authorization_code,client_credentials,password
     And the meta-info response contains the key token_endpoint_auth_methods_supported with values client_secret_post,client_secret_basic
     And the meta-info response contains the key response_types_supported with values code
