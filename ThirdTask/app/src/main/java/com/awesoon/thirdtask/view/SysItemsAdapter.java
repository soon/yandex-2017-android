package com.awesoon.thirdtask.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.domain.SysItem;
import com.awesoon.thirdtask.event.SysItemRemoveListener;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.Consumer;
import com.awesoon.thirdtask.util.ViewUtils;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SysItemsAdapter extends RecyclerView.Adapter<SysItemsAdapter.ViewHolder> implements Filterable {
  private final Object dataLock = new Object();

  private final List<SysItem> originalData;
  private final Context context;
  private List<SysItem> filteredData;
  private final List<SysItemRemoveListener> listeners = new ArrayList<>();
  private final int removeDialogMessageResource;
  private final int yesResource;
  private final int noResource;
  private final int viewResource;
  private Consumer<SysItem> onItemClickListener;

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
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(viewResource, parent, false);

    ViewHolder vh = new ViewHolder(v);

    vh.titleTextView = ViewUtils.findViewById(v, R.id.element_title, "R.id.element_title");
    vh.bodyTextView = ViewUtils.findViewById(v, R.id.element_body, "R.id.element_body");
    vh.createdTextView = ViewUtils.findViewById(v, R.id.element_created_time, "R.id.element_created_time");
    vh.lastEditedTextView = ViewUtils.findViewById(v, R.id.element_last_updated_time, "R.id.element_last_updated_time");
    vh.lastViewedTextView = ViewUtils.findViewById(v, R.id.element_last_viewed_time, "R.id.element_last_viewed_time");
    vh.elementColorView = ViewUtils.findViewById(v, R.id.element_color, "R.id.element_color");
    vh.removeElementButton = ViewUtils.findViewById(v, R.id.remove_element, "R.id.remove_element");

    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
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
          handleElementRemoving(sysItem, holder.getAdapterPosition());
        }
      });
    }
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onItemClickListener != null) {
          int position = holder.getAdapterPosition();
          SysItem item = getItem(position);
          onItemClickListener.apply(item);
        }
      }
    });
  }

  public SysItem getItem(int position) {
    return filteredData.get(position);
  }

  @Override
  public int getItemCount() {
    return filteredData.size();
  }

  @Override
  public Filter getFilter() {
    return null;
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
    notifyItemRemoved(position);
  }

  public void clear() {
    int size = filteredData.size();
    synchronized (dataLock) {
      filteredData.clear();
      originalData.clear();
    }
    notifyItemRangeRemoved(0, size);
  }

  public void addAll(List<SysItem> items) {
    int insertPosition = filteredData.size();
    synchronized (dataLock) {
      filteredData.addAll(items);
      originalData.addAll(items);
    }
    notifyItemRangeInserted(insertPosition, items.size());
  }

  public void addOnItemClickListener(Consumer<SysItem> consumer) {
    onItemClickListener = consumer;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
      super(itemView);
    }

    private TextView titleTextView;
    private TextView bodyTextView;
    private TextView createdTextView;
    private TextView lastEditedTextView;
    private TextView lastViewedTextView;
    private ElementColorView elementColorView;
    private View removeElementButton;
  }
}
