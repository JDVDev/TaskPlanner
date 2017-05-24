package com.jdv.retail.taskplanner.listadapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jdv.retail.taskplanner.R;
import com.jdv.retail.taskplanner.notification.NotificationAction;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by TFI on 20-3-2017.
 */

public class NotificationActionRecycleAdapter extends RecyclerView.Adapter<NotificationActionRecycleAdapter.ViewHolder> {

    public interface OnItemClickedCallback {
        void onListItemClicked(int position);
    }

    private RecyclerView mRecyclerView;
    private ArrayList<NotificationAction> actions;
    private OnItemClickedCallback callback;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(LinearLayout v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.textView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationActionRecycleAdapter(RecyclerView rv,
                                            ArrayList<NotificationAction> actions,
                                            OnItemClickedCallback cb) {
        this.mRecyclerView = rv;
        this.actions = actions;
        this.callback = cb;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationActionRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_element, parent, false);
        // set the view's size, margins, paddings and layout parameters
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                if(callback != null){
                    callback.onListItemClicked(itemPosition);
                }
            }
        });
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String contentText = actions.get(position).getActionText();
        holder.mTextView.setText(contentText);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount(){
        return actions.size();
    }

}