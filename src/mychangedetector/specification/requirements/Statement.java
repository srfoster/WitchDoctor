package mychangedetector.specification.requirements;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.matching.constraints.ConsecutiveConstraint;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class Statement extends Requirement {

	FreeVar statement_var;
		
	public Statement(String operation, String free_var_name) {
		this.operation = operation;
		
		statement_var = new FreeVar(free_var_name);
		bindings.add(statement_var);
	}
	
	@Override
	public ChangeMatcher buildChangeMatcher()
	{
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType(operation);
		change_event.setBeforeNodeMatcher(buildBeforeNodeMatcher());

		return change_event;
	}

	private ASTNodeDescriptor buildBeforeNodeMatcher()
	{
	
		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.Statement");
		descriptor.setBindingName(statement_var.name());
		
		return descriptor;
	}

	@Override
	protected void afterMatch(ChangeWrapper change) {
		// TODO Auto-generated method stub
		
	}

	

}
