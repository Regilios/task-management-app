package dev.stockflow.core.model;

import dev.stockflow.core.exceprion.MessageException;
import dev.stockflow.core.state.Direction;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class Message {
    private final String messageId;
    private final String textMessage;
    private final Direction direction;
    private final Instant time;
    static final Integer minLengthMessage = 1;
    static final Integer maxLengthMessage = 2000;

    public Message(String messageId, String textMessage, Direction direction, Instant time) {
        if (Objects.isNull(messageId)) {
            throw new MessageException("Не указан идентификатор сообщения");
        }

        String messageIdTrimmed = messageId.trim();
        if (messageIdTrimmed.isEmpty()) {
            throw new MessageException("Идентификатор сообщения пуст");
        }

        if (Objects.isNull(textMessage)) {
            throw new MessageException("Не указан текст сообщения");
        }

        String textMessageTrimmed = textMessage.trim();
        if (textMessageTrimmed.isEmpty()) {
            throw new MessageException("Текст сообщения пуст");
        }

        if (textMessageTrimmed.length() > maxLengthMessage || textMessageTrimmed.length() < minLengthMessage) {
            throw new MessageException(String.format("Сообщение вышло за допустимый диапазон: минимум %s и максимум %s символов", minLengthMessage, maxLengthMessage));
        }

        if (Objects.isNull(direction) || ((!direction.equals(Direction.INCOMING) && (!direction.equals(Direction.OUTGOING))))) {
            throw new MessageException("Не указано или неверное направление сообщения");
        }

        if (Objects.isNull(time)) {
            throw new MessageException("Не указано время сообщения");
        }

        this.messageId = messageIdTrimmed;
        this.textMessage = textMessageTrimmed;
        this.direction = direction;
        this.time = time;
    }
}

