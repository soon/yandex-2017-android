package com.awesoon.thirdtask.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.util.ViewUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ColorsFilterAdapter extends RecyclerView.Adapter<ColorsFilterAdapter.ViewHolder> {
  private Context context;
  private List<Integer> colorsData;

  public ColorsFilterAdapter(Context context, Collection<Integer> sortFilters) {
    this.context = context;
    this.colorsData = sortFilters == null ? new ArrayList<Integer>() : new ArrayList<>(sortFilters);
  }

  @Override
  public ColorsFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_color_view, parent, false);

    ColorsFilterAdapter.ViewHolder vh = new ColorsFilterAdapter.ViewHolder(v);

    vh.colorView = ViewUtils.findViewById(v, R.id.color_view, "R.id.color_view");

    return vh;
  }

  @Override
  public void onBindViewHolder(final ColorsFilterAdapter.ViewHolder holder, int position) {
    Integer colorValue = colorsData.get(position);
    if (colorValue != null) {
      holder.colorView.setBackgroundColor(colorValue);
    } else {
      holder.colorView.setBackgroundColor(Color.TRANSPARENT);
    }

    holder.colorView.setClickable(true);
    holder.colorView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        remove(holder.getAdapterPosition());
      }
    });
  }

  @Override
  public int getItemCount() {
    return colorsData.size();
  }

  public void add(int color) {
    colorsData.add(color);
    notifyDataSetChanged();
  }

  public void remove(int position) {
    colorsData.remove(position);
    notifyDataSetChanged();
  }

  public List<Integer> getItems() {
    return colorsData;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }

    private View colorView;
  }
}
