package mychangedetector.matching.constraints;

import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * 
 * This class SHOULD be used to determine if the method call refers to a method which 
 * 1) is not defined in a 3rd party API, and 
 * 2) does not currently exist.
 * 
 * What it DOES is check to see that the method call's parent is a statement node -- which
 * checks #1 by default and ignores #2.  It also will prohibit at least one thing that it
 * shouldn't: extracting into a method in a different class.  For example, you can't
 * insert SomeClass.new_method() and hope that the autocomplete will create new_method()
 * in SomeClass.
 * 
 * Eventually, we should fix this stuff.
 * 
 * @author stephenfoster
 *
 */
public class IsAvailableMethod extends Constraint {

	String key;
	public IsAvailableMethod(String string) {
		key = string;
	}
	

	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!key.equals(this.key))
			return false;
		
		ASTNode node;
		
		//See if we've already bound it.
		FreeVar var = requirement.getSpecification().getProperty(key);
		if(var == null)
			return false;
		
		node = var.binding();
			
		//See if we just bound it.
		if(node == null)
			node  = matched_bindings.get(key);
	
		
		if(node == null)
			return false;
		
		
		return !isAvailableMethod(node);
		
	}


	private boolean isAvailableMethod(ASTNode node) {
		
		ASTNode parent = node.getParent();
		
		boolean ret = !parent.toString().contains(".");
		
		return ret;
	}
}
