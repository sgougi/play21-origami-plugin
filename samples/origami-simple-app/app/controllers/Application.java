package controllers;

import static play.data.Form.form;

import java.util.List;

import models.incidences.HasComment;
import models.vertices.Comment;
import models.vertices.Log;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.logs;
import views.txt.sql.getLogs;

import com.wingnest.play2.origami.GraphDB;
import com.wingnest.play2.origami.annotations.Transactional;
import com.wingnest.play2.origami.annotations.WithGraphDB;

@WithGraphDB
public class Application extends Controller {

	public static Result index() {
		return ok(index.render());
	}

	@Transactional
	public static Result postLog() {
		final Form<Log> logForm = form(Log.class).bindFromRequest();
		if ( logForm.hasErrors() ) {
			return badRequest(logForm.errorsAsJson());
		}
		logForm.get().save();
		return ok();
	}

	public static Result getLogs() {
		final String query = getLogs.render().body();
		final List<Log> logModels = GraphDB.synchQueryModel(query);
		Logger.debug(logModels.toString());
		return ok(logs.render(logModels));
	}

	@Transactional
	public static Result postComment(String logId) {
		final Form<Comment> commentForm = form(Comment.class).bindFromRequest();
		if ( commentForm.hasErrors() ) {
			return badRequest(commentForm.errorsAsJson());
		}
		if ( logId == null ) {
			return notFound();
		}

		boolean disupdateFlag = Boolean.parseBoolean(form().bindFromRequest().get("disupdateFlagLog"));

		Log log = GraphDB.findById(logId);
		if ( log == null )
			return notFound();
		log.disupdateFlag = disupdateFlag;

		HasComment hasComment = new HasComment();
		hasComment.setVertexes(log, commentForm.get());
		hasComment.save();

		return ok();
	}
}
