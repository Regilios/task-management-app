package dev.stockflow.core;

import dev.stockflow.core.exceprion.MessageException;
import dev.stockflow.core.state.Direction;
import dev.stockflow.core.model.Message;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMessage {
    final String textMessage2000Symbol = " Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestib ";
    final String textMessage2000SymbolTrimmed = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestib";
    final String textMessage2001Symbol = " 1Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestib ";

    final Instant now = Instant.parse("2026-06-06T12:00:00Z");

    @Test
    public void create_normal_successful() {
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        assertEquals("mID-1", message.getMessageId());
        assertEquals("Привет", message.getTextMessage());
        assertEquals(Direction.INCOMING, message.getDirection());
        assertEquals(now, message.getTime());


        Message message_2 = new Message("mID-2", "Привет", Direction.OUTGOING, now);
        assertEquals("mID-2", message_2.getMessageId());
        assertEquals("Привет", message_2.getTextMessage());
        assertEquals(Direction.OUTGOING, message_2.getDirection());
        assertEquals(now, message_2.getTime());
    }

    @Test
    public void create_messageIdAndTextWithSpace_successful() {
        Message message = new Message(" mID-1 ", " Привет ", Direction.INCOMING, now);

        assertEquals("mID-1", message.getMessageId());
        assertEquals("Привет", message.getTextMessage());
        assertEquals(Direction.INCOMING, message.getDirection());
        assertEquals(now, message.getTime());
    }

    @Test
    public void create_messageIdAndTextWithSpace2_successful() {
        Message message = new Message(" mID 1 ", " Привет мир", Direction.INCOMING, now);

        assertEquals("mID 1", message.getMessageId());
        assertEquals("Привет мир", message.getTextMessage());
        assertEquals(Direction.INCOMING, message.getDirection());
        assertEquals(now, message.getTime());
    }


    @Test
    public void create_messageWithOneSymbol_successful() {
        Message message = new Message(" mID-1 ", " a ", Direction.INCOMING, now);

        assertEquals("mID-1", message.getMessageId());
        assertEquals("a", message.getTextMessage());
        assertEquals(Direction.INCOMING, message.getDirection());
        assertEquals(now, message.getTime());
    }


    @Test
    public void create_messageWith2000Symbol_successful() {
        Message message = new Message(" mID-1 ", textMessage2000Symbol, Direction.INCOMING, now);

        assertEquals("mID-1", message.getMessageId());
        assertEquals(textMessage2000SymbolTrimmed, message.getTextMessage());
        assertEquals(Direction.INCOMING, message.getDirection());
        assertEquals(now, message.getTime());
    }

    @Test
    public void create_messageWithMore2000Symbol_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message(" mID-1 ", textMessage2001Symbol, Direction.INCOMING, now);
        });
        assertEquals("Сообщение вышло за допустимый диапазон: минимум 1 и максимум 2000 символов", exception.getMessage());
    }

    @Test
    public void create_messageWithoutMessageId_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message(null, "Привет", Direction.INCOMING, now);
        });
        assertEquals("Не указан идентификатор сообщения", exception.getMessage());
    }

    @Test
    public void create_messageEmptyMessageId_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("", "Привет", Direction.INCOMING, now);
        });
        assertEquals("Идентификатор сообщения пуст", exception.getMessage());
    }

    @Test
    public void create_messageMessageIdConsistingOnlyOfSpaces_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("    ", "Привет", Direction.INCOMING, now);
        });
        assertEquals("Идентификатор сообщения пуст", exception.getMessage());
    }

    @Test
    public void create_messageWithoutMessage_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("mID-1", null, Direction.INCOMING, now);
        });
        assertEquals("Не указан текст сообщения", exception.getMessage());
    }

    @Test
    public void create_messageEmptyMessage_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("mID-1", "", Direction.INCOMING, now);
        });
        assertEquals("Текст сообщения пуст", exception.getMessage());
    }

    @Test
    public void create_messageMessageConsistingOnlyOfSpaces_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("mID-1", "    ", Direction.INCOMING, now);
        });
        assertEquals("Текст сообщения пуст", exception.getMessage());
    }

    @Test
    public void create_directionIsNull_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("mID-1", "aaaa", null, now);
        });
        assertEquals("Не указано или неверное направление сообщения", exception.getMessage());
    }

    @Test
    public void create_timeIsNull_throwException() {
        Throwable exception = assertThrows(MessageException.class, () -> {
            Message message = new Message("mID-1", "aaaa", Direction.INCOMING, null);
        });
        assertEquals("Не указано время сообщения", exception.getMessage());
    }
}
