package dev.stockflow.core.service;

import dev.stockflow.core.exceprion.MessageException;
import dev.stockflow.core.state.Direction;
import dev.stockflow.core.model.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class MessageListService {

    private Map<String, Message> messagesList = new LinkedHashMap<>();

    public void addMessage(Message message) {
        if (Objects.isNull(message)) {
            throw new MessageException("Сообщение отустствует");
        }
        if (Objects.isNull(message)) {
            throw new MessageException("Переданно пустое сообщение отустствует");
        }

        String messageId = message.getMessageId();
        if (messagesList.containsKey(messageId)) {
            Message existingMessage = messagesList.get(messageId);
            String textNew = message.getTextMessage();
            Direction directionNew = message.getDirection();
            Instant time = message.getTime();
            if (!existingMessage.getTextMessage().equals(textNew) || !existingMessage.getDirection().equals(directionNew) || !existingMessage.getTime().equals(time)) {
                throw new MessageException("id Сообщений совпадают, но данные отличаются");
            }
        } else {
            messagesList.putIfAbsent(messageId, message);
        }
    }

    public Map<String, Message> getMessagesList() {
        return Collections.unmodifiableMap(messagesList);
    }

    @Override
    public String toString() {
        return "MessageListService{" +
                "messagesList=" + messagesList +
                '}';
    }
}
