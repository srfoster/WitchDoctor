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

public class CodeBlock extends Requirement {
	
	FreeVar current_statement;
	FreeVar meth_decl_var;
	int counter = 1;
	String prefix;
	String operation;
		
	public CodeBlock(String operation, String prefix) {
		this.prefix = prefix;
		this.operation = operation;
		
		meth_decl_var = new FreeVar(prefix + "method_declaration");
		bindings.add(meth_decl_var);
		
		current_statement = new FreeVar(prefix + counter);
		bindings.add(current_statement);
	}
	
	public String getPrefix(){
		return prefix;
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
		/*
		ASTNodeBuilder builder = new ASTNodeBuilder();
			
		builder.statement(currentStatmentName());
		*/
		
		ASTNodeDescriptor descriptor = new ASTNodeDescriptor();
		descriptor.setClassName("org.eclipse.jdt.core.dom.Statement");
		descriptor.setBindingName(currentStatmentName());
		
		return descriptor;
	}

	private String currentStatmentName() {
		return prefix + counter;
	}

	@Override
	protected void afterMatch(ChangeWrapper change) {
		if(getProperty(meth_decl_var.name()).binding() == null)
		{
			ASTNode method_decl = getCurrentMethod();
			setProperty(meth_decl_var.name(),method_decl,change);
		}
		
		counter++; // If we've matched the current statement, we're ready to try matching another.
		current_statement = new FreeVar(prefix + counter);
		bindings.add(current_statement);
	}
	
	protected ASTNode getCurrentMethod()
	{
		ASTNode current_statement_node = getProperty(prefix + counter).binding();
		ASTNode current_parent = current_statement_node.getParent();
		
		//It would be nice to have an API for this.  Maybe part of ASTNodeJoiner.
		
		while(current_parent != null)
		{
			if(current_parent instanceof MethodDeclaration)
				return current_parent;
			
			current_parent = current_parent.getParent();
		}
		
		return null;
		
	}
	

	@Override
	protected List<Constraint> localConstraints() {
		ArrayList<Constraint> local_constraints = new ArrayList<Constraint>();
		
		local_constraints.add(new ConsecutiveConstraint(prefix));
		
		return local_constraints;
	}

	public FreeVar getMethodDeclarationVar() {
		return meth_decl_var;
	}
	

	

}
