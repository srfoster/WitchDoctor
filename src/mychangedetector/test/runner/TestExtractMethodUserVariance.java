package mychangedetector.test.runner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mychangedetector.editors.RefactoringEditor;
import mychangedetector.test.ExtractMethodDiffSimulator;
import mychangedetector.util.EclipseUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.part.FileEditorInput;

public class TestExtractMethodUserVariance {
	Connection conn;
	
	java.sql.Statement stat = null;
	
	int data_set_id = 3;
	
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.compare/plugins/org.eclipse.compare/";
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.jface/";
	String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/current/struts2/";

	//String project_name = "org.eclipse.compare";
	//String project_name = "org.eclipse.jface";
	String project_name = "struts";
	
	boolean by_range_id = false;
	
	int total_batches = 1500;
	int batch_size = 1;
	int offset = 1500;
	
	int range_id = 0; //6058; // Only to be used when by_range_id = true.  Otherwise use the batch/offset stuff.
	
	int experiment_id = 1;
	
	int run_id;
	
	public void execute()
	{
		if(conn != null)
		{
			try {
				conn.close();
				stat.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		(new Thread(
				new Runnable(){
					public void run(){
						doExecute();
					}
				}
		)).start();
	}
	
	private void doExecute()
	{
	
		
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try{
		    conn = DriverManager.getConnection("jdbc:sqlite:/Users/stephenfoster/Documents/workspace/MyChangeDetectorNew/data/data.db");
		    stat = conn.createStatement();
		    conn.setAutoCommit(false);
		    
		    String query = "insert into runs (experiment_id) values ("+experiment_id+")";
		    stat.executeUpdate(query);
		    query = "select last_insert_rowid() as rowid";
		    ResultSet row_id = stat.executeQuery(query);
		    row_id.next();
		    run_id = row_id.getInt("rowid");
		    row_id.close();
		    
		    
		    for(int i = 0; i < total_batches; i++)
		    {
			    String q = null;
		    	
			    if(by_range_id)
			    	q = "select files.name as file_name, files.contents as original_text, target_texts.contents as target_text, * from ranges left join files on ranges.file_id = files.id left join target_texts on target_texts.range_id = ranges.id where ranges.id = " + range_id;
			    else
			    	q =	"select files.contents as original_text, files.name as file_name, target_texts.contents as target_text, * from ((ranges left join files on ranges.file_id = files.id) left join target_texts on target_texts.range_id = ranges.id) where files.data_set_id = "+data_set_id+" limit " + batch_size + " offset " + (i * batch_size + offset);

			    
			    ResultSet results = stat.executeQuery(q);
				
				List<PendingUpdate> updates = new ArrayList<PendingUpdate>();
				
				while(results.next())
				{
					int range_id = results.getInt("range_id");
					
					String full_name = results.getString("file_name");
					String file_name = full_name.replace(prefix,"");
					
					String original_text = results.getString("original_text");
					
					String target_text = results.getString("target_text");
					String snippet = results.getString("snippet");
					
					int seed = target_text.length() + snippet.length();
					
					int snippet_start = results.getInt("offset");
					
					String snippet_test = original_text.substring(snippet_start,snippet.length()+snippet_start);
					
					ExtractMethodDiffSimulator sim = getScore(file_name, target_text, new Random(seed), snippet_start, snippet.length());
					
					String score = sim.getScore();
					double averageRefactoringTime = sim.getAverageRefactoringTime();
					double averageCheckingTime = sim.getAverageCheckingTime();

					
					PendingUpdate p = new PendingUpdate();
					p.range_id = range_id;
					p.score    = score;
					p.refactoring_time     = averageRefactoringTime;
					p.checking_time     = averageCheckingTime;

					updates.add(p);
				}
				
				results.close();
				
				for(PendingUpdate p : updates)
				{
				    query = "insert into results (key,value) values ('Score, Refactoring Time, Checking Time','"+p.score+","+p.refactoring_time+","+p.checking_time+"')";
				    stat.executeUpdate(query);
				    query = "select last_insert_rowid() as rowid";
				    ResultSet row_id2 = stat.executeQuery(query);
				    row_id2.next();
				    int result_id = results.getInt("rowid");
				    row_id2.close();
					
				    query = "insert into tests (range_id, result_id, run_id) values ("+p.range_id+","+result_id+","+run_id+")";
				    stat.executeUpdate(query);

				}
				conn.commit();
		    }
			conn.close();

		} catch (SQLException e){
			e.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	
	private ExtractMethodDiffSimulator getScore(String file_name, final String target_text, Random random, int snippet_start, int snippet_length) {

		
		
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject(project_name);
		
		IPath location = new Path(file_name);
		IFile file = project.getFile(location);
		
		FileEditorInput in = new FileEditorInput(file);
		
		RefactoringEditor.refactoringEditor.setInputAsync(in, new Runnable(){
			public void run(){
				synchronized(TestExtractMethodUserVariance.this)
				{
					TestExtractMethodUserVariance.this.notify();
				}
			}
		});
		
		try {
			synchronized(TestExtractMethodUserVariance.this)
			{
				TestExtractMethodUserVariance.this.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		final ExtractMethodDiffSimulator sim = new ExtractMethodDiffSimulator(){

			
			@Override
			public void begun()
			{
				//setTargetText(target_text);
			}
			
			@Override
			public void finished()
			{
				synchronized(TestExtractMethodUserVariance.this)
				{
					TestExtractMethodUserVariance.this.notify();
				}
			}
		};
		try {
			sim.setBeforeAndAfter(EclipseUtil.convertStreamToString(file.getContents()), target_text, snippet_start, snippet_length);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sim.setRandomGenerator(random);
	
		RefactoringEditor.refactoringEditor.runSimulator(sim);
		
		//Now... wait as the DiffSimulator tries to do the diff.

		try {
			synchronized(TestExtractMethodUserVariance.this)
			{
				TestExtractMethodUserVariance.this.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return sim;
	}

	private class PendingUpdate
	{
		public int range_id;
		public String score;
		public double refactoring_time;
		public double checking_time;
	}
}
