Feature: Identity and access management
  # Acceptance specification; executable coverage is provided by IamIntegrationTests and IamSecurityTests.

  Scenario: An adult registers with trusted identity information
    Given the identity provider verifies the adult DNI and personal information
    When the adult registers with a unique email and a valid password
    Then a PATIENT account and a patient profile are created
    And the password is stored as a BCrypt hash
    And a welcome email is queued in the same transaction

  Scenario: Admission staff is created by a super administrator
    Given an authenticated SUPER_ADMIN and a verified adult identity
    When the administrator provides an unused corporate email and phone
    Then an ADMISSION_STAFF account is created
    And a single-use password setup invitation is emailed
    And a patient cannot perform the same operation

  Scenario: A tutor links a verified minor
    Given a registered adult patient and a trusted guardian relationship
    When the patient confirms filiation and submits the minor identity
    Then the minor has a patient profile without login credentials
    And an existing guardian link cannot be transferred automatically

  Scenario: Removing a minor link preserves patient history
    Given a minor linked to the authenticated tutor
    When the tutor removes the link
    Then the minor patient profile is preserved
    And the former tutor loses access to that profile

  Scenario: Password recovery avoids account enumeration
    Given a known or unknown email address
    When recovery is requested
    Then the API returns the same generic confirmation
    And only an active registered account receives a recovery token

  Scenario: A recovery link expires and is single use
    Given a password recovery link
    When the link is redeemed before its fifteen minute deadline
    Then the password is updated and previous sessions are revoked
    And a second or concurrent redemption is rejected
    And a link at or after its deadline is rejected

  Scenario: An operator handles total loss of account access
    Given a recovery request without access to the previous email or phone
    When a SUPER_ADMIN verifies the physical DNI and approves the requested email
    Then an audited approval sends a password reset link to the confirmed email
    And the unapproved public request alone never grants account access

  Scenario: Updating contact information does not change identity or role
    Given an authenticated account
    When the owner changes their email and phone
    Then the verified DNI and role are preserved
    And the previous and new email addresses receive a security notification
    And updating another account is forbidden

  Scenario: Login validates the selected role and current session state
    Given a registered active account
    When the user supplies their email or DNI, password and stored role
    Then a signed expiring JWT and persisted session are issued
    And an incorrect role or password returns a generic unauthorized response
    And logout invalidates that session
