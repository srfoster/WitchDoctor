package mychangedetector.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mychangedetector.differencer.simple_differencer.SimpleTextDiff;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class DiffSimulator extends ScriptSimulator {
	String target;
	List<Pair> deltas;
	ScriptBuilder builder = new ScriptBuilder(getText());
	boolean keep_going = true;
	
	public void setTargetText(String target)
	{
		this.target = target;
		
		List<String> original = Arrays.asList(getText().getText().split("\n"));
        List<String> revised  = Arrays.asList(target.split("\n"));
        
        Patch patch = DiffUtils.diff(original, revised);

        
        this.deltas = new ArrayList<Pair>();
        
        for(Delta delta : patch.getDeltas())
        {
        	if(isJustWhitespaceChange(delta))
        		continue;
        	
    		SimpleTextDiff diff = new SimpleTextDiff(delta, original, revised, "\\n");
    		
    		Pair p = new Pair();
    		p.diff = diff;
    		p.delta = delta;
    		
    		int start = p.diff.originalToEntity().getSourceRange().getOffset();
    		
    		p.offset = start;
    		
    		
    		deltas.add(p);
        }
        
       // Collections.reverse(deltas);
     }
	

	private boolean isJustWhitespaceChange(Delta delta) {
		if(delta.getType() != Delta.TYPE.CHANGE)
		{
			return false;
		}
		
		List lines_1 = delta.getOriginal().getLines();
		List lines_2 = delta.getRevised().getLines();
		
		for(int i = 0; i < lines_1.size(); i++)
		{
			String first = (String) lines_1.get(i);
			String second = (String) lines_2.get(i);
			
			String first_no_space = first.replaceAll("\\s","");
			String second_no_space = second.replaceAll("\\s","");

			if(!first_no_space.equals(second_no_space))
			{
				return false;
			}
		}
		
		return true;
	}


	@Override
	protected List<SimulationOperation> getScript() {
		List<SimulationOperation> script = new ArrayList<SimulationOperation>();
		
		SimulationOperation myOp = new SimulationOperation(getText()){

			@Override
			public void operate() {
				setTargetText(target); //Does a new diff.
				if(getText().getText().replace("\\s","").equals(target.replace("\\s","")))
				{
					current_operation = -1;
					keep_going = false;
					return;
				}
				
				builder = new ScriptBuilder(getText());  // Calculate the script for the next diff.
				
				final SimulationOperation thisOp = this;
				builder.addOp(thisOp);

				handleDelta(deltas.get(0)); // The script to fix one diff.

				builder.addOp(new SimulationOperation(getText()){ //To loop the script back.

					@Override
					public void operate() {
						current_operation = 0;
						List<SimulationOperation> new_script = new ArrayList<SimulationOperation>();
						
						new_script.add(thisOp);
						operations = new_script;
					}
				});
				
				operations = builder.toScript();
			}
		};

		

		script.add(myOp);
		return script;
		
	}
	
	
	private void handleDelta(Pair p){
		if(p.diff.isDelete())
		{
			doDelete(p);
		}
		
		if(p.diff.isInsert())
		{
			doInsert(p);
		}
		
		if(p.diff.isUpdate())
		{
			doUpdate(p);
		}
	}
	
	private void doUpdate(Pair p) {
		doDelete(p);
		doInsert(p);
	}


	private void doDelete(Pair p) {
		int start = p.offset;
		int length = p.diff.originalToEntity().getSourceRange().getLength();
		
		builder.jumpToOffset(start);
		RecordSelectionOperation op = builder.startSelect();
		builder.jumpToOffset(start+length);
		builder.endSelect(op);
		
		builder.pressDelete();

		
		//fixOffsets(start + length, -length);
	}
	
	private void doInsert(Pair p)
	{
		int start = p.offset;
		


		builder.jumpToOffset(start);
		
		int length = 0;
		for(Object o : p.delta.getRevised().getLines())
		{
			String line = (String) o;
			builder.typeString(line + "\n");
			length += line.length() + 1;
		}
		
		
		//fixOffsets(start, length);
	}
	
	private void fixOffsets(int start, int length)
	{
		for(Pair p : deltas)
		{
			if(p.offset > start)
			{
				p.offset += length;
			}
		}
	}
	
	@Override
	public boolean hasNextTick() {
		if(first_time)
		{
			first_time = false;
			return true; // We need to allow act() to happen at least once, so that some setup can happen inside of display.asyncExec
		}
		return keep_going;
	}

	private class Pair
	{
		public SimpleTextDiff diff;
		public Delta delta;
		public Integer offset;
	}

}
