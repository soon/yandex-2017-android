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
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.StringUtils;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SysItemsAdapter extends BaseAdapter implements Filterable {
  private final Object dataLock = new Object();

  private final List<SysItem> originalData;
  private final Context context;
  private List<SysItem> filteredData;
  private final List<SysItemRemoveListener> listeners = new ArrayList<>();
  private final int removeDialogMessageResource;
  private final int yesResource;
  private final int noResource;
  private final ItemFilter filter = new ItemFilter();
  private final int viewResource;

  public SysItemsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SysItem> objects,
                         @StringRes int removeDialogMessageResource, @StringRes int yesResource,
                         @StringRes int noResource) {
    this.context = context;
    this.viewResource = resource;
    this.originalData = new ArrayList<>(objects);
    this.filteredData = new ArrayList<>(objects);
    this.removeDialogMessageResource = removeDialogMessageResource;
    this.yesResource = yesResource;
    this.noResource = noResource;
  }

  @Override
  public int getCount() {
    return filteredData.size();
  }

  @Override
  public SysItem getItem(int position) {
    return filteredData.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public Filter getFilter() {
    return filter;
  }

  @NonNull
  @Override
  public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    ViewHolder holder;

    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(viewResource, null);
      holder = new ViewHolder();
      holder.titleTextView = (TextView) convertView.findViewById(R.id.element_title);
      holder.bodyTextView = (TextView) convertView.findViewById(R.id.element_body);
      holder.createdTextView = (TextView) convertView.findViewById(R.id.element_created_time);
      holder.lastEditedTextView = (TextView) convertView.findViewById(R.id.element_last_updated_time);
      holder.lastViewedTextView = (TextView) convertView.findViewById(R.id.element_last_viewed_time);
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

    setDateTime(sysItem.getCreatedTime(), holder.createdTextView);
    setDateTime(sysItem.getLastEditedTime(), holder.lastEditedTextView);
    setDateTime(sysItem.getLastViewedTime(), holder.lastViewedTextView);

    if (sysItem.getId() == null) {
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

  public Context getContext() {
    return context;
  }

  public void addOnSysItemRemoveListener(SysItemRemoveListener listener) {
    listeners.add(listener);
  }

  private void setDateTime(DateTime dateTime, TextView textView) {
    if (dateTime == null) {
      textView.setVisibility(View.GONE);
    } else {
      textView.setVisibility(View.VISIBLE);
      textView.setText(formatDateTime(dateTime));
    }
  }

  private String formatDateTime(DateTime dateTime) {
    Assert.notNull(dateTime, "dateTime must not be null");
    return DateUtils.getRelativeTimeSpanString(getContext(), dateTime, false).toString();
  }

  private void handleElementRemoving(final SysItem sysItem, final int position) {
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            removeItemOnPosition(position);
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

  private void removeItemOnPosition(int position) {
    synchronized (dataLock) {
      Assert.isTrue(position >= 0 && position < filteredData.size(),
          "Unable to remove element with " + position + " position. " +
              "It should be in range [0; " + filteredData.size() + ")");

      SysItem sysItem = filteredData.get(position);
      int originalPosition = originalData.indexOf(sysItem);
      Assert.isTrue(originalPosition >= 0, "Unable to find " + sysItem + " in the original data list");

      filteredData.remove(position);
      originalData.remove(position);
    }
    notifyDataSetChanged();
  }

  public void clear() {
    synchronized (dataLock) {
      filteredData.clear();
      originalData.clear();
    }
    notifyDataSetChanged();
  }

  public void addAll(List<SysItem> items) {
    synchronized (dataLock) {
      filteredData.addAll(items);
      originalData.addAll(items);
    }
    notifyDataSetChanged();
  }

  private class ItemFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      String filterString = constraint.toString().toLowerCase();

      int count = originalData.size();
      List<SysItem> filteredList = new ArrayList<>(count);

      for (SysItem sysItem : originalData) {
        String title = sysItem.getTitle();
        String body = sysItem.getBody();

        if (StringUtils.containsIgnoreCaseTrimmed(title, filterString) ||
            StringUtils.containsIgnoreCaseTrimmed(body, filterString)) {
          filteredList.add(sysItem);
        }
      }

      FilterResults results = new FilterResults();
      results.values = filteredList;
      results.count = filteredList.size();
      return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      filteredData = (List<SysItem>) results.values;
      notifyDataSetChanged();
    }
  }

  private static class ViewHolder {
    private TextView titleTextView;
    private TextView bodyTextView;
    private TextView createdTextView;
    private TextView lastEditedTextView;
    private TextView lastViewedTextView;
    private ElementColorView elementColorView;
    private View removeElementButton;
  }
}
