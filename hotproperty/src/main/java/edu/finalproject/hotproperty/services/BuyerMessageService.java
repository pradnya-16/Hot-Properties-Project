package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;

public interface BuyerMessageService {
    void sendMessage(User buyer, Long propertyId, String content) throws Exception;
    List<Message> getBuyerMessages(User buyer);
    void deleteMessage(User buyer, Long messageId) throws Exception;
}