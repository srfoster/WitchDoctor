package mychangedetector.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;

public class CharacterOperation extends SimulationOperation {
	private char character;
	
	int mask = -1;

	
	
	public CharacterOperation(TextAdapter text)
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
		e.keyCode = character;
		
		Event e2 = new Event();
		e2.character = character;
		e2.keyCode = character;

		
		if(mask != -1)
		{
			e.stateMask |= mask;
			e2.stateMask |= mask;
		}

		getAdapter().getText().notifyListeners(SWT.KeyDown,e);
		getAdapter().getText().notifyListeners(SWT.KeyUp,e2);

	}
	
	
	public void setMask(int mask)
	{
		this.mask = mask;
	}
}
