package se.gigurra.leavu3.datamodel

import se.gigurra.leavu3.interfaces.{Dlink, GameIn}

/**
  * Created by kjolh on 3/30/2016.
  */
object self {
  def dlinkCallsign: String = Dlink.config.callsign
  def planeId: Int = GameIn.renderThreadSnapshot.metaData.planeId
  def modelTime: Double = GameIn.renderThreadSnapshot.metaData.modelTime
  def coalition: Int = GameIn.renderThreadSnapshot.selfData.coalitionId
  def pitch: Float = GameIn.renderThreadSnapshot.selfData.pitch
  def roll: Float = GameIn.renderThreadSnapshot.selfData.roll
  def heading: Float = GameIn.renderThreadSnapshot.selfData.heading
  def position: Vec3 = GameIn.renderThreadSnapshot.selfData.position
  def velocity: Vec3 = GameIn.renderThreadSnapshot.flightModel.velocity
}

