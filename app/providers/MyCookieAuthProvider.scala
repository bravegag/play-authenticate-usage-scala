package providers

import com.feth.play.module.pa.PlayAuthenticate
import javax.inject.{Inject, Singleton}
import play.inject.ApplicationLifecycle
import com.feth.play.module.pa.providers.cookie.{CookieAuthProvider, CookieAuthUser}
import com.feth.play.module.pa.user.AuthUser
import dao.DaoContext
import services.UserService

@Singleton
class MyCookieAuthProvider @Inject()(implicit
                                     auth: PlayAuthenticate,
                                     lifecycle: ApplicationLifecycle,
                                     val userService: UserService,
                                     daoContext: DaoContext) extends CookieAuthProvider(auth, lifecycle) {

  override def save(cookieAuthUser: CookieAuthUser, loginUser: AuthUser): Unit = {
    auth.getUserService.link(loginUser, cookieAuthUser)
    val userRow = userService.findByAuthUser(loginUser).get
    daoContext.cookieTokenSeriesDao.create(userRow, cookieAuthUser.getSeries, cookieAuthUser.getToken)
  }

  override def deleteSeries(authUser: AuthUser, series: String): Unit = ???

  override def check(cookieAuthUser: CookieAuthUser): CookieAuthProvider.CheckResult = ???

  override def renew(cookieAuthUser: CookieAuthUser, newToken: String): Unit = ???
}