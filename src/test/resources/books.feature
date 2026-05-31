Feature: book management
  Scenario: client creates and retrieves books
    When the client creates a book with title "Dune" and author "Frank Herbert" and isbn dune-isbn-001
    Then the book response status code is 201
    When the client creates a book with title "Foundation" and author "Isaac Asimov" and isbn foundation-isbn-002
    Then the book response status code is 201
    When the client retrieves all books
    Then the client can see at least 2 books

  Scenario: client retrieves book by id
    Given a book with title "Neuromancer" and author "William Gibson" and isbn neuro-isbn-003 exists
    When the client retrieves the book by id
    Then the book response status code is 200
    And the book has title "Neuromancer" and author "William Gibson"

  Scenario: client deletes book
    Given a book with title "Fahrenheit 451" and author "Ray Bradbury" and isbn f451-isbn-004 exists
    When the client deletes the book
    Then the book response status code is 204
    When the client tries to retrieve the deleted book by id
    Then the book response status code is 404