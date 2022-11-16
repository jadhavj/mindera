package models

import play.api.libs.json.Json

object OutfitApparels  {
  implicit val outfitApparels = Json.format[Apparel]
}

case class OutfitApparels(id: Option[Long], outfitId: Option[Long], apparelId: Option[Long])
