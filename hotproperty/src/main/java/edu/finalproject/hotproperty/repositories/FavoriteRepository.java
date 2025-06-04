package edu.finalproject.hotproperty.repositories;

import edu.finalproject.hotproperty.entities.Favorite;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  Optional<Favorite> findByBuyerAndProperty(User buyer, Property property);

  List<Favorite> findByBuyer(User buyer);

  boolean existsByBuyerAndProperty(User buyer, Property property);

  @Transactional
  void deleteByBuyerAndProperty(User buyer, Property property);
}
