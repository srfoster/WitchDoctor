package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.builder.SuperResource;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.Differencer;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;

public class SimpleDifferencer implements Differencer {
	
	String delimiter; 
	
	public SimpleDifferencer(){
		delimiter = "\\n";
	}

	@Override
	public List<Diff> perform(SuperResource left, SuperResource right) {
		String left_string = left.getContents();
		String right_string = right.getContents();
		
        List<String> original = stringToLines(left_string);
        List<String> revised  = stringToLines(right_string);
        
        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        List<Diff> ret = new ArrayList<Diff>();
        
        for (Delta delta: patch.getDeltas()) {
        	
        	
        	if(isDelete(delta))
        	{
        		//If multiple lines were removed, treat it as multiple removals of single lines.

        		int pos = delta.getOriginal().getPosition();
	        	for(Object l : delta.getOriginal().getLines())
	        	{
	        		String line = (String) l;
	        		ArrayList<String> line_array = new ArrayList<String>();
	        		line_array.add(line);
	        		
	        		Chunk c = new Chunk(pos++, line_array);
	        		
	        		Delta new_delta = new DeleteDelta(c,delta.getRevised());
	        		
	        		Diff diff = new SimpleTextDiff(new_delta, original, revised, delimiter);
	        		diff.setParent(this);    		
	            	ret.add(diff);
	        	}
        	}
        	else if(isInsert(delta))
        	{
        		//If multiple lines were inserted, treat it as multiple inserts of single lines.

        		int pos = delta.getRevised().getPosition();
	        	for(Object l : delta.getRevised().getLines())
	        	{
	        		String line = (String) l;
	        		ArrayList<String> line_array = new ArrayList<String>();
	        		line_array.add(line);
	        		
	        		Chunk c = new Chunk(pos++, line_array);
	        		
	        		Delta new_delta = new InsertDelta(delta.getOriginal(),c);
	        		
	        		Diff diff = new SimpleTextDiff(new_delta, original, revised, delimiter);
	        		diff.setParent(this);    		
	            	ret.add(diff);
	        	}
        	}
        	else{
	    		Diff diff = new SimpleTextDiff(delta, original, revised, delimiter);
	    		diff.setParent(this);    		
	        	ret.add(diff);
        	}
        }

        
        
        
        //Now we diff the compiler messages.
        
        List<CompilerMessage> original_messages = left.getCompilerMessages();
        List<CompilerMessage> revised_messages = right.getCompilerMessages();

        Patch messages_patch = DiffUtils.diff(original_messages, revised_messages);
                
        for (Delta delta: messages_patch.getDeltas()) {
    		Diff diff = new SimpleMessageDiff(delta, original, revised, delimiter);
    		diff.setParent(this);    		
        	ret.add(diff);
        }
        
        return ret;
	}

	private List<String> stringToLines(String string){
		return new ArrayList<String>(Arrays.asList(string.split(delimiter)));
	}
	
	
	private boolean isDelete(Delta delta)
	{
		boolean replacement_is_blank = true;
		
		for(Object l : delta.getRevised().getLines())
		{
			String line = (String) l;
			
			
			if(!line.matches("^[\\t\\s\\n]*$"))
			{
				replacement_is_blank = false;
			}
		}
		
		
		return replacement_is_blank || delta.getType() == Delta.TYPE.DELETE;
	}
	
	private boolean isInsert(Delta delta)
	{
		boolean original_is_blank = true;
		
		for(Object l : delta.getOriginal().getLines())
		{
			String line = (String) l;
			
			if(!line.matches("[\\t\\s\\n]*"))
			{
				original_is_blank = false;
			}
		}
		
		
		return original_is_blank || delta.getType() == Delta.TYPE.INSERT;
	}
}
