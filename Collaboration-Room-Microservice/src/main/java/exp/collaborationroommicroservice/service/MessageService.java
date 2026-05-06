package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.SendMessageRequest;
import exp.collaborationroommicroservice.dto.UpdateMessageFlagsRequest;
import exp.collaborationroommicroservice.entity.LanguageMessage;

import java.util.List;

public interface MessageService {

    LanguageMessage send(Long roomId, Long userId, SendMessageRequest request);

    List<LanguageMessage> getRoomMessages(Long roomId);

    LanguageMessage getById(Long roomId, Long messageId);

    LanguageMessage updateFlags(Long messageId, Long userId, UpdateMessageFlagsRequest request);

    LanguageMessage acceptTranslation(Long roomId, Long messageId, Long userId);

    void delete(Long roomId, Long messageId, Long userId);
}
