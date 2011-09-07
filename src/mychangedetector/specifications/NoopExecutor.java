package mychangedetector.specifications;

import mychangedetector.builder.SuperResource;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

public class NoopExecutor extends Executor {
	public NoopExecutor(SpecificationAdapter spec)
	{
		setSpecification(spec);
	}
	
	@Override
	public boolean isReadyToExecute() {
		return false;
	}

	@Override
	public void execute() {
		System.out.println("Executing NOOP");
	}

	@Override
	protected void afterRollback(IEditorPart editor, IDocument doc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void rollback(IEditorPart editor,
			IDocument doc) {
		// TODO Auto-generated method stub
		
	}

	
}
