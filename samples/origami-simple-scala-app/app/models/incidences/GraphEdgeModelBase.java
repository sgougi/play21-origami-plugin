package models.incidences;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Version;

import com.wingnest.play2.origami.GraphEdgeModel;
import com.wingnest.play2.origami.GraphVertexModel;
import com.wingnest.play2.origami.annotations.DisupdateFlag;
import com.wingnest.play2.origami.annotations.Index;
import com.wingnest.play2.origami.annotations.SmartDate;
import com.wingnest.play2.origami.annotations.SmartDateType;

public abstract class GraphEdgeModelBase<OUT extends GraphVertexModel, IN extends GraphVertexModel> extends GraphEdgeModel<OUT, IN> {

	@Id
	public String id;

	@Version
	public Long version;

	@DisupdateFlag
	public boolean disupdateFlag = false;

	@SmartDate(type = SmartDateType.CreatedDate)
	@Index()
	public Date createdDate;

	@SmartDate(type = SmartDateType.UpdatedDate)
	@Index()
	public Date updatedDate;

}
