package controllers

import dal.ApparelRepository
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import util.WardrobeUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WardrobeController @Inject()(
                                    cc: ControllerComponents,
                                    apparelRepository: ApparelRepository,
                                    wardrobeUtil: WardrobeUtil
                                  ) extends AbstractController(cc) {

  def searchApparels(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val limit: Long = request.getQueryString("limit").map(_.toLong).getOrElse(50)
      val offset: Long = request.getQueryString("offset").map(_.toLong).getOrElse(0)
      val apparelname: String = request.getQueryString("apparelname").map(_.toString).getOrElse("")

      apparelRepository
        .searchClothes(apparelname,   limit, offset)
        .map(apparel => Ok(Json.toJson(apparel)))
  }

  def wardrobe(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      apparelRepository.wardrobeOutfits().map(ad => Ok(Json.toJson(ad)))
  }

  def tagOutfit: Action[AnyContent] = Action.async {
      implicit request: Request[AnyContent] =>
        val apparelId: Long = request.getQueryString("apparelId").map(_.toLong).getOrElse(50)
        val outfitId: Long = request.getQueryString("outfitId").map(_.toLong).getOrElse(0)

        apparelRepository.tagOutfit(outfitId, apparelId)
      Future { Ok("tagged") }
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("wardrobeFile").map { picture =>

      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType

      var file = new File(s"$filename")

      picture.ref.moveTo(file)

      wardrobeUtil.upload(file.getAbsolutePath, apparelRepository)

      Ok("File uploaded")
    }.getOrElse {
      Ok("Error uploading file")
    }
  }
}
