package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.matching.constraints.MethodMotionConstraint;
import mychangedetector.matching.constraints.NotEqualConstraint;
import mychangedetector.matching.constraints.VariableExtractionConstraint;
import mychangedetector.specification.requirements.CodeBlock;
import mychangedetector.specification.requirements.MethCall;
import mychangedetector.specification.requirements.MethodDef;
import mychangedetector.specification.requirements.NameChange;
import mychangedetector.specification.requirements.Statement;
import mychangedetector.specification.requirements.UpdateStatement;

import org.eclipse.jdt.core.dom.ASTNode;

   
/* 
 * Next steps: 
 *   
 *     Stabilize stuff...
 *        Extract Variable's constraint is failing on the case:
 *          		rename.addExecutor(new EclipseRenameExecutor(new SpecificationAdapter(rename)));   to....
 *					rename.addExecutor(new EclipseRenameExecutor(new_spec_adapter));
 *     	      The zipping stuff appears to be broken.
 *        Extract Method is fucked.
 *     
 *        Is Change distiller fucked too?  Try more cases.  I suspect that it's going to crap out if the changes are too drastic -- and it will return a bunch of removed statements even if I just do a simple update.
 *        
 * 
 *     Get undos working.
 *     
 *     User interface animations/ghosting would be really nice...
 *     
 *     The multi-step refactorings have not been well-tested....
 *     
 *     Add in new refactorings...
 *     
 *     		Make extract var work in reverse.  Detect when a variable is introduced and defined in a way that matches an expression nearby.
 *     			(Should just be a challenge related to research -- not to Eclipse crap.  We can use the same executor.  Yay.)
 * 
 *     
 *     		Fix up extract method.  It kind of sucks.  It should handle passing in local variables with more appropriate triggers.
 *     			(This'll probably come together as I start using the tool for development.)
 *     
 *          Multi-file refactorings -- push up / pull down method.  Change method signature.
 *          
 *          Local var to field.
 *          
 *          Anonymous class to nested.
 *     
 * Next next steps:
 * 
 *     Cool UIs (Lobo browser?)
 *     
 *     Gamify the refactoring experience.  Points.  Sound effects.  Etc.
 */


public class Specification implements Cloneable {
	List<Requirement> requirements = new ArrayList<Requirement>();
	List<Constraint>  constraints  = new ArrayList<Constraint>();
	
	Executor exec;
	
	String name;
	
	private boolean has_executed = false;
	
	public Specification(String name)
	{
		this.name = name;
	}

	public void match(ChangeWrapper change) {
		for(Requirement req : requirements)
		{
			req.match(change,constraints);
			
		} 
		

	}
	
	public boolean tryExecute(){
		if(exec.isReadyToExecute())
		{
			exec.execute();
			has_executed = true;
			return true;
		}
		return false;
	}
	
	public boolean hasExecuted(){
		return has_executed;
	} 
	 
	public boolean equals(Object other)
	{
		List<FreeVar> other_bindings = ((Specification)other).getBindings();
		
		if(other_bindings.size() != getBindings().size())
			return false;
		
		for(FreeVar binding : getBindings())
		{
			if(!other_bindings.contains(binding))
				return false;
		}
		
		
		return true;
	}
	
	public Specification copy()
	{
		Specification copy = null;
		try {
			copy = (Specification) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		copy.clearRequirements();
		copy.clearConstraints();
		
		for(Requirement r : requirements)
		{
			copy.addRequirement(r.copy());
		}
		
		for(Constraint c : constraints)
		{
			copy.addConstraint(c.copy());
		}
		
		copy.addExecutor((Executor)exec.copy(copy));
		
		return copy;
	}
	
	public void clearRequirements(){ requirements = new ArrayList<Requirement>(); }
	public void clearConstraints(){ constraints = new ArrayList<Constraint>(); }

	public void addRequirement(Requirement r)
	{
		requirements.add(r);
		r.setSpecification(this);
	}
	
	public void addConstraint(Constraint c)
	{
		constraints.add(c);
	}
	
	public void addExecutor(Executor e)
	{
		exec = e;
	}
	
	public String toString()
	{
		return name + getBindings().toString();
	}
	
	public List<FreeVar> getBindings()
	{
		List<FreeVar> ret = new ArrayList<FreeVar>();
		for(Requirement r : requirements)
			ret.addAll(r.getBindings());
		
		return ret;
	}
	
	public FreeVar getProperty(String key)
	{
		for(FreeVar var : getBindings())
		{
			if(var.name().equals(key) && var.isBound())
				return var;
		}
		
		return null;
	}

	
	public void setProperty(String key, ASTNode node)
	{
		for(FreeVar var : getBindings())
		{
			if(var.name().equals(key) && var.isNotBound())
				var.bind(node);
		}
	}
	
	public static Specification newExtractVariableSpecification()
	{
		Specification extract_variable = new Specification("Extract Variable");
		
		/**
		 * 
		 * Case 1:
		 * 
		 * Given
		 * 
		 * a = b + c + d
		 * 
		 * User deletes "b + c"
		 * 
		 * a = + d
		 * 
		 * Tool infers (by checking parse distance)
		 * 
		 * i = b + c
		 * a = i + d
		 * 
		 */
		
		Statement new_statement = new Statement("INSERT", "new_statement");
		Statement old_statement = new Statement("DELETE", "old_statement");
		UpdateStatement updated_statement = new UpdateStatement("old_statement","new_statement");
		
		extract_variable.addRequirement(new_statement);
		extract_variable.addRequirement(old_statement);
		extract_variable.addRequirement(updated_statement);
		
		extract_variable.addConstraint(new VariableExtractionConstraint("old_statement","new_statement", "variable_name", "extracted_expression"));
		
		//extract_variable.addConstraint(new SameLocation("old_statement","new_statement"));
		
		/**
		 * Case 2:
		 *  
		 * Given
		 * 
		 * a = b + c + d
		 *  
		 * User adds "i = b + c"
		 * 
		 * i = b + c
		 * a = b + c + d
		 * 
		 * Tool infers (by searching document for matching sub-expr)
		 *  
		 * i = b + c
		 * a = i + d
		 * 
		 * 
		 *  Note:
		 *    (An alternative would be to use some other tool to always keep track of refactorings 
		 *    that could be performed, and then to prompt the user when they put the document into a 
		 *    state where a brand new refactoring exists.  In the long run, this method might take
		 *    care of a lot of cases that I'm having to craft manually.  I'm sure Miryung Kim has
		 *    some tools for detecting refactoring potentials...)
		 * 
		 * 
		 */
		
		/*
		Statement new_var_declaration = new Statement("INSERT", "new_var_decl");
		SearchRequirement extractable_statement = new SearchRequirement("extractable_statement");
		extractable_statement.searchFor(old_statement);
		
		extract_variable.addRequirement(new_var_declaration);
		extract_variable.addRequirement(extractable_statement);
		
		SubexpressionsMatch subexpressions_match = new SubexpressionsMatch("new_var_decl","extractable_statement");
		*/

		/**
		 * 
		 * Add the executor to interface with Eclipse's refactoring tool.
		 * 
		 */
		
		EclipseExtractVariableExecutor executor = new EclipseExtractVariableExecutor();
		executor.setSpecification(new SpecificationAdapter(extract_variable));
		extract_variable.addExecutor(executor);
		
		//Temporary
		//extract_variable.addExecutor(new NoopExecutor(new SpecificationAdapter(extract_variable)));
		
		return extract_variable;
	}
	
	public static Specification newRenameSpecification()
	{
		Specification rename = new Specification("Rename Method");
		
		NameChange nameChange = new NameChange("old_name","new_name");
		
		
		rename.addRequirement(nameChange);
		rename.addConstraint(new NotEqualConstraint("old_name","new_name"));
		rename.addConstraint(new VariableRenamedConstraint("old_name","new_name"));
		rename.addExecutor(new EclipseRenameExecutor(new SpecificationAdapter(rename)));
		
		return rename;
	}
	
	public static Specification newExtractMethodSpecification()
	{
		Specification extract = new Specification("Extract Method");
		
		MethodDef addMeth  = new MethodDef("INSERT", "method_declaration", "method_name");
		CodeBlock remBlock = new CodeBlock("DELETE", "removed_statement_");
		CodeBlock addBlock = new CodeBlock("INSERT", "added_statement_");
		MethCall  addCall  = new MethCall("INSERT",  "call_name");
		
		extract.addRequirement(remBlock);
		extract.addRequirement(addMeth);
		extract.addRequirement(addBlock);  
		extract.addRequirement(addCall);

		//Ensure that the added block wasn't to the same method as the removed block, which would be a deal breaker.
		extract.addConstraint(
				new MethodMotionConstraint(
						remBlock.getMethodDeclarationVar().name(),
						remBlock.getPrefix(),
						addBlock.getMethodDeclarationVar().name(),
						addBlock.getPrefix()
				)
		);
		
		//Constraint needed because adding a method call gets recorded as adding a one-line code block.
		//This might not be the best way to fix it -- because a legitimate code block could, in theory, have a method call on its first line.
		//    I guess this should be combined with some kind of length constraint: if code block is only one statement, the first statement's type should not be a method invocation.
		//    I'll wait on that, though.
		
		ASTNodeDescriptor descriptor = (new ASTNodeDescriptor("ExpressionStatement")).withChild("MethodInvocation");
		
		
		extract.addConstraint(
				new IsTypeConstraint("NOT", "added_statement_1", descriptor)
		);

	//	extract.addConstraint(new ChildOfMethodConstraint("added_statement_\\d+", addMeth.getMethodDeclarationVar().name()));
	//	extract.addConstraint(new ChildOfMethodConstraint("call_name", remBlock.getMethodDeclarationVar().name()));
	
		extract.addExecutor(new EclipseExtractMethodExecutor(new ExtractMethodSpecificationAdapter(extract)));
		
		return extract; 
	}    

	public List<FreeVar> getPropertiesByRegex(String regex) {
		List<FreeVar> ret = new ArrayList<FreeVar>(); 
		
		for(FreeVar var : getBindings())
		{
			if(var.name().matches(regex))
				ret.add(var);
		}
		
		return ret;
	}
}
