package models.vertices;

import java.util.List;

import play.Logger;
import play.data.validation.Constraints.Required;

import com.wingnest.play2.origami.GraphDB;
import com.wingnest.play2.origami.annotations.Index;

import views.txt.sql.*;

public class Log extends GraphVertexModelBase {

	@Required
	public String title;

	@Required
	@Index
	public String name;

	@Required
	public String logBody;

	public List<Comment> getComments() {
		final String query = getComments.render(this.getORID().toString()).body();
		Logger.debug("getComments : query = " + query);
		return GraphDB.synchQueryModel(query);
	}
}
