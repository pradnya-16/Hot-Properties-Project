package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Favorite;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;

public interface FavoriteService {
  List<Favorite> getFavoritesByBuyer(User buyer);

  void addFavorite(User buyer, Long propertyId);

  void removeFavorite(User buyer, Long propertyId);

  boolean isPropertyFavorite(User buyer, Long propertyId);
}
