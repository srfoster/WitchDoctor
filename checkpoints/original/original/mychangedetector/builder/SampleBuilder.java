package mychangedetector.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mychangedetector.builder.SampleBuilder.SampleDeltaVisitor;
import mychangedetector.builder.SampleBuilder.SampleResourceVisitor;
import mychangedetector.change_management.ChangeStream;
import mychangedetector.change_management.ExtendedDistiller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;
import org.evolizer.changedistiller.model.entities.SourceCodeEntity;
import org.evolizer.changedistiller.model.entities.Update;
import org.evolizer.changedistiller.treedifferencing.ITreeEditOperation;


/**
 * 
 * @author stephenfoster
 *
 *
 */

public class SampleBuilder extends IncrementalProjectBuilder {
	
	SampleResourceVisitor resource_visitor = new SampleResourceVisitor();
	SampleDeltaVisitor delta_visitor = new SampleDeltaVisitor();
	
	IFile right, left, original_left;
	
	ChangeStream stream = new ChangeStream();
	
	private boolean first_run = true;
	
	public static IProject project;
	
	public static boolean no_build = false;
		
	public static SampleBuilder builder = null;
	
	public static boolean reset = false;
		
	public static void pause(){
		no_build = true;
	}
	
	public static void unpause(){
		no_build = false;
	}

	public static void reset(){
		reset = true;
	}
	
	public void fullBuild(){
		try{
			fullBuild(null);
		}catch(CoreException ce){
			
		}
	}
	
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		if(no_build)
			return;
		
		try {
			getProject().accept(resource_visitor);
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		if(no_build)
			return;
		// the visitor does the work.
		delta.accept(delta_visitor);
	}
	

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */		
		public boolean visit(IResourceDelta delta) throws CoreException {

			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkChanges(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkChanges(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {

		public boolean visit(IResource resource) {
			checkChanges(resource);
			//return true to continue visiting children.
			return true;
		}
	}


	public static final String BUILDER_ID = "MyChangeDetector.sampleBuilder";


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	public void checkChanges(IResource resource) {
		if(builder == null)
			builder = this;
		
		createFolder("checkpoints");
		createFolder("checkpoints/recent");
		createFolder("checkpoints/original");

		
		if (resource instanceof IFile && resource.getName().endsWith(".java") && !(resource.getName().startsWith("recent.") || resource.getName().startsWith("original."))) {
	        right = (IFile) resource;	        
	        deleteMarkers(right);
	       
	        IPath right_path = right.getFullPath();
	        List<String> path_segments = Arrays.asList(right_path.segments());
	        List<String> relevant_path_segments = path_segments.subList(2,path_segments.size()-1);
	        
	        String path_prefix = "";
	        
	        for(String seg : relevant_path_segments) 
	        {
	        	
		    	path_prefix += "/" + seg;
		    	
	        	createFolder("checkpoints/recent" + path_prefix);
	        	createFolder("checkpoints/original" + path_prefix);
	        }
	        

	        left = getProject().getFile("checkpoints/recent" + path_prefix + "/" + right.getName());
	        project = getProject();
	        
	        original_left = getProject().getFile("checkpoints/original" + path_prefix + "/" + right.getName());


	        if(!left.exists() || first_run)
	        {
	        	try {
					left.create(right.getContents(), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
	        	
	        }
	        

	        if(!original_left.exists() || first_run)
	        {
	        	try {
					original_left.create(right.getContents(), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
	        	
	        }
	        
	        first_run = false;
	        	
	        ExtendedDistiller fDJob = new ExtendedDistiller();
	        fDJob.performDistilling(left, right);
	        
	       
	        List<SourceCodeChange> list = fDJob.getSourceCodeChanges();
	        List<ITreeEditOperation> script = fDJob.getEditScript();
	        

	        if(list != null)
	        {
	        	String left_contents = null;
	        	String right_contents = null;
				try {
					left_contents = convertStreamToString(left.getContents());
					right_contents = convertStreamToString(right.getContents());
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
	        	
		        if(reset)
		        {
		        	stream.clear();
		        	
		        	deleteFile("checkpoints/recent" + path_prefix + "/" + right.getName());
		        	deleteFile("checkpoints/original" + path_prefix + "/" + right.getName());

		        	reset = false;
		        }
	        	


		        try {
					left.delete(true,false,null);
			        left.create(right.getContents(), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
		        stream.addSet(list, new FileVersion(left.getName(), left_contents), new FileVersion(right.getName(), right_contents), script);
		        stream.print();
				
	        } else {
	        	System.out.println("List was null.");
	        }
	        
	        
	        //For the purposes of placing markers, we'll calculate the clean list of changes -- i.e. noops ignored.
	        
	        fDJob = new ExtendedDistiller();
	        fDJob.performDistilling(original_left, right);

	        List<SourceCodeChange> clean_list = fDJob.getSourceCodeChanges();

	        if(clean_list != null)
	        {
		        for(int i = 0; i < clean_list.size(); i++)
		        {
		        	SourceCodeChange scc = clean_list.get(i);
		        
		        	String change_type = scc.getChangeType().toString();
		        	
		        	SourceCodeEntity entity = null;
		        	
		        	if(scc instanceof Update)
		        		entity = ((Update)scc).getNewEntity();
		        	else
		        		entity = scc.getChangedEntity();
		        	
		        	int offset = entity.getSourceRange().getOffset();
		        	int length = entity.getSourceRange().getLength();
		        	
		     
		        	addMarker(right, change_type,offset, offset+length, change_type);
		        }
	        }
	        
		}
	}
	
	private void addMarker(IFile file, String message, int start, int end, String type) {
		try {
			
			String color = "";
			
			if(type == "REMOVING_CLASS_DERIVABILITY")
				color = "green";
			
			if(type == "ADDITIONAL_OBJECT_STATE")
				color = "blue";
			
			if(type == "ADDITIONAL_FUNCTIONALITY")
				color = "red";
			
			if(type == "METHOD_RENAMING")
				color = "purple";
			
			if(type == "ATTRIBUTE_RENAMING")
				color = "yellow";
			
			if(type == "STATEMENT_INSERT")
				color = "green";
			
			if(type == "STATEMENT_UPDATE")
				color = "green";
			
			//System.out.println("Adding " + color + " marker");
			
			InputStream contents = file.getContents();
			String contents_string = "";
			try {
				contents_string = convertStreamToString(contents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String string_being_marked = contents_string.substring(start,end);
			
			
			
			IMarker marker = file.createMarker("MyChangeDetector.highlight_"+color);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
	        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);

		} catch (CoreException e) {
		}
	}

	void deleteMarkers(IFile file)
	{
		try {
			file.deleteMarkers("MyChangeDetector.highlight_green", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_red", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_blue", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_purple", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_yellow", false, IResource.DEPTH_ZERO);

		} catch (CoreException ce) {
		}
	}
	
	public String convertStreamToString(InputStream is) throws IOException {
			/*
			* To convert the InputStream to String we use the
			* Reader.read(char[] buffer) method. We iterate until the
			* Reader return -1 which means there's no more data to
			* read. We use the StringWriter class to produce the string.
			*/
			if (is != null) {
				Writer writer = new StringWriter();
				 
				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					is.close();
				}
				return writer.toString();
			} else {        
				return "";
			}
	}
	
	private void createFolder(String name)
	{
		IFolder folder = getProject().getFolder(name);
		
		if(!folder.exists())
		{
			try {
				folder.create(true,true,null);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	private void deleteFile(String file_name)
	{
		IFile file = getProject().getFile(file_name);
		
		try {
			file.delete(true, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
