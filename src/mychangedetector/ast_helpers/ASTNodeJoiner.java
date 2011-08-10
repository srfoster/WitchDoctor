package mychangedetector.ast_helpers;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class ASTNodeJoiner {
	private ASTNode node;

	public void setNode(ASTNode node) {
		this.node = node;
	}

	public void addChild(ASTNode child){
		JoinPoint join_point = joinPointFor(child);
		
		join_point.add();
	}
	
	public void addChildAt(int position, ASTNode child){
		JoinPoint join_point = joinPointFor(child);
		join_point.add(position);
	}
	
	public void replaceChildWith(ASTNode child, ASTNode replacement)
	{
		JoinPoint join_point = joinPointFor(child);
		join_point.replace(replacement);
	}
	
	public abstract JoinPoint joinPointFor(ASTNode child);
	
	protected ASTNode getNode(){
		return this.node;
	}

	public static ASTNodeJoiner joinerFor(ASTNode md) {
		String name = md.getClass().getName();
		String[] ast_type_arr = name.split("\\.");
		String ast_type = ast_type_arr[ast_type_arr.length-1];

		Object object = null;
		try {
			Class classDefinition = Class.forName("mychangedetector.ast_helpers." + ast_type + "Joiner");
			object = classDefinition.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ASTNodeJoiner joiner = (ASTNodeJoiner) object;
		joiner.setNode(md);

		return joiner;
	}
	
	public static ASTNode unparent(ASTNode node)
	{
		try{
			node.delete();
			return node;
		} catch (IllegalArgumentException iae) {  
			return ASTNodeJoiner.unparent(node.getParent());  //A bit dangerous. Just calls unparent() until it works, moving up the tree each time.
		}
	}
	
	public static ASTNode parentThisCouldBeRemovedFrom(ASTNode node)
	{
		return ASTNodeJoiner.parentThisCouldBeRemovedFromHelper(node, node.getParent());
	}
	
	private static ASTNode parentThisCouldBeRemovedFromHelper(ASTNode node, ASTNode parent)
	{
		ASTNodeJoiner joiner = ASTNodeJoiner.joinerFor(parent);
		
		if(joiner.joinPointFor(node) instanceof ListJoinPoint) //Might want to make JoinPoints have a parameterized type and check that instead...
		{
			return parent;
		}
		
		return parentThisCouldBeRemovedFromHelper(node,parent.getParent());
	}
	
	public static ASTNode parentThatShouldBeRemoved(ASTNode node)
	{
		return ASTNodeJoiner.parentThatShouldBeRemovedHelper(node, node);
	}
	
	public static ASTNode parentThatShouldBeRemovedHelper(ASTNode node, ASTNode parent)
	{
		ASTNodeJoiner joiner = ASTNodeJoiner.joinerFor(parent.getParent());
		JoinPoint jp = joiner.joinPointFor(node);
		if(joiner.joinPointFor(node) instanceof ListJoinPoint) //Might want to make JoinPoints have a parameterized type and check that instead...
		{
			return parent;
		}
		
		return parentThatShouldBeRemovedHelper(node,parent.getParent());
	}

}
