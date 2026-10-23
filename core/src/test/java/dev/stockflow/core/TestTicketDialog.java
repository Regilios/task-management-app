package dev.stockflow.core;

import dev.stockflow.core.model.Dialog;
import dev.stockflow.core.model.Message;
import dev.stockflow.core.model.Ticket;
import dev.stockflow.core.service.DialogListService;
import dev.stockflow.core.service.MessageListService;
import dev.stockflow.core.state.Direction;
import dev.stockflow.core.state.StatusTicket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTicketDialog {
    private static final Log log = LogFactory.getLog(TestTicketDialog.class);
    final Instant now = Instant.parse("2026-06-06T12:00:00Z");
    final Instant beforePeriod = Instant.parse("2026-10-23T09:59:00Z");
    final Instant periodStart = Instant.parse("2026-10-23T10:00:00Z");
    final Instant middleOfPeriod = Instant.parse("2026-10-23T10:30:00Z");
    final Instant periodEnd = Instant.parse("2026-10-23T11:00:00Z");
    final Instant afterPeriod = Instant.parse("2026-10-23T11:01:00Z");

    @Test
    @DisplayName("1: Новый диалог получает одно открытое обращение и пустую историю")
    public void open_newDialog_createsOpenTicketWithEmptyHistory() {
        DialogListService dialogListService = new DialogListService();
        assertTrue(dialogListService.getDialogList().isEmpty());
        assertTrue(dialogListService.getQueryTicket().isEmpty());
        assertTrue(dialogListService.getQueryTicket().isEmpty());

        Dialog dialog = dialogListService.open("D-1", "C-1", "T-1");
        assertEquals(1, dialog.getTicketsList().size());
        assertEquals("T-1", dialog.getTicketById("T-1").getTicketId());
        assertEquals(StatusTicket.OPEN, dialog.getTicketById("T-1").getStatusTicket());
        assertTrue(dialog.getMessageListService().getMessagesList().isEmpty());
        assertEquals("D-1", dialog.getTicketById("T-1").getDialogId());
        assertNull(dialog.getTicketById("T-1").getOperatorId());
        Ticket firstTicket = dialog.getTicketById("T-1");
        assertSame(dialog, dialogListService.getDialog("D-1"));
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket), List.of());
        assertEquals(List.of(firstTicket), dialogListService.getQueryTicket());
    }

    @Test
    @DisplayName("2: Повтор открытия сохраняет обращение, оператора и сообщения")
    public void open_activeTicket_returnsSameTicketAndPreservesAssignment() {
        DialogListService dialogListService = new DialogListService();
        Dialog dialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticket = dialog.getTicketById("T-1");
        Message message = new Message("mID-1", "Привет", Direction.INCOMING, now);
        dialog.addMessage(message);

        Dialog dialog_1 = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticket_1 = dialog_1.getTicketById("T-1");
        assertSame(ticket, ticket_1);
        assertTicketState(ticket_1, StatusTicket.OPEN, null);
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(message));
        assertEquals(List.of(ticket), dialogListService.getQueryTicket());

        dialog_1.getTicketById("T-1").setOperatorId("O-1");

        Dialog dialog_2 = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticket_2 = dialog_2.getTicketById("T-1");
        assertSame(ticket, ticket_2);
        assertEquals(StatusTicket.ASSIGNED, ticket_2.getStatusTicket());
        assertEquals("O-1", ticket_2.getOperatorId());
        assertTrue(dialogListService.getQueryTicket().isEmpty());
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(message));
    }

    @Test
    @DisplayName("3: Общая очередь содержит только ожидающие обращения в порядке создания")
    public void getQueue_mixedStates_returnsOpenTicketsInCreationOrder() {
        DialogListService dialogListService = new DialogListService();
        Dialog dialog = dialogListService.open("D-3", "C-1", "T-1");
        Ticket firstTicket = dialog.getTicketById("T-1");

        Dialog dialog2 = dialogListService.open("D-1", "C-1", "T-2");
        Ticket otherDialogTicket = dialog2.getTicketById("T-2");

        Dialog dialog3 = dialogListService.open("D-2", "C-2", "T-3");
        Ticket thirdDialogTicket = dialog3.getTicketById("T-3");

        List<Ticket> list = Arrays.asList(firstTicket, otherDialogTicket, thirdDialogTicket);
        assertEquals(list, dialogListService.getQueryTicket());

        otherDialogTicket.setOperatorId("O-1");
        thirdDialogTicket.setOperatorId("O-2");
        thirdDialogTicket.close("O-2");

        Dialog dialog4 = dialogListService.open("D-0", "C-3", "T-4");
        Ticket fourthDialogTicket = dialog4.getTicketById("T-4");

        List<Ticket> list2 = Arrays.asList(firstTicket, fourthDialogTicket);
        assertEquals(list2, dialogListService.getQueryTicket());

        List<Ticket> list3 = Arrays.asList(firstTicket);
        assertEquals(list3, dialog.getTicketsList());

        List<Ticket> list4 = Arrays.asList(otherDialogTicket);
        assertEquals(list4, dialog2.getTicketsList());

        List<Ticket> list5 = Arrays.asList(thirdDialogTicket);
        assertEquals(list5, dialog3.getTicketsList());

        List<Ticket> list6 = Arrays.asList(fourthDialogTicket);
        assertEquals(list6, dialog4.getTicketsList());
        assertEquals(list2, dialogListService.getQueryTicket(), "Повтор чтения не меняет очередь всех ожидающих обращений");
        assertEquals(list3, dialogListService.getDialog("D-3").getTicketsList());
        assertEquals(list4, dialogListService.getDialog("D-1").getTicketsList());
        assertEquals(list5, dialogListService.getDialog("D-2").getTicketsList());
        assertEquals(list6, dialogListService.getDialog("D-0").getTicketsList());
    }

    @Test
    @DisplayName("4: Назначение и сообщение сохраняют данные, чужой оператор получает отказ")
    public void assign_repeatsMessagesAndConflicts_preservesAssignedOperator() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket firstTicket = firstDialog.getTicketById("T-1");
        Message message1_1 = new Message("mID-1_1", "Привет", Direction.INCOMING, now);
        firstDialog.addMessage(message1_1);

        Dialog secondDialog = dialogListService.open("D-2", "C-2", "T-2");
        Ticket otherDialogTicket = secondDialog.getTicketById("T-2");
        Message message2_1 = new Message("mID-1_1", "Пока", Direction.OUTGOING, now);
        secondDialog.addMessage(message2_1);

        List<Ticket> list = Arrays.asList(firstTicket, otherDialogTicket);
        assertEquals(list, dialogListService.getQueryTicket());

        firstTicket.setOperatorId("O-1");
        Message message1_2 = new Message("mID-1_2", "Иван", Direction.INCOMING, now);
        firstDialog.addMessage(message1_2);
        assertEquals(StatusTicket.ASSIGNED, firstTicket.getStatusTicket());

        assertTicketState(firstTicket, StatusTicket.ASSIGNED, "O-1");
        assertEquals(List.of(otherDialogTicket), dialogListService.getQueryTicket());

        firstTicket.setOperatorId("O-1");
        assertTicketState(firstTicket, StatusTicket.ASSIGNED, "O-1");

        assertThrows(IllegalStateException.class, () -> {
            firstTicket.setOperatorId("O-2");
        });
        assertTicketState(firstTicket, StatusTicket.ASSIGNED, "O-1");
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket),
                List.of(message1_1, message1_2));
        assertDialogContents(dialogListService, "D-2", "C-2", List.of(otherDialogTicket),
                List.of(message2_1));
    }

    @Test
    @DisplayName("5: Закрыть обращение может его оператор, повтор сохраняет данные")
    public void close_mixedOwnership_allowsOnlyOwnerAndSameOwnerRepeat() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket firstTicket = firstDialog.getTicketById("T-1");
        assertEquals(StatusTicket.OPEN, firstTicket.getStatusTicket());

        Dialog secondDialog = dialogListService.open("D-2", "C-2", "T-2");
        Ticket otherDialogTicket = secondDialog.getTicketById("T-2");
        secondDialog.assignTicket("T-2", "O-1");
        assertEquals(StatusTicket.ASSIGNED, otherDialogTicket.getStatusTicket());

        Message firstMessage = new Message("M-1", "Первый вопрос", Direction.INCOMING, now);
        Message secondMessage = new Message("M-2", "Второй вопрос", Direction.INCOMING, now);
        firstDialog.addMessage(firstMessage);
        secondDialog.addMessage(secondMessage);

        assertThrows(IllegalStateException.class, () -> {
            firstTicket.close("O-1");
        });
        assertTicketState(firstTicket, StatusTicket.OPEN, null);

        assertThrows(IllegalStateException.class, () -> {
            otherDialogTicket.close("O-2");
        });
        assertTicketState(otherDialogTicket, StatusTicket.ASSIGNED, "O-1");

        otherDialogTicket.close("O-1");
        assertTicketState(otherDialogTicket, StatusTicket.CLOSED, "O-1");

        otherDialogTicket.close("O-1");
        assertTicketState(otherDialogTicket, StatusTicket.CLOSED, "O-1");
        assertEquals(List.of(firstTicket), dialogListService.getQueryTicket());
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket), List.of(firstMessage));
        assertDialogContents(dialogListService, "D-2", "C-2", List.of(otherDialogTicket), List.of(secondMessage));
    }

    @Test
    @DisplayName("6: Новое обращение идёт в конец общей очереди; старые команды его не меняют")
    public void open_closedDialog_createsNewTicketAtQueueEnd() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket firstTicket = firstDialog.getTicketById("T-1");
        assertEquals(StatusTicket.OPEN, firstTicket.getStatusTicket());

        Dialog secondDialog = dialogListService.open("D-2", "C-2", "T-2");
        Ticket otherDialogTicket = secondDialog.getTicketById("T-2");
        assertEquals(StatusTicket.OPEN, otherDialogTicket.getStatusTicket());

        firstDialog.assignTicket("T-1", "O-1");
        firstDialog.closeTicket("T-1", "O-1");
        assertEquals(StatusTicket.CLOSED, firstTicket.getStatusTicket());
        assertEquals(StatusTicket.OPEN, otherDialogTicket.getStatusTicket());

        Dialog firstDialog_2 = dialogListService.open("D-1", "C-1", "T-1-2");
        Ticket secondTicket = firstDialog_2.getTicketById("T-1-2");
        assertNotSame(firstTicket, secondTicket);
        assertTicketState(secondTicket, StatusTicket.OPEN, null);
        Dialog repeatedDialog = dialogListService.open("D-1", "C-1", "T-1-2");
        assertSame(secondTicket, repeatedDialog.getTicketById("T-1-2"));

        assertThrows(IllegalStateException.class, () -> {
            firstDialog.assignTicket("T-1", "O-1");
        });
        assertTicketState(secondTicket, StatusTicket.OPEN, null);
        assertThrows(IllegalStateException.class, () -> {
            firstDialog.assignTicket("T-1", "O-2");
        });
        assertTicketState(secondTicket, StatusTicket.OPEN, null);
        firstDialog.closeTicket("T-1", "O-1");
        assertTicketState(secondTicket, StatusTicket.OPEN, null);

        assertThrows(IllegalStateException.class, () -> {
            firstDialog.closeTicket("T-1", "O-2");
        });
        assertTicketState(secondTicket, StatusTicket.OPEN, null);
        assertTicketState(firstTicket, StatusTicket.CLOSED, "O-1");
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket, secondTicket), List.of());
        assertEquals(List.of(otherDialogTicket, secondTicket), dialogListService.getQueryTicket(),
                "Очередь всех ожидающих обращений: старое обращение D-2 должно быть раньше нового обращения D-1");
    }

    @Test
    @DisplayName("7: Сообщение после закрытия сохраняет историю и само не открывает обращение")
    public void addMessage_closedDialog_preservesSharedHistoryWithoutOpeningTicket() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Dialog otherDialog = dialogListService.open("D-2", "C-1", "T-1");

        Message firstMessage = new Message("mID-1_1", "Привет", Direction.INCOMING, now);
        Message secondMessage = new Message("mID-1_2", "прекрасный", Direction.INCOMING, now);
        Message otherDialogMessage = new Message("mID-2_1", "Тест", Direction.INCOMING, now);

        firstDialog.addMessage(firstMessage);
        firstDialog.addMessage(secondMessage);
        otherDialog.addMessage(otherDialogMessage);

        Ticket firstTicket = firstDialog.getTicketById("T-1");
        Ticket otherDialogTicket = otherDialog.getTicketById("T-1");
        MessageListService messageHistory = firstDialog.getMessageListService();
        Map<String, Message> historyFirstDialog = messageHistory.getMessagesList();
        firstDialog.assignTicket("T-1", "O-1");
        firstDialog.closeTicket("T-1", "O-1");

        assertEquals(1, firstDialog.getTicketsList().size());
        assertEquals(StatusTicket.CLOSED, firstDialog.getTicketsList().getFirst().getStatusTicket());


        Message thirdMessage = new Message("mID-1_3", "Мир", Direction.INCOMING, now);
        firstDialog.addMessage(thirdMessage);
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket), List.of(firstMessage, secondMessage, thirdMessage));
        assertTicketState(firstTicket, StatusTicket.CLOSED, "O-1");
        assertEquals(List.of(otherDialogTicket), dialogListService.getQueryTicket(), "Добавление сообщения в закрытый диалог само по себе не открывает обращение");
        assertIterableEquals(List.of(firstMessage, secondMessage, thirdMessage), historyFirstDialog.values());
        Dialog nextDialog = dialogListService.open("D-1", "C-1", "T-2");
        assertEquals(2, nextDialog.getTicketsList().size());
        assertEquals(StatusTicket.CLOSED, nextDialog.getTicketsList().getFirst().getStatusTicket());
        assertEquals(StatusTicket.OPEN, nextDialog.getTicketsList().getLast().getStatusTicket());

        List<Message> checkListMessage = Arrays.asList(firstMessage, secondMessage, thirdMessage);
        Map<String, Message> messagesList = firstDialog.getMessageListService().getMessagesList();
        List<Message> messages = new ArrayList<>(messagesList.values());
        assertEquals(checkListMessage, messages);

        List<Message> checkListMessage2 = List.of(otherDialogMessage);
        Map<String, Message> messagesList2 = otherDialog.getMessageListService().getMessagesList();
        List<Message> messages2 = new ArrayList<>(messagesList2.values());
        assertEquals(1, otherDialog.getMessageListService().getMessagesList().size());
        assertEquals(checkListMessage2, messages2);
        Ticket newTicket = nextDialog.getTicketById("T-2");
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket, newTicket), checkListMessage);
        assertDialogContents(dialogListService, "D-2", "C-1", List.of(otherDialogTicket), checkListMessage2);
        assertTicketState(otherDialogTicket, StatusTicket.OPEN, null);
        assertTicketState(newTicket, StatusTicket.OPEN, null);
        assertSame(messageHistory, nextDialog.getMessageListService(), "Диалог сохраняет один MessageListService");
        assertIterableEquals(checkListMessage, historyFirstDialog.values(),
                "Ранее полученная история сообщений видит добавленное сообщение");
        assertEquals(List.of(otherDialogTicket, newTicket), dialogListService.getQueryTicket(),
                "Новое обращение D-1 появляется после уже ожидающего обращения D-2");
    }

    @Test
    @DisplayName("8: Другой клиент не может изменить существующий диалог")
    public void open_differentClient_throwsWithoutChangingDialog() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket firstTicket = firstDialog.getTicketById("T-1");
        Message firstMessage = new Message("mID-1_1", "Привет", Direction.INCOMING, now);
        firstDialog.addMessage(firstMessage);

        assertThrows(IllegalStateException.class, () -> {
            Dialog enemyDialog = dialogListService.open("D-1", "C-2", "T-1");
        });
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket), List.of(firstMessage));
        assertTicketState(firstTicket, StatusTicket.OPEN, null);
        assertEquals(List.of(firstTicket), dialogListService.getQueryTicket());
        firstDialog.assignTicket("T-1", "O-1");
        firstDialog.closeTicket("T-1", "O-1");
        assertThrows(IllegalStateException.class, () -> {
            Dialog enemyDialog = dialogListService.open("D-1", "C-2", "T-1");
        });
        assertTrue(dialogListService.getQueryTicket().isEmpty());
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(firstTicket), List.of(firstMessage));
        assertTicketState(firstTicket, StatusTicket.CLOSED, "O-1");
    }

    @Test
    @DisplayName("9: Пробелы по краям ID удаляются, регистр и внутренние пробелы значимы")
    public void ids_spacesAndCase_matchOnlyAfterTrimmingOuterSpaces() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open(" D 1 ", "C 1 ", " T 1");
        Dialog firstDialogAgain = dialogListService.open("D 1", "C 1", "T 1");
        assertSame(firstDialog, firstDialogAgain);

        Message firstMessage = new Message("mID-1_1", "Привет", Direction.INCOMING, now);
        firstDialog.addMessage(firstMessage);

        assertSame(firstDialog, dialogListService.getDialog(new String(" D 1 ")));
        assertSame(firstDialog, dialogListService.getDialog("D 1"));
        Ticket normalizedTicket = firstDialog.getTicketById("T 1");
        assertDialogContents(dialogListService, "D 1", "C 1", List.of(normalizedTicket), List.of(firstMessage));

        List<Message> checkListMessage = Arrays.asList(firstMessage);
        Map<String, Message> messagesList = firstDialog.getMessageListService().getMessagesList();
        List<Message> messages = new ArrayList<>(messagesList.values());
        assertEquals(checkListMessage, messages);

        List<Message> checkListMessage2 = Arrays.asList(firstMessage);
        Map<String, Message> messagesList2 = firstDialogAgain.getMessageListService().getMessagesList();
        List<Message> messages2 = new ArrayList<>(messagesList2.values());
        assertEquals(checkListMessage2, messages2);

        firstDialog.assignTicket("T 1", " O 1 ");
        firstDialog.assignTicket("T 1", "O 1");
        assertThrows(IllegalStateException.class, () -> {
            firstDialog.assignTicket("T 1", "o 1");
        });

        assertThrows(IllegalStateException.class, () -> {
            firstDialog.assignTicket("T 1", "O1");
        });

        assertThrows(IllegalStateException.class, () -> {
            firstDialog.closeTicket("T 1", "o 1");
        });

        assertThrows(IllegalStateException.class, () -> {
            firstDialog.closeTicket("T 1", "O1");
        });

        firstDialog.closeTicket("T 1", " O 1");

        Dialog secondDialog = dialogListService.open("D1", "C 1 ", " T 1");
        Dialog secondDialogAgain = dialogListService.open("d 1", "C 1", "T 1");

        assertNotSame(firstDialog, secondDialog);
        assertNotSame(firstDialog, secondDialogAgain);
        assertNotSame(secondDialog, secondDialogAgain);
        assertEquals(3, dialogListService.getDialogList().size());
        assertSame(secondDialog, dialogListService.getDialog("D1"));
        assertSame(secondDialogAgain, dialogListService.getDialog("d 1"));
        assertTicketState(normalizedTicket, StatusTicket.CLOSED, "O 1");

        assertThrows(IllegalStateException.class, () -> {
            dialogListService.open("D 1", "c 1", " T 1");
        });

        assertThrows(IllegalStateException.class, () -> {
            dialogListService.open("D 1", "C1", " T 1");
        });
        assertDialogContents(dialogListService, "D 1", "C 1", List.of(normalizedTicket), List.of(firstMessage));
        assertTicketState(normalizedTicket, StatusTicket.CLOSED, "O 1");
    }

    @Test
    @DisplayName("10: Недопустимые ID дают отказ без изменения обоих диалогов")
    public void operations_invalidIds_throwWithoutChangingState() {
        DialogListService service = new DialogListService();
        Dialog firstDialog = service.open("D-1", "C-1", "T-1");
        Ticket firstTicket = firstDialog.getTicketById("T-1");
        Dialog secondDialog = service.open("D-2", "C-1", "T-1");
        Ticket secondTicket = secondDialog.getTicketById("T-1");
        secondDialog.assignTicket("T-1", "O-1");

        List<String> invalidIds = Arrays.asList(null, "", "   ");
        for (String invalidId : invalidIds) {
            assertThrows(IllegalArgumentException.class, () -> service.open(invalidId, "C-1", "T-1"));
            assertThrows(IllegalArgumentException.class, () -> service.open("D-1", invalidId, "T-1"));
            assertThrows(IllegalArgumentException.class, () -> service.getDialog(invalidId));
            assertThrows(IllegalArgumentException.class, () -> firstDialog.assignTicket(invalidId, "O-1"));
            assertThrows(IllegalArgumentException.class, () -> firstDialog.assignTicket("T-1", invalidId));
            assertThrows(IllegalArgumentException.class, () -> secondDialog.closeTicket("T-1", invalidId));
            assertThrows(IllegalArgumentException.class, () -> secondDialog.closeTicket(invalidId, "O-1"));
        }
        assertEquals(2, service.getDialogList().size());
        assertSame(firstDialog, service.getDialog("D-1"));
        assertSame(secondDialog, service.getDialog("D-2"));
        assertEquals("C-1", firstDialog.getClientId());
        assertEquals(List.of(firstTicket), firstDialog.getTicketsList());
        assertTicketState(firstTicket, StatusTicket.OPEN, null);
        assertTicketState(secondTicket, StatusTicket.ASSIGNED, "O-1");
    }

    @Test
    @DisplayName("11: Чтение и добавление сообщения не создают неизвестный диалог")
    public void readAndAddMessage_unknownDialog_throwWithoutCreatingDialog() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticket = firstDialog.getTicketById("T-1");
        Message firstMessage = new Message("mID-1_1", "Привет", Direction.INCOMING, now);
        Message enemyMessage = new Message("mID-2_1", "Мир", Direction.INCOMING, now);
        firstDialog.addMessage(firstMessage);

        assertThrows(NoSuchElementException.class, () -> {
            dialogListService.getDialog("X").getMessageListService().getMessagesList();
        });
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(firstMessage));
        assertTicketState(ticket, StatusTicket.OPEN, null);
        assertEquals(1, dialogListService.getDialogList().size());
        assertFalse(dialogListService.getDialogList().containsKey("X"));
        assertEquals(List.of(ticket), dialogListService.getQueryTicket());

        assertThrows(NoSuchElementException.class, () -> {
            dialogListService.getDialog("X").getTicketsList();
        });
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(firstMessage));
        assertTicketState(ticket, StatusTicket.OPEN, null);
        assertEquals(1, dialogListService.getDialogList().size());
        assertFalse(dialogListService.getDialogList().containsKey("X"));
        assertEquals(List.of(ticket), dialogListService.getQueryTicket());

        assertThrows(NoSuchElementException.class, () -> {
            dialogListService.getDialog("X").addMessage(enemyMessage);
        });
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(firstMessage));
        assertTicketState(ticket, StatusTicket.OPEN, null);
        assertFalse(dialogListService.getDialogList().containsKey("X"));
        assertEquals(List.of(ticket), dialogListService.getQueryTicket());

        assertEquals(1, dialogListService.getDialogList().size());
        assertEquals(firstDialog, dialogListService.getDialogList().get("D-1"));
        assertEquals(firstMessage, firstDialog.getMessageListService().getMessagesList().get("mID-1_1"));
        assertEquals(ticket, firstDialog.getTicketById("T-1"));

        Dialog enemyDialog = dialogListService.open("X", "C-2", "T-X-1");
        assertEquals("T-X-1", enemyDialog.getTicketById("T-X-1").getTicketId());
        assertEquals(StatusTicket.OPEN, enemyDialog.getTicketById("T-X-1").getStatusTicket());
        assertTrue(enemyDialog.getMessageListService().getMessagesList().isEmpty());
        Ticket newDialogTicket = enemyDialog.getTicketById("T-X-1");
        assertDialogContents(dialogListService, "X", "C-2", List.of(newDialogTicket), List.of());
        assertTicketState(newDialogTicket, StatusTicket.OPEN, null);
        assertDialogContents(dialogListService, "D-1", "C-1", List.of(ticket), List.of(firstMessage));
        assertEquals(List.of(ticket, newDialogTicket), dialogListService.getQueryTicket());
    }

    @Test
    @DisplayName("12: Полученную очередь и списки обращений нельзя редактировать")
    public void getQueueAndTickets_modifyResults_throwUnsupportedOperationException() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticketFirstDialog = firstDialog.getTicketById("T-1");

        Dialog secondDialog = dialogListService.open("D-X-1", "C-1", "T-X-1");
        Ticket ticketSecondDialog = secondDialog.getTicketById("T-X-1");

        List<Ticket> queue = dialogListService.getQueryTicket();
        List<Ticket> dialogTickets = firstDialog.getTicketsList();

        List<Ticket> expectedQueue = List.of(ticketFirstDialog, ticketSecondDialog);
        List<Ticket> expectedDialogTickets = List.of(ticketFirstDialog);
        assertEquals(expectedQueue, queue);
        assertEquals(expectedDialogTickets, dialogTickets);

        assertThrows(UnsupportedOperationException.class, () -> {
            queue.add(ticketFirstDialog);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            queue.remove(0);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            queue.set(0, ticketSecondDialog);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            dialogTickets.add(ticketSecondDialog);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            dialogTickets.remove(0);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            dialogTickets.set(0, ticketSecondDialog);
        });
        assertEquals(expectedQueue, queue);
        assertEquals(expectedDialogTickets, dialogTickets);
        assertEquals(expectedQueue, dialogListService.getQueryTicket());
        assertEquals(expectedDialogTickets, firstDialog.getTicketsList());

        firstDialog.assignTicket("T-1", "O-1");
        secondDialog.assignTicket("T-X-1", "O-1");

        assertTrue(dialogListService.getQueryTicket().isEmpty());

        List<Ticket> queueEmpty = dialogListService.getQueryTicket();
        assertThrows(UnsupportedOperationException.class, () -> {
            queueEmpty.add(ticketSecondDialog);
        });
        assertTrue(dialogListService.getQueryTicket().isEmpty());
    }

    @Test
    @DisplayName("13: Старые списки сохраняют состав, но показывают новое состояние тех же обращений")
    public void getQueueAndTickets_laterChanges_keepSnapshotContentsAndLiveReferences() {
        DialogListService dialogListService = new DialogListService();
        Dialog firstDialog = dialogListService.open("D-1", "C-1", "T-1");
        Ticket ticketFirstDialog = firstDialog.getTicketById("T-1");

        List<Ticket> oldQueue = dialogListService.getQueryTicket();
        List<Ticket> oldTickets = firstDialog.getTicketsList();

        firstDialog.assignTicket("T-1", "O-1");
        firstDialog.closeTicket("T-1", "O-1");


        Dialog secondDialog = dialogListService.open("D-1", "C-1", "T-2");
        Ticket ticketSecondDialog = secondDialog.getTicketById("T-2");

        assertEquals(secondDialog, dialogListService.getDialog("D-1"));
        assertEquals(StatusTicket.OPEN, ticketSecondDialog.getStatusTicket());

        List<Ticket> freshQueue = dialogListService.getQueryTicket();
        List<Ticket> freshTickets = secondDialog.getTicketsList();

        assertEquals(StatusTicket.CLOSED, oldQueue.getLast().getStatusTicket());
        assertEquals("O-1", oldQueue.getLast().getOperatorId());

        assertEquals(StatusTicket.CLOSED, oldTickets.getFirst().getStatusTicket());
        assertEquals("O-1", oldTickets.getFirst().getOperatorId());

        assertEquals(StatusTicket.OPEN, freshQueue.getLast().getStatusTicket());
        assertNull(freshTickets.getLast().getOperatorId());

        assertSame(ticketFirstDialog, freshTickets.getFirst());
        assertSame(ticketSecondDialog, freshTickets.getLast());
        assertEquals(List.of(ticketFirstDialog), oldQueue,
                "Ранее полученная очередь сохраняет только прежнее обращение T-1");
        assertEquals(List.of(ticketFirstDialog), oldTickets,
                "Ранее полученный список обращений D-1 не должен пополниться новым T-2");
        assertEquals(List.of(ticketSecondDialog), freshQueue,
                "Свежая очередь содержит только новое ожидающее обращение T-2");
        assertEquals(List.of(ticketFirstDialog, ticketSecondDialog), freshTickets,
                "Свежий список обращений D-1 содержит старое T-1 и новое T-2");
    }

    @Test
    @DisplayName("14: Два сервиса с одинаковыми ID хранят независимые данные")
    public void services_sameIds_storeStateIndependently() {
        DialogListService firstService = new DialogListService();
        Dialog firstDialog = firstService.open("D-1", "C-1", "T-1");
        Ticket firstServiceTicket = firstDialog.getTicketById("T-1");


        DialogListService secondService = new DialogListService();
        Dialog secondDialog = secondService.open("D-1", "C-1", "T-1");
        Ticket secondServiceTicket = secondDialog.getTicketById("T-1");

        assertNotSame(firstDialog, secondDialog);
        assertNotSame(firstServiceTicket, secondServiceTicket);

        Message firstMessage = new Message("M_1", "Первый сервис", Direction.INCOMING, now);
        firstDialog.addMessage(firstMessage);
        Message secondMessage = new Message("M_1", "Второй сервис", Direction.INCOMING, now);
        secondDialog.addMessage(secondMessage);

        firstDialog.assignTicket("T-1", "O-1");
        firstDialog.closeTicket("T-1", "O-1");

        Dialog newFirstDialog = firstService.open("D-1", "C-1", "T-2");
        Ticket newFirstServiceTicket = newFirstDialog.getTicketById("T-2");

        assertEquals(1, firstService.getDialogList().size());
        assertEquals(1, secondService.getDialogList().size());
        assertEquals(firstDialog, firstService.getDialog("D-1"));
        assertEquals(secondDialog, secondService.getDialog("D-1"));
        assertNotSame(firstDialog, secondDialog);

        List<Ticket> checkupTicketList = List.of(firstServiceTicket, newFirstServiceTicket);
        assertEquals(checkupTicketList, firstService.getDialog("D-1").getTicketsList());

        List<Ticket> checkupTicketList2 = List.of(secondServiceTicket);
        assertEquals(checkupTicketList2, secondService.getDialog("D-1").getTicketsList());
        assertEquals(StatusTicket.CLOSED, firstService.getDialog("D-1").getTicketById("T-1").getStatusTicket());
        assertEquals("O-1", firstService.getDialog("D-1").getTicketById("T-1").getOperatorId());
        assertEquals(StatusTicket.OPEN, firstService.getDialog("D-1").getTicketById("T-2").getStatusTicket());
        assertNull(firstService.getDialog("D-1").getTicketById("T-2").getOperatorId());

        assertSame(newFirstServiceTicket, firstService.getQueryTicket().getLast());
        assertSame(secondServiceTicket, secondService.getQueryTicket().getLast());
        assertEquals(1, firstDialog.getMessageListService().getMessagesList().size());
        assertEquals(1, secondDialog.getMessageListService().getMessagesList().size());
        assertSame(firstMessage, firstDialog.getMessageListService().getMessagesList().get("M_1"));
        assertSame(secondMessage, secondDialog.getMessageListService().getMessagesList().get("M_1"));
        assertEquals(List.of(newFirstServiceTicket), firstService.getQueryTicket(), "Очередь первого сервиса");
        assertEquals(List.of(secondServiceTicket), secondService.getQueryTicket(), "Очередь второго сервиса");
        assertTicketState(secondServiceTicket, StatusTicket.OPEN, null);
    }

    private void assertDialogContents(DialogListService service, String dialogId, String clientId,
                                      List<Ticket> expectedTickets, List<Message> expectedMessages) {
        Dialog savedDialog = service.getDialog(dialogId);
        assertEquals(dialogId, savedDialog.getDialogId(), "ID диалога");
        assertEquals(clientId, savedDialog.getClientId(), "Клиент диалога");
        assertEquals(expectedTickets, savedDialog.getTicketsList(), "Все обращения диалога " + dialogId);
        assertEquals(expectedMessages, new ArrayList<>(savedDialog.getMessageListService().getMessagesList().values()),
                "Все сообщения диалога " + dialogId);
    }

    private void assertTicketState(Ticket ticket, StatusTicket expectedStatus, String expectedOperatorId) {
        assertEquals(expectedStatus, ticket.getStatusTicket(), "Статус обращения " + ticket.getTicketId());
        assertEquals(expectedOperatorId, ticket.getOperatorId(), "Ответственный оператор обращения " + ticket.getTicketId());
    }


    @Test
    public void findOpenTickets_currentStatuses_returnsOnlyOpen() {
        DialogListService dialogService = new DialogListService();
        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", periodStart);
        Ticket annaTicket = annaDialog.getTicketById("TICKET-ANNA-1");

        Dialog borisDialog = dialogService.open("DIALOG-BORIS-1", "CLIENT-BORIS", "TICKET-BORIS-1", periodStart);
        Ticket borisTicket = borisDialog.getTicketById("TICKET-BORIS-1");

        Dialog veraDialog = dialogService.open("DIALOG-VERA-1", "CLIENT-VERA", "TICKET-VERA-1", periodStart);
        Ticket veraTicket = veraDialog.getTicketById("TICKET-VERA-1");

        borisDialog.assignTicket("TICKET-BORIS-1", "OPERATOR-1");
        veraDialog.assignTicket("TICKET-VERA-1", "OPERATOR-1");
        veraDialog.closeTicket("TICKET-VERA-1", "OPERATOR-1");

        List<Ticket> ticketList = dialogService.findOpenTickets(periodStart, periodEnd);
        assertIterableEquals(List.of(annaTicket), ticketList);
        assertEquals(StatusTicket.OPEN, annaTicket.getStatusTicket());
        assertEquals(StatusTicket.ASSIGNED, borisTicket.getStatusTicket());
        assertEquals(StatusTicket.CLOSED, veraTicket.getStatusTicket());
    }

    @Test
    public void findOpenTickets_inclusiveBounds_selectsExactPeriod() {
        DialogListService dialogService = new DialogListService();

        Dialog endDialog = dialogService.open("DIALOG-END", "CLIENT-END", "TICKET-END", periodEnd);
        Ticket endTicket = endDialog.getTicketById("TICKET-END");

        Dialog beforeDialog = dialogService.open("DIALOG-BEFORE", "CLIENT-BEFORE", "TICKET-BEFORE", beforePeriod);
        Ticket beforeTicket = beforeDialog.getTicketById("TICKET-BEFORE");

        Dialog middleDialog = dialogService.open("DIALOG-MIDDLE", "CLIENT-MIDDLE", "TICKET-MIDDLE", middleOfPeriod);
        Ticket middleTicket = middleDialog.getTicketById("TICKET-MIDDLE");

        Dialog afterDialog = dialogService.open("DIALOG-AFTER", "CLIENT-AFTER", "TICKET-AFTER", afterPeriod);
        Ticket afterTicket = afterDialog.getTicketById("TICKET-AFTER");

        Dialog startDialog = dialogService.open("DIALOG-START", "CLIENT-START", "TICKET-START", periodStart);
        Ticket startTicket = startDialog.getTicketById("TICKET-START");

        assertIterableEquals(List.of(startTicket, middleTicket, endTicket), dialogService.findOpenTickets(periodStart, periodEnd));
        assertIterableEquals(List.of(middleTicket), dialogService.findOpenTickets(middleOfPeriod, middleOfPeriod));
    }

    @Test
    public void findOpenTickets_equalTimes_ordersByDialogId() {
        DialogListService dialogService = new DialogListService();
        Dialog borisDialog = dialogService.open("DIALOG-BORIS-1", "CLIENT-BORIS", "TICKET-BORIS-1", periodStart);
        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", periodStart);

        Ticket borisTicket = borisDialog.getTicketById("TICKET-BORIS-1");
        Ticket annaTicket = annaDialog.getTicketById("TICKET-ANNA-1");

        assertIterableEquals(List.of(annaTicket, borisTicket), dialogService.findOpenTickets(periodStart, periodStart));
    }

    @Test
    public void findOpenTickets_noMatches_returnsEmpty() {
        DialogListService dialogService = new DialogListService();
        assertTrue(dialogService.findOpenTickets(periodStart, periodStart).isEmpty());
        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", beforePeriod);
        assertTrue(dialogService.findOpenTickets(periodStart, periodStart).isEmpty());
    }

    @Test
    public void findOpenTickets_invalidPeriod_throws() {
        DialogListService dialogService = new DialogListService();
        assertThrows(IllegalArgumentException.class, () -> dialogService.findOpenTickets(null, periodEnd));
        assertThrows(IllegalArgumentException.class, () -> dialogService.findOpenTickets(periodStart, null));
        assertThrows(IllegalArgumentException.class, () -> dialogService.findOpenTickets(periodEnd, periodStart));
    }

    @Test
    public void ticket_openingTime_isFixedForEachTicket() {
        DialogListService dialogService = new DialogListService();
        Dialog firstDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", beforePeriod);
        Ticket firstTicket = firstDialog.getTicketById("TICKET-ANNA-1");
        assertEquals(beforePeriod, firstTicket.getOpenedAt());

        Dialog secondDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", periodStart);
        Ticket secondTicket = secondDialog.getTicketById("TICKET-ANNA-1");
        assertEquals(firstTicket, secondTicket);
        assertEquals(beforePeriod, secondTicket.getOpenedAt());

        firstDialog.assignTicket("TICKET-ANNA-1", "OPERATOR-1");
        Dialog thirdDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", middleOfPeriod);
        Ticket thirdTicket = thirdDialog.getTicketById("TICKET-ANNA-1");
        assertEquals(firstTicket, thirdTicket);
        assertEquals(beforePeriod, thirdTicket.getOpenedAt());

        firstDialog.closeTicket("TICKET-ANNA-1", "OPERATOR-1");

        Dialog secondDialog2 = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-2", periodEnd);
        Ticket secondTicket2 = secondDialog2.getTicketById("TICKET-ANNA-2");
        assertEquals(beforePeriod, firstTicket.getOpenedAt());
        assertEquals(periodEnd, secondTicket2.getOpenedAt());

        assertThrows(IllegalArgumentException.class, () -> new Ticket("TICKET-DIRECT", "DIALOG-DIRECT", null));
        assertThrows(IllegalArgumentException.class, () -> dialogService.open("DIALOG-MISSING", "CLIENT-MISSING", "TICKET-MISSING", null));
        assertThrows(IllegalArgumentException.class, () -> dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-2", null));
        assertSame(secondTicket2, firstDialog.getTicketById("TICKET-ANNA-2"));
        assertEquals(periodEnd, secondTicket2.getOpenedAt());
        assertFalse(dialogService.getDialogList().containsKey("DIALOG-MISSING"));

    }

    @Test
    public void findOpenTickets_result_keepsSnapshotAndOriginalTickets() {
        DialogListService dialogService = new DialogListService();
        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1", periodStart);
        Ticket annaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        List<Ticket> savedResult = dialogService.findOpenTickets(periodStart, periodEnd);

        assertSame(savedResult.getFirst(), annaTicket);
        assertThrows(UnsupportedOperationException.class, () -> savedResult.add(annaTicket));
        assertThrows(UnsupportedOperationException.class, () -> savedResult.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> savedResult.set(0, annaTicket));

        annaDialog.assignTicket("TICKET-ANNA-1", "OPERATOR-1");
        Dialog borisDialog = dialogService.open("DIALOG-BORIS-1", "CLIENT-BORIS", "TICKET-BORIS-1", middleOfPeriod);
        Ticket borisTicket = borisDialog.getTicketById("TICKET-BORIS-1");

        assertIterableEquals(savedResult, List.of(annaTicket));
        assertEquals(StatusTicket.ASSIGNED, annaTicket.getStatusTicket());
        List<Ticket> newResult = dialogService.findOpenTickets(periodStart, periodEnd);
        assertIterableEquals(newResult, List.of(borisTicket));
    }
}
