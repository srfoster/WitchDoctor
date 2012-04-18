package mychangedetector.specification.requirements;

import java.util.Map;

import mychangedetector.ast_helpers.NodeComparisonStrategy;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class SameParentConstraint<T> extends Constraint {
	
	String first_key,second_key;
	Class klass;

	public SameParentConstraint(String first_key, String second_key, Class klass)
	{
		this.first_key = first_key;
		this.second_key = second_key;
		this.klass = klass;
	}


	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!key.equals(first_key) && !key.equals(second_key))
			return false;
		
		String first_key_temp = first_key;
		String second_key_temp = second_key;
		
		ASTNode first_node, second_node;
		
		//See if we've already bound it.
		FreeVar first_free_var = requirement.getSpecification().getProperty(first_key_temp);
		if(first_free_var == null)
			return false;
		
		first_node = first_free_var.binding();
			
		//See if we just bound it.
		if(first_node == null)
			first_node  = matched_bindings.get(first_key_temp);
			
		//See if we've already bound it.
		FreeVar second_free_var = requirement.getSpecification().getProperty(second_key_temp);
		if(second_free_var == null)
			return false;
		
		second_node = second_free_var.binding();
		
		//See if we just bound it.
		if(second_node == null)
			second_node = matched_bindings.get(second_key_temp);
		
		
		if(first_node == null || second_node == null)
			return false;
		
		
		return !sameParent(first_node, second_node);
		
	}
	
	private boolean sameParent(ASTNode first, ASTNode second)
	{
		ASTNode first_parent = first.getParent();
		int first_depth = 0;
		//It would be nice to have an API for this.  Maybe part of ASTNodeJoiner.
		
		while(first_parent != null)
		{
			first_depth++;
			if(klass.isInstance(first_parent))
				break;
			
			first_parent = first_parent.getParent();
		}
		
		if(first_parent == null)
			return false;
		
		ASTNode second_parent = second.getParent();
		int second_depth = 0;
		
		//It would be nice to have an API for this.  Maybe part of ASTNodeJoiner.
		
		while(second_parent != null)
		{
			second_depth++;
			if(klass.isInstance(second_parent))
				break;
			
			second_parent = second_parent.getParent();
		}
		
		if(second_parent == null)
			return false;
		
		NodeComparisonStrategy comp = new NodeComparisonStrategy();
		boolean ret = comp.comp(first_parent,second_parent);
		
		
		
		return ret;
	}
	
}
