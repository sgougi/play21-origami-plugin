package models.vertices;

import com.wingnest.play2.origami.annotations.Index;
import scala.annotation.meta.field

class Comment extends GraphVertexModelBase {

  @(Index @field)
  var name: String = null

  @field
  var content: String = null

}
