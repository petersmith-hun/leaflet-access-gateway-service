Feature: External provider based user sign-in flow tests

  @PositiveScenario
  Scenario: Successfully signing-in with a GitHub account for the first time

    Given the external user uses the email address ghuser1-primary@dev.local

     When the user requests sign-in via GitHub
      And the user authorizes access on GitHub
      And the user is returned to the authorization page

     Then the external user is logged in as Test User
      And the external user is logged in with the email address ghuser1-primary@dev.local
      And the external user has the scopes read:comments:own read:users:own write:comments:own

  @PositiveScenario
  Scenario: Successfully returning with a GitHub account

    Given the external user uses the email address test-user-github@ac-leaflet.local

     When the user requests sign-in via GitHub
      And the user authorizes access on GitHub
      And the user is returned to the authorization page

     Then the external user is logged in as Test External User GitHub
      And the external user is logged in with the email address test-user-github@ac-leaflet.local
      And the external user has the scopes read:comments:own read:users:own write:comments:own

  @PositiveScenario
  Scenario: Successfully signing-in with a Google account for the first time

    Given the external user uses the email address googleuser1@dev.local

     When the user requests sign-in via Google
      And the user authorizes access on Google
      And the user is returned to the authorization page

     Then the external user is logged in as Test User
      And the external user is logged in with the email address googleuser1@dev.local
      And the external user has the scopes read:comments:own read:users:own write:comments:own

  @PositiveScenario
  Scenario: Successfully returning with a Google account

    Given the external user uses the email address test-user-google@ac-leaflet.local

     When the user requests sign-in via Google
      And the user authorizes access on Google
      And the user is returned to the authorization page

     Then the external user is logged in as Test External User Google
      And the external user is logged in with the email address test-user-google@ac-leaflet.local
      And the external user has the scopes read:comments:own read:users:own write:comments:own

  @NegativeScenario
  Scenario: Sign-up is not possible via external provider (GitHub) with an already used email address from a different source

    Given the external user uses the email address test-user-1@ac-leaflet.local

     When the user requests sign-in via GitHub
      And the user authorizes access on GitHub

     Then the user is redirected to /login?ext_auth=ADDRESS_IN_USE

  @NegativeScenario
  Scenario: Sign-up is not possible via external provider (Google) with an already used email address from a different source

    Given the external user uses the email address test-user-1@ac-leaflet.local

     When the user requests sign-in via Google
      And the user authorizes access on Google

     Then the user is redirected to /login?ext_auth=ADDRESS_IN_USE

  @NegativeScenario
  Scenario: User rejects access on GitHub

    Given the external user uses the email address ghuser1-primary@dev.local

     When the user requests sign-in via GitHub
      And the user rejects access on GitHub

     Then the user is redirected to /login?ext_auth=FAILURE

  @NegativeScenario
  Scenario: User rejects access on Google

    Given the external user uses the email address googleuser1@dev.local

     When the user requests sign-in via Google
      And the user rejects access on Google

     Then the user is redirected to /login?ext_auth=FAILURE
