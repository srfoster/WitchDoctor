package mychangedetector.specifications;

import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorPart;

public class EclipseExtractVariableExecutor extends Executor {
	boolean already_executed = false;;

	ASTNode variable_name, extracted_expression;

	@Override
	public boolean isReadyToExecute() {
		if (already_executed)
			return false;

		FreeVar variable_name_var = getSpecification().getProperty("variable_name");
		if (variable_name_var == null || variable_name_var.isNotBound())
			return false;
		variable_name = variable_name_var.binding();

		FreeVar extracted_expression_var = getSpecification().getProperty(
		"extracted_expression");
		if (extracted_expression_var.isNotBound()
				|| extracted_expression_var == null)
			return false;
		extracted_expression = extracted_expression_var.binding();

		return true;
	}

	@Override
	public void afterRollback(final IEditorPart editor, final IDocument doc,
			final IFile file) {

		final int start_position = extracted_expression.getStartPosition();
		final int length         = extracted_expression.getLength();

		ExtractTempRefactoring refactoring = new ExtractTempRefactoring(
				(ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor, true),
				start_position, length);
		
		try {
			RefactoringStatus init_status = refactoring
			.checkInitialConditions(new NullProgressMonitor());
			
			refactoring.guessTempNames();
			refactoring.setTempName(variable_name.toString());

			RefactoringStatus final_status = refactoring
			.checkFinalConditions(new NullProgressMonitor());
			
			Change change = refactoring
			.createChange(new NullProgressMonitor());
			
			try {
				change.initializeValidationData(new NullProgressMonitor());

				if (!change.isEnabled())
					return;
				RefactoringStatus valid = change
				.isValid(new NullProgressMonitor());
				if (valid.hasFatalError())
					return;
				Change undo = change
				.perform(new IProgressMonitor(){

					@Override
					public void beginTask(String name, int totalWork) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void done() {
						
						TextSelection current_selection = (TextSelection) ((JavaEditor) editor).getSelectionProvider().getSelection();

						int new_start_position = current_selection.getOffset() + current_selection.getLength();
						
						TextSelection t = new TextSelection(new_start_position,0);
						((JavaEditor) editor).getSelectionProvider().setSelection(t);
						
						//rollForward(doc, editor, start_position);
						
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
			System.out.println(e);
		}


	}

}
