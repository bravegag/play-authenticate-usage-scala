
import slick.driver.PostgresDriver
import slick.jdbc.meta.MTable
import slick.codegen.SourceCodeGenerator
import slick.driver.PostgresDriver.backend.Database
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Generator extends App {
  val slickDriver = "slick.driver.PostgresDriver"
  val jdbcDriver = "org.postgresql.Driver"
  val url = "jdbc:postgresql://localhost:5432/exampledb?searchpath=public"
  val outputDir = "./app/"
  val pkg = "generated"
  val username = "postgres"
  val password = "postgres"

  val db = Database.forURL(url, username, password)
  val model = db.run(PostgresDriver.createModel(Some(MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW"))))))
  // customize code generator
  val codegenFuture : Future[SourceCodeGenerator] = model.map(model => new SourceCodeGenerator(model) {
    // add some custom import
    override def code = "import dao._" + "\n" + super.code

  })
  Await.result(codegenFuture, Duration.Inf).writeToFile(slickDriver, outputDir, pkg, "Tables", "Tables.scala")
}