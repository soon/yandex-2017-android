package com.awesoon.thirdtask.config;

import android.app.Activity;

import com.awesoon.thirdtask.activity.MainActivity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = MainActivitySubcomponent.class)
abstract class MainActivityModule {
  @Binds
  @IntoMap
  @ActivityKey(MainActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindInjectorFactory(
      MainActivitySubcomponent.Builder builder);
}