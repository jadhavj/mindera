package util

import java.io.File
import java.nio.charset.CodingErrorAction
import dal.ApparelRepository
import scala.io.Codec

class WardrobeUtil {


  def upload(filename: String, repo: ApparelRepository) : Unit = {
    val f = new File(filename)
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    val bufferedSource = scala.io.Source.fromFile(filename)(codec)
    var i = 1;
    for (line <- bufferedSource.getLines) {
      if (i != 1) {
        val cols : Array[String] = line.split(",").map(_.trim)
        System.out.println(line)
        val aname = cols(0)
        val cat = cols(1)
        repo.addApparel(aname, Option(cat))
      }
      i+=1
    }

  }
}