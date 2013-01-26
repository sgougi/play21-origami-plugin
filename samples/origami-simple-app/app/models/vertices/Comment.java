package models.vertices;

import com.wingnest.play2.origami.annotations.Index;

public class Comment extends GraphVertexModelBase {

	@Index
	public String name;

	public String content;

}
