package mychangedetector.matching.constraints;


import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;

public class NotEqualConstraint extends Constraint {

	String first, second;
	
	public NotEqualConstraint(String v1, String v2)
	{
		first = v1;
		second = v2;
	}

	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		
		if(!key.equals(first) && !key.equals(second))
			return false;
		
	
		ASTNode potential = null;
		FreeVar already_matched_var = null;
		
		if(key.equals(first))
		{
			potential = matched_bindings.get(first);
			already_matched_var = requirement.getProperty(second);
		} else if (key.equals(second)) {
			potential = matched_bindings.get(second);
			already_matched_var = requirement.getProperty(first);
		}
		
		if(already_matched_var.isNotBound()) // i.e. it is not already matched.
			return false;
		
		ASTNode already_matched = already_matched_var.binding();
		
		return already_matched.subtreeMatch(new ASTMatcher(), potential);
	}
}
