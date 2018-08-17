package generated
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import be.objectify.deadbolt.scala.models._
  import dao.generic._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(CookieTokenSeries.schema, LinkedAccount.schema, PlayEvolutions.schema, SecurityPermission.schema, SecurityRole.schema, TokenAction.schema, User.schema, UserSecurityPermission.schema, UserSecurityRole.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table CookieTokenSeries
   *  @param userId Database column user_id SqlType(int8)
   *  @param series Database column series SqlType(varchar), Length(50,true)
   *  @param token Database column token SqlType(varchar), Length(50,true)
   *  @param created Database column created SqlType(timestamp)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class CookieTokenSeriesRow(userId: Long, series: String, token: String, created: Option[java.sql.Timestamp], modified: Option[java.sql.Timestamp] = None) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching CookieTokenSeriesRow objects using plain SQL queries */
  implicit def GetResultCookieTokenSeriesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[java.sql.Timestamp]]): GR[CookieTokenSeriesRow] = GR{
    prs => import prs._
    CookieTokenSeriesRow.tupled((<<[Long], <<[String], <<[String], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table cookie_token_series. Objects of this class serve as prototypes for rows in queries. */
  class CookieTokenSeries(_tableTag: Tag) extends profile.api.Table[CookieTokenSeriesRow](_tableTag, "cookie_token_series") with IdentifyableTable[Long] {
              override def id = userId

    def * = (userId, series, token, created, modified) <> (CookieTokenSeriesRow.tupled, CookieTokenSeriesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(series), Rep.Some(token), created, modified).shaped.<>({r=>import r._; _1.map(_=> CookieTokenSeriesRow.tupled((_1.get, _2.get, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column series SqlType(varchar), Length(50,true) */
    val series: Rep[String] = column[String]("series", O.Length(50,varying=true))
    /** Database column token SqlType(varchar), Length(50,true) */
    val token: Rep[String] = column[String]("token", O.Length(50,varying=true))
    /** Database column created SqlType(timestamp) */
    val created: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("created")
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Foreign key referencing User (database name cookie_token_series_user_id_fkey) */
    lazy val userFk = foreignKey("cookie_token_series_user_id_fkey", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
              }
  /** Collection-like TableQuery object for table CookieTokenSeries */
  lazy val CookieTokenSeries = new TableQuery(tag => new CookieTokenSeries(tag))

  /** Entity class storing rows of table LinkedAccount
   *  @param userId Database column user_id SqlType(int8)
   *  @param providerUserId Database column provider_user_id SqlType(varchar), Length(100,true)
   *  @param providerKey Database column provider_key SqlType(varchar), Length(50,true)
   *  @param series Database column series SqlType(varchar), Length(50,true), Default(None)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class LinkedAccountRow(userId: Long, providerUserId: String, providerKey: String, series: Option[String] = None, modified: Option[java.sql.Timestamp] = None) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching LinkedAccountRow objects using plain SQL queries */
  implicit def GetResultLinkedAccountRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[java.sql.Timestamp]]): GR[LinkedAccountRow] = GR{
    prs => import prs._
    LinkedAccountRow.tupled((<<[Long], <<[String], <<[String], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table linked_account. Objects of this class serve as prototypes for rows in queries. */
  class LinkedAccount(_tableTag: Tag) extends profile.api.Table[LinkedAccountRow](_tableTag, "linked_account") with IdentifyableTable[Long] {
              override def id = userId

    def * = (userId, providerUserId, providerKey, series, modified) <> (LinkedAccountRow.tupled, LinkedAccountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(providerUserId), Rep.Some(providerKey), series, modified).shaped.<>({r=>import r._; _1.map(_=> LinkedAccountRow.tupled((_1.get, _2.get, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column provider_user_id SqlType(varchar), Length(100,true) */
    val providerUserId: Rep[String] = column[String]("provider_user_id", O.Length(100,varying=true))
    /** Database column provider_key SqlType(varchar), Length(50,true) */
    val providerKey: Rep[String] = column[String]("provider_key", O.Length(50,varying=true))
    /** Database column series SqlType(varchar), Length(50,true), Default(None) */
    val series: Rep[Option[String]] = column[Option[String]]("series", O.Length(50,varying=true), O.Default(None))
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Foreign key referencing User (database name linked_account_user_id_fkey) */
    lazy val userFk = foreignKey("linked_account_user_id_fkey", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
              }
  /** Collection-like TableQuery object for table LinkedAccount */
  lazy val LinkedAccount = new TableQuery(tag => new LinkedAccount(tag))

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param hash Database column hash SqlType(varchar), Length(255,true)
   *  @param appliedAt Database column applied_at SqlType(timestamp)
   *  @param applyScript Database column apply_script SqlType(text), Default(None)
   *  @param revertScript Database column revert_script SqlType(text), Default(None)
   *  @param state Database column state SqlType(varchar), Length(255,true), Default(None)
   *  @param lastProblem Database column last_problem SqlType(text), Default(None) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.sql.Timestamp, applyScript: Option[String] = None, revertScript: Option[String] = None, state: Option[String] = None, lastProblem: Option[String] = None) 
  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends profile.api.Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
              def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column hash SqlType(varchar), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255,varying=true))
    /** Database column applied_at SqlType(timestamp) */
    val appliedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("applied_at")
    /** Database column apply_script SqlType(text), Default(None) */
    val applyScript: Rep[Option[String]] = column[Option[String]]("apply_script", O.Default(None))
    /** Database column revert_script SqlType(text), Default(None) */
    val revertScript: Rep[Option[String]] = column[Option[String]]("revert_script", O.Default(None))
    /** Database column state SqlType(varchar), Length(255,true), Default(None) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255,varying=true), O.Default(None))
    /** Database column last_problem SqlType(text), Default(None) */
    val lastProblem: Rep[Option[String]] = column[Option[String]]("last_problem", O.Default(None))
              }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))

  /** Entity class storing rows of table SecurityPermission
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param value Database column value SqlType(varchar), Length(255,true)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class SecurityPermissionRow(id: Long, value: String, modified: Option[java.sql.Timestamp] = None) extends EntityAutoInc[Long, SecurityPermissionRow] with Permission 
  /** GetResult implicit for fetching SecurityPermissionRow objects using plain SQL queries */
  implicit def GetResultSecurityPermissionRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[java.sql.Timestamp]]): GR[SecurityPermissionRow] = GR{
    prs => import prs._
    SecurityPermissionRow.tupled((<<[Long], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table security_permission. Objects of this class serve as prototypes for rows in queries. */
  class SecurityPermission(_tableTag: Tag) extends profile.api.Table[SecurityPermissionRow](_tableTag, "security_permission") with IdentifyableTable[Long] {
              def * = (id, value, modified) <> (SecurityPermissionRow.tupled, SecurityPermissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(value), modified).shaped.<>({r=>import r._; _1.map(_=> SecurityPermissionRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column value SqlType(varchar), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))
              }
  /** Collection-like TableQuery object for table SecurityPermission */
  lazy val SecurityPermission = new TableQuery(tag => new SecurityPermission(tag))

  /** Entity class storing rows of table SecurityRole
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar), Length(255,true) */
  case class SecurityRoleRow(id: Long, name: String) extends EntityAutoInc[Long, SecurityRoleRow] with Role 
  /** GetResult implicit for fetching SecurityRoleRow objects using plain SQL queries */
  implicit def GetResultSecurityRoleRow(implicit e0: GR[Long], e1: GR[String]): GR[SecurityRoleRow] = GR{
    prs => import prs._
    SecurityRoleRow.tupled((<<[Long], <<[String]))
  }
  /** Table description of table security_role. Objects of this class serve as prototypes for rows in queries. */
  class SecurityRole(_tableTag: Tag) extends profile.api.Table[SecurityRoleRow](_tableTag, "security_role") with IdentifyableTable[Long] {
              def * = (id, name) <> (SecurityRoleRow.tupled, SecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> SecurityRoleRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
              }
  /** Collection-like TableQuery object for table SecurityRole */
  lazy val SecurityRole = new TableQuery(tag => new SecurityRole(tag))

  /** Entity class storing rows of table TokenAction
   *  @param userId Database column user_id SqlType(int8)
   *  @param token Database column token SqlType(varchar), Length(50,true)
   *  @param `type` Database column type SqlType(bpchar), Length(2,false)
   *  @param created Database column created SqlType(timestamp)
   *  @param expires Database column expires SqlType(timestamp)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class TokenActionRow(userId: Long, token: String, `type`: String, created: java.sql.Timestamp, expires: java.sql.Timestamp, modified: Option[java.sql.Timestamp] = None) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching TokenActionRow objects using plain SQL queries */
  implicit def GetResultTokenActionRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[java.sql.Timestamp]]): GR[TokenActionRow] = GR{
    prs => import prs._
    TokenActionRow.tupled((<<[Long], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table token_action. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class TokenAction(_tableTag: Tag) extends profile.api.Table[TokenActionRow](_tableTag, "token_action") with IdentifyableTable[Long] {
              override def id = userId

    def * = (userId, token, `type`, created, expires, modified) <> (TokenActionRow.tupled, TokenActionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(token), Rep.Some(`type`), Rep.Some(created), Rep.Some(expires), modified).shaped.<>({r=>import r._; _1.map(_=> TokenActionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column token SqlType(varchar), Length(50,true) */
    val token: Rep[String] = column[String]("token", O.Length(50,varying=true))
    /** Database column type SqlType(bpchar), Length(2,false)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Length(2,varying=false))
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")
    /** Database column expires SqlType(timestamp) */
    val expires: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expires")
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Foreign key referencing User (database name token_action_user_id_fkey) */
    lazy val userFk = foreignKey("token_action_user_id_fkey", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (token) (database name token_action_token_key) */
    val index1 = index("token_action_token_key", token, unique=true)
              }
  /** Collection-like TableQuery object for table TokenAction */
  lazy val TokenAction = new TableQuery(tag => new TokenAction(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(varchar), Length(50,true), Default(None)
   *  @param middleName Database column middle_name SqlType(varchar), Length(50,true), Default(None)
   *  @param lastName Database column last_name SqlType(varchar), Length(50,true), Default(None)
   *  @param dateOfBirth Database column date_of_birth SqlType(date), Default(None)
   *  @param username Database column username SqlType(varchar), Length(100,true)
   *  @param email Database column email SqlType(varchar), Length(100,true)
   *  @param lastLogin Database column last_login SqlType(timestamp), Default(None)
   *  @param active Database column active SqlType(bool), Default(false)
   *  @param emailValidated Database column email_validated SqlType(bool), Default(false)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class UserRow(id: Long, firstName: Option[String] = None, middleName: Option[String] = None, lastName: Option[String] = None, dateOfBirth: Option[java.sql.Date] = None, username: String, email: String, lastLogin: Option[java.sql.Timestamp] = None, active: Boolean = false, emailValidated: Boolean = false, modified: Option[java.sql.Timestamp] = None) extends EntityAutoInc[Long, UserRow] 
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Date]], e3: GR[String], e4: GR[Option[java.sql.Timestamp]], e5: GR[Boolean]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Long], <<?[String], <<?[String], <<?[String], <<?[java.sql.Date], <<[String], <<[String], <<?[java.sql.Timestamp], <<[Boolean], <<[Boolean], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, "user") with IdentifyableTable[Long] {
              def * = (id, firstName, middleName, lastName, dateOfBirth, username, email, lastLogin, active, emailValidated, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), firstName, middleName, lastName, dateOfBirth, Rep.Some(username), Rep.Some(email), lastLogin, Rep.Some(active), Rep.Some(emailValidated), modified).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5, _6.get, _7.get, _8, _9.get, _10.get, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first_name SqlType(varchar), Length(50,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(50,varying=true), O.Default(None))
    /** Database column middle_name SqlType(varchar), Length(50,true), Default(None) */
    val middleName: Rep[Option[String]] = column[Option[String]]("middle_name", O.Length(50,varying=true), O.Default(None))
    /** Database column last_name SqlType(varchar), Length(50,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(50,varying=true), O.Default(None))
    /** Database column date_of_birth SqlType(date), Default(None) */
    val dateOfBirth: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("date_of_birth", O.Default(None))
    /** Database column username SqlType(varchar), Length(100,true) */
    val username: Rep[String] = column[String]("username", O.Length(100,varying=true))
    /** Database column email SqlType(varchar), Length(100,true) */
    val email: Rep[String] = column[String]("email", O.Length(100,varying=true))
    /** Database column last_login SqlType(timestamp), Default(None) */
    val lastLogin: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("last_login", O.Default(None))
    /** Database column active SqlType(bool), Default(false) */
    val active: Rep[Boolean] = column[Boolean]("active", O.Default(false))
    /** Database column email_validated SqlType(bool), Default(false) */
    val emailValidated: Rep[Boolean] = column[Boolean]("email_validated", O.Default(false))
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))
              }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table UserSecurityPermission
   *  @param userId Database column user_id SqlType(int8)
   *  @param securityPermissionId Database column security_permission_id SqlType(int8)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class UserSecurityPermissionRow(userId: Long, securityPermissionId: Long, modified: Option[java.sql.Timestamp] = None) 
  /** GetResult implicit for fetching UserSecurityPermissionRow objects using plain SQL queries */
  implicit def GetResultUserSecurityPermissionRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityPermissionRow] = GR{
    prs => import prs._
    UserSecurityPermissionRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_permission. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityPermission(_tableTag: Tag) extends profile.api.Table[UserSecurityPermissionRow](_tableTag, "user_security_permission") {
              def * = (userId, securityPermissionId, modified) <> (UserSecurityPermissionRow.tupled, UserSecurityPermissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(securityPermissionId), modified).shaped.<>({r=>import r._; _1.map(_=> UserSecurityPermissionRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_permission_id SqlType(int8) */
    val securityPermissionId: Rep[Long] = column[Long]("security_permission_id")
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Primary key of UserSecurityPermission (database name user_security_permission_pkey) */
    val pk = primaryKey("user_security_permission_pkey", (userId, securityPermissionId))

    /** Foreign key referencing SecurityPermission (database name user_security_permission_security_permission_id_fkey) */
    lazy val securityPermissionFk = foreignKey("user_security_permission_security_permission_id_fkey", securityPermissionId, SecurityPermission)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing User (database name user_security_permission_user_id_fkey) */
    lazy val userFk = foreignKey("user_security_permission_user_id_fkey", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
              }
  /** Collection-like TableQuery object for table UserSecurityPermission */
  lazy val UserSecurityPermission = new TableQuery(tag => new UserSecurityPermission(tag))

  /** Entity class storing rows of table UserSecurityRole
   *  @param userId Database column user_id SqlType(int8)
   *  @param securityRoleId Database column security_role_id SqlType(int8)
   *  @param modified Database column modified SqlType(timestamp), Default(None) */
  case class UserSecurityRoleRow(userId: Long, securityRoleId: Long, modified: Option[java.sql.Timestamp] = None) 
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityRoleRow] = GR{
    prs => import prs._
    UserSecurityRoleRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends profile.api.Table[UserSecurityRoleRow](_tableTag, "user_security_role") {
              def * = (userId, securityRoleId, modified) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(securityRoleId), modified).shaped.<>({r=>import r._; _1.map(_=> UserSecurityRoleRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_role_id SqlType(int8) */
    val securityRoleId: Rep[Long] = column[Long]("security_role_id")
    /** Database column modified SqlType(timestamp), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Primary key of UserSecurityRole (database name user_security_role_pkey) */
    val pk = primaryKey("user_security_role_pkey", (userId, securityRoleId))

    /** Foreign key referencing SecurityRole (database name user_security_role_security_role_id_fkey) */
    lazy val securityRoleFk = foreignKey("user_security_role_security_role_id_fkey", securityRoleId, SecurityRole)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing User (database name user_security_role_user_id_fkey) */
    lazy val userFk = foreignKey("user_security_role_user_id_fkey", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
              }
  /** Collection-like TableQuery object for table UserSecurityRole */
  lazy val UserSecurityRole = new TableQuery(tag => new UserSecurityRole(tag))
}
