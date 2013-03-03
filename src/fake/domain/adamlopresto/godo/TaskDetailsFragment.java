package fake.domain.adamlopresto.godo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

public class TaskDetailsFragment extends Fragment {
	
	private long task_id;
	private long instance_id;
	private CheckBox done;
	private EditText taskName;
	private EditText taskNotes;
	private EditText instanceNotes;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null){
			task_id = args.getLong("task_id", -1L);
			instance_id = args.getLong("instance_id", -1L);
		}
		View v = inflater.inflate(R.layout.fragment_task_details, group, false);
		
		done      = (CheckBox) v.findViewById(R.id.check);
		taskName  = (EditText) v.findViewById(R.id.task_name);
		taskNotes = (EditText) v.findViewById(R.id.task_notes);
		instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
		
		fillData();
		
		return v;
	}
	
	private void fillData(){
		Uri uri = Uri.withAppendedPath(GoDoContentProvider.INSTANCES_URI, String.valueOf(instance_id));
		//Cursor c = getContentResolver().query()
	}
}
