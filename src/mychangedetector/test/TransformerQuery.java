package mychangedetector.test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.custom.StyledText;

public class TransformerQuery extends JumpQuery {
	JumpQuery query;
	
	public TransformerQuery(JumpQuery query) {
		super(query.getText());
		this.query = query;
	}

	@Override
	public List<ASTNode> resolveToASTNodes() {
		return getQuery().resolveToASTNodes();
	}
	
	public JumpQuery getQuery()
	{
		return query;
	}

}
