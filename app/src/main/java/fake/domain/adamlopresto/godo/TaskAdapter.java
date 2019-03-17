package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> implements ListAdapter {

    public static final String[] PROJECTION = {"_id", "task_name",
            "task_notes", "instance_notes", "due_date", "plan_date", "done_date"};
    public static final String SORT = "done_date is not null, " +
            "case when DATE(due_date) <= DATE('now', 'localtime') then due_date || ' 23:59:59' else '9999-99-99' end, " +
            "coalesce(plan_date || ' 23:59:58', DATE('now', 'localtime') || ' 23:59:59'), " +
            "due_date || ' 23:59:59', notification DESC, next_steps DESC, random()";

    @SuppressWarnings ("UnusedDeclaration")
    private static final int ID = 0;
    private static final int TASK_NAME = 1;
    private static final int TASK_NOTES = 2;
    private static final int INSTANCE_NOTES = 3;
    private static final int DUE_DATE = 4;
    private static final int PLAN_DATE = 5;
    private static final int DONE_DATE = 6;
    private final boolean showCheckBox;

    @Nullable
    private View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            toggleSelected(v);
            return true;
        }
    };
    private int choiceMode;
    private ActionMode.Callback multiChoiceModeListener;
    @Nullable
    private ActionMode actionMode;

    private final Context context;
    private Cursor cursor;

    private final SparseBooleanArray selectedItems = new SparseBooleanArray();
    private int numSelected = 0;

    public TaskAdapter(@NonNull Context context, Cursor cursor, boolean showCheckBox, final View.OnClickListener onClickListener) {
        this.context = context;
        this.cursor = cursor;
        this.showCheckBox = showCheckBox;
        if (onClickListener != null) {
            this.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (numSelected > 0)
                        toggleSelected(v);
                    else
                        onClickListener.onClick(v);
                }
            };
        }
    }

    public void toggleSelected(View view) {
        TaskHolder holder = (TaskHolder) view.getTag();
        if (holder != null) {
            int pos = holder.getAdapterPosition();
            boolean wasSelected = selectedItems.get(pos);
            if (wasSelected) {
                selectedItems.delete(pos);
                if (0 == --numSelected && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            } else {
                selectedItems.put(pos, true);
                Log.e("GoDo", "putting true for " + pos);
                Log.e("GoDo", "value is now " + selectedItems.get(pos));
                if (1 == ++numSelected && multiChoiceModeListener != null) {
                    actionMode = view.startActionMode(multiChoiceModeListener);
                }
            }
            holder.itemView.setActivated(!wasSelected);
        }
    }

    public void clearSelection() {
        numSelected = 0;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    private static void setTextViewDate(@NonNull TextView v, String prefix, @Nullable String s, boolean done, boolean overdue,
                                        boolean future) {
        if (!hideView(v, s))
            //noinspection ConstantConditions
            setTextViewInner(v, prefix + Utils.formatShortRelativeDate(s), done, overdue, future);
    }

    private static void setTextView(@NonNull TextView v, CharSequence s, boolean done, boolean overdue,
                                    boolean future) {
        if (!hideView(v, s))
            setTextViewInner(v, s, done, overdue, future);

    }

    private static void setTextViewInner(@NonNull TextView v, CharSequence s, boolean done, boolean overdue,
                                         boolean future) {
        v.setText(s);
        //noinspection IfMayBeConditional
        if (done)
            v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            v.setPaintFlags(v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        v.setTextColor(overdue ? Color.RED :
                       future ? Color.GRAY :
                       Color.BLACK);
    }

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    //@Contract("_, null -> true")
    private static boolean hideView(@NonNull TextView v, CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            v.setVisibility(View.GONE);
            return true;
        }
        v.setVisibility(View.VISIBLE);
        return false;
    }

    private View createView(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
            view.setOnLongClickListener(onLongClickListener);
        }
        return view;
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskHolder(createView(parent), showCheckBox);
    }

    @Override
    public void onBindViewHolder(final TaskHolder holder, int position) {
        cursor.moveToPosition(position);
        boolean done = !cursor.isNull(DONE_DATE);
        String dueDate = cursor.getString(DUE_DATE);
        boolean overdue = Utils.isBeforeNow(dueDate);
        String planDate = cursor.getString(PLAN_DATE);
        boolean future = Utils.isAfterNow(planDate);
        holder.done.setChecked(done);
        holder.id = cursor.getLong(ID);
        setTextView(holder.name, cursor.getString(TASK_NAME), done, overdue, future);
        setTextView(holder.taskNotes, cursor.getString(TASK_NOTES), done, overdue, future);
        setTextView(holder.instanceNotes, cursor.getString(INSTANCE_NOTES), done, overdue, future);
        setTextViewDate(holder.dueDate, "D: ", cursor.getString(DUE_DATE), done, overdue, future);
        setTextViewDate(holder.planDate, "P: ", cursor.getString(PLAN_DATE), done, overdue, future);

        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.id != RecyclerView.NO_ID) {
                    int position = holder.getAdapterPosition();
                    Instance inst = Instance.get(DatabaseHelper.getInstance(v.getContext()), holder.id);
                    boolean done = holder.done.isChecked();
                    inst.updateDone(done);
                    inst.flush();
                    if (done)
                        notifyItemMoved(position, getCount() - 1);
                    else
                        notifyItemMoved(position, 0);
                    //TODO update
                }
            }
        });

        holder.itemView.setActivated(selectedItems.get(position));
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                observer.onChanged();
            }
        });
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        //TODO
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
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
        if (convertView == null) {
            convertView = createView(parent);
            //noinspection ResultOfObjectAllocationIgnored
            new TaskHolder(convertView, showCheckBox);
        }
        bindViewHolder((TaskHolder) convertView.getTag(), position);
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * @return true if this adapter doesn't contain any data.  This is used to determine
     * whether the empty view should be displayed.  A typical implementation will return
     * getCount() == 0 but since getCount() includes the headers and footers, specialized
     * adapters might want a different behavior.
     */
    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    public long[] getCheckedItemIds() {
        long[] ids = new long[numSelected];
        int arrayIndex = 0;
        Log.e("GoDo", "getCheckItemIds: count: " + numSelected);
        for (int item = 0; item < selectedItems.size(); ++item) {
            int pos = selectedItems.keyAt(item);
            Log.e("GoDo", "getCheckItemIds: arrayIndex=" + arrayIndex + ", pos=" + pos);
            if (selectedItems.valueAt(item)) {
                Log.e("GoDo", "getCheckItemIds: item is selected");
                ids[arrayIndex++] = getItemId(pos);
                Log.e("GoDo", "getCheckItemIds: id=" + getItemId(pos));
            }
        }
        return ids;
    }

    public void setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            return cursor.getLong(ID);
        }
        return RecyclerView.NO_ID;
    }

    public void setMultiChoiceModeListener(ActionMode.Callback multiChoiceModeListener) {
        this.multiChoiceModeListener = multiChoiceModeListener;
    }

    public static class TaskHolder extends RecyclerView.ViewHolder {
        public CheckBox done;
        public TextView name;
        public TextView taskNotes;
        public TextView instanceNotes;
        public TextView planDate;
        public TextView dueDate;
        public long id = RecyclerView.NO_ID;

        public TaskHolder(View itemView, boolean showCheckBox) {
            super(itemView);
            itemView.setTag(this);
            done = (CheckBox) itemView.findViewById(R.id.check);
            done.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
            name = (TextView) itemView.findViewById(R.id.task_name);
            taskNotes = (TextView) itemView.findViewById(R.id.task_notes);
            instanceNotes = (TextView) itemView.findViewById(R.id.instance_notes);
            planDate = (TextView) itemView.findViewById(R.id.plan_date);
            dueDate = (TextView) itemView.findViewById(R.id.due_date);

        }
    }
}
