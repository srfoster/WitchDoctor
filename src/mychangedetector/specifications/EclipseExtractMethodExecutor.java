package mychangedetector.specifications;

import java.util.List;

import mychangedetector.builder.SuperResource;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;

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
	protected void rollback(final IEditorPart editor,
			final IDocument document) {
		
		
		List<ASTNode> removed_statements = ((ExtractMethodSpecificationAdapter)getSpecification()).getRemovedStatements();
	
		MethodInvocation invocation = ((ExtractMethodSpecificationAdapter)getSpecification()).getMethodInvocation();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(document.get().toCharArray());
		AST ast = invocation.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		ASTNode statements = rewriter.createGroupNode(removed_statements.toArray(new ASTNode[]{}));
		rewriter.replace(invocation.getParent(), statements, null);
		 
		 TextEdit edits = rewriter.rewriteAST(document, null);
		 UndoEdit undo = null;
		 try {
		     undo = edits.apply(document);
		 } catch(MalformedTreeException e) {
		     e.printStackTrace();
		 } catch(BadLocationException e) {
		     e.printStackTrace();
		 }

		 
		 
	}

	@Override
	public void afterRollback(final IEditorPart editor, final IDocument doc) {
		
		String class_name = getSpecification().getCheckpointName();
  
    	ICompilationUnit i = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor, true);
    	
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
    	
		TextSelection new_selection = new TextSelection(doc,start_position,length);
		
		((JavaEditor) editor).getSelectionProvider().setSelection(new_selection);
    	

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
