package mychangedetector.specification.requirements;

import mychangedetector.ast_helpers.ASTNodeBuilder;
import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;

public class MethCall extends Requirement {
	FreeVar call;
	
	public MethCall(String operation, String call_name)
	{
		call = new FreeVar(call_name);
		bindings.add(call);
		this.operation = operation;
	}
	
	@Override
	protected void afterMatch(ChangeWrapper change) {
		// TODO Auto-generated method stub

	}



	@Override
	public ChangeMatcher buildChangeMatcher()
	{
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType(operation);
		
		ASTNodeDescriptor call_descriptor = new ASTNodeDescriptor();
		call_descriptor.setClassName("org.eclipse.jdt.core.dom.MethodInvocation");
		call_descriptor.setBindingName(call.name());
		
		change_event.setAfterNodeMatcher(call_descriptor);
		
		if(operation == "UPDATE")
		{
			ASTNodeDescriptor statement_descriptor = new ASTNodeDescriptor();
			statement_descriptor.setClassName("org.eclipse.jdt.core.dom.Statement");
			statement_descriptor.setBindingName("replaced_statement");
			change_event.setBeforeNodeMatcher(statement_descriptor);
		}


		return change_event;
		
		
	}

}
