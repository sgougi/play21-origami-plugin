package models.vertices;

import com.wingnest.play2.origami.annotations.Index
import scala.annotation.meta.field
import scala.beans.BeanProperty

case class Comment (
		@(Index @field) 
		var name: String,
		
		@field
		var content: String
    ) 	extends GraphVertexModelBase {
  
  def this() = this( null, null )
  
}

/*
class Comment extends GraphVertexModelBase {

  @(Index @field)
  var name: String = null

  @field
  var content: String = null

}
*/