package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class BeginningQuery extends TransformerQuery {

	public BeginningQuery(JumpQuery query) {
		super(query);
	}

	@Override
	public int resolveToOffset()
	{
		return super.resolveToOffset() + 1;
	}

}
