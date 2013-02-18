package models.vertices

import java.util.Date
import javax.persistence.Id
import javax.persistence.Version
import org.apache.commons.lang.builder.ReflectionToStringBuilder
import com.wingnest.play2.origami.GraphVertexModel
import com.wingnest.play2.origami.GraphModel
import com.wingnest.play2.origami.annotations.DisupdateFlag
import com.wingnest.play2.origami.annotations.Index
import com.wingnest.play2.origami.annotations.SmartDate
import scala.beans.BeanProperty
import scala.annotation.meta.field

abstract class GraphVertexModelBase extends GraphVertexModel {

  @(Id @field)
  var id: String = null

  @(Version @field)
  var version: Long = 0

  @(DisupdateFlag @field)
  var disupdateFlag: Boolean = false

  @(SmartDate @field)(dateType = GraphModel.SMART_DATE_TYPE.CREATED_DATE)
  @(Index @field)
  var createdDate: Date = null

  @(SmartDate @field)(dateType = GraphModel.SMART_DATE_TYPE.UPDATED_DATE)
  @(Index @field)
  var updatedDate: Date = null

  @(Index @field)
  var deletedDate: Date = null

  def isDeleted(): Boolean = {
    deletedDate != null;
  }

  override def toString(): String = {
    new ReflectionToStringBuilder(this).toString();
  }

}
