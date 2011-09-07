package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.builder.SuperResource;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.Differencer;
import difflib.Delta;
import difflib.DiffUtils;
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
    		Diff diff = new SimpleTextDiff(delta, original, revised, delimiter);
    		diff.setParent(this);    		
        	ret.add(diff);
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
	
}
