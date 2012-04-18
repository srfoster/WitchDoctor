package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.custom.StyledText;

public class OffsetQuery extends JumpQuery {
	int offset;

	public OffsetQuery(TextAdapter text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<ASTNode> resolveToASTNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int resolveToOffset(){
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}
