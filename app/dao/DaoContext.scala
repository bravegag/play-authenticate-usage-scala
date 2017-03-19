package dao

import javax.inject._

/**
  * Simplifies access to all available Dao implementations so that they don't have to
  * be injected one by one and therefore, reduces the otherwise injection cluttering.
  * @param linkedAccountDao
  * @param securityRoleDao
  * @param tokenActionDao
  * @param userDao
  */
@Singleton
class DaoContext @Inject()(val linkedAccountDao: LinkedAccountDao,
                           val securityRoleDao: SecurityRoleDao,
                           val tokenActionDao: TokenActionDao,
                           val userDao: UserDao)
