package mychangedetector.specification.requirements;

import mychangedetector.ast_helpers.ASTNodeBuilder;
import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.ChangeMatcher;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

public class MethodDef extends Requirement {

	/*
	 * These variables are specific to MethodDef
	 */
	private FreeVar method_name_var;
	private FreeVar method_body_var;
	private FreeVar method_decl_var;
		
	public MethodDef(String operation, String method_decl_var_name, String method_name_var_name) {
		method_name_var = new FreeVar(method_name_var_name);
		method_decl_var  = new FreeVar(method_decl_var_name);
		method_body_var = new FreeVar("method_body");
		
		bindings.add(method_decl_var);
		bindings.add(method_name_var);
		bindings.add(method_body_var);
		
		this.operation = operation;
	}
	
	public FreeVar getMethodDeclarationVar()
	{
		return method_decl_var;
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
			
		builder.methodDeclaration(method_decl_var.name())
		           .hasChild(builder.simpleName(method_name_var.name()))
		           .hasChild(builder.block(method_body_var.name()));
		
		return builder.getNode();
		*/
		
		ASTNodeDescriptor meth_decl_desc = new ASTNodeDescriptor();
		meth_decl_desc.setClassName("org.eclipse.jdt.core.dom.MethodDeclaration");
		meth_decl_desc.setBindingName(method_decl_var.name());
		
		ASTNodeDescriptor meth_name_desc = new ASTNodeDescriptor();
		meth_name_desc.setClassName("org.eclipse.jdt.core.dom.SimpleName");
		meth_name_desc.setBindingName(method_name_var.name());
		
		ASTNodeDescriptor meth_body_desc = new ASTNodeDescriptor();
		meth_body_desc.setClassName("org.eclipse.jdt.core.dom.Block");
		meth_body_desc.setBindingName(method_body_var.name());
		
		meth_decl_desc.addChild(meth_name_desc);
		meth_decl_desc.addChild(meth_body_desc);
		
		return meth_decl_desc;
	}


	@Override
	protected void afterMatch(ChangeWrapper change) {
	}





}


