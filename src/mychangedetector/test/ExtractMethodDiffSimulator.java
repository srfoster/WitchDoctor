package mychangedetector.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mychangedetector.util.EclipseUtil;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


public class ExtractMethodDiffSimulator extends ScriptSimulator {
	String target;
	
	
	ScriptBuilder builder = new ScriptBuilder(getAdapter());
	boolean keep_going = true;
	
	List<Long> refactoring_times = new LinkedList<Long>();
	
	List<Long> checking_times = new LinkedList<Long>();

	MyChange new_method;
	MyChange new_method_call;
	List<MyChange> deletions;
	List<MyChange> noise_changes;
	
	ASTNode changed_method;

	
	String score = "False Negative";
	
	Random random = new Random();
	
	float NOISE_AMOUNT = 0.5f;
	
	
	public void setRandomGenerator(Random random)
	{
		this.random = random;
	}


	@Override
	protected List<SimulationOperation> getScript() {
 		builder = new ScriptBuilder(getAdapter());
 		builder.setSimulator(this);
 		
 		
 		Runnable handleDeletions = new Runnable(){
 			public void run(){
		 		for(MyChange deletion : deletions)
		 			handleDelta(deletion);
 			}
 		};
 		
 		Runnable handleNewMethodCall = new Runnable(){
 			public void run(){
 		 		handleDelta(new_method_call);
 			}
 		};
 		
 		
 		Runnable handleNewMethod = new Runnable(){
 			public void run(){
 		 		handleDelta(new_method);
 			}
 		};
 		
 		List<Runnable> things_to_handle = new ArrayList<Runnable>();
 		things_to_handle.add(handleDeletions);
 		things_to_handle.add(handleNewMethod);
 		things_to_handle.add(handleNewMethodCall);
 		
 		Collections.shuffle(things_to_handle,random);
 
 		
 		for(Runnable thing : things_to_handle)
 		{
 			thing.run();
 			builder.delay();
 		}
		
		builder.addOp(new SimulationOperation(getAdapter()){
			public void operate(){
				keep_going = false;
			}
		});
		
		return builder.toScript();
	}
	
	
	private void handleDelta(MyChange p){
		if(p == null)
			return;
		
		if(p.type == "Delete")
		{
			doDelete(p);
		}
		
		if(p.type == "Insert")
		{
			doInsert(p);
		}
		
		if(p.type == "Update")
		{
			doUpdate(p);
		}
		
		if(p.type == "Move")
		{
			doMove(p);
		}
	}
	
	private void doUpdate(MyChange p) {
		keep_going = false;
		score = "None";
	}


	private void doDelete(MyChange p) {

		
		builder.jumpToPositionStart(p.position);

		RecordSelectionOperation op = builder.startSelect();
		builder.jumpToPositionEnd(p.position);
		builder.endSelect(op);
		
		builder.pressDelete();

		
		//fixOffsets(start + length, -length);
	}
	
	private void doInsert(MyChange p)
	{
		builder.jumpToPositionStart(p.position);

		List<String> lines = Arrays.asList(p.node.toString().split("\n"));

		if(p.node instanceof MethodDeclaration) {
			int r = random.nextInt(2);

			if(r == 0)
			{
				insertPerturbedLines(lines);

			} else if (r == 1){ // The way I write a new method, closing the curly first.
				String line = lines.get(0);
				builder.typeString("\n\n\n");
				builder.pressUpArrow();
				builder.pressUpArrow();
				randomInsertionOperation(line.replaceAll("\t","") + "\n");
				builder.pressDownArrow();
				builder.pressDownArrow();
				line = lines.get(lines.size() - 1);
				randomInsertionOperation(line.replaceAll("\t","") + "\n");
				builder.pressUpArrow();
				builder.pressUpArrow();
				builder.pressUpArrow();

				if(lines.size() >= 3)
				{	
					List<String> other_lines = new ArrayList<String>();
				
					for(int i = 1; i < lines.size() - 1; i++)
					{
						line = lines.get(i);
						other_lines.add(line);
					}
					insertPerturbedLines(other_lines);
				}
			}
		} else {
			for(String line : lines)
			{
				builder.typeString("\n");
				builder.pressUpArrow();
				randomInsertionOperation(line.replaceAll("\t","") + ";");
			}
		}
	}
	
	private void insertPerturbedLines(List<String> lines)
	{
		for(String line : lines)
		{
			String noisey_line = perturbLine(line);
			randomInsertionOperation(noisey_line.replaceAll("\t","") + "\n");
		}
	}
	
	private String perturbLine(String line) {
		
		float r = random.nextFloat();
		
		if(r > NOISE_AMOUNT)
		{
			return line;
		}
		
		String new_line = line;
		new_line = new_line.replace(";", "");
		new_line = new_line.replace("}", "");
		new_line = new_line.replace("{", "");
	
		return new_line;
	}


	private void randomInsertionOperation(String line)
	{
		int r = random.nextInt(3);
		
		String padded_line = line;
		
		builder.correctIndentation();
		
		if(r == 0)
			builder.typeString(padded_line);
		else if(r == 1)   //Seems to be working.  Let's remove it if it stops working, though.
		{
			/*
			builder.typeString("\n");
			builder.pressUpArrow();
			builder.pasteString(padded_line);
			builder.pressDownArrow();
			*/
			builder.typeString(padded_line);

		}
		else if(r == 2)
		{
			builder.typeString("\n");
			builder.typeString("\n");
			builder.typeString("\n");
			builder.pressUpArrow();
			builder.pressUpArrow();

			
			builder.typeString(line);
		}
	}
	
	
	private void doMove(MyChange p)
	{
		keep_going = false;
		score = "None";
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
	
	@Override
	public void act(){
		try{
			super.act();
		} catch(Exception e ){
			keep_going = false;
			score = "None";
		}
	}


	public void checkForAutoCompletion() {
		paused = true;
	
		String text = getAdapter().getText().getText();
		
		ASTNode tree = EclipseUtil.convertStringToAst(text);
		
		MethodDeclaration[] methods = MethodQuery.getMethodsFor(text);
		
		boolean has_new_method = false;
		boolean new_method_correct = false;
		boolean new_method_partially_correct = false;

		
		int num_new_methods = 0;
		
		for(MethodDeclaration m : methods)
		{
			String name = m.getName().toString();
			if(name.toString().equals("new_method"))
			{
				num_new_methods++;
				
				has_new_method = true;
				
				String method_string = m.toString().replaceAll("\\s","");
				String intended_method_string = new_method.node.toString().replaceAll("\\s","");
				
				if(method_string.equals(intended_method_string))
				{
					new_method_correct = true;
				} else {
					
					for(MyChange deletion : deletions)
					{
						if(method_string.contains(deletion.node.toString().replaceAll("\\s","")))
						{
							new_method_partially_correct = true;
						}
							
					}
				}
			}
		}
		
		if(num_new_methods == 1)
		{
			if(new_method_correct)
				score = "Correct";
			
			if(new_method_partially_correct)
				score = "Partially Correct";
			
			if(!(new_method_correct || new_method_partially_correct))
				score = "Incorrect";

		}
		
		if(num_new_methods > 1)
		{
			score = "Detected";

		}
		
		
		
		keep_going = false;
	
	}

	public String getScore()
	{
		return score;
	}

	public void setBeforeAndAfter(String original_text, String target_text, int snippet_start, int snippet_length) {
	
		try{
			ASTNode original_tree = EclipseUtil.convertStringToAst(original_text);
			ASTNode target_tree = EclipseUtil.convertStringToAst(target_text);
	
			
			new_method_call = findNewMethodCall(target_tree);
			new_method_call.position = makeNewPosition(snippet_start);
			
			deletions  = findDeletions(original_tree,snippet_start,snippet_length);
	
			new_method = findNewMethod(target_tree);
			
			changed_method = deletions.get(0).node.getParent();
			
			while(!(changed_method instanceof MethodDeclaration))
			{
				changed_method = changed_method.getParent();
			}
			
			int changed_method_end = changed_method.getStartPosition() + changed_method.getLength() + 1;
			Position new_method_position = makeNewPosition(changed_method_end);
			new_method.position = new_method_position;
						
		} catch (Exception e) {
			score = "None";
			keep_going = false;
		}
		
	}



	private Position makeNewPosition(int start)
	{
		Position position = new Position(start); 

		return position;
	};
	
	private Position makeNewPosition(int start, int length)
	{
		Position position = new Position(start, length); 
		
		return position;
	};

	
	private MyChange findNewMethod(ASTNode target_tree)
	{
		MethodDeclaration[] methods = MethodQuery.getMethodsFor(target_tree);
		
		for(MethodDeclaration method : methods)
		{
			if(method.getName().toString().equals("new_method"))
			{
				MyChange change = new MyChange();
				change.node = method;
				change.type = "Insert";
				return change;
			}
		}
		
		return null;
	}
	
	private MyChange findNewMethodCall(ASTNode target_tree)
	{
		final MyChange method_call = new MyChange();
		
		target_tree.accept(new ASTVisitor(){
			public void preVisit(ASTNode node)
			{
				if(node instanceof MethodInvocation && ((MethodInvocation) node).getName().toString().equals("new_method"))
				{
					method_call.node = node;
					method_call.type = "Insert";
				}
			}
		});
		
		return method_call;
	}
	
	private List<MyChange> findDeletions(ASTNode original_tree, final int snippet_start, final int snippet_length)
	{
		final List<MyChange> deleted_nodes = new ArrayList<MyChange>();
		
		original_tree.accept(new ASTVisitor(){
			@Override
			public boolean preVisit2(ASTNode node)
			{
				if(node instanceof Statement && node.getStartPosition() >= snippet_start && (node.getStartPosition() + node.getLength()) <= (snippet_start + snippet_length))
				{
					MyChange deletion = new MyChange();
					deletion.node = node;
					deletion.position = makeNewPosition(node.getStartPosition(),node.getLength());
					deletion.type = "Delete";
					deleted_nodes.add(deletion);
					return false;
				}
				return true;
			}
		});
		
		
		return deleted_nodes;
	}
	
	@Override
	public void init(TextAdapter textAdapter) {
		setText(textAdapter);
		
		IDocument doc = null;
		
		for(MyChange c : deletions)
		{
			doc = getAdapter().getDocument();
			
			try {
				doc.addPosition(c.position);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			doc.addPosition(new_method.position);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			doc.addPosition(new_method_call.position);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void recordRefactoringTime(long time) {
		refactoring_times.add(time);
	}
	
	public double getAverageRefactoringTime()
	{
		long sum = 0;
		for(Long time : refactoring_times)
		{
			sum += time;
		}
		
		return ((double) sum) / refactoring_times.size();
	}
	
	public void recordCheckingTime(long time) {
		checking_times.add(time);
	}
	
	public double getAverageCheckingTime()
	{
		long sum = 0;
		for(Long time : checking_times)
		{
			sum += time;
		}
		
		return ((double) sum) / checking_times.size();
	}
	
	private class MyChange
	{
		public ASTNode node;
		public String type;
		
		public Position position;
	}
}
