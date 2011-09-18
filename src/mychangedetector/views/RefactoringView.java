package mychangedetector.views;


import mychangedetector.editors.RefactoringEditor;
import mychangedetector.test.data.FindExtractMethodPoints;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;



public class RefactoringView extends ViewPart {
	
	private abstract class MyTask
	{
		String name;
		public MyTask(String name)
		{
			this.name = name;
		}
		public abstract void doTask();
		
		public String toString()
		{
			return name;
		}
	}
	

	public static final String ID = "mychangedetector.views.RefactoringView";

	private TableViewer viewer;

	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new MyTask[] { 
					new MyTask("Turn on Refactoring")
					{
						public void doTask(){
							RefactoringEditor.refactoringEditor.toggleRefactoringSupport();
						}
					}
					
					, 
					new MyTask("Test DiffSimulator"){
						public void doTask(){
							RefactoringEditor.refactoringEditor.runSimulator();
						}
					}
					
					, 
					new MyTask("Find ExtractMethod Points"){
						public void doTask(){
							new FindExtractMethodPoints().execute();
						}
					}
					};
		}
	}
	
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public RefactoringView() {

	}


	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		
		viewer.addDoubleClickListener(
			new IDoubleClickListener(){

				@Override
				public void doubleClick(DoubleClickEvent event) {
					
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					
					MyTask t = (MyTask)selection.getFirstElement();
					t.doTask();
					
					System.out.println();
					
					
				}
				
			}
		);
		
		
		/*
		Button reset = new Button (parent, SWT.PUSH);
		reset.setText ("Run Task");
		reset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				FindExtractMethodPoints.execute();
				


			}
		});
		

		
		
		
		
		Button ref = new Button (parent, SWT.PUSH);
		ref.setText ("Refactor");
		ref.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				//refactor();
				
			}
		});
		*/
		

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MyChangeDetector.viewer");
		


	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}




}