package com.awesoon.thirdtask.config;

import android.app.Activity;

import com.awesoon.thirdtask.activity.ElementEditorActivity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = ElementEditorActivitySubcomponent.class)
abstract class ElementEditorActivityModule {
  @Binds
  @IntoMap
  @ActivityKey(ElementEditorActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindInjectorFactory(
      ElementEditorActivitySubcomponent.Builder builder);
}