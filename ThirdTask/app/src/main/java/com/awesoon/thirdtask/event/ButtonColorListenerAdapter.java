package com.awesoon.thirdtask.event;

public abstract class ButtonColorListenerAdapter implements ButtonColorListener {
  @Override
  public boolean onBeforeColorSelected(int newColor) {
    return true;
  }

  @Override
  public void onColorChanged(int newColor) {

  }

  @Override
  public void onColorSelected(int newColor) {

  }
}
