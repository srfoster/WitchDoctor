package mychangedetector.matching.constraints;

import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

public class VariableRenamedConstraint extends Constraint {
	
	String first_key,second_key;
	
	public VariableRenamedConstraint(String first_key, String second_key) {
		this.first_key = first_key;
		this.second_key = second_key;
	}

	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			

		if(!key.equals(first_key) && !key.equals(second_key))
			return false;
		
		ASTNode first_node, second_node;
		
		if(key.equals(first_key))
		{
			first_node  = matched_bindings.get(first_key);
			FreeVar second_free_var = requirement.getSpecification().getProperty(second_key);
			if(second_free_var == null || second_free_var.isNotBound())
				return false;
			
			second_node = second_free_var.binding();
		} else {
			second_node  = matched_bindings.get(second_key);
			FreeVar first_free_var = requirement.getSpecification().getProperty(first_key);
			if(first_free_var == null || first_free_var.isNotBound())
				return false;
			
			first_node = first_free_var.binding();
		}
		
		return !isRenaming(first_node,second_node);
	}
	
	private boolean isRenaming(ASTNode first_node, ASTNode second_node)
	{
		boolean both_simple_names = (first_node instanceof SimpleName) && (second_node instanceof SimpleName);
		
		boolean same_start_location = first_node.getStartPosition() == second_node.getStartPosition();
		
		
		return both_simple_names && same_start_location;
	}
}
