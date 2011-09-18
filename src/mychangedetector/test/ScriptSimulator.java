package mychangedetector.test;

import java.util.List;


public abstract class ScriptSimulator extends Simulator {
	int current_operation = 0;
	SimulationOperation last_op;
	MoveCaretOperation last_move_op;
	List<SimulationOperation> operations;

	boolean first_time = true;
	
	public boolean hasNextTick() {
		if(first_time)
		{
			first_time = false;
			return true; // We need to allow act() to happen at least once, so that some setup can happen inside of display.asyncExec
		}
		return current_operation < operations().size();
	}

	protected void act() {
		List<SimulationOperation> operations = operations();
		if(current_operation >= operations.size() || current_operation < 0)
			return;
		
		last_op = operations.get(current_operation++);
		if(last_op instanceof MoveCaretOperation)
		{
			last_move_op = (MoveCaretOperation) last_op;
		}
		last_op.operate();
	}
	
	protected int updateProgramCounter(int current_operation)
	{
		return current_operation++;
	}

	protected List<SimulationOperation> operations()
	{
		if(operations != null)
			return operations;
		
		List<SimulationOperation> ret = getScript();
		
		operations = ret;
		return ret;
	}
	
	protected abstract List<SimulationOperation> getScript();

	
}
