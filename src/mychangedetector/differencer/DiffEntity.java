package mychangedetector.differencer;


public interface DiffEntity {
	
	public DiffRange getSourceRange();
	
	public String getLabel();
}