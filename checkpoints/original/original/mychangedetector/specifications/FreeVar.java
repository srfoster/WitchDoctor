package mychangedetector.specifications;

import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;


public class FreeVar implements Cloneable {
	String name;
	ASTNode binding;
	ChangeWrapper change_context;

	public FreeVar(String string) {
		name = string;
		binding = null;
	}

	public boolean isNotBound() {
		return binding == null;
	}
	
	public boolean isBound(){
		return binding != null;
	}

	public boolean nameIs(String key) {
		return name.equals(key);
	}
	
	public String name()
	{
		return name;
	}
	
	public ASTNode binding()
	{
		return binding;
	}

	public void bind(ASTNode binding) {
		this.binding = binding;
	}
	
	@Override
	public boolean equals(Object other)
	{
		FreeVar other_var = (FreeVar) other;
		
		if(name().equals(other_var.name()) && binding() == null && other_var.binding() == null)
			return true;
		
		return name().equals(other_var.name()) && binding().subtreeMatch(new ASTMatcher(), other_var.binding());
		
	}

	public FreeVar copy() {
		try {
			FreeVar copy = (FreeVar) this.clone();
			
			/*
			if(binding != null)
			{
				AST ast = binding.getAST();
				ASTNode ast_node_copy = ASTNode.copySubtree(ast,binding);
				copy.bind(ast_node_copy);
			}
			*/
			
			
			return copy;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String toString()
	{
		if(binding == null)
			return name + ": null";
		return name + ": " + binding.toString();
	}

	public void setContext(ChangeWrapper change) {
		this.change_context = change;
	}

	public ChangeWrapper getChangeContext() {
		return this.change_context;
	}
}
