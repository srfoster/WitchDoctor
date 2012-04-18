package mychangedetector.editors;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.builder.SampleBuilder;
import mychangedetector.builder.SuperResource;
import mychangedetector.specifications.Executor;
import mychangedetector.test.ExtractMethodDiffSimulator;
import mychangedetector.test.MyScriptSimulator;
import mychangedetector.test.Simulator;
import mychangedetector.test.TextAdapter;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.internal.resources.File;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaAnnotationIterator;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
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

	private List<Position> grayRanges;
	
	public static StyledText styledText;
	
	private boolean is_paused = false;
	private boolean is_drawing_paused = false;
	
	boolean refactoring_on = false;
	
	Thread sim_thread;
	
	long startTime;
	
	private Integer[] cancellationKeyCodes = {
		SWT.ESC,
		SWT.ARROW_DOWN,
		SWT.ARROW_UP,
		SWT.ARROW_LEFT,
		SWT.ARROW_RIGHT
	};
	
	private boolean should_notify_simulator_of_refactoring;
	
	Simulator simulator = new MyScriptSimulator();
	
	public RefactoringEditor()
	{
		grayRanges = new ArrayList<Position>();
		builder = new SampleBuilder();
		

		refactoringEditor = this;
	}
	
	public SampleBuilder getBuilder()
	{
		return builder;
	}
	
	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		setSourceViewerConfiguration(new RefactoringSourceViewerConfiguration(textTools.getColorManager(), store, this, IJavaPartitions.JAVA_PARTITIONING));	
	}
	
	@Override
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration()
	{
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();

		return new RefactoringSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
	}
	
	@Override
	protected void installSemanticHighlighting()
	{
		
	}

	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);


		try{
			

			builder.setUp(new SuperResource(currentDocument().get(), currentFile().getName()));
			
			//Trigger refactoring when problem markers may have changed.
			IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
			model.addAnnotationModelListener(
			
					new IAnnotationModelListener(){
	
						@Override
						public void modelChanged(IAnnotationModel model) {
							
							List<CompilerMessage> messages = getCompilerMessages(model);
							
							//refactor(messages,RefactoringEditor.this);
						
						}
						
					}
			);
			
			//Code folding makes everything really annoying.  So we'll quick-fix everything by disabling it.
			if(JavaPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED))
			{
				IAction action= getAction("FoldingToggle"); //$NON-NLS-1$
				action.run();
			}
			
			final IDocument doc = currentDocument();
					
			ISourceViewer viewer = getSourceViewer();
					
	        final StyledText text = viewer.getTextWidget();
	        
	                
	        
	        styledText = text;
	       
	        
	        text.addVerifyListener(new VerifyListener(){
	        	public void verifyText(VerifyEvent event)
	        	{
	        		/*
					if(SampleBuilder.builder.isPaused()){
						event.doit = false;
						return;
					}	
					*/
					
					IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
					List<CompilerMessage> messages = getCompilerMessages(model);
		
					refactor(messages,RefactoringEditor.this);
	        	}
	        });
	        
	        
	        text.addVerifyKeyListener(new VerifyKeyListener(){
	        	public void verifyKey(VerifyEvent e)
	        	{
					if ((e.keyCode == SWT.TAB || e.keyCode == SWT.CR) && grayRanges.size() > 0)
					{
						confirmChanges(); //Get rid of the gray
	
						e.doit = false;   //Don't do the usual tab-press action.
					}
					
					if (isCancellation(e.keyCode))
					{
						rejectChanges(); //Get rid of the gray
					}
	        	}
	        });
	        
	        
	        text.addMouseListener(new MouseListener(){
	
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					rejectChanges();
				}
	
				@Override
				public void mouseDown(MouseEvent e) {
					rejectChanges();
				}
	
				@Override
				public void mouseUp(MouseEvent e) {
					rejectChanges();
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
	        text.addLineStyleListener(new LineStyleListener(){
				@Override
				public void lineGetStyle(LineStyleEvent event) {
					// TODO Auto-generated method stub
					
				}
	        });
	        */
	        
	        
	        
	        for(Listener l : text.getListeners(SWT.ALL))
	        {
	        	System.out.println(l);
	        }
	
	        
	
			IOperationHistory o = OperationHistoryFactory.getOperationHistory();
			o.addOperationHistoryListener(new IOperationHistoryListener(){
	
				@Override
				public void historyNotification(OperationHistoryEvent event) {
					
					if(event.getEventType() == OperationHistoryEvent.UNDONE && command_down)
					{
						reset();
					}		
					
				}
			});
			



		
			

			
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	
	public File currentFile()
	{		
		FileEditorInput input = (FileEditorInput) this.getEditorInput();
		
		File file = (File)input.getFile();
		return file;
	}
	
	public IDocument currentDocument()
	{
		IDocumentProvider dp = ((CompilationUnitEditor) this)
		.getDocumentProvider();
		IDocument doc = dp.getDocument(this.getEditorInput());
		
		String text2 = doc.get();
		
		return doc;
	}
	


	private void refactor(final List<CompilerMessage> messages, final IEditorPart editor)
	{
		if(!refactoring_on) return;
		try{
			final Display display = PlatformUI.getWorkbench().getDisplay();

			new Thread(
					new Runnable(){
						public void run(){
							display.asyncExec(new Runnable() {
								public void run(){

									SuperResource super_resource = new SuperResource(getText(), currentFile().getName());
									super_resource.setCompilerMessages(messages);
									
									long checkingStarted = System.nanoTime();
									builder.checkChanges(super_resource);
									long estimatedTime   = System.nanoTime() - checkingStarted;
									
									if(simulator != null)
										((ExtractMethodDiffSimulator)simulator).recordCheckingTime(estimatedTime);

								}
							});
						}
					}
			).start();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	
	public static String getText(){
		if(styledText != null)
			return styledText.getText();
		else
			return null;
	}
	
	public static int getScrollOffset(){
		return styledText.getTopIndex();
	}
	
	public static void setScrollOffset(int offset)
	{
		styledText.setTopIndex(offset);
	}

	
	
	public void grayRange(final int start, final int end)
	{

		Position position = new Position(start,end-start);
		
		getGrayRanges().add(position);
		
		final Display display = PlatformUI.getWorkbench().getDisplay();

		/*
		(new Thread(new Runnable(){
			public void run(){
				display.asyncExec(new Runnable(){
					@Override
					public void run() {
						styledText.setStyleRange(new StyleRange(start,end-start, display.getSystemColor(SWT.COLOR_GRAY), display.getSystemColor(SWT.COLOR_WHITE)));
					}
				});
			}
		})).start();
		*/
		
		display.asyncExec(new Runnable(){
			@Override
			public void run() {
				styledText.setStyleRange(new StyleRange(start,end-start, display.getSystemColor(SWT.COLOR_GRAY), display.getSystemColor(SWT.COLOR_WHITE)));
			}
		});
		
		
		try {
			currentDocument().addPosition(position);
			
			IDocument doc = currentDocument();
			
			
			damageDocument(start,end);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} 
		
		//Mostly for debugging
		/*
		IDocument doc = currentDocument();
		try {
			String grayed_text = doc.get(start, end - start);
			System.out.println();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		*/
	}

	public List<Position> getGrayRanges()
	{
		return grayRanges;
	}
	
	public void reset(){
	
		IDocument doc = currentDocument();
		
		for(Position range : grayRanges)
		{
			currentDocument().removePosition(range);
			range.delete();
		}
			
		grayRanges.clear();
		
		String text = doc.get();
		
		builder.setUp(new SuperResource(currentDocument().get(), currentFile().getName()));
		builder.resetCheckpoints(text);

	
	}

	public boolean offsetWithinGray(int offset) {
		for(Position range : grayRanges)
		{
			if(range.includes(offset))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void confirmChanges()
	{
		IDocument doc = currentDocument();
		
		for(Position range : grayRanges)
		{
			currentDocument().removePosition(range);
			range.delete();
			
			damageDocument(range.offset, range.offset + range.length);


			
		}
			
		grayRanges.clear();
		String text = doc.get();
		builder.resetCheckpoints(text);
	}

	
	public void rejectChanges()
	{
		if(grayRanges.size() == 0)
			return;
		
		IDocument doc = currentDocument();
		
		for(Position range : grayRanges)
		{
			currentDocument().removePosition(range);
			range.delete();
			
			try {
				doc.replace(range.offset,range.length - 1,"");
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		grayRanges.clear();
		String text = doc.get();
		builder.resetCheckpoints(text);
	}
	
	public boolean isCancellation(int keyCode)
	{
		for(Integer i : Arrays.asList(cancellationKeyCodes))
		{
			if(keyCode == i)
				return true;
		}
		
		return false;
	}
	
	public void damageDocument(int start, int end)
	{
		IDocument doc = currentDocument();
		
		try {
			//Manually damage each line, thus triggering the color update. 
			for(int i = start; i < end; i++)
			{
				String character;
	
				character = doc.get(i, 1);
				
				if(character.equals("\n"))
				{
					doc.replace(i,0,""); 
				}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	private List<CompilerMessage> getCompilerMessages(IAnnotationModel model)
	{
		
		int offset = 0;
		int length = currentDocument().get().length();
		
		Iterator parent;
		if (model instanceof IAnnotationModelExtension2)
			parent= ((IAnnotationModelExtension2)model).getAnnotationIterator(offset, length, true, true);
		else
			parent= model.getAnnotationIterator();

		Iterator e= new JavaAnnotationIterator(parent, false);
		
		List<CompilerMessage> messages = new ArrayList<CompilerMessage>();
		
		while (e.hasNext()) {
			Annotation annotation= (Annotation) e.next();
			Position p= model.getPosition(annotation);
			if (p != null && p.overlapsWith(offset,length))
			{
				String message = "";
				if (annotation instanceof IJavaAnnotation && ((IJavaAnnotation) annotation).isProblem() && !annotation.getType().equals("org.eclipse.ui.workbench.texteditor.spelling"))
				{
					messages.add(new CompilerMessage(annotation, p.getOffset(), p.getLength()));
				}
				
			}
		}
		
		return messages;
	}
	
	private List<CompilerMessage> getCompilerMessages()
	{
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		return getCompilerMessages(model);
	}

	public SuperResource getCurrentSuperResource() {
		SuperResource r = new SuperResource(getText(),currentFile().getName());
		
		r.setCompilerMessages(getCompilerMessages());
		
		return r;
	}

	public String getCurrentDocumentName() {
		return currentFile().getName();
	}

	public void pause(){
		if(is_paused)
			return;
		
		is_paused = true;
		
		builder.pause();
		noDraw();
		
		startTime = System.nanoTime();
	}
	
	public void unpause(){
		if(!is_paused)
			return;
		
		is_paused = false;
		
		builder.unpause();
		doDraw();
		
		long estimatedTime = System.nanoTime() - startTime;
		

		if(simulator != null)
			((ExtractMethodDiffSimulator)simulator).recordRefactoringTime(estimatedTime);
	}
	
	public void doDraw()
	{
		if(!is_drawing_paused)
			return;
		
		is_drawing_paused = false;
		styledText.setRedraw(true);
	}
	
	public void noDraw()
	{
		if(is_drawing_paused)
			return;
		
		is_drawing_paused = true;
		styledText.setRedraw(false);
	}
	
	
	public void runSimulator(Simulator sim)
	{
		should_notify_simulator_of_refactoring = false;

		simulator = sim;

		final Display display = PlatformUI.getWorkbench().getDisplay();




		sim_thread = (new Thread(new Runnable(){
			public void run()
			{
				simulator.init(new TextAdapter(styledText,currentDocument()));
				
				display.asyncExec(new Runnable(){

					@Override
					public void run() {
						reset();				
					}
				});
				
				System.out.println();
				
				while(simulator.hasNextTick())
				{

					display.asyncExec(new Runnable() {

						public void run(){
							if(simulator.hasJustBegun())
								simulator.begun();
							
							if(simulator != null)
							{
								if(should_notify_simulator_of_refactoring)
								{
									should_notify_simulator_of_refactoring = false;
									((ExtractMethodDiffSimulator) simulator).checkForAutoCompletion();
								} else {
									simulator.tick();
								}
								
								
								synchronized(simulator){
									simulator.notify();
								}
							}
						}
					});
					
					try {
						synchronized(simulator){
							simulator.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					simulator.sleep();
					
				}
				
				simulator.finished();
			}
		}));
		
		sim_thread.start();
	}
	

	public void toggleRefactoringSupport()
	{
		refactoring_on = !refactoring_on;
	}

	public void setInputAsync(final FileEditorInput in, final Runnable andThen) {

		final Display display = PlatformUI.getWorkbench().getDisplay();

		display.asyncExec(new Runnable() {
			public void run(){
				RefactoringEditor.refactoringEditor.setInput(in);
				andThen.run();
			}
		});
	}

	public void refactoringBegun(Executor executor) {
		should_notify_simulator_of_refactoring = true;
	}
	

}
