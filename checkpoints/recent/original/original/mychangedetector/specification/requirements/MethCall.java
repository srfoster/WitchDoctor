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
	String operation;
	
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

	private ASTNodeDescriptor buildBeforeNodeMatcher()
	{
		/*
		ASTNodeBuilder builder = new ASTNodeBuilder();
		builder.methodCallExpression(call.name());
		return builder.getNode();
		*/
		
		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.MethodInvocation");
		descriptor.setBindingName(call.name());
		
		return descriptor;
	}

	@Override
	public ChangeMatcher buildChangeMatcher()
	{
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType(operation);
		change_event.setBeforeNodeMatcher(buildBeforeNodeMatcher());

		return change_event;
		
		
	}

}
