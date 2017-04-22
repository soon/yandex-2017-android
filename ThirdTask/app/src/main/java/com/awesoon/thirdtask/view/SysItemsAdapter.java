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
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class SysItemsAdapter extends ArrayAdapter<SysItem> {
  private final List<SysItem> data;
  private final List<SysItemRemoveListener> listeners = new ArrayList<>();

  public SysItemsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SysItem> objects) {
    super(context, resource, objects);
    this.data = objects;
  }

  @NonNull
  @Override
  public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    ViewHolder holder;

    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.element_view, null);
      holder = new ViewHolder();
      holder.titleTextView = (TextView) convertView.findViewById(R.id.element_title);
      holder.bodyTextView = (TextView) convertView.findViewById(R.id.element_body);
      holder.elementColorView = (ElementColorView) convertView.findViewById(R.id.element_color);
      holder.removeElementButton = convertView.findViewById(R.id.remove_element);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final SysItem sysItem = getItem(position);
    Assert.notNull(sysItem, "sysItem must not be null");

    holder.titleTextView.setText(sysItem.getTitle());
    holder.bodyTextView.setText(sysItem.getBody());
    holder.elementColorView.setColor(sysItem.getColor());
    holder.removeElementButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        data.remove(position);
        notifyDataSetChanged();
        for (SysItemRemoveListener listener : listeners) {
          listener.onSysItemRemove(sysItem);
        }
      }
    });

    return convertView;
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  public void addOnSysItemRemoveListener(SysItemRemoveListener listener) {
    listeners.add(listener);
  }

  private static class ViewHolder {
    private TextView titleTextView;
    private TextView bodyTextView;
    private ElementColorView elementColorView;
    private View removeElementButton;
  }
}
