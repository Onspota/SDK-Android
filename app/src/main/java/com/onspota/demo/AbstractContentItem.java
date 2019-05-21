package com.onspota.demo;

import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Copyright (c) 2017, Polygon Group
 * Project:     buttons_master
 * Author:      nadia
 * Date:        4/26/2017
 * Description:
 */
public abstract class AbstractContentItem<VH extends FlexibleViewHolder, T> extends AbstractItem<VH> {

  private T mData;

  public AbstractContentItem(String id) {
    super(id);
  }

  public void setData(T data) {
    mData = data;
  }

  public T getData() {
    return mData;
  }
}
