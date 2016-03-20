package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import se.gigurra.leavu3.externaldata.GameData
import se.gigurra.leavu3.util.{Resource2String, RestClient, SimpleTimer}
import se.gigurra.serviceutils.json.JSON
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/11/2016.
  */
