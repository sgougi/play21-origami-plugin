package models.vertices

import java.util.List
import play.Logger
import com.wingnest.play2.origami.GraphDB
import com.wingnest.play2.origami.annotations.Index
import scala.annotation.meta.field

class Log extends GraphVertexModelBase {

  @field
  var title: String = null

  @(Index @field)
  var name: String = null

  @field
  var logBody: String = null

  def getComments(): List[Comment] = {
    var query = views.txt.sql.getComments.render(getORID().toString()).body
    GraphDB.synchQueryModel(query)
  }

}
