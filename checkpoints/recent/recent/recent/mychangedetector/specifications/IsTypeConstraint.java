package mychangedetector.specifications;

import java.util.Map;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.ASTNode;

public class IsTypeConstraint extends Constraint {

	String is_or_not;
	String key;
	ASTNodeDescriptor descriptor;
	Class class_to_check;
	
	public IsTypeConstraint(String is_or_not, String key, ASTNodeDescriptor descriptor) {
		this.is_or_not  = is_or_not;
		this.key        = key;
		this.descriptor = descriptor;
	}

	@Override
	public boolean isViolatedBy(String new_key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!new_key.equals(key))
			return false;
		
		ASTNode current = matched_bindings.get(new_key);
				
		if(descriptor.describes(current) && is_or_not.equals("NOT"))
			return true;
		
		if(!descriptor.describes(current) && is_or_not.equals("IS"))
			return true;
		
		
		return false;
	}
}
	
