package mychangedetector.specifications;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import mychangedetector.builder.SampleBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;

public abstract class Executor implements Cloneable {
	SpecificationAdapter specification;
	protected boolean already_executed = false;

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

		final String class_name = specification.getCheckpointName();

		final IFile backup = SampleBuilder.project
		.getFile("checkpoints/original/" + class_name + ".java");
		final IFile file = SampleBuilder.project.getFile("src/" + class_name
				+ ".java");

		final Display display = PlatformUI.getWorkbench().getDisplay();

		SampleBuilder.pause();


		display.asyncExec(new Runnable() {
			public void run() {
				final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
				final IDocumentProvider dp = ((CompilationUnitEditor) editor)
				.getDocumentProvider();
				final IDocument doc = dp.getDocument(editor.getEditorInput());

				doc.addDocumentListener(new IDocumentListener() {
					@Override
					public void documentAboutToBeChanged(DocumentEvent event) {

					}

					@Override
					public void documentChanged(DocumentEvent event) {
						event.getDocument().removeDocumentListener(this);

						display.asyncExec(new Runnable() {
							public void run(){
								afterRollback(editor, doc, file);
							}
						});
					}

				});

				try {
					final String backup_text = convertStreamToString(backup
							.getContents());
					doc.set(backup_text);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

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
				resetCheckpoints(doc, checkpoint_name);
			}

		});

	}

	public void resetCheckpoints(IDocument doc, String check_point_name) {
		resetCheckpointsCallback(doc, check_point_name).run();
	}

	public Runnable resetCheckpointsCallback(final IDocument doc,
			final String check_point_name) {
		String s = doc.get();

		return new Runnable() {

			public void run() {

				String s = doc.get();

				IFile original_checkpoint = SampleBuilder.project
				.getFile("checkpoints/original." + check_point_name
						+ ".java");
				final IFile recent_checkpoint = SampleBuilder.project
				.getFile("checkpoints/recent." + check_point_name
						+ ".java");
				
		        try {
					original_checkpoint.delete(true,false,null);
			        original_checkpoint.create(new ByteArrayInputStream(doc.get().getBytes()), true, null);
			        
			    	recent_checkpoint.delete(true,false,null);
			        recent_checkpoint.create(new ByteArrayInputStream(doc.get().getBytes()), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				

				SampleBuilder.reset();
				SampleBuilder.unpause();

			

			}
		};
	}

	protected abstract void afterRollback(final IEditorPart editor,
			final IDocument doc, IFile file);

}
