package com.onspota.demo;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onspota.sdk.model.Event;
import com.onspota.sdk.model.PlacesListItem;
import com.onspota.sdk.model.SpotSearchResponse;

import java.util.List;

import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Copyright (c) 2017, Polygon Group
 * Project:     buttons_master
 * Author:      nadia
 * Date:        4/21/2017
 * Description:**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */


public class ContentItem<T> extends AbstractContentItem<ContentItem.ChildViewHolder, T>
        implements ISectionable<ContentItem.ChildViewHolder, IHeader>, IFilterable {

    private static final String TAG = ContentItem.class.getSimpleName();
    /**
     * The header of this item
     */
    IHeader header;

    public ContentItem(String id) {
        super(id);
        setDraggable(false);
        setSelectable(true);
    }

    public ContentItem(String id, T data) {
        this(id);
        setData(data);
    }
    @Override
    public IHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(IHeader header) {
        this.header = header;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.content_item;
    }

    @Override
    public ChildViewHolder createViewHolder(View view, FlexibleAdapter flexibleAdapter) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ChildViewHolder(inflater.inflate(getLayoutRes(), (ViewGroup) view, false), flexibleAdapter);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

        T item = getData();
        String title = "";
        if (item instanceof PlacesListItem)
            title = ((PlacesListItem)item).getName();
        else if (item instanceof SpotSearchResponse)
            title = ((SpotSearchResponse)item).getName();
        else if (item instanceof Event)
            title = ((Event)item).getType().name();

        holder.tvTitle.setText(title);
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static final class ChildViewHolder extends ExpandableViewHolder {

        TextView tvTitle;
        ViewGroup frontView;
        Context mContext;

        public ChildViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            this.mContext = view.getContext();
            frontView = (ViewGroup) ButterKnife.findById(view, R.id.container);
            tvTitle = (TextView) ButterKnife.findById(view, R.id.title);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f);
        }
    }

    @Override
    public String toString() {
        return "ContentItem[" + super.toString() + "]";
    }
}
