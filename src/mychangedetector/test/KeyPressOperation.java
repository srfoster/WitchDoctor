package mychangedetector.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;

public class KeyPressOperation extends SimulationOperation {
	private int code;
	
	public static final int ARROW_UP = SWT.ARROW_UP;
	public static final int ARROW_DOWN = SWT.ARROW_DOWN;
	public static final int ARROW_LEFT = SWT.ARROW_LEFT;
	public static final int ARROW_RIGHT = SWT.ARROW_RIGHT;
	public static final int DEL = SWT.DEL;
	public static final int TAB = SWT.TAB;

	
	public KeyPressOperation(StyledText text) {
		super(text);
	}

	@Override
	public void operate() {
		Event e = new Event();
		e.keyCode = code;
		
		Event e2 = new Event();
		e2.keyCode = code;


		getText().notifyListeners(SWT.KeyDown,e);
		getText().notifyListeners(SWT.KeyUp,e2);
	}
	
	public void setCode(int code)
	{
		this.code = code;
	}

	
}
