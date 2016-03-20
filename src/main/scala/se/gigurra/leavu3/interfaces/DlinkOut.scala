package se.gigurra.leavu3.interfaces

import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, Mark, Member}
import se.gigurra.leavu3.util.{RestClient, SimpleTimer}
import se.gigurra.leavu3.datamodel.DlinkConfiguration
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}
