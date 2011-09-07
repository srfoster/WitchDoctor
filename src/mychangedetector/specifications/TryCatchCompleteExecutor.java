package mychangedetector.specifications;

import mychangedetector.builder.SampleBuilder;
import mychangedetector.builder.SuperResource;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.corext.refactoring.surround.SurroundWithTryCatchRefactoring;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class TryCatchCompleteExecutor extends Executor {


	//These isReadyToExecute() methods are all bloated and very similar.  We should just have a list of required bindings and move the checking to the Executor class.
	@Override
	public boolean isReadyToExecute() {
		if (already_executed)
			return false;

		FreeVar error_statement_var = getSpecification().getProperty("error_statement");
		if (error_statement_var == null || error_statement_var.isNotBound())
			return false;
		
		return true;
	}

	@Override
	public void execute() {
		final Display display = PlatformUI.getWorkbench().getDisplay();;

		RefactoringEditor.refactoringEditor.pause();
		
		old_string = RefactoringEditor.getText();
		
		display.asyncExec(new Runnable() {
			public void run(){
				final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
				
				final IDocumentProvider dp = ((CompilationUnitEditor) editor)
				.getDocumentProvider();
				final IDocument doc = dp.getDocument(editor.getEditorInput());
				
				doIt(editor, doc);
			}
		});
	}
	
	private void doIt(IEditorPart editor, final IDocument doc){
		String class_name = getSpecification().getCheckpointName();
		  
    	ICompilationUnit i = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor, true);
    	
		FreeVar error_statement_var = getSpecification().getProperty("error_statement");
    	ASTNode error_statement = error_statement_var.binding();

    	
    	int start_position = error_statement.getStartPosition();
    	int length         = error_statement.getLength();
    	

    	SurroundWithTryCatchRefactoring refactoring = SurroundWithTryCatchRefactoring.create(i,start_position,length);
    	
    	
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
			resetCheckpoints(doc);
			e.printStackTrace();
		} catch (Exception e) {
			resetCheckpoints(doc);
		}
		
	
	}

	@Override
	protected void afterRollback(IEditorPart editor, IDocument doc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void rollback(IEditorPart editor,
			IDocument doc) {
		// TODO Auto-generated method stub
		
	}

}
