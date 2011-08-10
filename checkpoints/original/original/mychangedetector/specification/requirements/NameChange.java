package mychangedetector.specification.requirements;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

public class NameChange extends Requirement {
	FreeVar old_name_var, new_name_var;
	
	public NameChange(String old_name, String new_name)
	{
		old_name_var = new FreeVar(old_name);
		new_name_var = new FreeVar(new_name);

		bindings.add(old_name_var);
		bindings.add(new_name_var);
	}
	
	@Override
	protected void afterMatch(ChangeWrapper change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ChangeMatcher buildChangeMatcher() {
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType("UPDATE");
		change_event.setBeforeNodeMatcher(buildSimpleNameMatcher(old_name_var.name()));
		change_event.setAfterNodeMatcher(buildSimpleNameMatcher(new_name_var.name()));

		return change_event;
		
	}

	private ASTNodeDescriptor buildSimpleNameMatcher(String binding_name) {

		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.SimpleName");
		descriptor.setBindingName(binding_name);
		
		return descriptor;
	}

}
