package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.custom.StyledText;

public abstract class JumpQuery {
	StyledText text;
	public JumpQuery(StyledText text)
	{
		this.text = text;
	}

	public StyledText getText()
	{
		return text;
	}

	public abstract List<ASTNode> resolveToASTNodes();
	public int resolveToOffset()
	{
		return resolveToASTNodes().get(0).getStartPosition();
	}
	
	public int resolveToLength()
	{
		return resolveToASTNodes().get(0).getLength();
	}	

}
