package se.gigurra.leavu3.datamodel

import se.gigurra.leavu3.interfaces.{Dlink, GameIn}

/**
  * Created by kjolh on 3/30/2016.
  */
object self {
  def dlinkCallsign: String = Dlink.config.callsign
  def planeId: Int = GameIn.snapshot.metaData.planeId
  def modelTime: Double = GameIn.snapshot.metaData.modelTime
  def coalition: Int = GameIn.snapshot.selfData.coalitionId
  def pitch: Float = GameIn.snapshot.selfData.pitch
  def roll: Float = GameIn.snapshot.selfData.roll
  def heading: Float = GameIn.snapshot.selfData.heading
  def position: Vec3 = GameIn.snapshot.selfData.position
  def velocity: Vec3 = GameIn.snapshot.flightModel.velocity
  def acceleration: Vec3 = GameIn.snapshot.flightModel.acceleration
}

