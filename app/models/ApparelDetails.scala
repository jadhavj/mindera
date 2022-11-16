package models

import play.api.libs.json.Json

object ApparelDetails  {
  implicit val apparelDetails = Json.format[ApparelDetails]
}

case class ApparelDetails(apparelId: Option[Long], apparelName: String, category: Option[String], outfits: Option[String])