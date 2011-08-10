package mychangedetector.editors;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.builder.SampleBuilder;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class RefactoringEditor extends CompilationUnitEditor {

	private ColorManager colorManager;
	
	
	private SampleBuilder builder;
	
	private boolean command_down;

	
	public static RefactoringEditor refactoringEditor;
	
	// We can do better than static variables.  Come now.
	private static List<StyleRange> styleRanges;
	
	public static StyledText styledText;
	
	
	public RefactoringEditor()
	{
		styleRanges = new ArrayList<StyleRange>();
		builder = new SampleBuilder();

		refactoringEditor = this;
	}
	
	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		setSourceViewerConfiguration(new RefactoringSourceViewerConfiguration(textTools.getColorManager(), store, this, IJavaPartitions.JAVA_PARTITIONING));	
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		final IDocument doc = currentDocument();
		
		ISourceViewer viewer = getSourceViewer();
				
        final StyledText text = viewer.getTextWidget();
        
                
        
        styledText = text;
       
        
        text.addVerifyListener(new VerifyListener(){
        	public void verifyText(VerifyEvent event)
        	{

				if(SampleBuilder.builder.isPaused()){
					
					event.doit = false;
					return;
				}	
				
				try{
					final Display display = PlatformUI.getWorkbench().getDisplay();

					new Thread(
							new Runnable(){
								public void run(){
									display.asyncExec(new Runnable() {
										public void run(){
											refactor(currentFile(),doc,RefactoringEditor.this);
										}
									});
								}
							}
					).start();
				}catch(Exception e){
					e.printStackTrace();
				}
				
        	}
        });
        
        text.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {
				
				
				if (e.keyCode == SWT.COMMAND || e.keyCode == SWT.CTRL){
					command_down = true;
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.COMMAND || e.keyCode == SWT.CTRL){
					command_down = false;
				}
			}
        	
        	
        });

        /*        
        new Thread(new Runnable(){
        	public void run(){
        		while(true)
        		{
	        		try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		
					setGrayRanges();
        		}
        	}
        }).start();
        */
        
        
        

		IOperationHistory o = OperationHistoryFactory.getOperationHistory();
		o.addOperationHistoryListener(new IOperationHistoryListener(){

			@Override
			public void historyNotification(OperationHistoryEvent event) {
				
				if(event.getEventType() == OperationHistoryEvent.UNDONE && command_down)
				{
					IDocument doc = currentDocument();
					String text = doc.get();
					builder.resetCheckpoints(text);
					styleRanges.clear();
				}		
				
			}
		});
		

		
	}
	
	private void setGrayRanges()
	{
		if(styleRanges.size() > 0)
		{
			final Display display = PlatformUI.getWorkbench().getDisplay();

			display.asyncExec(new Runnable(){
				public void run(){
		           
					for(StyleRange range : styleRanges)
						styledText.setStyleRange(range);
				}
			});
		}
	}

	
	private File currentFile()
	{		
		FileEditorInput input = (FileEditorInput) this.getEditorInput();
		
		File file = (File)input.getFile();
		return file;
	}
	
	private IDocument currentDocument()
	{
		IDocumentProvider dp = ((CompilationUnitEditor) this)
		.getDocumentProvider();
		IDocument doc = dp.getDocument(this.getEditorInput());
		
		return doc;
	}
	


	private void refactor(final File file, final IDocument doc, final IEditorPart editor)
	{

		editor.doSave(

				new IProgressMonitor(){

					@Override
					public void beginTask(String name, int totalWork) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void done() {
						// TODO Auto-generated method stub
						
						builder.checkChanges(file);
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
					
					
				}
		);
		//file.setContents(new ByteArrayInputStream(doc.get().getBytes()),true,false,null);

	}
	
	
	public static String getText(){
		return styledText.getText();
	}

	
	
	public static void grayRange(int start, int end)
	{
		Display display = PlatformUI.getWorkbench().getDisplay();
		
		StyleRange range = new StyleRange();
     	range.start = start;
     	range.length = end - start;
     	range.fontStyle = SWT.BOLD;
     	range.foreground = display.getSystemColor(SWT.COLOR_GRAY);
     	//styleRange.background = display.getSystemColor(SWT.COLOR_RED);
     	
		styleRanges.add(range);
	}

	
	public void reset(){
	
		IDocument doc = currentDocument();

		String text = doc.get();
		
		builder.resetCheckpoints(text);
	
	}

	public static boolean offsetWithinGray(int offset) {
		for(StyleRange range : styleRanges)
		{
			if(offset >= range.start && offset <= (range.start + range.length))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/*
	public RefactoringEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new RefactoringSourceViewerConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	*/

}
