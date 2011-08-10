package mychangedetector.matching;

import mychangedetector.ast_helpers.ASTNodeDescriptor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class MatchingASTVisitor extends ASTVisitor {
	MyASTMatcher matcher;
	ASTNodeDescriptor to_match;
	
	public MatchingASTVisitor(MyASTMatcher matcher,ASTNodeDescriptor to_match)
	{
		this.matcher = matcher;
		this.to_match = to_match;
	}
	
	public void postVisit(ASTNode node){
		node.subtreeMatch(matcher,to_match);
	}
}
