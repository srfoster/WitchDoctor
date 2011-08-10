package mychangedetector.specifications;

import java.io.IOException;

import mychangedetector.builder.SampleBuilder;
import mychangedetector.copyclasses.RenameJavaElementAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameLinkedMode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class EclipseRenameExecutor extends Executor {
	boolean already_executed = false;
	ASTNode old_name, new_name;

	public EclipseRenameExecutor(SpecificationAdapter specificationAdapter) {
		this.specification = specificationAdapter;
	}

	@Override
	public boolean isReadyToExecute() {
		if (already_executed)
			return false;

		FreeVar old_name_var = getSpecification().getProperty("old_name");
		if (old_name_var == null || old_name_var.isNotBound())
			return false;
		old_name = old_name_var.binding();

		FreeVar new_name_var = getSpecification().getProperty("new_name");
		if (new_name_var == null || new_name_var.isNotBound())
			return false;
		new_name = new_name_var.binding();

		return true;
	}

	@Override
	public void afterRollback(final IEditorPart editor, final IDocument doc, final IFile file) {
		SampleBuilder.pause();
		
		final int start_position = old_name.getStartPosition();
		final int length = old_name.getLength();

		TextSelection t = new TextSelection(doc,
				start_position, length);
		
		
		//((TextViewer) ((JavaEditor) editor).getViewer())
		//.addPostSelectionChangedListener(new ISelectionChangedListener() {

		//	@Override
		//	public void selectionChanged(
		//			SelectionChangedEvent event) {
			
		//		((TextViewer) ((JavaEditor) editor)
		//				.getViewer())
		//				.removePostSelectionChangedListener(this);

				RenameJavaElementAction action = new RenameJavaElementAction(
						(JavaEditor) editor);


				action.setCallback(
						new Runnable(){
							public void run(){
								doDiff(doc.get());
								resetCheckpoints(doc);
								SampleBuilder.unpause();
							}
						}
				);
				action.run(t);

				rollForward(doc, editor,start_position);

		//	}

		//});
		
		//((JavaEditor) editor).getSelectionProvider()
		//.setSelection(t);
	}

}
