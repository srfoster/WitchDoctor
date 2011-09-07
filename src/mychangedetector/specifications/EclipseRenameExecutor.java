package mychangedetector.specifications;

import mychangedetector.copyclasses.RenameJavaElementAction;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;

public class EclipseRenameExecutor extends Executor {
	boolean already_executed = false;
	ASTNode old_name, new_name;
	UndoEdit undo;

	int cursor_offset = -1;
	Position cursor_position;

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
	protected void rollback(final IEditorPart editor, final IDocument document) {

		// Renaming the top-level type results in errors when there are
		// compilation problems.
		// Let's implement the most drastic fix.
		if (((CompilationUnit) new_name.getRoot()).getProblems().length > 0) {
			return;
		}
		
		//String s = document.get();

		TextSelection current_selection = (TextSelection) ((JavaEditor) editor)
				.getSelectionProvider().getSelection();

		cursor_offset = current_selection.getOffset();
		
		cursor_position = new Position(cursor_offset,0);
		try {
			document.addPosition(cursor_position);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(document.get().toCharArray());
		AST ast = new_name.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		rewriter.replace(new_name, old_name, null);
		
		


		TextEdit edits = rewriter.rewriteAST(document, null);
		undo = null;
		try {
			undo = edits.apply(document);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
//		s = document.get();

	//	System.out.println();;
	}

	@Override
	protected void rollForward(IDocument document, IEditorPart editor,
			int start_position) {

		int start = old_name.getStartPosition();
		int length = old_name.getLength();
		
		try {
			document.replace(start,length,new_name.toString());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextSelection new_selection = new TextSelection(document,
				cursor_position.getOffset(), 0);

		((JavaEditor) editor).getSelectionProvider().setSelection(new_selection);

	}

	@Override
	public void afterRollback(final IEditorPart editor, final IDocument doc) {

		if (((CompilationUnit) new_name.getRoot()).getProblems().length > 0) {
			resetCheckpoints(doc);
			return;
		}

		RefactoringEditor.refactoringEditor.doDraw();

		final int start_position = old_name.getStartPosition();
		final int length = old_name.getLength();

		TextSelection t = new TextSelection(doc, start_position, length);

		RenameJavaElementAction action = new RenameJavaElementAction(
				(JavaEditor) editor);

		action.setCallback(new Runnable() {
			public void run() {
				resetCheckpoints(doc);
			}
		});

		String top_level_type_name = editor.getEditorInput().getName()
				.split("\\.")[0];
		action.setProhibittedName(top_level_type_name);

		action.run(t);

		rollForward(doc, editor, start_position);

	}

}
