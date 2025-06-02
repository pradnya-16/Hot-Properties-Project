package edu.finalproject.hotproperty.repositories;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
  List<Property> findByAgent(User agent);
  // will need more methods for searching, filtering, etc.
  @EntityGraph(attributePaths = "images")
  List<Property> findAll();

  @EntityGraph(attributePaths = "images")
  Optional<Property> findWithImagesById(Long id);
}
