# [![Build Status](https://travis-ci.org/bravegag/play-authenticate-usage-scala.svg?branch=master)](https://travis-ci.org/bravegag/play-authenticate-usage-scala) [![Apache 2.0 License](https://img.shields.io/badge/license-Apache2-green.svg) ](https://github.com/bravegag/play-authenticate-usage-scala/blob/master/LICENSE) PlayAuthenticate - usage sample application Play! Framework 2 (Scala)

# Play Authenticate usage in Scala

This project is a complete rewrite of [PlayAuthenticate usage sample](https://github.com/joscha/play-authenticate):
* Rewritten completely in Scala 2.11.8
* It is a database-driven application i.e. the database design dictates what the generated Scala model is.
* Builds on top of Play 2.5.x, Postgres 9.4 and Slick 3.1.1
* Features a generic Slick DAO that is self-contained and context-free, it can be reused in other projects `app/dao/generic`.
* Integrates [Web Jars](https://github.com/webjars/webjars-play) for all the relevant dependencies. 
* Features reCAPTCHA using the excellent [Play reCAPTCHA Module](https://github.com/chrisnappin/play-recaptcha) implementation.
* Integrates [Play-Bootstrap](http://adrianhurt.github.io/play-bootstrap/) for all form implementations.
* Integrates [Font Awesome icons](http://fontawesome.io/) that is used as part of forms and design details.
* Integrates one free [Anli Zaimi](http://azmind.com/)'s custom Bootstrap theme for the different forms providing a neat consistent L&F that integrates well with the two points above.  
* A custom Slick code generator is implemented under `test/generator` and it generates the class `app/generated/Tables.scala`. 
* Features very strict separation of application layers and programming to the interface: DAO, Service, Play MVC:
    - completely context-free reusable DAO under `app/dao/generic`.
    - dao extensions e.g. UserDao have no dependencies outside the dao package.
    - Service layer under `app/services` uses the `app/dao` package.
    - A pattern "Pluggable Service" is implemented that enables calling natural members on vanilla 
      case class e.g. `app/generated/Tables#UserRow` doesn't have a method `.roles` but this pattern allows
      calling that on any `UserRow` instance.
    - Play controllers and views only depend on the Service layer implemented under `app/services`.
* The DAO layer is fully unit tested under the test package `test/dao/`.
* There are two configured databases: 
    - `myappdb` for running the application.
    - `mytestdb` for running the unit testing.
* The application builds on Travis CI https://travis-ci.org/bravegag/play-authenticate-usage-scala including 
  execution of the unit tests on Postgres.

# Future work

Although I have tried very hard to have this project as a perfect template code base for my web 
application projects there is some room for improvement:

* The PlayAuthenticate plugin is written in Play Java therefore to make this project in Scala 
  it was necessary tunneling top Play abstractions (e.g. Context, Session, Request, etc) 
  between Scala -> Java and back to -> Scala. The Play Java <-> Scala interoperability due to 
  reusing a Java plugin resulted in some using of the Play internal `play.core.j.JavaHelpers` 
  implementation. Once the top Play abstractions are made compatible between Java and Scala 
  then one can refactor away from using `play.core.j.JavaHelpers`. See discussion the Playframework 
  support page [NPE attempting to bindFromRequest a Java Form from a Scala App](https://github.com/playframework/playframework/issues/6831).

# FAQ

## Create/drop Postgres App database in Linux with:

`sudo -u postgres createdb myappdb`

`sudo -u postgres dropdb myappdb`

## Create/drop Postgres Test database in Linux with:

`sudo -u postgres createdb mytestdb`

`sudo -u postgres dropdb mytestdb`

# License

Copyright (c) 2016-2017 Giovanni Azua

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. 
You may obtain a copy of the License in the LICENSE file, or at:
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language governing permissions and limitations under the License.
