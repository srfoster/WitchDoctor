package mychangedetector.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class BlockQuery extends TransformerQuery {
	
	public BlockQuery(JumpQuery query) {
		super(query);
	}

	@Override
	public List<ASTNode> resolveToASTNodes() {
		List<ASTNode> nodes = getQuery().resolveToASTNodes();
		
		List<ASTNode> list = new ArrayList<ASTNode>();
		
		for(ASTNode node : nodes)
		{
			if(node instanceof MethodDeclaration)
				list.add(((MethodDeclaration)node).getBody());
		
		}
		return list;
	}


}
