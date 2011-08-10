package mychangedetector.ast_helpers;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

public class MethodDeclarationJoiner extends ASTNodeJoiner {
	
	@Override
	public JoinPoint joinPointFor(final ASTNode child) {
		if(child instanceof SimpleName)
		{
			return new SimpleJoinPoint(
					new JoinAction(){
						public void doAction(JoinPoint j){
							((MethodDeclaration)getNode()).setName((SimpleName)child);
						}
					}
			);
		}
		
		if(child instanceof Block)
		{
			return new SimpleJoinPoint(
					new JoinAction(){
						public void doAction(JoinPoint j){
							((MethodDeclaration)getNode()).setBody((Block)child);
						}
					}
			);
		}
		
		//Try to add to the body block of the method.
		if(child instanceof Statement || child instanceof Expression)
		{
			Block body = ((MethodDeclaration)getNode()).getBody();
			return ASTNodeJoiner.joinerFor(body).joinPointFor(child);
		}
				
		throw new RuntimeException("Could not figure out how to add a " + child.getClass() + " as a child of a " + getNode().getClass());

	}

}
