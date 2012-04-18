package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public abstract class SimulationOperation {
	protected TextAdapter text;
	
	public SimulationOperation(TextAdapter text)
	{
		this.text = text;
	}
	
	protected TextAdapter getAdapter()
	{
		return text;
	}

	public abstract void operate();
}
