package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import util._
import util.Helpers._

import $package$.lib.{ErrorHandler, Gravatar, SmtpMailer}
import $package$.locs.Sitemap
import $package$.model.{MongoConfig, User}

import com.eltimn.auth.mongo._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    logger.info("Run Mode: "+Props.mode.toString)

    // init mongodb
    MongoConfig.init()

    // init auth-mongo
    AuthRules.authUserMeta.default.set(User)
    AuthRules.afterloginTokenUrl.default.set(Sitemap.password.path)
    AuthRules.siteName.default.set("$name$")
    AuthRules.systemEmail.default.set("info@$domain$")
    AuthRules.systemUsername.default.set("$name$ Staff")

    // checks for ExtSession cookie
    LiftRules.earlyInStateful.append(User.testForExtSession)

    // Gravatar
    Gravatar.defaultImage.default.set("wavatar")

    // config an email sender
    SmtpMailer.init

    // where to search snippet
    LiftRules.addToPackages("$package$")

    // set the default htmlProperties
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    // Build SiteMap
    LiftRules.setSiteMap(Sitemap.siteMap)

    // Error handler
    ErrorHandler.init

    // 404 handler
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    // Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("/img/ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("/img/ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
  }
}
