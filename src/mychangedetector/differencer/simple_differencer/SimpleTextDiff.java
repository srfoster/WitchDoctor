package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.Differencer;

import org.eclipse.core.resources.IMarker;

import difflib.Delta;

public class SimpleTextDiff extends SimpleDiff { 


	public SimpleTextDiff(Delta delta, List<String> original,
			List<String> revised, String delimiter) {
		super(delta, original, revised, delimiter);
	}

	public DiffEntity originalToEntity() {
		SimpleTextDiffEntity entity = new SimpleTextDiffEntity(delta.getOriginal(),original, delimiter);
		entity.setParent(parent);
		
		return entity;
	}

	public DiffEntity revisedToEntity() {
		SimpleTextDiffEntity entity = new SimpleTextDiffEntity(delta.getRevised(),revised, delimiter);
		entity.setParent(parent);
		
		return entity;
	}
	
	
	@Override
	public boolean isUpdate() {
		if(delta.getRevised().getLines().size() > 0)
		{
			String s = delta.getRevised().getLines().get(0).toString();
			
			if(s.matches("^\\s*$"))
				return false;
		}
		
		return delta.getType() == Delta.TYPE.CHANGE;
	}
	
	@Override
	public boolean isDelete() {
		if(delta.getRevised().getLines().size() > 0)
		{
			String s = delta.getRevised().getLines().get(0).toString();
			
			if(s.matches("^\\s*$"))
				return true;
		}
		
		return delta.getType() == Delta.TYPE.DELETE;
	}
	



}
