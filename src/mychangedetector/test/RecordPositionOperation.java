package mychangedetector.test;

import org.eclipse.jface.text.Position;

public class RecordPositionOperation extends SimulationOperation {
	private Position position;
	private boolean done = false;
	
	public RecordPositionOperation(TextAdapter text) {
		super(text);
	}

	@Override
	public void operate() {
		if(done)
		{
			throw new RuntimeException("Cannot call operate() on the same RecordSelectionOperation more than once.");
		}
		
		if(position == null)
		{
			int offset = getAdapter().getText().getCaretOffset();
			position = new Position(offset);
			return;
		}
	}
	
	public Position getPosition(){
		return position;
	}

}