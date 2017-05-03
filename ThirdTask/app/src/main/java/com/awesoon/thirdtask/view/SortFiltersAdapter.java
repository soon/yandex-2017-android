package com.awesoon.thirdtask.view;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.awesoon.thirdtask.R;
import com.awesoon.thirdtask.repository.filter.FilteredColumn;
import com.awesoon.thirdtask.repository.filter.SortFilter;
import com.awesoon.thirdtask.util.Assert;
import com.awesoon.thirdtask.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class SortFiltersAdapter extends RecyclerView.Adapter<SortFiltersAdapter.ViewHolder> {
  public static final int ASC_DIRECTION_IDX = 0;
  public static final int DESC_DIRECTION_IDX = 1;

  private Context context;
  private List<SortFilter> sortFilters;
  private List<FilteredColumn> fieldIdents;

  public SortFiltersAdapter(Context context, List<SortFilter> sortFilters) {
    this.context = context;
    this.sortFilters = sortFilters == null ? new ArrayList<SortFilter>() : new ArrayList<>(sortFilters);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_element_view, parent, false);

    ViewHolder vh = new ViewHolder(v);

    vh.sortableFieldsSpinner = ViewUtils.findViewById(v, R.id.sortable_fields_spinner, "R.id.sortable_fields_spinner");
    vh.sortDirectionsSpinner = ViewUtils.findViewById(v, R.id.sort_order_spinner, "R.id.sort_order_spinner");
    vh.removeElementButton = ViewUtils.findViewById(v, R.id.remove_element, "R.id.remove_element");

    initializeSpinnerValues(vh.sortableFieldsSpinner, R.array.sortable_sys_item_fields);
    initializeSpinnerValues(vh.sortDirectionsSpinner, R.array.sort_directions);

    return vh;
  }

  private void initializeSpinnerValues(Spinner s, @ArrayRes int fields) {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        context, fields, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    s.setAdapter(adapter);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final SortFilter sortFilter = sortFilters.get(position);

    holder.sortableFieldsSpinner.setSelection(getFieldIndex(sortFilter.getFilteredColumn()));
    holder.sortableFieldsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
        sortFilter.setFilteredColumn(getFilteredColumn((int) id));
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    holder.sortDirectionsSpinner.setSelection(sortFilter.isAsc() ? ASC_DIRECTION_IDX : DESC_DIRECTION_IDX);
    holder.sortDirectionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
        sortFilter.setAsc(id == ASC_DIRECTION_IDX);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    holder.removeElementButton.setClickable(true);
    holder.removeElementButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        remove(holder.getAdapterPosition());
      }
    });
  }

  private FilteredColumn getFilteredColumn(int index) {
    initFieldIdents();
    return fieldIdents.get(index);
  }

  private int getFieldIndex(@Nullable FilteredColumn filteredIdent) {
    initFieldIdents();

    if (filteredIdent == null) {
      return 0;
    }

    int idx = fieldIdents.indexOf(filteredIdent);
    Assert.isTrue(idx >= 0, "Unable to find filtered column " + filteredIdent);
    return idx;
  }

  private void initFieldIdents() {
    if (fieldIdents == null) {
      fieldIdents = new ArrayList<>();
      String[] fieldIdentNames = context.getResources().getStringArray(R.array.sortable_sys_item_fields_values);
      for (String fieldIdentName : fieldIdentNames) {
        fieldIdents.add(Enum.valueOf(FilteredColumn.class, fieldIdentName));
      }
    }
  }

  @Override
  public int getItemCount() {
    return sortFilters.size();
  }

  public void add(SortFilter sortFilter) {
    sortFilters.add(sortFilter);
    notifyDataSetChanged();
  }

  public void remove(int position) {
    sortFilters.remove(position);
    notifyDataSetChanged();
  }

  public List<SortFilter> getItems() {
    return sortFilters;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }

    private Spinner sortableFieldsSpinner;
    private Spinner sortDirectionsSpinner;
    private View removeElementButton;
  }
}
