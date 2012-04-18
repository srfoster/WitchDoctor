package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.matching.constraints.IsAvailableMethod;
import mychangedetector.matching.constraints.NotEqualConstraint;
import mychangedetector.matching.constraints.VariableExtractionConstraint;
import mychangedetector.matching.constraints.VariableRenamedConstraint;
import mychangedetector.specification.requirements.CodeBlock;
import mychangedetector.specification.requirements.MethCall;
import mychangedetector.specification.requirements.NameChange;
import mychangedetector.specification.requirements.SameParentConstraint;
import mychangedetector.specification.requirements.Statement;
import mychangedetector.specification.requirements.StatementWithCompilerMessage;
import mychangedetector.specification.requirements.UpdateStatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

   
/* 
 * Next steps:
 * 
 * 
 * 		Holy crap -- if we can't execute a refactoring because of syntax errors... we could just comment them out and try again.
 * 			This would be a cool thing to do during the assessment.  If many refactorings are failing, we can increase our numbers with the commenting trick.
 * 				(Then we'll have something else to put into the Execution section of the paper.)
 * 
 * 
 * 		Rename will ignore non-available types.  Now make it reset everything as if we were never there.  As it stands, it reverts any changes to non available types.
 * 
 * 
 * 
 * 
 * 		Cancelations:
 * 			They can happen in mid-refactoring -- i.e. while renaming in Linked mode.
 * 			They can happen afterward, in proposal mode.  This works already though.
 * 			So it'd be nice if Linked mode worked in the same way.
 * 
 * 
 *  	Can't just rename any simple name.  Consider a method that returns an ArrayList.
 * 				You want to change it to return a LinkedList.  When you edit the return type,
 * 				you don't want the tool to try to rename the ArrayList type.
 * 				The question is how to restrict this.  You probably DO want to be able to rename types sometimes.
 * 				Could check that the types are defined by you -- not a library.  But that's tricky.
 * 				Could require that types are renamed at their declaration.  But that's different from rename var.
 * 				And for that matter, we might have an analogous problem with variables.
 * 				
 * 				Maybe the problem is best solved by giving users an ability to cancel a change.
 * 					And perhaps we can have the tool learn from the cancels...  Future work...
 * 	
 * 				Some kind of cancellation feature plus a tool-side tendency to discriminate.
 * 
 * 
 * 		Once the above is done, need to get back to writing test code.  Don't want to get sidetracked with issues that aren't super-critical.
 * 		Write MergeSort.  Find more bugs.  Kill them.
 * 
 * 
 * 		I'm still worried that changes which arrive in mid-refactoring will screw things up.  Might need to lock the document again...
 * 			Actually.  We might be able to get away with cleverly placing the cursor, so that changes end up the right place.
 * 				(Extract method should not end with the method call selected.)
 * 			Looks like this will work for extract variable too -- once we fix the rename bug above.
 * 			Hmmm.  Maybe not though.... when you type REALLY fast it messes up.  We'll have to see how annoying this is in practice.
 * 				It might be something we can ignore -- as long as we can get the cursor positioning right -- which helps with 90% of hte issue.
 * 
 * 
 * 
 * 
 * 		Extract variable has a gray-out bug.  The gray continues to the next line.  I assume this is because of the way we're doing diffs.
 * 
 * 		Extract Variable might be a bit too aggressive.
 * 	
 * 		Might need an inverse to rollback(), in the event that stuff fails.
 * 			-- In most cases, we can use the UndoEdit that ASTRewrite produces.
 * 
 *  	Some bugs:
 *  		If the user rejects the change, the tool needs to stop asking (i.e. try/catch)
 * 
 * 		Wow.  Try/catch completion is working.
 * 			Now...
 * 				3) The gray color doesn't "stick" on some key words -- but maybe we can ignore that for a while.
 * 				5) We could now potentially use the expanded model to check that Extract Method involves a non-existent method -- thus avoiding the refactoring, which for some reason duplicates the method.
 *    
 * 
 * 
 * 	  The Executor stuff needs to be on the refactoring chopping block soon.  It's confusing to think about how all the call backs and document change listeners are interacting.  Eclipse probably has a better way.  Look into batched runnables?
 * 			With the try/catch executor, it became clear that we don't always want to roll back.  It was hard to make the new executor.  There was lots of cut and paste.  The code looks ugly.  And I forgot to set old_string, which broke the gray-diff process.
 * 			Since we make an executor every time we make a new refactoring, it should be as easy as possible.  We'll be making a lot of them.
 * 
 *
 *
 * 			
 *     
 *    Add in new refactorings...
 *     
 *     
 *     As another extension of the model, it would be nice to incorporate the location of the cursor.
 *     		Then we could trigger the Rename linked mode when the user's cursor moves onto a SimpleName.
 *     			The Requirement would change very little.  It would just check to see if the old SimpleName had no cursor, and the new simple name does.
 *     
 *     		We'd need to trigger the refactoring whenever the cursor position changed.
 *     
 *     		And we'd need to do something like the above -- do a diff and decorate it with the new cursor position.  The node that changed would be the one the cursor is on.
 *     		Like the above, it would be a special kind of diff (although not necessarily a subclass) which would involve a before/after ASTNode whose "change" would be the cursor attribute.
 *     		This would allow us to maintain our same basic model: Sifting through AST diffs.
 *     
 *     
 *     
 *     
 *
 *     
 *     
 *     		Generating getters and setters would be super cool too.
 *     
 *     		Change method signature would be cool -- especially if it uses linked mode and could be done in real-time like Rename.  Should investigate whether it works like that.
 *     			Then you could trigger the Change Signature right after you do the Extract Method -- analogous to how Rename executes after Extract Variable
 *     
 *     
 *     		Make extract var work in reverse.  Detect when a variable is introduced and defined in a way that matches an expression nearby.
 *     			(Should just be a challenge related to research -- not to Eclipse crap.  We can use the same executor.  Yay.)
 *      
 *          Multi-file refactorings -- push up / pull down method.  Change method signature.
 *          
 *          Local var to field.
 *          
 *          Anonymous class to nested.
 *     
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
		
		Specification.addExecutor(copy, (Executor)exec.copy(copy));
		
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
		Specification.addExecutor(extract_variable, executor);
		
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
		Specification.addExecutor(rename, new EclipseRenameExecutor(new SpecificationAdapter(rename)));
		
		return rename;
	}
	
	public static Specification newExtractMethodSpecification()
	{
		Specification extract = new Specification("Extract Method");

		//Not well named.  This looks for a deleted statement, then binds the parent method declaration.
		//UpdateMethod updated_method = new UpdateMethod("old_method","new_method");

		CodeBlock deleted_block = new CodeBlock("DELETE","removed_statement_");
		MethCall  added_method_call = new MethCall("INSERT","method_call");

		extract.addRequirement(deleted_block);
		extract.addRequirement(added_method_call);
		
		extract.addConstraint(new SameParentConstraint("removed_statement_1","method_call", MethodDeclaration.class));
		extract.addConstraint(new IsAvailableMethod("method_call"));
		
		
		Specification.addExecutor(extract, new EclipseExtractMethodExecutor(new ExtractMethodSpecificationAdapter(extract)));
		
		return extract; 
	}
	
	/*
	public static Specification newExtractMethodSpecification()
	{
		Specification extract = new Specification("Extract Method");

		//Not well named.  This looks for a deleted statement, then binds the parent method declaration.
		//UpdateMethod updated_method = new UpdateMethod("old_method","new_method");

		Statement deleted_statement = new Statement("DELETE","hook_statement");
		
		//Now we want to detect the introduction of a method call.  Whatever was deleted in between must be the extracted lines.
		// Actually, it's not working.  Deleting a statement and moving a method call up into its position looks like updating to a method call.
		//MethCall change_to_method_call = new MethCall("UPDATE","method_call");
		
		//Now we want to detect the introduction of a method call.  Whatever was deleted in between must be the extracted lines.
		MethCall insert_new_method_call = new MethCall("INSERT","method_call");
		
		extract.addRequirement(deleted_statement);
		extract.addRequirement(insert_new_method_call);
//		extract.addRequirement(change_to_method_call);


		//We also need a constraint that will verify that the new method call replaces some contiguous sequence of code lines.
		//   This can also check to see that there haven't been any other changes to the method -- i.e. not related to the extraction.
		extract.addConstraint(new MethodExtractionConstraint("method_call","hook_statement"));  //Bonus points if this works in both directions.
	
		Specification.addExecutor(extract, new EclipseExtractMethodExecutor(new ExtractMethodSpecificationAdapter(extract)));
		
		return extract; 
	}
	*/
	
	public static Specification newTryCatchSpecification()
	{
		Specification try_catch = new Specification("Try/Catch Complete");
		
		StatementWithCompilerMessage statement = new StatementWithCompilerMessage("INSERT", "error_statement", CompilerMessage.UNHANDLED_EXCEPTION);
		try_catch.addRequirement(statement);
		
		statement = new StatementWithCompilerMessage("UPDATE", "error_statement", CompilerMessage.UNHANDLED_EXCEPTION);
		try_catch.addRequirement(statement);

		TryCatchCompleteExecutor executor = new TryCatchCompleteExecutor();
		executor.setSpecification(new SpecificationAdapter(try_catch));
		Specification.addExecutor(try_catch, executor);

		
		return try_catch;
	}

	public static void addExecutor(Specification specification, Executor e) {
		specification.addExecutor(e);
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
 *          On a related note: We introduce greater and greater semantic awareness as the tool progresses.
 *          	1) First, we detect any kind of action that MIGHT change the document.  (The simplest kind of change detection there is.)
 *          	2) Then, we do a "dumb" diff on the text of the document.  (Slightly more semantic awareness).
 *          	3) Then, we get the relevant ASTNodes, if we can.
 *          	4) Then, we use Specifications to combine isolated changes into a larger story -- one that involves the user's edits over time.
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
 * 
 * 
 *    The model now includes more than just changes to the text of the document.  It also includes the compiler errors -- which is pretty cool, and I think definitely worth writing about.
 *    
 *    
 *    By way of justification for this kind of paradigm: The built-in refactorings are a little bit arbitrary in how they're triggered.  "Place the caret inside the anonymous class" for Anonymous Class to Nested.  Weird.  Why can't I highlight it?  There are other examples too.
 *      For Encapsulate Field, you have to select the field NAME, not the whole declaration.  Annoying popus are the result.  They tell you that you didn't use the interface correctly.  Thus, not only must the
 *      user be aware of the available refactorings, they must also trigger them correctly.  This constitutes more training, and exposes the disconnect between the tool suite and the user's intention.
 *      It's worth stating in the paper that, fundamentally, the present interface and mine are really subsets of the same idea: The user interfaces with a collection of triggerable tools.
 *      
 *    Even more compelling, however, is the fact that the growing suite of tools creates new problems.  Already, there are too many refactorings in eclipse.  There need to be more.  Add in the number of templates.  Wowzers.  
 *     
 *    Other reasons?
 *      
 *      
 *    Possible paper story:
 *    
 *    Every new tool that ships with Eclipse makes the Radical Minimalist more and more antiquated and makes the Tool Cyborg more and more mythical.
 *    		It's more stuff that the Radical Minimalist doesn't benefit from.
 *    		It's more stuff that the Tool Cyborg must remember.
 *    		For The Average Programmer, they are doubly abused.  If they choose not to use it... See Radical Minimalist above.  If they chose to remember it... See Tool Cyborg above.
 *      	It's a sad state of affairs when the very advancement of the state of the art actually creates new problems
 *      		A new interface can help.
 *      
 *      
 *    Subtle problem that I'm not going to fix, but which is possibly worth mentioning:
 *    	The Rename changes the name of the file, so the checkpoint name is invalidated.  This causes a hickup where the tool won't "catch" after the first character after a Rename.  
 *  * 		Would like to do new method completion suggestion.  Looks like too much trouble at the moment.
 *         But here are some things I've found.  
 * 		
 * 				JavaCorrectionProcessor.collectCorrections(context, new IProblemLocation[] { location }, proposals);
 * 	
 * 					Where...
 * 
 * 				ArrayList proposals= new ArrayList();
 * 				IInvocationContext context= new AssistContext(cu, sourceViewer, location.getOffset(), location.getLength(), SharedASTProvider.WAIT_ACTIVE_ONLY);
 * 				ProblemLocation location= new ProblemLocation(position.getOffset(), position.getLength(), javaAnnotation);

				And to apply a proposal:
				
						private void apply(ICompletionProposal p, ITextViewer viewer, int offset, boolean isMultiFix) {
			//Focus needs to be in the text viewer, otherwise linked mode does not work
			dispose();

			IRewriteTarget target= null;
			try {
				IDocument document= viewer.getDocument();

				if (viewer instanceof ITextViewerExtension) {
					ITextViewerExtension extension= (ITextViewerExtension) viewer;
					target= extension.getRewriteTarget();
				}

				if (target != null)
					target.beginCompoundChange();

				if (p instanceof ICompletionProposalExtension2) {
					ICompletionProposalExtension2 e= (ICompletionProposalExtension2) p;
					e.apply(viewer, (char) 0, isMultiFix ? SWT.CONTROL : SWT.NONE, offset);
				} else if (p instanceof ICompletionProposalExtension) {
					ICompletionProposalExtension e= (ICompletionProposalExtension) p;
					e.apply(document, (char) 0, offset);
				} else {
					p.apply(document);
				}

				Point selection= p.getSelection(document);
				if (selection != null) {
					viewer.setSelectedRange(selection.x, selection.y);
					viewer.revealRange(selection.x, selection.y);
				}
			} finally {
				if (target != null)
					target.endCompoundChange();
			}
		}
		
		
		Can't just rename any simple name.  Consider a method that returns an ArrayList.
 * 				You want to change it to return a LinkedList.  When you edit the return type,
 * 				you don't want the tool to try to rename the ArrayList type.
 * 				The question is how to restrict this.  You probably DO want to be able to rename types sometimes.
 * 				Could check that the types are defined by you -- not a library.  But that's tricky.
 * 				Could require that types are renamed at their declaration.  But that's different from rename var.
 * 				And for that matter, we might have an analogous problem with variables.
 * 				
 * 				Maybe the problem is best solved by giving users an ability to cancel a change.
 * 					And perhaps we can have the tool learn from the cancels...  Future work...
		
		
 *
 *
 *
 *
 */




