package models

import play.api.libs.json.Json

object Apparel  {
  implicit val apparel = Json.format[Apparel]
}

case class Apparel(id: Option[Long], apparelname: String, category: Option[String])
