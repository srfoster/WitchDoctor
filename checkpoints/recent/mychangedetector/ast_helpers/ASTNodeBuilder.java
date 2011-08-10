package mychangedetector.ast_helpers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ASTNodeBuilder {
	AST ast;
	ASTNode node;
	ASTNodeJoiner joiner;  // Mainly for adding children to ASTNodes.  There's not a single method for doing this.  So we'll encapsulate the various scenarios inside of joiners specific to AST node types.
	
	public ASTNodeBuilder()
	{
		ast = AST.newAST(AST.JLS3);	
	}
	
	public ASTNodeBuilder(AST ast)
	{
		this.ast = ast;
	}

	public ASTNode getNode()
	{
		return node;
	}
	
	public ASTNodeBuilder hasChild(ASTNodeBuilder child)
	{
		((List)node.getProperty("children")).add(child.getNode());
		
		joiner.addChild(child.getNode());
		
		return this;
		
	}
	
	private void afterNodeCreate() {
		node.setProperty("children",new ArrayList<ASTNode>());
		
		joiner = ASTNodeJoiner.joinerFor(node);
	}
	
	
	
	
	
	/**
	 *  Builder methods
	 */

	public ASTNodeBuilder methodDeclaration(String string) {
		if(node == null)
		{
			node = ast.newMethodDeclaration();
			node.setProperty("binding",string);
			afterNodeCreate();
			
			return this;
		} else {
			ASTNodeBuilder new_builder = new ASTNodeBuilder(ast);
			return new_builder.methodDeclaration(string);	
		}
	}
	


	public ASTNodeBuilder simpleName(String string) {
		if(node == null)
		{
			node = ast.newSimpleName("WILD_CARD");
			node.setProperty("binding",string);
			afterNodeCreate();
			
			return this;
		} else {
			ASTNodeBuilder new_builder = new ASTNodeBuilder(ast);
			return new_builder.simpleName(string);	
		}
	}

	public ASTNodeBuilder block(String string) {
		if(node == null)
		{
			node = ast.newBlock();
			node.setProperty("binding",string);
			afterNodeCreate();
			
			return this;
		} else {
			ASTNodeBuilder new_builder = new ASTNodeBuilder(ast);
			return new_builder.block(string);	
		}	
	}

	public ASTNodeBuilder statement(String string) {
		if(node == null)
		{
			node = ast.newExpressionStatement(ast.newSimpleName("WILD_CARD"));    // Ultimately we'll want other kinds of statements too.  But this will do for now.
			node.setProperty("binding",string);
			afterNodeCreate();
			
			return this;
		} else {
			ASTNodeBuilder new_builder = new ASTNodeBuilder(ast);
			return new_builder.statement(string);	
		}	
	}

	public ASTNodeBuilder methodCallExpression(String string) {
		if(node == null)
		{
			node = ast.newMethodInvocation();  
			((MethodInvocation)node).setName(ast.newSimpleName(string));
			node.setProperty("binding",string);
			afterNodeCreate();
			
			return this;
		} else {
			ASTNodeBuilder new_builder = new ASTNodeBuilder(ast);
			return new_builder.statement(string);	
		}		
	}


}
