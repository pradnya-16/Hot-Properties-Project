package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.Property;
import edu.finalproject.hotproperty.entities.User;
import edu.finalproject.hotproperty.exceptions.InvalidMessageParameterException;
import edu.finalproject.hotproperty.repositories.MessageRepository;
import edu.finalproject.hotproperty.repositories.PropertyRepository;
import edu.finalproject.hotproperty.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class BuyerMessageServiceImpl implements BuyerMessageService {

    private static final Logger log = LoggerFactory.getLogger(BuyerMessageServiceImpl.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public BuyerMessageServiceImpl(MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   PropertyRepository propertyRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void sendMessage(User buyer, Long propertyId, String content) throws Exception {
        if (content == null || content.isBlank()) {
            throw new InvalidMessageParameterException("Message content cannot be empty");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new InvalidMessageParameterException("Invalid property ID"));

        User agent = property.getAgent();

        Message message = new Message();
        message.setSender(buyer);
        message.setReceiver(agent);
        message.setProperty(property);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);
        log.info("Message sent from Buyer {} to Agent {} for Property {}", buyer.getId(), agent.getId(), propertyId);
    }

    @Override
    public List<Message> getBuyerMessages(User buyer) {
        return messageRepository.findBySender(buyer);

    }

    @Override
    public void deleteMessage(User buyer, Long messageId) throws Exception {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new InvalidMessageParameterException("Invalid message ID"));

        if (!message.getSender().getId().equals(buyer.getId())) {
            throw new InvalidMessageParameterException("You are not authorized to delete this message.");
        }

        messageRepository.delete(message);
        log.info("Buyer {} deleted message {}", buyer.getId(), messageId);
    }
}