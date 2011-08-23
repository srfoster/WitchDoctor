package mychangedetector.change_management;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.Diff;

import org.eclipse.jdt.core.dom.ASTNode;

public class ChangeWrapper {
	Diff change;
	List<ASTNode> relevant_nodes;
	
	ASTNode full_tree_before = null;
	ASTNode full_tree_after  = null;
	
	
	public ChangeWrapper(Diff change, ASTNode full_tree_before, ASTNode full_tree_after){
		this.change = change;
		this.full_tree_before = full_tree_before;
		this.full_tree_after  = full_tree_after;
		relevant_nodes = new ArrayList<ASTNode>();
	}
	
	public void addASTNode(ASTNode node)
	{
		relevant_nodes.add(node);
	}
	
	public String getChangeType()
	{
		if(change.isUpdate())
			return "UPDATE";
			
		if(change.isInsert())
			return "INSERT";
			
		if(change.isDelete())
			return "DELETE";
		
		if(change.isMove())
			return "MOVE";
		
		return null;
	}
	
	public ASTNode getNode()
	{
		return relevant_nodes.get(0);
	}
	
	public ASTNode getUpdatedNode(){
		if(!(change.isUpdate()))
		{
			return null;
		}
		
		return relevant_nodes.get(1);
	}
	
	
	public Diff unwrap()
	{
		return change;
	}


}
