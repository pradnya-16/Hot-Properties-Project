package edu.finalproject.hotproperty.services;

import edu.finalproject.hotproperty.entities.Message;
import edu.finalproject.hotproperty.entities.User;
import java.util.List;

public interface AgentMessageService {
  List<Message> getMessagesForAgent(User agent);

  Message getMessageByIdForAgent(Long messageId, User agent);

  void replyToMessage(Long messageId, String reply, User agent);

  void deleteMessageForAgent(Long messageId, User agent);

  long getUnrepliedMessageCount(User agent);
}
