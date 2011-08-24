package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mychangedetector.builder.SuperResource;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.Differencer;
import mychangedetector.util.EclipseUtil;
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
    		SimpleDiff diff = new SimpleDiff(delta, original, revised, delimiter);
    		diff.setParent(this);    		
        	ret.add(diff);
        }

        return ret;
	}

	private List<String> stringToLines(String string){
		return new ArrayList<String>(Arrays.asList(string.split(delimiter)));
	}
	
}
