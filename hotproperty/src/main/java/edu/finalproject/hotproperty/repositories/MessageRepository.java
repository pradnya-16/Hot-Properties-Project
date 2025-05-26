package edu.finalproject.hotproperty.repositories;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByProperty(Property property);

  List<Message> findBySender(User sender);

  // allows viewing all messages sent on properties specific to agent
  List<Message> findByProperty_Agent(User agent);
}
