package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

/**
  * Created by kjolh on 3/10/2016.
  */
case class GameData(source: SourceData) extends Parsed[GameData.type] {

}

object GameData extends Schema[GameData] {

}