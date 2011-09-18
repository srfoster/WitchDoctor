package mychangedetector.test;

public class EndQuery extends TransformerQuery {

	public EndQuery(JumpQuery query) {
		super(query);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public int resolveToOffset()
	{
		return super.resolveToOffset() + super.resolveToLength() - 1;
	}

}
