package providers

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.TimeZone

import com.feth.play.module.pa.PlayAuthenticate
import javax.inject.{Inject, Singleton}
import play.inject.ApplicationLifecycle
import com.feth.play.module.pa.providers.cookie._
import com.feth.play.module.pa.user.AuthUser
import dao.DaoContext
import services.UserService
import java.time._

@Singleton
class MyCookieAuthProvider @Inject()(implicit
                                     auth: PlayAuthenticate,
                                     lifecycle: ApplicationLifecycle,
                                     val userService: UserService,
                                     daoContext: DaoContext) extends CookieAuthProvider(auth, lifecycle) {
  import utils.AwaitUtils._
  import CookieAuthProvider._

  //-------------------------------------------------------------------
  // public
  //-------------------------------------------------------------------
  override def save(cookieAuthUser: CookieAuthUser, loginUser: AuthUser): Unit = {
    auth.getUserService.link(loginUser, cookieAuthUser)
    val userRow = userService.findByAuthUser(loginUser).get
    daoContext.cookieTokenSeriesDao.create(userRow, cookieAuthUser.getSeries, cookieAuthUser.getToken)
  }

  //-------------------------------------------------------------------
  override def deleteSeries(authUser: AuthUser, series: String): Unit = {
    val linkedAccountRow = daoContext.linkedAccountDao.findByProvider(getKey(), series).get
    getAuth.getUserService.unlink(authUser)
    daoContext.cookieTokenSeriesDao.deleteBySeries(linkedAccountRow.userId, series)
  }

  //-------------------------------------------------------------------
  override def check(cookieAuthUser: CookieAuthUser): CookieAuthProvider.CheckResult = {
    if (cookieAuthUser.getSeries == null) {
      return CheckResult.MISSING_SERIES
    }

    val linkedAccount = daoContext.linkedAccountDao.findByProvider(PROVIDER_KEY, cookieAuthUser.getSeries).
      getOrElse(return CheckResult.ERROR)

    val cookieSeries = daoContext.cookieTokenSeriesDao.findBySeries(linkedAccount.userId, linkedAccount.providerUserId).
      getOrElse(return CheckResult.MISSING_SERIES)

    if (!(cookieSeries.token == cookieAuthUser.getToken)) return CheckResult.INVALID_TOKEN

    val timeCreated = LocalDateTime.ofInstant(Instant.ofEpochMilli(cookieSeries.created.get.getTime),
      TimeZone.getDefault().toZoneId())
    val timeUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(cookieSeries.modified.get.getTime),
      TimeZone.getDefault().toZoneId())

    val daysSinceCreated = ChronoUnit.DAYS.between(timeCreated, LocalDateTime.now)
    val daysSinceUpdated = ChronoUnit.DAYS.between(timeUpdated, LocalDateTime.now)

    val timeoutDaysSinceCreated = auth.getConfiguration.getLong("cookie.timeoutDays.sinceFirstLogin")

    val timeoutDaysSinceUpdated = auth.getConfiguration.getLong("cookie.timeoutDays.sinceLastLogin")

    if (daysSinceCreated > timeoutDaysSinceCreated) {
      return CheckResult.EXPIRED
    }

    if (daysSinceUpdated > timeoutDaysSinceUpdated) {
      return CheckResult.EXPIRED
    }

    return CheckResult.SUCCESS
  }

  //-------------------------------------------------------------------
  override def renew(cookieAuthUser: CookieAuthUser, newToken: String): Unit = {
    val linkedAccountRow = daoContext.linkedAccountDao.findByProvider(PROVIDER_KEY, cookieAuthUser.getSeries).get

    val cookieSeriesRow = daoContext.cookieTokenSeriesDao.findBySeries(linkedAccountRow.userId, linkedAccountRow.providerUserId).get

    daoContext.cookieTokenSeriesDao.updateToken(cookieSeriesRow, newToken)
  }
}