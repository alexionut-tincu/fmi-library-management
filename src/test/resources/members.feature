Feature: member management
  Scenario: client creates and retrieves members
    When the client creates a member named "Frodo" with email frodo@theshire.me
    Then the member response status code is 201
    When the client creates a member named "Samwise" with email sam@theshire.me
    Then the member response status code is 201
    When the client retrieves all members
    Then the client can see at least 2 members

  Scenario: client retrieves member by id
    Given a member named "Gandalf" with email gandalf@istari.me exists
    When the client retrieves the member by id
    Then the member response status code is 200
    And the member has name "Gandalf" and email gandalf@istari.me

  Scenario: client deletes member
    Given a member named "Boromir" with email boromir@gondor.me exists
    When the client deletes the member
    Then the member response status code is 204
    When the client tries to retrieve the deleted member by id
    Then the member response status code is 404

  Scenario: client cannot create duplicate email
    Given a member named "Legolas" with email legolas@mirkwood.me exists
    When the client tries to create a member named "Legolas Twin" with email legolas@mirkwood.me
    Then the member response status code is 400