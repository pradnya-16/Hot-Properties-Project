package edu.finalproject.hotproperty.repositories;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByProperty(Property property);

  @EntityGraph(attributePaths = {"property", "receiver"})
  List<Message> findBySender(User sender);

  @EntityGraph(attributePaths = {"property", "sender"})
  List<Message> findByProperty_Agent(User agent);

  @EntityGraph(attributePaths = {"sender", "property"})
  Optional<Message> findWithSenderAndPropertyById(Long id);

  @Query(
      "SELECT COUNT(m) FROM Message m WHERE m.receiver = :agent AND (m.reply IS NULL OR m.reply ="
          + " '')")
  long countUnrepliedMessagesForAgent(@Param("agent") User agent);
}
