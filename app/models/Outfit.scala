package models


import play.api.libs.json.Json

object Outfit  {
  implicit val outfit = Json.format[Outfit]
}
case class Outfit(id: Option[Long], outfitName: Option[String])
