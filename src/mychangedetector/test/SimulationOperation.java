package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public abstract class SimulationOperation {
	protected StyledText text;
	
	public SimulationOperation(StyledText text)
	{
		this.text = text;
	}
	
	protected StyledText getText()
	{
		return text;
	}

	public abstract void operate();
}
