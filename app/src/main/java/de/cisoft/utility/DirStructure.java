package de.cisoft.utility;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.R;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import de.cisoft.utility.ftp.FtpClient;

public class DirStructure {
	private List<DirStructure> subdirs;
	private FtpClient ftpClient;
	private String dirName;
	private String path;
	//private DirStructure dirs;
	private ListAdapter adapter;
	private String[] names;
	private DirStructure parent;
	
	public DirStructure(FtpClient ftpClient, String path) {
		this(ftpClient, path, null);

	}
	
	public DirStructure(FtpClient ftpClient, String path, DirStructure parent) {
		this.ftpClient = ftpClient;
		this.path = path;
		String[] parts = path.split("/");
		dirName = parts[parts.length-1];
		subdirs = new LinkedList<DirStructure>();		
		this.parent = parent;
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void init() throws IOException {
		ftpClient.changeDirectory(path);
		String[] subdirectories = ftpClient.getSubdirectories();
		if (subdirectories == null) {
			return;
		}
		for (String subdirectory: subdirectories) {
			subdirs.add(new DirStructure(ftpClient, path + "/" + subdirectory, this));
		}
	}
	
	private String[] getNames() {
		if (names == null) {
			names = new String[subdirs.size()];
			int i = 0;
			for (DirStructure dir : subdirs) {
				names[i++] = dir.dirName;
			}
		}
		return names;
	}

	public ListAdapter getAdapter(Context context) {
		if (adapter == null) {
			adapter = new ArrayAdapter<String>(context, R.layout.simple_list_item_1, getNames());
			
		}
		return adapter;
	}

	public DirStructure getSubdir(int position) {
		
		if (position < 0 || position >= subdirs.size()) {
			return null;
		}
		
		return subdirs.get(position);
	}

	public int getSubdirCount() {
		if (subdirs == null) {
			return 0;
		}
		return subdirs.size();
	}

	public DirStructure getParent() {
		return parent;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return dirName;
	}
	
	public String getBreadCrum() {
		if (parent != null) {
			return parent.getBreadCrum()+"/"+dirName;
		} else {
			return "/"+dirName;
		}
	}
}
