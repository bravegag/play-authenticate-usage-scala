import java.util.concurrent.TimeUnit

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
  val dbio = PostgresDriver.createModel(Some(MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW")))))
  val model = db.run(dbio)
  val future : Future[SourceCodeGenerator] = model.map(model => new SourceCodeGenerator(model))
  val codegen : SourceCodeGenerator = Await.result(future, Duration.create(5, TimeUnit.MINUTES))
  codegen.writeToFile(slickDriver, outputDir, pkg, "Tables", "Tables.scala")
}