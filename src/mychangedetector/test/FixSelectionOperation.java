package mychangedetector.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Point;

public class FixSelectionOperation extends SimulationOperation {
	List<Position> positions_to_save = new ArrayList<Position>();
	boolean first_run = true;
	
	public FixSelectionOperation(TextAdapter text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void operate() {
		if(first_run)
			savePositions();
		else
			restorePositions();
	}
	
	private void savePositions()
	{
		IDocument doc = getAdapter().getDocument();
		
		Position[] positions = null;
		try {
			positions= doc.getPositions(IDocument.DEFAULT_CATEGORY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Point selection = getAdapter().getText().getSelection();
	
		for(Position p : positions)
		{
			if(p.getOffset() >= selection.x && p.getOffset() + p.getLength() <= selection.y)
			{
				positions_to_save.add(p);
			}
		}
		
		first_run = false;
	}
	
	private void restorePositions()
	{
		Point selection = getAdapter().getText().getSelection();

		for(Position p : positions_to_save)
		{
			p.setOffset(selection.x);
		}
	}

}
