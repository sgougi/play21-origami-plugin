package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.vertices._
import models.incidences._

import com.wingnest.play2.origami._
import scala.collection.JavaConverters._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  val logForm = Form(
    tuple(
      "title" -> nonEmptyText,
      "name" -> nonEmptyText,
      "logBody" -> nonEmptyText))

  def postLog = Action { implicit request =>
    logForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (title, name, logBody) =>
          GraphDB.begin()
          try {
            val log = new Log()
            log.title = title
            log.name = name
            log.logBody = logBody
            log.save()
            GraphDB.commit()
          } catch {
            case e: Exception => {
              GraphDB.rollback()
            }
          } finally {
            GraphDB.close()
          }
          Ok
      })
  }

  def getLogs = Action {
    try {
      GraphDB.open()
      val query = views.txt.sql.getLogs.render().body
      val logModels: java.util.List[Log] = GraphDB.synchQueryModel(query)
      Ok(views.html.logs.render(logModels))
    } finally {
      GraphDB.close()
    }
  }

  val commentForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "content" -> nonEmptyText,
      "disupdateFlagLog" -> boolean))

  def postComment(logId: String) = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (name, content, disupdateFlagLog) =>
          GraphDB.begin()
          try {
            val log: Log = GraphDB.findById(logId);
            if (log == null)
              NotFound
            log.disupdateFlag = disupdateFlagLog;

            val comment = new Comment()
            comment.name = name
            comment.content = content

            val hasComment = new HasComment()
            hasComment.setVertexes(log, comment)
            hasComment.save()

            GraphDB.commit()
          } catch {
            case e: Exception => {
              GraphDB.rollback()
            }
          } finally {
            GraphDB.close()
          }
          Ok
      })
  }

}