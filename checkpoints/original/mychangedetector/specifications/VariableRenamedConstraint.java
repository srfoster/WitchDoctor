package mychangedetector.specifications;

import java.util.List;
import java.util.Map;

import mychangedetector.ast_helpers.ZippingASTVisitor;
import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

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
		try{
			AST temp_ast = AST.newAST(AST.JLS3);
			
			ASTNode first_parent = first_node.getParent();
			ASTNode second_parent = second_node.getParent();
			
			StructuralPropertyDescriptor first_location = first_node.getLocationInParent();
			
			ASTNode second_parent_copy = ASTNode.copySubtree(temp_ast,second_parent);
			
			ASTNode first_copy = ASTNode.copySubtree(temp_ast,first_node);
			
			second_parent_copy.setStructuralProperty(first_location,first_copy);
			
			boolean ret = second_parent_copy.subtreeMatch(new ASTMatcher(), first_parent);
			
			return ret;
		} catch(RuntimeException re){ 
			// Doing the above gymnastics with the replacement will fail 
			//  if the first and second node parents have different tree structures because 
			//  the structural property won't exist in both.  So we'll just catch the
			//  runtime exception.  (Why is it a RuntimeException and not some reasonable subclass??  IDK)
			return false;
		}
	}
}
