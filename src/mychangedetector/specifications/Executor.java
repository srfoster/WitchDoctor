package mychangedetector.specifications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mychangedetector.builder.SampleBuilder;
import mychangedetector.builder.SuperResource;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.internal.commands.operations.GlobalUndoContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
 



public abstract class Executor implements Cloneable {
	SpecificationAdapter specification;
	protected boolean already_executed = false;
	
	List<IUndoableOperation> wasUndone = new ArrayList<IUndoableOperation>();

	
	//For doing diffs against.
	String old_string;

	public SpecificationAdapter getSpecification() {
		return this.specification;
	}

	public void setSpecification(SpecificationAdapter specification) {
		this.specification = specification;
	}

	public Executor copy(Specification spec_copy) {
		try {
			Executor copy = (Executor) this.clone();
			SpecificationAdapter spec_adapter_copy = getSpecification().copy(
					spec_copy);
			copy.setSpecification(spec_adapter_copy);
			return copy;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public abstract boolean isReadyToExecute();

	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public void execute() {
		already_executed = true;

		final String class_name = RefactoringEditor.refactoringEditor.getCurrentDocumentName();

		//final SuperResource backup = RefactoringEditor.refactoringEditor.getBuilder().getOriginal(class_name);
		
		final IFile file = RefactoringEditor.refactoringEditor.currentFile();
			
		//	= SampleBuilder.project.getFile("src/" + class_name
		//		+ ".java");
		
		

	
		
		final Display display = PlatformUI.getWorkbench().getDisplay();;



		display.asyncExec(new Runnable() {
			public void run() {

				
				
				final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
				
				final IDocumentProvider dp = ((CompilationUnitEditor) editor)
				.getDocumentProvider();
				final IDocument doc = dp.getDocument(editor.getEditorInput());

				old_string = RefactoringEditor.getText();
				
				try {
					RefactoringEditor.refactoringEditor.pause();

					rollback(editor, doc);

					display.asyncExec(new Runnable() {
						public void run(){
							try{
								afterRollback(editor, doc);
							} catch (Exception e) {
								e.printStackTrace();
								resetCheckpoints(doc);
							}
						}
					});
				} catch(Exception e){
					e.printStackTrace();
					resetCheckpoints(doc);
				} finally {
					
				}

			}
		});

	}


	
	protected void doDiff(String new_string){

        List<String> original = stringToLines(old_string);
        List<String> revised  = stringToLines(new_string);
        
        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);
        
        
        for (Delta delta: patch.getDeltas()) {
        	
        	int gray_offset = 0;
        	int gray_length = 0;
        	
        	for(int i = 0; i < delta.getRevised().getPosition(); i++)
        	{
        		String line = revised.get(i);
        		gray_offset += line.length() + 1; //The +1 is for the length of the delimiter.
        	}
        	
        	for(int i = 0; i < delta.getRevised().getLines().size(); i++)
        	{
        		String line = (String) delta.getRevised().getLines().get(i);
        		gray_length += line.length() + 1;
        	}
        	
    		//Tell the refactoring view to make the text gray
        	
    		RefactoringEditor.refactoringEditor.grayRange(gray_offset,gray_offset + gray_length);
    		
            System.out.println("Gray?");
        }
	}
	
	public void resetCheckpointsAfterNextAction(final IDocument doc,
			final String checkpoint_name) {
		doc.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void documentChanged(DocumentEvent event) {
				doc.removeDocumentListener(this);
				resetCheckpoints(doc);
			}

		});

	}

	public void resetCheckpoints(IDocument doc) {
		resetCheckpointsCallback(doc).run();
	}

	public Runnable resetCheckpointsCallback(final IDocument doc) {
		String s = doc.get();

		return new Runnable() {

			public void run() {
				String text = doc.get();
				SampleBuilder.builder.resetCheckpoints(text);
				
			}
		};
	}

	protected abstract void afterRollback(final IEditorPart editor,
			final IDocument doc);
	
	
	//Errr... Do we need start_position here??
	protected void rollForward(IDocument document, IEditorPart editor, int start_position){

		//Clear any current selection
		
		TextSelection current_selection = (TextSelection) ((JavaEditor) editor).getSelectionProvider().getSelection();
		
		TextSelection new_selection = new TextSelection(document, current_selection.getOffset() + current_selection.getLength(), 0);		

		((JavaEditor) editor).getSelectionProvider().setSelection(new_selection);
		
		//Undo stuff
		
		for(IUndoableOperation op : wasUndone)
		{
	
			
			//try {
				//Fuck you, Eclipse.
				
				String parse_me = op.toString();
				
				Pattern p = Pattern.compile("start: (\\d+)"); 
				Matcher m = p.matcher(parse_me);
				boolean b = m.find();

				int start = Integer.parseInt(m.group(1));
				
				
				p = Pattern.compile("end: (\\d+)"); 
				m = p.matcher(parse_me);
				b = m.find();

				int end = Integer.parseInt(m.group(1));
				
				p = Pattern.compile("text: '(.*?)'"); 
				m = p.matcher(parse_me);
				b = m.find();

				String text = m.group(1);
				
				//We have the change info we need, now we need to keep track of the cursor so we can update its position afterward.
				
				Position change_position = new Position(start,0);
				
				try {
					document.addPosition(change_position);
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				try {
					document.replace(start,0,text);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				int updated_start_position = change_position.getOffset();
				

				TextSelection t = new TextSelection(document, updated_start_position, 0);		

				((JavaEditor) editor).getSelectionProvider().setSelection(t);	
				
				
				//op.redo(null,null);
				//history.redoOperation(op,null,null);
		//	} catch (ExecutionException e) {
		//		e.printStackTrace();
		//	}
			
			
			/*
			try {
				document.replace(start_position+1, 0, "BLAH");
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/

		}
		
		wasUndone = new ArrayList<IUndoableOperation>();
	}


	protected abstract void rollback(final IEditorPart editor,
			final IDocument doc);

	
	
	public List<String> stringToLines(String string){
		return new ArrayList<String>(Arrays.asList(string.split("\\s")));
	}

}
