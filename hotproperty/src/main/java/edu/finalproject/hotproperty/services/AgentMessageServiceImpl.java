package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidMessageParameterException;
import edu.finalproject.hotproperty.repositories.MessageRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentMessageServiceImpl implements AgentMessageService {

  private static final Logger log = LoggerFactory.getLogger(AgentMessageServiceImpl.class);
  private final MessageRepository messageRepository;

  @Autowired
  public AgentMessageServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> getMessagesForAgent(User agent) {
    if (agent == null) {
      log.warn("Attempted to get messages for a null agent.");
      throw new IllegalArgumentException("Agent cannot be null.");
    }
    return messageRepository.findByProperty_Agent(agent);
  }

  @Override
  @Transactional(readOnly = true)
  public Message getMessageByIdForAgent(Long messageId, User agent) {
    if (agent == null) {
      log.warn("Attempted to get message by ID for a null agent.");
      throw new IllegalArgumentException("Agent cannot be null.");
    }
    Message message =
        messageRepository
            .findWithSenderAndPropertyById(messageId)
            .orElseThrow(
                () ->
                    new InvalidMessageParameterException(
                        "Message not found with ID: " + messageId));

    if (message.getProperty() == null
        || message.getProperty().getAgent() == null
        || !message.getProperty().getAgent().getId().equals(agent.getId())) {
      log.warn(
          "Agent {} attempted to access message {} not belonging to them or their properties.",
          agent.getEmail(),
          messageId);
      throw new AccessDeniedException("You are not authorized to view this message.");
    }
    return message;
  }

  @Override
  @Transactional
  public void replyToMessage(Long messageId, String reply, User agent) {
    Message message = getMessageByIdForAgent(messageId, agent);
    if (reply == null || reply.isBlank()) {
      throw new InvalidMessageParameterException("Reply content cannot be empty.");
    }
    message.setReply(reply);
    messageRepository.save(message);
    log.info("Agent {} replied to message {}", agent.getEmail(), messageId);
  }

  @Override
  @Transactional
  public void deleteMessageForAgent(Long messageId, User agent) {
    Message message = getMessageByIdForAgent(messageId, agent);
    messageRepository.deleteById(message.getId());
    log.info("Agent {} deleted message {}", agent.getEmail(), messageId);
  }
}
