package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Favorite;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidFavoriteParameterException;
import edu.finalproject.hotproperty.exceptions.InvalidPropertyParameterException;
import edu.finalproject.hotproperty.repositories.FavoriteRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteServiceImpl implements FavoriteService {

  private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);

  private final FavoriteRepository favoriteRepository;
  private final PropertyRepository propertyRepository;

  @Autowired
  public FavoriteServiceImpl(
      FavoriteRepository favoriteRepository, PropertyRepository propertyRepository) {
    this.favoriteRepository = favoriteRepository;
    this.propertyRepository = propertyRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Favorite> getFavoritesByBuyer(User buyer) {
    if (buyer == null) {
      log.warn("Attempted to get favorites for a null buyer.");
      throw new UsernameNotFoundException("Buyer cannot be null.");
    }
    List<Favorite> favorites = favoriteRepository.findByBuyer(buyer);
    return favorites;
  }

  @Override
  @Transactional
  public void addFavorite(User buyer, Long propertyId) {
    if (buyer == null) {
      throw new UsernameNotFoundException("Buyer cannot be null for adding favorite.");
    }
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Attempted to add non-existent property ID {} to favorites by user {}",
                      propertyId,
                      buyer.getEmail());
                  return new InvalidPropertyParameterException(
                      "Property not found with ID: " + propertyId);
                });

    if (favoriteRepository.existsByBuyerAndProperty(buyer, property)) {
      log.info("Property ID {} is already in favorites for user {}.", propertyId, buyer.getEmail());
      throw new InvalidFavoriteParameterException("Property is already in favorites.");
    }

    Favorite favorite = new Favorite(buyer, property);
    favoriteRepository.save(favorite);
    log.info("Property ID {} added to favorites for user {}.", propertyId, buyer.getEmail());
  }

  @Override
  @Transactional
  public void removeFavorite(User buyer, Long propertyId) {
    if (buyer == null) {
      throw new UsernameNotFoundException("Buyer cannot be null for removing favorite.");
    }
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Attempted to remove non-existent property ID {} from favorites by user {}",
                      propertyId,
                      buyer.getEmail());
                  return new InvalidPropertyParameterException(
                      "Property not found with ID: " + propertyId);
                });

    if (!favoriteRepository.existsByBuyerAndProperty(buyer, property)) {
      log.info(
          "Property ID {} was not in favorites for user {} to remove.",
          propertyId,
          buyer.getEmail());
      throw new InvalidFavoriteParameterException("Property is not in favorites to remove.");
    }
    favoriteRepository.deleteByBuyerAndProperty(buyer, property);
    log.info("Property ID {} removed from favorites for user {}.", propertyId, buyer.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isPropertyFavorite(User buyer, Long propertyId) {
    if (buyer == null) {
      return false;
    }
    Property property = propertyRepository.findById(propertyId).orElse(null);
    if (property == null) {
      log.debug(
          "Property ID {} not found when checking favorite status for user {}.",
          propertyId,
          buyer.getEmail());
      return false;
    }
    return favoriteRepository.existsByBuyerAndProperty(buyer, property);
  }
}
