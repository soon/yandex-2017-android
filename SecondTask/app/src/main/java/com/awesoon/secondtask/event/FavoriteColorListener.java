package com.awesoon.secondtask.event;

public interface FavoriteColorListener {
  /**
   * Called when a user wants to add a color to the list of favorite colors.
   *
   * @param color A new favorite color.
   */
  void addToFavorites(int color);

  /**
   * Called when a user wants to remove a color from the list of favorite colors.
   *
   * @param color A color to be removed from favorites.
   */
  void removeFromFavorites(int color);
}
