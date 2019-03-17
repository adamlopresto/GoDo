package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DependencyAdapter extends BaseAdapter{

    private final TaskAdapter taskAdapter;
    private Cursor cursor;
    private final Context context;
    private final View.OnClickListener newDependency;
    private final View.OnClickListener pickDependency;

    public DependencyAdapter(@NonNull Context context, View.OnClickListener newDependency, View.OnClickListener pickDependency){
        this.context = context;
        this.newDependency = newDependency;
        this.pickDependency = pickDependency;
        taskAdapter = new TaskAdapter(context, null, false, null); //TODO
        taskAdapter.registerDataSetObserver(new DataSetObserver() {
            /**
             * This method is called when the entire data set has changed,
             * most likely through a call to {@link android.database.Cursor#requery()} on a {@link android.database.Cursor}.
             */
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        });
    }

    public void swapCursor(@Nullable Cursor c){
        cursor = c;
        taskAdapter.swapCursor(c);
    }

    public Cursor getCursor(){
        return cursor;
    }

    @Override
    public int getCount() {
        return cursor == null || cursor.isClosed()
               ? 0
               : cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return cursor != null && cursor.moveToPosition(position)
               ? cursor
               : null;
    }

    @Override
    public long getItemId(int position) {
        return cursor != null && cursor.moveToPosition(position)
               ? cursor.getLong(0)
               : 0L;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)){
            case 0:
                return taskAdapter.getView(position, convertView, parent);
            case 1:
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.dependency_middle, parent, false);
                    convertView.findViewById(R.id.pick_prereq_from_list).setOnClickListener(pickDependency);
                    convertView.findViewById(R.id.create_prereq).setOnClickListener(newDependency);
                }

                ((Toolbar)convertView.findViewById(R.id.next_steps_label))
                        .setTitle(cursor != null && cursor.getCount() == position + 1
                                  ? "No next steps"
                                  : "Next steps"
                        );


                return convertView;
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return cursor != null && cursor.moveToPosition(position) && cursor.getInt(7) == 1
               ? 1
               : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == 0;
    }
}
