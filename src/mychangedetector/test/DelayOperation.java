package mychangedetector.test;

public class DelayOperation extends SimulationOperation {
	Simulator simulator;
	int delay;
	
	public DelayOperation(TextAdapter text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	public void setSimulator(Simulator sim)
	{
		simulator = sim;
	}
	
	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	@Override
	public void operate() {
		simulator.setDelay(delay);
	}

}
