package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.matching.constraints.MethodExtractionConstraint;
import mychangedetector.matching.constraints.NotEqualConstraint;
import mychangedetector.matching.constraints.VariableExtractionConstraint;
import mychangedetector.matching.constraints.VariableRenamedConstraint;
import mychangedetector.specification.requirements.MethCall;
import mychangedetector.specification.requirements.NameChange;
import mychangedetector.specification.requirements.Statement;
import mychangedetector.specification.requirements.UpdateMethod;
import mychangedetector.specification.requirements.UpdateStatement;

import org.eclipse.jdt.core.dom.ASTNode;

   
/* 
 * Next steps:
 *     
 *     
 *     Holy fuck, Extract Method is working!  
 *
 *	   Extract expression is working well too.
 *			The current problem crops up when you try to do two ExtractExpression refactorings at once.
 *			Part of the problem is the rollback.
 *				This might be a good opportunity to explore doing something more intelligent (local) with the roll back -- i.e. don't roll back the entire document, just the place where the change is happening.
 *				Then again, this may not work with complex refactorings like Extract Method.
 *
 *			The easy fix is to just disallow simultaneous refactorings...
 *				We reenabled it to deal with non-parsable states.
 *				But we can safely drop in-flight refactorings whenever a new one takes flight.
 *
 *
 *
 *
 *        
 *     
 *     Add in new refactorings...
 *     
 *     		Adding unimplemented methods and try/catch blocks would be ridiculously useful.  I know they're not "refactorings", but I think they should be the first things we add.
 *     			The try/catch would be a good example of a refactoring where you'd cycle through multiple options: You might want the try/catch block to surround the current statement, or its parent block, and so on.  Or you might want to add a throws declaration.		
 *     
 *     		Change method signature would be cool -- especially if it uses linked mode and could be done in real-time like Rename.  Should investigate whether it works like that.
 *     			Then you could trigger the Change Signature right after you do the Extract Method -- analogous to how Rename executes after Extract Variable
 *     
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
 *     
 *     What are some good ways to demo this tool?  Are there any canonical programs that we can write and show that the process is easier with the tool?
 *     Is there some way to examine a program and determine whether it would have benefitted from having been written with the help of the tool? (That's crazy, I know.  But it's worth writing down all ideas -- even the "out there" ones.)
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
		
		for(FreeVar var : getBindings())
		{
			if(var.name().equals(key))
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

		//Not well named.  This looks for a deleted statement, then binds the parent method declaration.
		UpdateMethod updated_method = new UpdateMethod("old_method","new_method");

		
		//Now we want to detect the introduction of a method call.  What ever was deleted in between must be the extracted lines.
		MethCall change_to_method_call = new MethCall("UPDATE","method_call");
		
		//Now we want to detect the introduction of a method call.  What ever was deleted in between must be the extracted lines.
		MethCall insert_new_method_call = new MethCall("INSERT","method_call");
		
		extract.addRequirement(updated_method);
		extract.addRequirement(insert_new_method_call);
		extract.addRequirement(change_to_method_call);

		
		
		//We also need a constraint that will verify that the new method call replaces some contiguous sequence of code lines.
		//   This can also check to see that there haven't been any other changes to the method -- i.e. not related to the extraction.
		extract.addConstraint(new MethodExtractionConstraint("method_call","hook_statement"));  //Bonus points if this works in both directions.
		
	
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
 *            Locking the user out after detection works, but it would be better to cache the user's ineffectual edits and execute them afterward.
 *                  
 *                  
 *         Ghosting!  (A note for the paper:  We give the illusion of proposing a change but have, in reality, already made the change)
 *                (Another note: Since we can't [or don't want to] rely on knowing the exact changes the refactoring made.  We do a diff on the fly to determine what to ghost.)
 *                (The diff on the fly uses what algorithm?  And what tokens?)
 *                
 *                
 *        The last part of the ghosting puzzle is to give the user a way to confirm or reject the change.   
 *        What constitutes a confirm?  What constitutes a cancel?  What are the "gray" areas?
 *        Case:  Extracting a variable and then renaming it.
 *            Should the tool confirm when they begin the rename refactoring? 
 *            Should it wait until afterward for an explicit acceptance? (No reason why it couldn't).
 *            The rename action isn't necessarily acceptance or rejection.  So it might be snazzy to keep the proposal gray for as long as possible.  We just need a clear way to accept/reject...
 *        The solution used here is to be conservative: Most actions are cancellations (arrow key presses, mouse clicks).  The only way to confirm is to press tab.  Actions that will not confirm or reject are alphanumeric key presses.
 *        		This is motivated by the way that renames (I surmise) will often follow extractions.  So the subsequent alphanumeric presses are likely a rename.  And it looks cool to modify the suggestion rather than auto-accept it.
 *                 
 * 
 *                  
 *                   *     The multi-step are supported because we're basically looking for a simulation relation,
 *        a series of steps that constitute a refactoring, regardless of what comes in between.
 *        
 *     I think the simulation relation theoretical stuff would be good to put into the paper.
 *     
 *     Here's an example of a problem that arose when I wasn't doing it quite right.
 *         
 *         E.G.
 *         
 *            xxxx.methodCall(expression + expression)
 *            
 *            to...
 *            
 *            xxxx.methodCall(variable)
 *            
 *            ... works just fine.
 *            
 *            
 *            But:
 *            
 *            xxxx.methodCall(expression + expression)
 *            
 *            to...
 *            
 *            xxxx.methodCall()
 *            
 *            to...
 *            
 *            xxxx.methodCall(variable)
 *            
 *            Causes the engine to bind after the first change:
 *            
 *               old_statement: xxxx.methodCall(expression + expression)
 *               new_statement: xxxx.methodCall()
 *           
 *            And after the second change:
 *            
 *               old_statement: xxxx.methodCall()
 *         		 new_statement: xxxx.methodCall(variable)
 *         
 *            Whereas the "correct" bindings should be:
 *               
 *               old_statement: xxxx.methodCall(expression + expression)
 *               new_statement: xxxx.methodCall(variable)
 *               
 *            There are various possible fixes.  Off the top of my head:
 *              1) Constrain {new_statement: xxxx.methodCall()} from being bound.
 *              	  This will keep new_statement free to bind to {new_statement: xxxx.methodCall(variable)}
 *              2) Allow {new_statement: xxxx.methodCall()} to bind, but update it when we detect that the user is making changes to a previously bound expression.
 *              	  This lies outside of the current model, but it's a pretty cool solution -- so it's worth looking into.
 *              3) Allow specifications to be chained together or merged such that 
 *                    old_statement: xxxx.methodCall(expression + expression)
 *                    new_statement: xxxx.methodCall()
 *           
 *                    +
 *            
 *                    old_statement: xxxx.methodCall()
 *         		      new_statement: xxxx.methodCall(variable)
 *         
 *                    =
 *                    
 *                    old_statement: xxxx.methodCall(expression + expression)
 *         		      new_statement: xxxx.methodCall(variable)
 *         
 *                 This would require doing some kind of search-and-combine through the existing in-flight specifications.
 *                 It's tricky because we drop specifications that don't get executed immediately.
 *                 		But with some tweaks, this would be possible.
 *         
 *        
 *        
 *          Tried making a "dumber" differencing package based on the one we're using to gray-out stuff.
 *          
 *          Thought it might work better.
 *          	faster.
 *          	able to handle unparsable lines.
 *          	matches the gray-out differencing package (so there won't be two separate ones)
 *          	
 *          
 *          Turns out I was right: Dumber diffs appear to be better for real-time refactoring.
 *          	(I'm just splitting on semi-colons to detect change areas.  Only extracting type information later.
 * 
 *     
 *     
 *     The way that the Extract Method specification has been simplified is pretty interesting.  I think it's worth arguing for a certain kind of minimalist aesthetics in these specs.
 *     The old way was certainly a big mess.  This might be worth writing about.
 *     		Before:
 * 				We were trying to bind every piece of necessary information when it "arrived" -- i.e. each deleted statement that the user removed.
 * 				This turned out to be difficult because there wasn't a good way to match nodes in ASTs between edits.  Plus it wouldn't have been efficient anyway.
 * 			After:
 * 				We try to match the first and last requirement (removing one statement, adding a method call).
 * 				The tool is able to infer the rest of the information (i.e. the other removed statements).
 */




