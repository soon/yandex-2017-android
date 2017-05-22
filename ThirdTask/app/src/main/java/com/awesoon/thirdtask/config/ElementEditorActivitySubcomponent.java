package com.awesoon.thirdtask.config;


import com.awesoon.thirdtask.activity.ElementEditorActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface ElementEditorActivitySubcomponent extends AndroidInjector<ElementEditorActivity> {
  @Subcomponent.Builder
  abstract class Builder extends AndroidInjector.Builder<ElementEditorActivity> {
  }
}