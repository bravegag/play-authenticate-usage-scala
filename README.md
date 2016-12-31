# [![Build Status](https://travis-ci.org/bravegag/play-authenticate-usage-scala.svg?branch=master)](https://travis-ci.org/bravegag/play-authenticate-usage-scala) PlayAuthenticate - usage sample application Play! Framework 2 (Scala)

# Play Authenticate usage in Scala

This project is a complete rewrite of [PlayAuthenticate usage sample](https://github.com/joscha/play-authenticate):
* Rewritten completely in Scala 2.11.8
* It is a database-driven application i.e. the database design dictates what the generated Slick model is. 
* Uses Postgres and Slick 3.1.1
* Features a generic Slick DAO that is self contained and can be reused in isolation under `app/dao/generic`
* Integrates [Web Jars](https://github.com/webjars/webjars-play) for all the relevant dependencies. 
* Features reCAPTCHA using the excellent [Play reCAPTCHA Module](https://github.com/chrisnappin/play-recaptcha) implementation.
* A custom Slick code generator is implemented under `test/generator` and it generates the class `app/generated/Tables.scala` 
* Features very strict separation of application layers and programming to the interface: DAO, Service, Play MVC:
    - completely context-free reusable DAO under `app/dao/generic`
    - dao extensions e.g. UserDao have no dependencies outside the dao package
    - Service layer under `app/services` uses the `app/dao` package
    - A pattern "Pluggable Service" is implemented that enables calling natural members on vanilla 
      case class e.g. `app/generated/Tables#UserRow` doesn't have a method `.roles` but this pattern allows
      calling that on any `UserRow` instance
    - Play controllers and views only depend on the Service layer implemented under `app/services`
* The DAO layer is fully unit tested under the test package `test/dao/`
* There are two configured databases: 
    - `myappdb` for running the application
    - `mytestdb` for running the unit testing
* The application builds on Travis CI https://travis-ci.org/bravegag/play-authenticate-usage-scala including 
  execution of the unit tests on Postgres.

# FAQ

## Create/drop Postgres App database in Linux with:

`sudo -u postgres createdb myappdb`

`sudo -u postgres dropdb myappdb`

## Create/drop Postgres Test database in Linux with:

`sudo -u postgres createdb mytestdb`

`sudo -u postgres dropdb mytestdb`
