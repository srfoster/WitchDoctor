package mychangedetector.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;

public class CharacterOperation extends SimulationOperation {
	private char character;
	
	public CharacterOperation(StyledText text)
	{
		super(text);
	}
	
	public void setCharacter(char c)
	{
		character = c;
	}
	
	@Override
	public void operate()
	{
		Event e = new Event();
		e.character = character;
		
		Event e2 = new Event();
		e2.character = character;

		getText().notifyListeners(SWT.KeyDown,e);
		getText().notifyListeners(SWT.KeyUp,e2);

	}
}
