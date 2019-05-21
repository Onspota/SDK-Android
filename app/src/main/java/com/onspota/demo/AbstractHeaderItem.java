package com.onspota.demo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Created by alek on 11/10/17.
 */

public abstract class AbstractHeaderItem<GroupItem extends IFlexible>
        extends AbstractItem<AbstractHeaderItem.HeaderViewHolder>
        implements IExpandable<AbstractHeaderItem.HeaderViewHolder, GroupItem>, IHeader<AbstractHeaderItem.HeaderViewHolder> {

    private static final String TAG = AbstractHeaderItem.class.getSimpleName();
    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<GroupItem> mSubItems;

    public AbstractHeaderItem(String id) {
        super(id);
        //We start with header shown and expanded
        setHidden(false);
        setExpanded(false);
        //NOT selectable (otherwise ActionMode will be activated on long click)!
        setSelectable(false);
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<GroupItem> getSubItems() {
        return mSubItems;
    }

    public final boolean hasSubItems() {
        return mSubItems != null && mSubItems.size() > 0;
    }

    public boolean removeSubItem(GroupItem item) {
        return item != null && mSubItems.remove(item);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

    public void addSubItem(GroupItem subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<GroupItem>();
        mSubItems.add(subItem);
    }

    public void addSubItem(int position, GroupItem subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else
            addSubItem(subItem);
    }

    @Override public HeaderViewHolder createViewHolder(View view, FlexibleAdapter flexibleAdapter) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new HeaderViewHolder(inflater.inflate(getLayoutRes(), (ViewGroup)view, false), flexibleAdapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
        } else {
            holder.mTitle.setText(getTitle());
        }
        holder.mRowHandle.setImageResource(isExpanded() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
    }


    @Override
    public String toString() {
        return "AbstractHeaderItem[" + super.toString() + "//SubItems" + mSubItems + "]";
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static class HeaderViewHolder extends ExpandableViewHolder {

        public TextView mTitle;
        public ImageView mRowHandle;

        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = (TextView) ButterKnife.findById(view, R.id.title);
            this.mRowHandle = (ImageView) ButterKnife.findById(view, R.id.row_handle);
            Log.d(TAG, "HeaderViewHolder constructor invoked");
        }

        @Override
        protected boolean isViewExpandableOnClick() {
            Log.d(TAG, "isViewExpandableOnClick");
            return true;
        }

        @Override protected void collapseView(int position) {
            Log.d(TAG, "collapseView");
            super.collapseView(position);
            mRowHandle.setImageResource(R.drawable.ic_arrow_down);
        }

        @Override protected void expandView(int position) {
            Log.d(TAG, "expandView");
            super.expandView(position);
            mRowHandle.setImageResource(R.drawable.ic_arrow_up);
        }
    }
}

