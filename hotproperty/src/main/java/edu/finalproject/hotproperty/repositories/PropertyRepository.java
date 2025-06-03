package edu.finalproject.hotproperty.repositories;

import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
  List<Property> findByAgent(User agent);

  @EntityGraph(attributePaths = "images")
  List<Property> findAll();

  @EntityGraph(attributePaths = "images")
  Optional<Property> findWithImagesById(Long id);


  @EntityGraph(attributePaths = "images")
  @Query("""
    SELECT p FROM Property p
    WHERE (:zip IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :zip, '%')))
      AND (:minSqFt IS NULL OR p.size >= :minSqFt)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    ORDER BY p.price ASC
    """)
  List<Property> filterPropertiesOrderByAsc(
          @Param("zip") String zip,
          @Param("minSqFt") Integer minSqFt,
          @Param("minPrice") Double minPrice,
          @Param("maxPrice") Double maxPrice
  );


  @EntityGraph(attributePaths = "images")
  @Query("""
    SELECT p FROM Property p
    WHERE (:zip IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :zip, '%')))
      AND (:minSqFt IS NULL OR p.size >= :minSqFt)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    ORDER BY p.price DESC
    """)
  List<Property> filterPropertiesOrderByDesc(
          @Param("zip") String zip,
          @Param("minSqFt") Integer minSqFt,
          @Param("minPrice") Double minPrice,
          @Param("maxPrice") Double maxPrice
  );

  @EntityGraph(attributePaths = "images")
  @Query("SELECT p FROM Property p WHERE p.agent = :agent")
  List<Property> findWithImagesByAgent(@Param("agent") User agent);

  @EntityGraph(attributePaths = {"agent", "images"})
  @Query("SELECT p FROM Property p WHERE p.id = :id")
  Optional<Property> findWithAgentAndImagesById(@Param("id") Long id);


}
