package dev.stockflow.core;

import dev.stockflow.core.exceprion.MessageException;
import dev.stockflow.core.state.Direction;
import dev.stockflow.core.model.Message;
import dev.stockflow.core.service.MessageListService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMessageList {

    final Instant now = Instant.parse("2026-06-06T12:00:00Z");
    final Instant now_2 = Instant.parse("2026-06-06T16:00:00Z");
    final Instant now_3 = Instant.parse("2026-06-06T17:00:00Z");

    @Test
    public void create_collectEmpty_successful() {
        MessageListService testMessageList = new MessageListService();
        assertEquals(true, testMessageList.getMessagesList().isEmpty());
    }

    @Test
    public void create_normal_successful() {
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        MessageListService testMessageList = new MessageListService();
        testMessageList.addMessage(message);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());
    }

    @Test
    public void create_normalTwoMessage_successful() {
        MessageListService testMessageList = new MessageListService();
        List<String> orderCheckMessageControlList = List.of("mID-3", "mID-2", "mID-1");
        List<String> orderMessageList = new ArrayList<>();

        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-2", "Мир", Direction.OUTGOING, now_2);
        Message message_3 = new Message("mID-3", "!", Direction.INCOMING, now_3);

        testMessageList.addMessage(message_3);
        testMessageList.addMessage(message_2);
        testMessageList.addMessage(message);

        Map<String, Message> messagesList = testMessageList.getMessagesList();

        for (String item : messagesList.keySet()) {
            orderMessageList.add(item);
        }

        assertEquals(orderMessageList, orderCheckMessageControlList);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        Message messageCheck_2 = testMessageList.getMessagesList().get("mID-2");
        assertEquals("mID-2", messageCheck_2.getMessageId());
        assertEquals("Мир", messageCheck_2.getTextMessage());
        assertEquals(Direction.OUTGOING, messageCheck_2.getDirection());
        assertEquals(now_2, messageCheck_2.getTime());
    }

    @Test
    public void create_diffIdSameMessage_successful() {
        MessageListService testMessageList = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-2", "Привет", Direction.INCOMING, now);

        testMessageList.addMessage(message);
        testMessageList.addMessage(message_2);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        Message messageCheck_2 = testMessageList.getMessagesList().get("mID-2");
        assertEquals("mID-2", messageCheck_2.getMessageId());
        assertEquals("Привет", messageCheck_2.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck_2.getDirection());
        assertEquals(now, messageCheck_2.getTime());
    }

    @Test
    public void create_diffObject_throwException() {
        MessageListService testMessageList = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message(" mID-1 ", " Мир   ", Direction.OUTGOING, now_2);

        testMessageList.addMessage(message);
        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(message_2);
        });
        assertEquals("id Сообщений совпадают, но данные отличаются", exception.getMessage());

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        Integer size = testMessageList.getMessagesList().size();

        assertEquals(1, size);
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());
    }

    @Test
    public void create_diffObjectSameMessage_successful() {
        MessageListService testMessageList = new MessageListService();
        List<String> orderCheckMessageControlList = List.of("mID-1", "mID-2");
        List<String> orderMessageList = new ArrayList<>();

        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-2", "Привет", Direction.INCOMING, now);
        Message message_3 = new Message(" mID-1 ", " Привет ", Direction.INCOMING, now);

        testMessageList.addMessage(message);
        testMessageList.addMessage(message_2);
        testMessageList.addMessage(message_3);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        Message messageCheck_2 = testMessageList.getMessagesList().get("mID-2");
        Integer size = testMessageList.getMessagesList().size();

        assertEquals(2, size);

        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        assertEquals("mID-2", messageCheck_2.getMessageId());
        assertEquals("Привет", messageCheck_2.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck_2.getDirection());
        assertEquals(now, messageCheck_2.getTime());

        Map<String, Message> messagesList = testMessageList.getMessagesList();

        for (String item : messagesList.keySet()) {
            orderMessageList.add(item);
        }

        assertEquals(orderMessageList, orderCheckMessageControlList);
    }

    @Test
    public void create_diffObjectDiffMessage_throwException() {
        MessageListService testMessageList = new MessageListService();
        List<String> orderCheckMessageControlList = List.of("mID-1", "mID-2");
        List<String> orderMessageList = new ArrayList<>();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-1", "Привет Мир", Direction.INCOMING, now);
        Message message_3 = new Message("mID-2", "Привет Мир", Direction.INCOMING, now);

        testMessageList.addMessage(message);
        testMessageList.addMessage(message_3);
        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(message_2);
        });
        assertEquals("id Сообщений совпадают, но данные отличаются", exception.getMessage());


        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        Message messageCheck_2 = testMessageList.getMessagesList().get("mID-2");
        Integer size = testMessageList.getMessagesList().size();

        assertEquals(2, size);
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        assertEquals("mID-2", messageCheck_2.getMessageId());
        assertEquals("Привет Мир", messageCheck_2.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck_2.getDirection());
        assertEquals(now, messageCheck_2.getTime());

        Map<String, Message> messagesList = testMessageList.getMessagesList();

        for (String item : messagesList.keySet()) {
            orderMessageList.add(item);
        }

        assertEquals(orderMessageList, orderCheckMessageControlList);
    }

    @Test
    public void create_diffObjectDiffDirection_throwException() {
        MessageListService testMessageList = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-1", "Привет", Direction.OUTGOING, now);

        testMessageList.addMessage(message);
        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(message_2);
        });
        assertEquals("id Сообщений совпадают, но данные отличаются", exception.getMessage());

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        Integer size = testMessageList.getMessagesList().size();

        assertEquals(1, size);
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());
    }

    @Test
    public void create_diffObjectDiffTime_throwException() {
        MessageListService testMessageList = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-1", "Привет", Direction.INCOMING, now_2);

        testMessageList.addMessage(message);
        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(message_2);
        });
        assertEquals("id Сообщений совпадают, но данные отличаются", exception.getMessage());

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        Integer size = testMessageList.getMessagesList().size();

        assertEquals(1, size);
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());
    }


    @Test
    public void create_diffRegister_successful() {
        MessageListService testMessageList = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mid-1", "Мир", Direction.OUTGOING, now_2);

        testMessageList.addMessage(message);
        testMessageList.addMessage(message_2);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        Message messageCheck_2 = testMessageList.getMessagesList().get("mid-1");
        assertEquals("mid-1", messageCheck_2.getMessageId());
        assertEquals("Мир", messageCheck_2.getTextMessage());
        assertEquals(Direction.OUTGOING, messageCheck_2.getDirection());
        assertEquals(now_2, messageCheck_2.getTime());
    }

    @Test
    public void create_diffMessageList_successful() {
        MessageListService testMessageList = new MessageListService();
        MessageListService testMessageListEnemy = new MessageListService();
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-1", "Привет я Максим", Direction.INCOMING, now_2);
        Message message_3 = new Message("mID-2", "Мир", Direction.OUTGOING, now);
        Message message_4 = new Message("mID-2", "Очень приятно", Direction.OUTGOING, now_2);

        testMessageList.addMessage(message);
        assertEquals(true, testMessageListEnemy.getMessagesList().isEmpty());
        testMessageListEnemy.addMessage(message_2);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());


        Message messageCheck_2 = testMessageListEnemy.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck_2.getMessageId());
        assertEquals("Привет я Максим", messageCheck_2.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck_2.getDirection());
        assertEquals(now_2, messageCheck_2.getTime());

        testMessageList.addMessage(message_3);
        testMessageListEnemy.addMessage(message_4);

        Message messageCheck_3 = testMessageList.getMessagesList().get("mID-2");
        assertEquals("mID-2", messageCheck_3.getMessageId());
        assertEquals("Мир", messageCheck_3.getTextMessage());
        assertEquals(Direction.OUTGOING, messageCheck_3.getDirection());
        assertEquals(now, messageCheck_3.getTime());

        Message messageCheck_4 = testMessageListEnemy.getMessagesList().get("mID-2");
        assertEquals("mID-2", messageCheck_4.getMessageId());
        assertEquals("Очень приятно", messageCheck_4.getTextMessage());
        assertEquals(Direction.OUTGOING, messageCheck_4.getDirection());
        assertEquals(now_2, messageCheck_4.getTime());
    }

    @Test
    public void create_messageNull_throwException() {
        MessageListService testMessageList = new MessageListService();
        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(null);
        });

        assertEquals("Сообщение отустствует", exception.getMessage());
    }

    @Test
    public void create_notEmptyListAddNull_throwException() {
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        Message message_2 = new Message("mID-2", "Мир", Direction.OUTGOING, now);
        List<String> orderCheckMessageControlList = List.of("mID-1", "mID-2");
        List<String> orderMessageList = new ArrayList<>();
        MessageListService testMessageList = new MessageListService();
        testMessageList.addMessage(message);
        testMessageList.addMessage(message_2);

        Throwable exception = assertThrows(MessageException.class, () -> {
            testMessageList.addMessage(null);
        });
        assertEquals("Сообщение отустствует", exception.getMessage());


        Integer size = testMessageList.getMessagesList().size();
        assertEquals(2, size);

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());

        Message messageCheck_2 = testMessageList.getMessagesList().get("mID-2");
        assertEquals("mID-2", messageCheck_2.getMessageId());
        assertEquals("Мир", messageCheck_2.getTextMessage());
        assertEquals(Direction.OUTGOING, messageCheck_2.getDirection());
        assertEquals(now, messageCheck_2.getTime());

        Map<String, Message> messagesList = testMessageList.getMessagesList();
        for (String item : messagesList.keySet()) {
            orderMessageList.add(item);
        }
        assertEquals(orderMessageList, orderCheckMessageControlList);
    }

    @Test
    public void edit_unmodifiableMap_throwException() {
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        MessageListService testMessageList = new MessageListService();
        testMessageList.addMessage(message);

        Map<String, Message> messagesList = testMessageList.getMessagesList();
        Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
            messagesList.remove("mID-1");
        });
        assertEquals(null, exception.getMessage());

        Message messageCheck = testMessageList.getMessagesList().get("mID-1");
        assertEquals(1, testMessageList.getMessagesList().size());
        assertEquals("mID-1", messageCheck.getMessageId());
        assertEquals("Привет", messageCheck.getTextMessage());
        assertEquals(Direction.INCOMING, messageCheck.getDirection());
        assertEquals(now, messageCheck.getTime());
    }
}
