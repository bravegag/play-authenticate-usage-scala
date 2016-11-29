package generated

import dao._

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(LinkedAccount.schema, PlayEvolutions.schema, SecurityPermission.schema, SecurityRole.schema, TokenAction.schema, User.schema, UserSecurityPermission.schema, UserSecurityRole.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table LinkedAccount
   *  @param userId Database column user_id SqlType(int8), Default(None)
   *  @param providerUsername Database column provider_username SqlType(varchar), Length(255,true), Default(None)
   *  @param providerKey Database column provider_key SqlType(varchar), Length(255,true), Default(None)
   *  @param modified Database column modified SqlType(timestamp) */
  case class LinkedAccountRow(userId: Option[Long] = None, providerUsername: Option[String] = None, providerKey: Option[String] = None, modified: Option[java.sql.Timestamp]) extends Entity[Option[Long]] {
    override def id() = userId
  }
  /** GetResult implicit for fetching LinkedAccountRow objects using plain SQL queries */
  implicit def GetResultLinkedAccountRow(implicit e0: GR[Option[Long]], e1: GR[Option[String]], e2: GR[Option[java.sql.Timestamp]]): GR[LinkedAccountRow] = GR{
    prs => import prs._
    LinkedAccountRow.tupled((<<?[Long], <<?[String], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table linked_account. Objects of this class serve as prototypes for rows in queries. */
  class LinkedAccount(_tableTag: Tag) extends Table[LinkedAccountRow](_tableTag, "linked_account") {
    def * = (userId, providerUsername, providerKey, modified) <> (LinkedAccountRow.tupled, LinkedAccountRow.unapply)

    /** Database column user_id SqlType(int8), Default(None) */
    val userId: Rep[Option[Long]] = column[Option[Long]]("user_id", O.Default(None))
    /** Database column provider_username SqlType(varchar), Length(255,true), Default(None) */
    val providerUsername: Rep[Option[String]] = column[Option[String]]("provider_username", O.Length(255,varying=true), O.Default(None))
    /** Database column provider_key SqlType(varchar), Length(255,true), Default(None) */
    val providerKey: Rep[Option[String]] = column[Option[String]]("provider_key", O.Length(255,varying=true), O.Default(None))
    /** Database column modified SqlType(timestamp) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified")

    /** Foreign key referencing User (database name linked_account_user_id_fkey) */
    lazy val userFk = foreignKey("linked_account_user_id_fkey", userId, User)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
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
   *  @param value Database column value SqlType(varchar), Length(255,true), Default(None)
   *  @param modified Database column modified SqlType(timestamp) */
  case class SecurityPermissionRow(id: Long, value: Option[String] = None, modified: Option[java.sql.Timestamp]) extends StrongEntity[Long] {
    override def copyWithNewId(id : Long) : Entity[Long] = this.copy(id = id)
  }
  /** GetResult implicit for fetching SecurityPermissionRow objects using plain SQL queries */
  implicit def GetResultSecurityPermissionRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Timestamp]]): GR[SecurityPermissionRow] = GR{
    prs => import prs._
    SecurityPermissionRow.tupled((<<[Long], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table security_permission. Objects of this class serve as prototypes for rows in queries. */
  class SecurityPermission(_tableTag: Tag) extends Table[SecurityPermissionRow](_tableTag, "security_permission") {
    def * = (id, value, modified) <> (SecurityPermissionRow.tupled, SecurityPermissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), value, modified).shaped.<>({r=>import r._; _1.map(_=> SecurityPermissionRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column value SqlType(varchar), Length(255,true), Default(None) */
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Length(255,varying=true), O.Default(None))
    /** Database column modified SqlType(timestamp) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified")
  }
  /** Collection-like TableQuery object for table SecurityPermission */
  lazy val SecurityPermission = new TableQuery(tag => new SecurityPermission(tag))

  /** Entity class storing rows of table SecurityRole
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param roleName Database column role_name SqlType(varchar), Length(255,true), Default(None) */
  case class SecurityRoleRow(id: Long, roleName: Option[String] = None) extends StrongEntity[Long] {
    override def copyWithNewId(id : Long) : Entity[Long] = this.copy(id = id)
  }
  /** GetResult implicit for fetching SecurityRoleRow objects using plain SQL queries */
  implicit def GetResultSecurityRoleRow(implicit e0: GR[Long], e1: GR[Option[String]]): GR[SecurityRoleRow] = GR{
    prs => import prs._
    SecurityRoleRow.tupled((<<[Long], <<?[String]))
  }
  /** Table description of table security_role. Objects of this class serve as prototypes for rows in queries. */
  class SecurityRole(_tableTag: Tag) extends Table[SecurityRoleRow](_tableTag, "security_role") {
    def * = (id, roleName) <> (SecurityRoleRow.tupled, SecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), roleName).shaped.<>({r=>import r._; _1.map(_=> SecurityRoleRow.tupled((_1.get, _2)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column role_name SqlType(varchar), Length(255,true), Default(None) */
    val roleName: Rep[Option[String]] = column[Option[String]]("role_name", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table SecurityRole */
  lazy val SecurityRole = new TableQuery(tag => new SecurityRole(tag))

  /** Entity class storing rows of table TokenAction
   *  @param userId Database column user_id SqlType(int8), Default(None)
   *  @param token Database column token SqlType(varchar), Length(255,true), Default(None)
   *  @param `type` Database column type SqlType(varchar), Length(2,true), Default(None)
   *  @param created Database column created SqlType(timestamp), Default(None)
   *  @param expires Database column expires SqlType(timestamp), Default(None) */
  case class TokenActionRow(userId: Option[Long] = None, token: Option[String] = None, `type`: Option[String] = None, created: Option[java.sql.Timestamp] = None, expires: Option[java.sql.Timestamp] = None) extends Entity[Option[Long]] {
    override def id() = userId
  }
  /** GetResult implicit for fetching TokenActionRow objects using plain SQL queries */
  implicit def GetResultTokenActionRow(implicit e0: GR[Option[Long]], e1: GR[Option[String]], e2: GR[Option[java.sql.Timestamp]]): GR[TokenActionRow] = GR{
    prs => import prs._
    TokenActionRow.tupled((<<?[Long], <<?[String], <<?[String], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table token_action. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class TokenAction(_tableTag: Tag) extends Table[TokenActionRow](_tableTag, "token_action") {
    def * = (userId, token, `type`, created, expires) <> (TokenActionRow.tupled, TokenActionRow.unapply)

    /** Database column user_id SqlType(int8), Default(None) */
    val userId: Rep[Option[Long]] = column[Option[Long]]("user_id", O.Default(None))
    /** Database column token SqlType(varchar), Length(255,true), Default(None) */
    val token: Rep[Option[String]] = column[Option[String]]("token", O.Length(255,varying=true), O.Default(None))
    /** Database column type SqlType(varchar), Length(2,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Option[String]] = column[Option[String]]("type", O.Length(2,varying=true), O.Default(None))
    /** Database column created SqlType(timestamp), Default(None) */
    val created: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("created", O.Default(None))
    /** Database column expires SqlType(timestamp), Default(None) */
    val expires: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("expires", O.Default(None))

    /** Foreign key referencing User (database name token_action_user_id_fkey) */
    lazy val userFk = foreignKey("token_action_user_id_fkey", userId, User)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (token) (database name token_action_token_key) */
    val index1 = index("token_action_token_key", token, unique=true)
  }
  /** Collection-like TableQuery object for table TokenAction */
  lazy val TokenAction = new TableQuery(tag => new TokenAction(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param lastName Database column last_name SqlType(varchar), Length(50,true), Default(None)
   *  @param middleName Database column middle_name SqlType(varchar), Length(50,true), Default(None)
   *  @param firstName Database column first_name SqlType(varchar), Length(50,true), Default(None)
   *  @param dob Database column dob SqlType(date), Default(None)
   *  @param telephone Database column telephone SqlType(varchar), Length(100,true), Default(None)
   *  @param locationId Database column location_id SqlType(int8), Default(None)
   *  @param username Database column username SqlType(varchar), Length(100,true), Default(None)
   *  @param email Database column email SqlType(varchar), Length(100,true), Default(None)
   *  @param password Database column password SqlType(varchar), Length(100,true), Default(None)
   *  @param salt Database column salt SqlType(varchar), Length(100,true), Default(None)
   *  @param lastLogin Database column last_login SqlType(timestamp), Default(None)
   *  @param active Database column active SqlType(bool), Default(None)
   *  @param emailValidated Database column email_validated SqlType(bool), Default(None)
   *  @param modified Database column modified SqlType(timestamp) */
  case class UserRow(id: Long, lastName: Option[String] = None, middleName: Option[String] = None, firstName: Option[String] = None, dob: Option[java.sql.Date] = None, telephone: Option[String] = None, locationId: Option[Long] = None, username: Option[String] = None, email: Option[String] = None, password: Option[String] = None, salt: Option[String] = None, lastLogin: Option[java.sql.Timestamp] = None, active: Option[Boolean] = None, emailValidated: Option[Boolean] = None, modified: Option[java.sql.Timestamp]) extends StrongEntity[Long] {
    override def copyWithNewId(id : Long) : Entity[Long] = this.copy(id = id)
  }
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Date]], e3: GR[Option[Long]], e4: GR[Option[java.sql.Timestamp]], e5: GR[Option[Boolean]]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Long], <<?[String], <<?[String], <<?[String], <<?[java.sql.Date], <<?[String], <<?[Long], <<?[String], <<?[String], <<?[String], <<?[String], <<?[java.sql.Timestamp], <<?[Boolean], <<?[Boolean], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "user") with IdentifyableTable[Long] {
    def * = (id, lastName, middleName, firstName, dob, telephone, locationId, username, email, password, salt, lastLogin, active, emailValidated, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), lastName, middleName, firstName, dob, telephone, locationId, username, email, password, salt, lastLogin, active, emailValidated, modified).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column last_name SqlType(varchar), Length(50,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(50,varying=true), O.Default(None))
    /** Database column middle_name SqlType(varchar), Length(50,true), Default(None) */
    val middleName: Rep[Option[String]] = column[Option[String]]("middle_name", O.Length(50,varying=true), O.Default(None))
    /** Database column first_name SqlType(varchar), Length(50,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(50,varying=true), O.Default(None))
    /** Database column dob SqlType(date), Default(None) */
    val dob: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("dob", O.Default(None))
    /** Database column telephone SqlType(varchar), Length(100,true), Default(None) */
    val telephone: Rep[Option[String]] = column[Option[String]]("telephone", O.Length(100,varying=true), O.Default(None))
    /** Database column location_id SqlType(int8), Default(None) */
    val locationId: Rep[Option[Long]] = column[Option[Long]]("location_id", O.Default(None))
    /** Database column username SqlType(varchar), Length(100,true), Default(None) */
    val username: Rep[Option[String]] = column[Option[String]]("username", O.Length(100,varying=true), O.Default(None))
    /** Database column email SqlType(varchar), Length(100,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(100,varying=true), O.Default(None))
    /** Database column password SqlType(varchar), Length(100,true), Default(None) */
    val password: Rep[Option[String]] = column[Option[String]]("password", O.Length(100,varying=true), O.Default(None))
    /** Database column salt SqlType(varchar), Length(100,true), Default(None) */
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Length(100,varying=true), O.Default(None))
    /** Database column last_login SqlType(timestamp), Default(None) */
    val lastLogin: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("last_login", O.Default(None))
    /** Database column active SqlType(bool), Default(None) */
    val active: Rep[Option[Boolean]] = column[Option[Boolean]]("active", O.Default(None))
    /** Database column email_validated SqlType(bool), Default(None) */
    val emailValidated: Rep[Option[Boolean]] = column[Option[Boolean]]("email_validated", O.Default(None))
    /** Database column modified SqlType(timestamp) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified")
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table UserSecurityPermission
   *  @param userId Database column user_id SqlType(int8)
   *  @param securityPermissionId Database column security_permission_id SqlType(int8)
   *  @param modified Database column modified SqlType(timestamp) */
  case class UserSecurityPermissionRow(userId: Long, securityPermissionId: Long, modified: Option[java.sql.Timestamp]) extends Entity[(Long, Long)] {
    override def id() = (userId, securityPermissionId)
  }
  /** GetResult implicit for fetching UserSecurityPermissionRow objects using plain SQL queries */
  implicit def GetResultUserSecurityPermissionRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityPermissionRow] = GR{
    prs => import prs._
    UserSecurityPermissionRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_permission. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityPermission(_tableTag: Tag) extends Table[UserSecurityPermissionRow](_tableTag, "user_security_permission") {
    def * = (userId, securityPermissionId, modified) <> (UserSecurityPermissionRow.tupled, UserSecurityPermissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(securityPermissionId), modified).shaped.<>({r=>import r._; _1.map(_=> UserSecurityPermissionRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_permission_id SqlType(int8) */
    val securityPermissionId: Rep[Long] = column[Long]("security_permission_id")
    /** Database column modified SqlType(timestamp) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified")

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
   *  @param modified Database column modified SqlType(timestamp) */
  case class UserSecurityRoleRow(userId: Long, securityRoleId: Long, modified: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityRoleRow] = GR{
    prs => import prs._
    UserSecurityRoleRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends Table[UserSecurityRoleRow](_tableTag, "user_security_role") {
    def * = (userId, securityRoleId, modified) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(securityRoleId), modified).shaped.<>({r=>import r._; _1.map(_=> UserSecurityRoleRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_role_id SqlType(int8) */
    val securityRoleId: Rep[Long] = column[Long]("security_role_id")
    /** Database column modified SqlType(timestamp) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified")

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
