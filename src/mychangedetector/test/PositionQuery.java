package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;

public class PositionQuery extends JumpQuery {
	private boolean return_end = false;
	
	public PositionQuery(TextAdapter text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	Position position;
	
	public void setPosition(Position p) {
		position = p;
	}

	@Override
	public List<ASTNode> resolveToASTNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int resolveToOffset()
	{
		if(return_end)
		{
			int end = position.getOffset() + position.getLength();
			return end;
		}
		else
			return position.getOffset();
	}
	
	public void returnEndAsOffset(){
		return_end = true;
	}



}
