Feature: Password reset flow tests

  @PositiveScenario
  Scenario: Requesting and confirming password reset

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user receives the PASSWORD_RESET_REQUEST_MAIL mail
      And the mail contains a reset token
      And the reset token expires in 15 seconds
      And the reset token scope is limited write:reclaim authority

    Given the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass111
      And the user confirms the password is newpass111
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status FOUND
      And the user is redirected to /login?pwreset_status=success

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And the user uses the password newpass111

     When the user signs in

     Then the application responds with HTTP status FOUND
      And the user is redirected to /

  @NegativeScenario
  Scenario: Requesting password reset with a non-existing user

    Given the user identifies with the email address non-existing-user@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user does not receive the PASSWORD_RESET_REQUEST_MAIL mail

  @NegativeScenario
  Scenario: Requesting password reset with incorrect email

    Given the user identifies with the email address invalid-email-address
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the response body contains "You've provided incorrect information, please check and try again"

  @Ignore # TODO re-enable after LFLT-466
  @NegativeScenario
  Scenario: Requesting password reset with failed ReCaptcha

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And ReCaptcha verification failed

     When the user requests password reset

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Requesting password reset and confirming it with invalid password pair

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user receives the PASSWORD_RESET_REQUEST_MAIL mail
      And the mail contains a reset token
      And the reset token expires in 15 seconds
      And the reset token scope is limited write:reclaim authority

    Given the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass111
      And the user confirms the password is differentnewpass1
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status OK
      And the response body contains "You've provided incorrect information, please check and try again"

  @Ignore # TODO re-enable after LFLT-467
  @NegativeScenario
  Scenario: Confirming password reset with an expired token

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user receives the PASSWORD_RESET_REQUEST_MAIL mail
      And the mail contains a reset token
      And the reset token expires in 15 seconds
      And the reset token scope is limited write:reclaim authority

    Given the client waits for 16 seconds
      And the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass111
      And the user confirms the password is newpass111
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Trying to reuse an already used reset token

    Given the user identifies with the email address test-editor-2@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user receives the PASSWORD_RESET_REQUEST_MAIL mail
      And the mail contains a reset token
      And the reset token expires in 15 seconds
      And the reset token scope is limited write:reclaim authority

    Given the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass112
      And the user confirms the password is newpass112
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status FOUND

    Given the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass112
      And the user confirms the password is newpass112
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status UNAUTHORIZED

  @NegativeScenario
  Scenario: Attempting to use old credentials after password reset

    Given the user identifies with the email address test-editor-3@ac-leaflet.local
      And ReCaptcha verification succeeded

     When the user requests password reset

     Then the application responds with HTTP status OK
      And the UI notifies the user about the accepted password reset request
      And the user receives the PASSWORD_RESET_REQUEST_MAIL mail
      And the mail contains a reset token
      And the reset token expires in 15 seconds
      And the reset token scope is limited write:reclaim authority

    Given the user uses the link in the mail to navigate to the confirmation form
      And the user is authorized with the reset token
      And the user uses the password newpass114
      And the user confirms the password is newpass114
      And ReCaptcha verification succeeded

     When the user confirms the password reset

     Then the application responds with HTTP status FOUND
      And the user is redirected to /login?pwreset_status=success

    Given the user identifies with the email address test-editor-3@ac-leaflet.local
      And the user uses the password testpw01

     When the user signs in

     Then the application responds with HTTP status FOUND
      And the user is redirected to /login?auth=fail
