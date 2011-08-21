package mychangedetector.specification.requirements;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class UpdateMethod extends Requirement {

	FreeVar old_method_var, new_method_var;
	
	public UpdateMethod(String old_method, String new_method)
	{
		old_method_var = new FreeVar(old_method);
		new_method_var = new FreeVar(new_method);

		FreeVar hook_statement_var = new FreeVar("hook_statement");
		
		bindings.add(old_method_var);
		bindings.add(new_method_var);
		bindings.add(hook_statement_var);
	}
	
	@Override
	protected void afterMatch(ChangeWrapper change) {
		FreeVar hook_statement_var = this.getProperty("hook_statement");
		
		if(hook_statement_var.isBound())
		{
			ASTNode hook_statement = hook_statement_var.binding();
			
			ASTNode method_declaration = hook_statement.getParent();
			
			while(!(method_declaration instanceof MethodDeclaration))
			{
				method_declaration = method_declaration.getParent();
			}
			
			//This may not be the method (if the statement is nested under a deeper block), but that's okay for now.
			this.setProperty("old_method",method_declaration,change);
		}
	}

	@Override
	public ChangeMatcher buildChangeMatcher() {
		ChangeMatcher change_event = new ChangeMatcher();
		
		change_event.setChangeType("DELETE");
		change_event.setBeforeNodeMatcher(buildSimpleMethodMatcher(old_method_var.name()));
		change_event.setAfterNodeMatcher(buildSimpleMethodMatcher(new_method_var.name()));

		return change_event;
		
	}

	private ASTNodeDescriptor buildSimpleMethodMatcher(String binding_method) {

		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.Statement");
		descriptor.setBindingName("hook_statement");
		
		return descriptor;
	}

}
