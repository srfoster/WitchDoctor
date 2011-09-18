package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public class MoveCaretOperation extends SimulationOperation {
	JumpQuery query;
	int move_position;

	public MoveCaretOperation(StyledText text) {
		super(text);
	}

	@Override
	public void operate() {
		move_position = query.resolveToOffset();
		getText().setCaretOffset(move_position);
	}

	public void setQuery(JumpQuery query) {
		this.query = query;
	}
	
	public int movePosition()
	{
		return move_position;
	}

}
