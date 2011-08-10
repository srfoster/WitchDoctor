package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.matching.constraints.MethodMotionConstraint;
import mychangedetector.matching.constraints.NotEqualConstraint;
import mychangedetector.matching.constraints.VariableExtractionConstraint;
import mychangedetector.matching.constraints.VariableRenamedConstraint;
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
 *        
 *        
 *        That folding problem is becoming more and more annoying.
 *           The quick fix is to make the editor unfold everything, or make folding impossible in the first place.
 *
 *        OMG, it's gray!  Now make the gray range expand when the text expands, by using Positions instead of StyleRanges.
 *
 *  
 * 		  Need to fix the flicker by finding a way to set the styles without fighting the built in syntax highlighting.
 *  
 *            I'm not seeing anything clever that can be done within styledtext -- unless there's a listener we can remove/edit...
 *            Might need to think about making a custom editor plugin, rather than (or in addition to) the view plugin.
 *  
 *  
 *  	  Use Positions in the document to determine the style ranges, rather than static start/end values,
 *  			this will allow the grayed out area to persist through subsequent changes (like a rename following a refactoring).
 *              
 *        The last part of the ghosting puzzle is to give the user a way to confirm or reject the change.   
 *              
 *              
 *        Locking the user out after detection works, but it would be better to cache the user's ineffectual edits and execute them afterward.
 *     
 * 
 *          
 * 
 * 
 *     Stabilize stuff
 *
 *        Extract Method is fucked.
 *     
 *     User interface animations/ghosting would be really nice...  Show the resulting changes in gray.  And allow rolling back with tab...
 *     
 *     The multi-step refactorings are not working.  Try extracting a variable in 2 steps.
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




/****
 *  Notes that will be useful when writing the paper.
 *           
 *         Here's a challenge that should be fixable by the end of the day (it's also worth writing about, so keep these notes around.)
 *                
 *         Also, extract variable loses the characters after the first one typed.
 *         	  Case 1: Characters typed after detection and before refactoring (rollback) is started.
 *                  (Check again at the rollback?  Pipe changes to refactoring?  IDK.)
 *                  (Actually, what seems to be working is to lock the document immediately when we detect
 *                   the refactoring.  And since we run the detection on every keystroke, we can lock
 *                   out all changes after the first.  So this takes care of the first two cases.)
 *         	
 *            Case 2: Characters typed after refactoring is started, before it finishes.
 *                  (Easiest fix is to lock the document before rolling back -- intercepting the keystrokes until the refactoring finishes.
 *                   This moves the changes to case 3 below.)
 *            
 *            
 *            Case 3: Characters typed after refactoring is finished
 *            		(Hopefully, these will be detected as a new refactoring if they are relevant.
 *                   This is indeed the case with extract variable, thankfully.  They are detected as a subsequent rename.)
 *                  
 *                  
 *                  
 *         Ghosting!  (A note for the paper:  We give the illusion of proposing a change but have, in reality, already made the change)
 *                (Another note: Since we can't [or don't want to] rely on knowing the exact changes the refactoring made.  We do a diff on the fly to determine what to ghost.)
 *                (The diff on the fly uses what algorithm?  And what tokens?)
 *                  
 */




