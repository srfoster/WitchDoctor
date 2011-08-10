package mychangedetector.specification.requirements;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

public class UpdateStatement extends Requirement {

	FreeVar old_statement_var, new_statement_var;
	
	public UpdateStatement(String old_statement, String new_statement)
	{
		old_statement_var = new FreeVar(old_statement);
		new_statement_var = new FreeVar(new_statement);

		bindings.add(old_statement_var);
		bindings.add(new_statement_var);
	}
	
	@Override
	protected void afterMatch(ChangeWrapper change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ChangeMatcher buildChangeMatcher() {
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType("UPDATE");
		change_event.setBeforeNodeMatcher(buildSimplestatementMatcher(old_statement_var.name()));
		change_event.setAfterNodeMatcher(buildSimplestatementMatcher(new_statement_var.name()));

		return change_event;
		
	}

	private ASTNodeDescriptor buildSimplestatementMatcher(String binding_statement) {

		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.Statement");
		descriptor.setBindingName(binding_statement);
		
		return descriptor;
	}

}
