package mychangedetector.matching.constraints;

import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodMotionConstraint extends Constraint {
	String origin_method_var_name;
	String target_method_var_name;
	
	String added_block_prefix;
	String removed_block_prefix;
	
	public MethodMotionConstraint(String origin_method_var_name, String removed_block_prefix, String target_method_var_name, String added_block_prefix)
	{
		this.origin_method_var_name = origin_method_var_name;
		this.target_method_var_name = target_method_var_name;
		this.removed_block_prefix = removed_block_prefix;
		this.added_block_prefix = added_block_prefix;
	}
	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			

		ASTNode current_node = matched_bindings.get(key);
			
		FreeVar source_parent_var = requirement.getSpecification().getProperty(origin_method_var_name);
		FreeVar target_parent_var = requirement.getSpecification().getProperty(target_method_var_name);

		if(source_parent_var == null || target_parent_var == null)
			return false;
		
		ASTNode source_parent = source_parent_var.binding();
		ASTNode target_parent = target_parent_var.binding();

		if(source_parent != null && target_parent != null)
			return false;   //We've already matched both, so the issue of keeping them non-equal is moot by this point.
		
		
		
		ASTNode parent_to_compare = null;
		if(key.startsWith(removed_block_prefix))
		{
			parent_to_compare = target_parent;
		} else if(key.startsWith(added_block_prefix))
		{
			parent_to_compare = source_parent;
		} else {
			return false;
		}
		
		
		ASTNode current_parent = current_node.getParent();

		//It would be nice to have an API for this.  Maybe part of ASTNodeJoiner.

		while(current_parent != null)
		{
			if(current_parent instanceof MethodDeclaration)
				break;
			
			current_parent = current_parent.getParent();
		}
		
		
		if(parent_to_compare != null)
		{
			String parent_to_compare_name = ((MethodDeclaration)parent_to_compare).getName().toString();
			String parent_name = ((MethodDeclaration)current_parent).getName().toString();

			if(parent_to_compare_name.equals(parent_name))
			{
				return true;
			}
		}
	
		return false;
	}
}
