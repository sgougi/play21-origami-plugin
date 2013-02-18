package models.incidences;

import java.util.Date
import javax.persistence.Id
import javax.persistence.Version
import com.wingnest.play2.origami.GraphModel
import com.wingnest.play2.origami.GraphEdgeModel
import com.wingnest.play2.origami.GraphVertexModel
import com.wingnest.play2.origami.annotations.DisupdateFlag
import com.wingnest.play2.origami.annotations.Index
import com.wingnest.play2.origami.annotations.SmartDate
import com.orientechnologies.orient.core.metadata.schema.OClass
import scala.beans.BeanProperty
import scala.annotation.meta.field

abstract class GraphEdgeModelBase[OUT <: GraphVertexModel, IN <: GraphVertexModel] extends GraphEdgeModel[OUT, IN] {

  @(Id @field)
  var id: String = null

  @(Version @field)
  var version: Long = 0

  @(DisupdateFlag @field)
  var disupdateFlag: Boolean = false

  @(Index @field)
  @(SmartDate @field)(dateType = GraphModel.SMART_DATE_TYPE.CREATED_DATE)
  var createdDate: Date = null

  @(Index @field)
  @(SmartDate @field)(dateType = GraphModel.SMART_DATE_TYPE.UPDATED_DATE)
  var updatedDate: Date = null

}
