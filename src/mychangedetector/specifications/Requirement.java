package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class Requirement implements Cloneable {

	protected List<FreeVar> bindings = new ArrayList<FreeVar>();

	protected Specification specification;
	protected String operation;

	public void setSpecification(Specification specification)
	{
		this.specification = specification;
	}
	
	public Specification getSpecification()
	{
		return this.specification;
	}
	
	public Requirement copy()
	{
		Requirement copy = null;
		try {
			copy = (Requirement) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		copy.clearBindings();
		
		for(FreeVar binding : bindings)
		{
			copy.addBinding(binding.copy());
		}
		
		return copy;
	}
	
	
	//It's a bit weird that setProperty and getProperty don't exhibit the customary symmetry.
	
	//NOTE: Might want to change these methods to make them more symmetrical.
	
	public void setProperty(String key, ASTNode current, ChangeWrapper change) {
		
		for(FreeVar var : bindings)
		{
			if(var.nameIs(key))
			{
				if(var.isNotBound() && specification.getProperty(key).isNotBound())
				{
					var.bind(current);
					var.setContext(change);
				}
				return;
			}
		}
		
		FreeVar new_var = new FreeVar(key);
		new_var.bind(current);
		new_var.setContext(change);
		bindings.add(new_var);
	}
	
	public FreeVar getProperty(String key)
	{
		for(FreeVar var : bindings)
		{
			if(var.nameIs(key))
				return var;
		}
		return null;
	}
	
	public void match(ChangeWrapper change, List<Constraint> external_constraints) {
		
		//Find any bindings in this change item
		ChangeMatcher matcher = buildChangeMatcher(); 
		matcher.match(operation,change);
		Map<String,ASTNode> matched_before_bindings = matcher.getBeforeBindings();
		Map<String,ASTNode> matched_after_bindings = matcher.getAfterBindings();;

		boolean match_successful = constrain(matched_before_bindings, external_constraints, change);
		
		match_successful         = constrain(matched_after_bindings, external_constraints, change) || match_successful;
		
		if(match_successful){
			afterMatch(change);
		}
	}
	
	private boolean constrain(Map<String,ASTNode> matched_bindings, List<Constraint> external_constraints, ChangeWrapper change)
	{
		boolean match_successful = false;

		
		//Throw away any bindings that violate the constraints.  Save the rest.
		for(String key : matched_bindings.keySet())
		{
			ASTNode current = matched_bindings.get(key);
			
			boolean constraint_violated = false;
			for(Constraint constraint : external_constraints)
			{
				if(constraint.isViolatedBy(key, matched_bindings, this, change))
					constraint_violated = true;
			}
			for(Constraint constraint : localConstraints())
			{
				if(constraint.isViolatedBy(key, matched_bindings, this, change))
					constraint_violated = true;
			}
			
			if(!constraint_violated)
			{
				this.setProperty(key,current,change);
				match_successful = true;
			}
		}

		return match_successful;
	}
	
	protected List<Constraint> localConstraints(){
		return new ArrayList<Constraint>();
	}
	
	abstract protected void afterMatch(ChangeWrapper change);
	
	protected void addBinding(FreeVar b)
	{
		this.bindings.add(b);
	}

	protected void clearBindings()
	{
		this.bindings = new ArrayList<FreeVar>();
	}

	public Collection<? extends FreeVar> getBindings() {
		return bindings;
	}

	public abstract ChangeMatcher buildChangeMatcher();

}
