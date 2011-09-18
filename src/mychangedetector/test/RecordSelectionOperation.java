package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public class RecordSelectionOperation extends SimulationOperation {
	private int start = -1;
	private boolean done = false;
	
	public RecordSelectionOperation(StyledText text) {
		super(text);
	}

	@Override
	public void operate() {
		if(done)
		{
			throw new RuntimeException("Cannot call operate() on the same RecordSelectionOperation more than once.");
		}
		
		if(start == -1)
		{
			start = getText().getCaretOffset();
			return;
		}
		
		getText().setSelection(start,getText().getCaretOffset());
		
		done = true;
	}

}
