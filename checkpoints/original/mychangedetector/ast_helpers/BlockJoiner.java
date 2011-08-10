package mychangedetector.ast_helpers;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

public class BlockJoiner extends ASTNodeJoiner {

	@Override
	public JoinPoint joinPointFor(final ASTNode child) {
		
		if(child instanceof Statement)
		{
			List<ASTNode> list = ((Block)getNode()).statements();
			ASTNode to_add = (Statement)child;
			return new ListJoinPoint(list,to_add);
		}
		
		if(child instanceof Expression)
		{
			List<ASTNode> list = ((Block)getNode()).statements();
			ListJoinPoint join_point = new ListJoinPoint(list,null);
			join_point.setBeforeAction(
					new JoinAction(){
						public void doAction(JoinPoint j){
							ASTNode unparented = ASTNodeJoiner.unparent(child);
							
							j.setNode(unparented);
						}
					}
			);
			return join_point;
		}
	
		throw new RuntimeException("Could not figure out how to add a " + child.getClass() + " as a child of a " + getNode().getClass());
	}



}
