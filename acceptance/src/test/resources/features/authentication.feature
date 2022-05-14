Feature: Standard user sign-in flow tests

  @PositiveScenario
  Scenario: Successfully signing-in with a registered user

    Given the user identifies with the email address test-user-1@ac-leaflet.local
      And the user uses the password testpw01

     When the user signs in

     Then the application responds with HTTP status FOUND
      And the user is redirected to /

  @PositiveScenario
  Scenario: A new user signs up with local account and afterwards signs in

    Given the user identifies with the email address new-user-1001@dev.local
      And the user uses the password newpw01
      And the user confirms the password is newpw01
      And the user picks the username New User
      And ReCaptcha verification succeeded

     When the user signs up

     Then the application responds with HTTP status FOUND

    Given the user identifies with the email address new-user-1001@dev.local
      And the user uses the password newpw01

     When the user signs in

     Then the application responds with HTTP status FOUND
      And the user is redirected to /
      And the user receives the SIGN_UP_CONFIRMATION_MAIL mail

  @PositiveScenario
  Scenario: Successfully signing out a signed in user

    Given the user is signed in as test-user-1@ac-leaflet.local with password testpw01

     When the user signs out

     Then the application responds with HTTP status FOUND

  @NegativeScenario
  Scenario: Failed login attempt with non-existing user

    Given the user identifies with the email address non-existing@ac-leaflet.local
      And the user uses the password testpw01

     When the user signs in

    Then the application responds with HTTP status FOUND
     And the user is redirected to /login?auth=fail

  @NegativeScenario
  Scenario: Failed login attempt with invalid password

    Given the user identifies with the email address test-user-1@ac-leaflet.local
      And the user uses the password invalid1

     When the user signs in

    Then the application responds with HTTP status FOUND
     And the user is redirected to /login?auth=fail

  @NegativeScenario
  Scenario: A new user tries to sign up with an address already in use

    Given the user identifies with the email address test-user-1@ac-leaflet.local
      And the user uses the password newpw01
      And the user confirms the password is newpw01
      And the user picks the username New User
      And ReCaptcha verification succeeded

     When the user signs up

     Then the application responds with HTTP status FOUND
      And the user is redirected to /signup?signup_status=ADDRESS_IN_USE

  @NegativeScenario
  Scenario: A new user tries to sign up but ReCaptcha verification fails

    Given the user identifies with the email address new-user-1002@dev.local
      And the user uses the password newpw01
      And the user confirms the password is newpw01
      And the user picks the username New User
      And ReCaptcha verification failed

     When the user signs up

     Then the application responds with HTTP status FOUND
      And the user is redirected to /signup?signup_status=RE_CAPTCHA_VERIFICATION_FAILED

  @NegativeScenario
  Scenario: A new user tries to sign up but password validation fails

    Given the user identifies with the email address new-user-1003@dev.local
      And the user uses the password newpw01
      And the user confirms the password is differentpw01
      And the user picks the username New User
      And ReCaptcha verification succeeded

     When the user signs up

     Then the application responds with HTTP status OK
      And the response body contains "You've provided incorrect information, please check and try again"

  @NegativeScenario
  Scenario: A new user tries to sign up but email validation fails

    Given the user identifies with the email address some-invalid-email
      And the user uses the password newpw01
      And the user confirms the password is newpw01
      And the user picks the username New User
      And ReCaptcha verification succeeded

     When the user signs up

     Then the application responds with HTTP status OK
      And the response body contains "You've provided incorrect information, please check and try again"
