package mychangedetector.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;

public class ScriptBuilder {
	private Simulator simulator;
	private TextAdapter text;
	private List<SimulationOperation> script;
	
	public ScriptBuilder(TextAdapter text)
	{
		this.text = text;
		this.script = new ArrayList<SimulationOperation>();
	}
	
	public void setSimulator(Simulator sim)
	{
		simulator = sim;
	}
	
	private TextAdapter getText()
	{
		return text;
	}

	protected void typeString(String string) {
		for(int i = 0; i < string.length(); i++)
		{
			char c = string.charAt(i);
			
			CharacterOperation op = new CharacterOperation(getText());
			op.setCharacter(c);
			script.add(op);
			

		}
	}
	
	protected void delay(){
		
		int original_delay = simulator.delay();
		
		DelayOperation delay = new DelayOperation(getText());
		delay.setSimulator(simulator);
		delay.setDelay(1000);
		script.add(delay);
		
		DelayOperation delay2 = new DelayOperation(getText());
		delay2.setSimulator(simulator);
		delay2.setDelay(original_delay);
		script.add(delay2);
		
	}
	
	protected void jumpToMethod(int method) {
		jumpTo(new MethodQuery(getText()));
	}
	
	protected void jumpToMethodBeginning(int method_number) {
		MethodQuery method = new MethodQuery(getText());
		BlockQuery block = new BlockQuery(method);
		BeginningQuery beginning = new BeginningQuery(block);
		
		jumpTo(beginning);
	}
	
	protected void jumpToMethodEnd(int method_number) {
		MethodQuery method = new MethodQuery(getText());
		BlockQuery block = new BlockQuery(method);
		EndQuery end = new EndQuery(block);
		
		jumpTo(end);
	}
	
	protected void jumpToOffset(int offset)
	{
		OffsetQuery query = new OffsetQuery(getText());
		query.setOffset(offset);
		jumpTo(query);
	}

	protected void jumpToPositionStart(Position p)
	{
		PositionQuery pquery = new PositionQuery(getText());
		pquery.setPosition(p);
		jumpTo(pquery);
	}
	
	protected void jumpToPositionStart(final RecordPositionOperation p)
	{
		jumpTo(new JumpQuery(getText()){

			@Override
			public List<ASTNode> resolveToASTNodes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int resolveToOffset(){
				return p.getPosition().getOffset();
			}
			
		});
	}
	
	protected void jumpToPositionEnd(Position p)
	{
		PositionQuery pquery = new PositionQuery(getText());
		pquery.setPosition(p);
		pquery.returnEndAsOffset();
		jumpTo(pquery);
	}
	
	protected RecordPositionOperation saveCurrentPosition(){
		RecordPositionOperation op = new RecordPositionOperation(getText());
		script.add(op);
		return op;
	}
	
	protected RecordSelectionOperation startSelect()
	{
		RecordSelectionOperation op = new RecordSelectionOperation(getText());
		script.add(op);
		return op;
	}
	
	protected void endSelect(RecordSelectionOperation op)
	{
		script.add(op);
	}
	
	protected void pressUpArrow()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.ARROW_UP);
		
		script.add(op);
	}
	
	protected void pressDownArrow()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.ARROW_DOWN);
		
		script.add(op);
	}
	
	
	protected void pressDelete()
	{
		CharacterOperation op = new CharacterOperation(getText());
		op.setCharacter('\b');
		script.add(op);
	}
	
	
	protected void pressTab()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.TAB);
		
		script.add(op);
	}
	
	protected void correctIndentation()
	{
		//I friggin can't get this to work...
		/*
		CharacterOperation op = new CharacterOperation(getText());
		op.setCharacter('I');
		op.setMask(KeyPressOperation.COMMAND);
		script.add(op);
		*/
		

	}
	
	
	protected void jumpTo(JumpQuery query)
	{
		MoveCaretOperation op = new MoveCaretOperation(getText());
		op.setQuery(query);
		script.add(op);
	}

	public List<SimulationOperation> toScript() {
		return script;
	}

	public void addOp(SimulationOperation simulationOperation) {
		script.add(simulationOperation);
	}

	public void pasteString(String line) {
		PasteStringOperation op = new PasteStringOperation(getText());
		op.setString(line);
		script.add(op);
	}

	public void jumpForward(final int length) {
		MoveCaretOperation op = new MoveCaretOperation(getText());
		op.setQuery(new JumpQuery(getText()){

			@Override
			public List<ASTNode> resolveToASTNodes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int resolveToOffset(){
				return getAdapter().getText().getCaretOffset() + length;
			}
			
		});
		script.add(op);
	}
}
