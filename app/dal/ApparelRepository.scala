package dal

import javax.inject.Singleton
import models.{Apparel, Outfit, OutfitApparels}
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@Singleton
 class ApparelRepository {

  val db = Database.forConfig("h2mem")

  val apparel = TableQuery[ApparelTable]
  val outfit = TableQuery[OutfitTable]
  val outfitApparels = TableQuery[OutfitApparelsTable]

  class ApparelTable(tag: Tag) extends Table[Apparel](tag, "APPAREL") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def apparelname = column[String]("apparelname")

    def category = column[Option[String]]("category")

    def * =
      (
        id,
        apparelname,
        category,
        ).<>((Apparel.apply _).tupled, Apparel.unapply)
  }

  class OutfitTable(tag: Tag) extends Table[Outfit](tag, "OUTFIT") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def outfitName = column[Option[String]]("outfitName")
    def * =
      (
        id,
        outfitName
        ).<>((Outfit.apply _).tupled, Outfit.unapply)
  }

  class OutfitApparelsTable(tag: Tag) extends Table[OutfitApparels](tag, "OUTFIT_APPARELS") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def outfitId = column[Option[Long]]("outfitId")
    def apparelId = column[Option[Long]]("apparelId")

    def outfitFK =
      foreignKey("outfitId", outfitId, TableQuery[OutfitTable])(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)
    def apparelFK =
      foreignKey("apparelId", apparelId, TableQuery[ApparelTable])(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

    def * =
      (
        id,
        outfitId,
        apparelId,
        ).<>((OutfitApparels.apply _).tupled, OutfitApparels.unapply)
  }

  def searchClothes(apparelname: String, limit: Long, offset: Long): Future[Seq[Apparel]] = db.run {
    apparel.filter(_.apparelname === apparelname).drop(offset)
      .take(limit)
      .result
  }

  private val allTablesQry =
    for {
      app <- apparel
      outfitApps <- outfitApparels if app.id === outfitApps.apparelId
      otf <- outfit if outfitApps.outfitId === otf.id
    } yield (app, outfitApps, otf)


  def wardrobeOutfits(): Future[Seq[(Option[Long], String, Option[String], Option[String])]] = db.run {
    allTablesQry.map {
      case (apparel, outfitApparels, outfit) =>
        (outfitApparels.apparelId, apparel.apparelname, apparel.category, outfit.outfitName)
    }.drop(0).take(50).result
  }

  def tagOutfit(outfitId: Long, apparelId: Long): Unit = {
    Await.result(db.run(DBIO.seq(
      outfitApparels += OutfitApparels(Option(1L), Option(outfitId), Option(apparelId))
    )), Duration.Inf)
  }

  def addApparel(apparelname: String, category: Option[String]): Unit = {
    Await.result(db.run(DBIO.seq(
      apparel += Apparel(Option(1L), apparelname, category)
    )), Duration.Inf)
  }

  def init() : Unit ={
    db.createSession().prepareStatement("set REFERENTIAL_INTEGRITY false").execute
    db.createSession().prepareStatement("drop table if exists outfit_apparels").execute
    db.createSession().prepareStatement("drop table if exists apparel").execute
    db.createSession().prepareStatement("drop table if exists outfit").execute
    db.createSession().prepareStatement("set REFERENTIAL_INTEGRITY true").execute
    db.createSession().prepareStatement("create table apparel(\"id\" bigint auto_increment primary key, \"apparelname\" varchar, \"category\" varchar)").execute()
    db.createSession().prepareStatement("create table outfit(\"id\" bigint auto_increment primary key, \"outfitName\" varchar)").execute()
    db.createSession().prepareStatement("create table outfit_apparels(\"id\" bigint auto_increment primary key, \"outfitId\" bigint, \"apparelId\" bigint, foreign key (\"outfitId\") references outfit(\"id\"), foreign key (\"apparelId\") references apparel(\"id\"))") .execute()
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('Jeans', 'Tops')").execute
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('T-Shirt', 'Tops')").execute
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('Crop Top', 'Tops')").execute
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('Vest', 'Tops')").execute
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('Skirt', 'Bottoms')").execute
    db.createSession().prepareStatement("insert into apparel(\"apparelname\", \"category\") values ('Sweater', 'Tops')").execute
    db.createSession().prepareStatement("insert into outfit(\"outfitName\") values ('Winter Wear')").execute
    db.createSession().prepareStatement("insert into outfit(\"outfitName\") values ('Casuals')").execute
    db.createSession().prepareStatement("insert into outfit_apparels(\"outfitId\", \"apparelId\") values (1,4)").execute
    db.createSession().prepareStatement("insert into outfit_apparels(\"outfitId\", \"apparelId\") values (1,6)").execute
  }

  init();
}
