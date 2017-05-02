package com.awesoon.thirdtask.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
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
  private final int removeDialogMessageResource;
  private final int yesResource;
  private final int noResource;

  public SysItemsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SysItem> objects,
                         @StringRes int removeDialogMessageResource, @StringRes int yesResource,
                         @StringRes int noResource) {
    super(context, resource, objects);
    this.data = objects;
    this.removeDialogMessageResource = removeDialogMessageResource;
    this.yesResource = yesResource;
    this.noResource = noResource;
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

    final SysItem item = data.get(position);
    if (item.getId() == null) {
      holder.removeElementButton.setVisibility(View.GONE);
    } else {
      holder.removeElementButton.setVisibility(View.VISIBLE);
      holder.removeElementButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          handleElementRemoving(sysItem, position);
        }
      });
    }

    return convertView;
  }

  private void handleElementRemoving(final SysItem sysItem, final int position) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            removeItemOnPosition(position);
            notifyDataSetChanged();
            for (SysItemRemoveListener listener : listeners) {
              listener.onSysItemRemove(sysItem, position);
            }
            break;

          case DialogInterface.BUTTON_NEGATIVE:
            break;
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMessage(removeDialogMessageResource)
        .setPositiveButton(yesResource, dialogClickListener)
        .setNegativeButton(noResource, dialogClickListener)
        .show();
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  public void addOnSysItemRemoveListener(SysItemRemoveListener listener) {
    listeners.add(listener);
  }

  public void removeItemOnPosition(int position) {
    Assert.isTrue(position >= 0 && position < data.size(),
        "Unable to remove element with " + position + " position. It should be in range [0; " + data.size() + ")");

    data.remove(position);
  }

  private static class ViewHolder {
    private TextView titleTextView;
    private TextView bodyTextView;
    private ElementColorView elementColorView;
    private View removeElementButton;
  }
}
