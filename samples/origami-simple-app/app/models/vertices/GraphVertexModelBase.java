package models.vertices;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.wingnest.play2.origami.GraphModel;
import com.wingnest.play2.origami.GraphVertexModel;
import com.wingnest.play2.origami.annotations.DisupdateFlag;
import com.wingnest.play2.origami.annotations.Index;
import com.wingnest.play2.origami.annotations.SmartDate;

public abstract class GraphVertexModelBase extends GraphVertexModel {

	@Id
	public String id;

	@Version
	public Long version;

	@DisupdateFlag
	// is kind of @Transient implicitly
	public boolean disupdateFlag = false;

	@SmartDate(dateType = GraphModel.SMART_DATE_TYPE.CREATED_DATE)	
	@Index()
	public Date createdDate;

	@SmartDate(dateType = GraphModel.SMART_DATE_TYPE.UPDATED_DATE)		
	@Index()
	public Date updatedDate;

	@Index
	public Date deletedDate = null;

	public boolean isDeleted() {
		return deletedDate != null;
	}

	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
