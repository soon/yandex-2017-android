package com.awesoon.thirdtask.adapter.text;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Implements all methods from TextWatcher allowing user to skip empty methods.
 */
public abstract class TextWatcherAdapter implements TextWatcher {
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  @Override
  public void afterTextChanged(Editable s) {

  }
}
