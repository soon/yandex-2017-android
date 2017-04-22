package com.awesoon.thirdtask.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.util.Assert;

import java.util.List;

public class SysItemsAdapter extends ArrayAdapter<SysItem> {
  public SysItemsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SysItem> objects) {
    super(context, resource, objects);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    ViewHolder holder;

    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.element_view, null);
      holder = new ViewHolder();
      holder.titleTextView = (TextView) convertView.findViewById(R.id.element_title);
      holder.bodyTextView = (TextView) convertView.findViewById(R.id.element_body);
      holder.elementColorView = (ElementColorView)  convertView.findViewById(R.id.element_color);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    SysItem sysItem = getItem(position);
    Assert.notNull(sysItem, "sysItem must not be null");

    holder.titleTextView.setText(sysItem.getTitle());
    holder.bodyTextView.setText(sysItem.getBody());
    holder.elementColorView.setColor(sysItem.getColor());

    return convertView;
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  private static class ViewHolder {
    private TextView titleTextView;
    private TextView bodyTextView;
    private ElementColorView elementColorView;
  }
}
