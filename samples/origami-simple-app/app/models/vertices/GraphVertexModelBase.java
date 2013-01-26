package models.vertices;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.wingnest.play2.origami.GraphVertexModel;
import com.wingnest.play2.origami.annotations.DisupdateFlag;
import com.wingnest.play2.origami.annotations.Index;
import com.wingnest.play2.origami.annotations.SmartDate;
import com.wingnest.play2.origami.annotations.SmartDateType;

public abstract class GraphVertexModelBase extends GraphVertexModel {

	@Id
	public String id;

	@Version
	public Long version;

	@DisupdateFlag
	// is kind of @Transient implicitly
	public boolean disupdateFlag = false;

	@SmartDate(type = SmartDateType.CreatedDate)
	@Index()
	public Date createdDate;

	@SmartDate(type = SmartDateType.UpdatedDate)
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
