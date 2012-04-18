package mychangedetector.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;

public class PasteStringOperation extends SimulationOperation {
	String string;

	public PasteStringOperation(TextAdapter text) {
		super(text);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void operate() {
		
		int start = getAdapter().getText().getCaretOffset();
		getAdapter().getText().insert(string);
	}
	
	public void setString(String string)
	{
		this.string = string;
	}
	
	
}
