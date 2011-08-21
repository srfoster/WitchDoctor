package mychangedetector.specifications;

import java.util.List;

import mychangedetector.builder.SampleBuilder;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
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
		
		FreeVar statement_var = getSpecification().getProperty("hook_statement");
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
	public void afterRollback(final IEditorPart editor, final IDocument doc,
			final IFile file) {

		String class_name = getSpecification().getCheckpointName();
  
    	ICompilationUnit i = JavaCore.createCompilationUnitFrom(file);
    	
    	String method_name = ((ExtractMethodSpecificationAdapter)getSpecification()).getMethodName();
    	    	
    	FreeVar first_statement_var = ((ExtractMethodSpecificationAdapter)getSpecification()).getProperty("removed_statement_0");
    	ASTNode first_statement = first_statement_var.binding();
    	int start_position = first_statement.getStartPosition();
    	
    	FreeVar last_statement_var = first_statement_var;
    	ASTNode last_statement = null;
    	int end_position = 0;
    	int count = 1;
    	
    	while(last_statement_var != null)
    	{
    		last_statement = last_statement_var.binding();
    		
    		end_position = last_statement.getStartPosition() + last_statement.getLength();
    		
        	last_statement_var = ((ExtractMethodSpecificationAdapter)getSpecification()).getProperty("removed_statement_"+count++);
    	}
    	
    	
    	ASTNode call = getSpecification().getProperty("method_name").binding();
    	
    	int length = end_position - start_position;
    	
    	
    	

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
		     Change undo= change.perform(new IProgressMonitor(){

				@Override
				public void beginTask(String name, int totalWork) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void done() {
					doDiff(RefactoringEditor.getText());
					resetCheckpoints(doc);
				}

				@Override
				public void internalWorked(double work) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public boolean isCanceled() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setCanceled(boolean value) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void setTaskName(String name) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void subTask(String name) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void worked(int work) {
					// TODO Auto-generated method stub
					
				}
		    	 
		     });
		     if (undo != null) {
		        undo.initializeValidationData(new NullProgressMonitor());
		        // do something with the undo object
		     }
		   } finally {
			   change.dispose();
		   }

		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		

				
	}



}
