package mychangedetector.specifications;

import java.util.List;

import mychangedetector.builder.SampleBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class EclipseExtractMethodExecutor extends Executor {

	private boolean already_executed = false;
	
	public EclipseExtractMethodExecutor(ExtractMethodSpecificationAdapter extractMethodSpecificationAdapter) {
		this.specification = extractMethodSpecificationAdapter;
	}

	@Override
	public boolean isReadyToExecute() {
		if(already_executed)
			return false;
		
		FreeVar statement_var = getSpecification().getProperty("removed_statement_1");
		if(statement_var == null)
			return false;
		ASTNode statement     = statement_var.binding();
		
		FreeVar method_var    = getSpecification().getProperty("method_name");
		if(method_var == null)
			return false;
		ASTNode method        = method_var.binding();
		
		return statement != null && method != null;
	}

	@Override
	public void execute() {
		if(already_executed)
			return;
		already_executed = true;
		final Display display = PlatformUI.getWorkbench().getDisplay();
		
		SampleBuilder.pause(); //Ugly static global stuff.  But otherwise we get changes from the transformation in the stream, which causes weird emergent properties.
		
		
		display.asyncExec(
				  new Runnable() {
				    public void run(){
						String class_name = getSpecification().getCheckpointName();

				    	IFile backup = SampleBuilder.project.getFile("checkpoints/original/"+class_name+".java");
				    	IFile file = SampleBuilder.project.getFile("src/"+class_name+".java");
				    	
				    	//Rolling back
				    	try {
							file.setContents(backup.getContents(),IFile.FORCE,null);
						} catch (CoreException e1) {
							e1.printStackTrace();
						}
				    	
				    	
				    	ICompilationUnit i = JavaCore.createCompilationUnitFrom(file);
				    	
				    	String method_name = ((ExtractMethodSpecificationAdapter)getSpecification()).getMethodName();
				    	
				    	List<FreeVar> statement_vars = getSpecification().getPropertiesByRegex("removed_statement_\\d+");
				    	
				    	ASTNode first_statement = statement_vars.get(0).binding();
				    	
				    	int start_position = first_statement.getStartPosition();
				    	
				    	ASTNode last_statement = statement_vars.get(statement_vars.size()-2).binding(); //Get the second to last one; the last one is always bound to null.
				    	
				    	int end_position = last_statement.getStartPosition() + last_statement.getLength();
				    	
				    	/**
				    	 * Yuck.  In the case where you add the invocation before a block and then remove the block,
				    	 * then the selection we calculate will be wrong after the rollback because the invocation will get
				    	 * rolled back too.
				    	 */
				    	ASTNode call = getSpecification().getProperty("call_name").binding();
				    	
				    	if(call != null)
				    	{
				    		call = call.getParent();
				    		if(call.getStartPosition() <= start_position)
				    		{
				    			int adjust = first_statement.getStartPosition() - call.getStartPosition();
				    			start_position -= adjust;
				    			end_position -= adjust;
				    		}
				    	}
				    	
				    	
				    	int length = end_position - start_position + 1;  //Arrgh.  Why does the +1 fix everything??
				    	
				    	
				    	

				    	ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(i, start_position, length);
				    	
				    	refactoring.setMethodName(method_name);
				    	
				    	try {
				    		RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
				    		System.out.println(status.toString());
							Change change = refactoring.createChange(new NullProgressMonitor());
							
		
						   try {
						     change.initializeValidationData(new NullProgressMonitor());
						 								 
						     if (!change.isEnabled())
						         return;
						     RefactoringStatus valid= change.isValid(new NullProgressMonitor());
						     if (valid.hasFatalError())
						         return;
						     Change undo= change.perform(new NullProgressMonitor());
						     if (undo != null) {
						        undo.initializeValidationData(new NullProgressMonitor());
						        // do something with the undo object
						     }
						   } finally {
							   change.dispose();
						   }
							 
							
						   

						} catch (CoreException e) {
							e.printStackTrace();
						} finally {
							IFile original_checkpoint = SampleBuilder.project.getFile("checkpoints/original/"+class_name+".java");
							IFile recent_checkpoint = SampleBuilder.project.getFile("checkpoints/recent/"+class_name+".java");

							try {
								original_checkpoint.setContents(file.getContents(),IFile.FORCE,null);
								recent_checkpoint.setContents(file.getContents(),IFile.FORCE,null);
							} catch (CoreException e) {
								e.printStackTrace();
							}
							
							//SampleBuilder.reset();
							SampleBuilder.unpause();
						}
						
						

					 }
				  });
		
	
	}

	@Override
	protected void afterRollback(IEditorPart editor, IDocument doc, IFile file) {
		// Might want to refactor this executor to be like extract variable and rename, but one thing at a time...
		
	}
		

}
