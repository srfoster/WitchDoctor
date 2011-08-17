package mychangedetector.differencer.change_distiller;

import mychangedetector.differencer.DiffRange;

import org.evolizer.changedistiller.model.classifiers.SourceRange;

public class ChangeDistillerDiffRange implements DiffRange {
	private SourceRange range;
	
	public ChangeDistillerDiffRange(SourceRange range)
	{
		this.range = range;
	}
	
	/* (non-Javadoc)
	 * @see mychangedetector.differencer.change_distiller.DiffRange#getOffset()
	 */
	@Override
	public int getOffset()
	{
		return range.getOffset();
	}
	
	/* (non-Javadoc)
	 * @see mychangedetector.differencer.change_distiller.DiffRange#getLength()
	 */
	@Override
	public int getLength()
	{
		return range.getLength();
	}
}
