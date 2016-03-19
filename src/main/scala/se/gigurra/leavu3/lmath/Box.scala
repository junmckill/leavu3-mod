package se.gigurra.leavu3.lmath

import se.gigurra.leavu3.externaldata.Vec2

/**
  * Created by kjolh on 3/19/2016.
  */
case class Box(width: Double, height: Double, center: Vec2 = Vec2()) {
  def left = center.x - width / 2.0
  def right = center.x + width / 2.0
  def top = center.y + height / 2.0
  def bottom = center.y - height / 2.0

  def contains(p: Vec2): Boolean = {
    left <= p.x && p.x <= right && bottom <= p.y && p.y <= top
  }
}
