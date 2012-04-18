package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.custom.StyledText;

public abstract class JumpQuery {
	TextAdapter text;
	public JumpQuery(TextAdapter text)
	{
		this.text = text;
	}

	public TextAdapter getAdapter()
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
