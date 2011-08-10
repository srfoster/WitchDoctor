package mychangedetector.specifications;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
			IFile file) {



		ASTNode old_statement = getSpecification().getProperty("old_statement")
		.binding();


		final int start_position = extracted_expression.getStartPosition();
		final int length = extracted_expression.getLength();

		TextSelection t = new TextSelection(doc, start_position, length);

		

		((JavaEditor) editor).getSelectionProvider().setSelection(t);

		// ExtractTempRefactoring refactoring = new ExtractTempRefactoring(unit,
		// start_position, length);

		((TextViewer) ((JavaEditor) editor).getViewer())
		.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ExtractTempRefactoring refactoring = new ExtractTempRefactoring(
						SelectionConverter
						.getInputAsCompilationUnit((JavaEditor) editor),
						start_position, length);
				
				
				((TextViewer) ((JavaEditor) editor)
						.getViewer())
						.removePostSelectionChangedListener(this);
				

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
								resetCheckpoints(doc,"Other");
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

				// doc.set(unit.toString());

				// doc.replace(old_statement.getStartPosition(),0,
				// variable_name.toString() + " = " +
				// extracted_expression.toString() + ";\n");
			}
		});

	}

}
