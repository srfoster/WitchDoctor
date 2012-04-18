package mychangedetector.test;

import org.eclipse.swt.custom.StyledText;

public abstract class Simulator {
	protected int counter = 3;
	
	protected int delay = 50;
	
	protected TextAdapter text;
	boolean has_just_begun = true;
	boolean paused = false;

	public void tick() {
		if(paused)
			return;
		
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
		return delay;
	}
	
	public void setText(TextAdapter text)
	{
		this.text = text;
	}
	
	protected TextAdapter getAdapter()
	{
		return text;
	}
	
	protected abstract void act();
	public abstract boolean hasNextTick();

	public void init(TextAdapter textAdapter) {
		setText(textAdapter);
	}

	public void finished() {
		// Can be used as a callback by whomever created the simulator.
	}

	public void begun() {
		// Can be used as a callback by whomever created the simulator.
	}

	public boolean hasJustBegun() {
		if(has_just_begun == true)
		{
			has_just_begun = false;
			return true;
		}
		return false;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}



}
