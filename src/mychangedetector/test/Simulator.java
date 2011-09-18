package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public abstract class Simulator {
	protected int counter = 3;
	protected StyledText text;

	public void tick() {
		act();
		
		//sleep();
	}
	
	public void sleep()
	{
		try {
			Thread.sleep(delay());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	protected int delay()
	{
		return 250;
	}
	
	private void setText(StyledText text)
	{
		this.text = text;
	}
	
	protected StyledText getText()
	{
		return text;
	}
	
	protected abstract void act();
	public abstract boolean hasNextTick();

	public void init(StyledText text) {
		setText(text);
	}

}
