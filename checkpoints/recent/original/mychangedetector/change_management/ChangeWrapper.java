package mychangedetector.change_management;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.evolizer.changedistiller.model.entities.Delete;
import org.evolizer.changedistiller.model.entities.Insert;
import org.evolizer.changedistiller.model.entities.Move;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;
import org.evolizer.changedistiller.model.entities.Update;

public class ChangeWrapper {
	SourceCodeChange change;
	List<ASTNode> relevant_nodes;
	
	ASTNode full_tree_before = null;
	ASTNode full_tree_after  = null;
	
	
	public ChangeWrapper(SourceCodeChange change, ASTNode full_tree_before, ASTNode full_tree_after){
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
		if(change instanceof Update)
			return "UPDATE";
			
		if(change instanceof Insert)
			return "INSERT";
			
		if(change instanceof Delete)
			return "DELETE";
		
		if(change instanceof Move)
			return "MOVE";
		
		return null;
	}
	
	public ASTNode getNode()
	{
		return relevant_nodes.get(0);
	}
	
	public ASTNode getUpdatedNode(){
		if(!(change instanceof Update))
		{
			return null;
		}
		
		return relevant_nodes.get(1);
	}
	
	
	


}
