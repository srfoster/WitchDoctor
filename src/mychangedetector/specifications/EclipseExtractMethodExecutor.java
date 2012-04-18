package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.editors.RefactoringEditor;
import mychangedetector.matching.MatchingASTVisitor;
import mychangedetector.matching.MyASTMatcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;

public class EclipseExtractMethodExecutor extends Executor {

	private List<ASTNode> replacement_nodes;
	
	private boolean already_executed = false;
	
	Position replacement_position = null;
	
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
	protected void rollback(final IEditorPart editor,
			final IDocument document) {
		
		String text = document.get();
		List<ASTNode> removed_statements = ((ExtractMethodSpecificationAdapter)getSpecification()).getRemovedStatements();
	
		MethodInvocation invocation = ((ExtractMethodSpecificationAdapter)getSpecification()).getMethodInvocation();
		
		final ASTNode invocation_statement = invocation.getParent();
		

		
	
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(document.get().toCharArray());
		ASTNode tree = parser.createAST(null);

		MyASTMatcher matcher = new MyASTMatcher();
		ASTNodeDescriptor descriptor = new ASTNodeDescriptor(){
			@Override
			public boolean describes(ASTNode node)
			{
				boolean ret = node.subtreeMatch(new ASTMatcher(), invocation_statement);
				
				return ret;
			}
		};
		descriptor.setBindingName("invocation_statement");
		tree.accept(new MatchingASTVisitor(matcher, descriptor));
		
		ASTNode invocation_statement_current = matcher.getProperty("invocation_statement");
		
		
    	replacement_position = new Position(invocation_statement_current.getStartPosition()-1,invocation_statement_current.getLength()+2);
    	
    	try {
			document.addPosition(replacement_position);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		AST ast = invocation_statement_current.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		replacement_nodes = new ArrayList<ASTNode>();
		
		for(ASTNode n : removed_statements)
		{
			String node_string = n.toString().replace("\n","");
			replacement_nodes.add(rewriter.createStringPlaceholder(node_string,n.getNodeType()));
		}
		
		ASTNode statements = rewriter.createGroupNode(replacement_nodes.toArray(new ASTNode[]{}));
		
		rewriter.replace(invocation_statement_current, statements, null);;
		 
		
		String to_extract = text.substring(replacement_position.getOffset(), replacement_position.getOffset()+ replacement_position.getLength());
		
		 TextEdit edits = rewriter.rewriteAST(document, null);
		 UndoEdit undo = null;
		 try {
		     undo = edits.apply(document);
		 } catch(MalformedTreeException e) {
		     e.printStackTrace();
		 } catch(BadLocationException e) {
		     e.printStackTrace();
		 }

		replacement_position.setOffset(replacement_position.getOffset()+1);
		replacement_position.setLength(replacement_position.getLength()-2);


		 
		 
	}

	@Override
	public void afterRollback(final IEditorPart editor, final IDocument doc) {
		String text = doc.get();
		  
    	
    	String method_name = ((ExtractMethodSpecificationAdapter)getSpecification()).getMethodName();

    	int length = replacement_position.getLength();
    	int start_position = replacement_position.getOffset();
    	
    	String selected_text = text.substring(start_position,start_position + length);
    	System.out.println("Start: " + start_position + " Length: " + length);
    	System.out.println(selected_text);

	//	TextSelection new_selection = new TextSelection(doc,start_position,length);
		
	//	((JavaEditor) editor).getSelectionProvider().setSelection(new_selection);

    	ICompilationUnit i = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor, true);

    	ExtractMethodRefactoring refactoring = null;

    	RefactoringStatus status = null;
    	
		int count = 0;

		try {
			//Using the simulator, for some reason, results in a bad status sometimes (usually when it's running fast.)
			// So I can only assume that there's a nasty concurrency bug causing non-deterministic behaviour.  I
			// don't know if it's my bug or Eclipse's.  I'm just going to spin-wait for now.
			while(status == null || !status.isOK())
			{
		    	refactoring = new ExtractMethodRefactoring(i, start_position, length);
		    	refactoring.setMethodName(method_name);
				status = refactoring.checkInitialConditions(new NullProgressMonitor());
				count++;
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(count == 100)
					break;
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Tried " + count + " times");
		System.out.println(status.toString());
    		
    	try {
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
					TextSelection new_selection = new TextSelection(doc,0,0);
					
					((JavaEditor) editor).getSelectionProvider().setSelection(new_selection);
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
