package mychangedetector.change_management;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.Diff;
import mychangedetector.differencer.simple_differencer.SimpleMessageDiff;
import mychangedetector.differencer.simple_differencer.SimpleMessageDiffEntity;

import org.eclipse.jdt.core.dom.ASTNode;

public class ChangeWrapper {
	Diff change;
	ASTNode node;         //May be the original or the revised node, depending on the change type.
	ASTNode updated_node; //Only used for updates.
	
	ASTNode full_tree_before = null;
	ASTNode full_tree_after  = null;
	
	
	public ChangeWrapper(Diff change, ASTNode full_tree_before, ASTNode full_tree_after){
		this.change = change;
		this.full_tree_before = full_tree_before;
		this.full_tree_after  = full_tree_after;
	}
	
	public void setNode(ASTNode node)
	{
		if(node == null)
			return; 
		
		this.node = node;
		
		if(change instanceof SimpleMessageDiff)
		{
			node.setProperty("compiler_message",((SimpleMessageDiffEntity)change.getChangedEntity()).getMessage());
		}
	}
	
	public void setUpdatedNode(ASTNode node)
	{
		if(node == null)
			return; 
		
		this.updated_node = node;
		
		if(change instanceof SimpleMessageDiff)
		{
			node.setProperty("compiler_message",((SimpleMessageDiffEntity)change.getNewEntity()).getMessage());
		}
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
		return node;
	}
	
	public ASTNode getUpdatedNode(){
		if(!(change.isUpdate()))
		{
			return null;
		}
		
		return updated_node;
	}
	
	
	public Diff unwrap()
	{
		return change;
	}


}
