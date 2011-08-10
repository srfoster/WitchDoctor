package mychangedetector.matching.constraints;

import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ChildOfMethodConstraint extends Constraint {
	String child_regex;
	String parent_binding_name;
	
	/***
	 * @param child The Requirement whose bound AST nodes must all be children of the parent's bound ast node(s).
	 * @param parent The Requirement whose (one?) bound AST node should the the parent of the child Requirement's nodes.
	 ***/
	
	public ChildOfMethodConstraint(String child_regex, String parent_binding_name)
	{
		this.child_regex = child_regex;
		this.parent_binding_name = parent_binding_name;
	}
	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		
		if(!key.matches(child_regex))
			return false;
		
		ASTNode current = matched_bindings.get(key);
		FreeVar parent_node_var = requirement.getSpecification().getProperty(parent_binding_name);
	
		if(parent_node_var == null)
			return false;  // If there's no parent, let's say there's a violation.  (Might want to change this...)
		
		ASTNode parent_node = parent_node_var.binding();
		
		if(parent_node == null || !(parent_node instanceof MethodDeclaration))
			return true;
		
		String parent_meth_name =  ((MethodDeclaration)parent_node).getName().toString();
		
		ASTNode current_parent = current.getParent();
		
		while(current_parent != null)  // Travel up the tree in search of the parent
		{
			if(current_parent instanceof MethodDeclaration)
			{
				String meth_name = ((MethodDeclaration)current_parent).getName().toString();
								
				if(meth_name.equals(parent_meth_name))
					return false;
			}
			
			current_parent = current_parent.getParent();
		}
		
		return true;
	}
}
