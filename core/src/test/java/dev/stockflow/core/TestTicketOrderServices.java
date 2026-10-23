package dev.stockflow.core;

import dev.stockflow.core.model.Dialog;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Ticket;
import dev.stockflow.core.service.DialogListService;
import dev.stockflow.core.service.OrderService;
import dev.stockflow.core.service.TicketOrderService;
import dev.stockflow.core.state.StatusOrder;
import dev.stockflow.core.state.StatusTicket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTicketOrderServices {

    @Test
    @DisplayName("Создание заказа из обращения")
    public void createFromTicket_knownTicket_storesDraftForDialogClientAndSource() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket firstAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        Order firstOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");

        assertEquals("CLIENT-ANNA", firstOrder.getClientId());
        assertEquals(StatusOrder.DRAFT, firstOrder.getStatusOrder());
        assertTrue(firstOrder.getProducts().isEmpty());

        assertIterableEquals(List.of(firstOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
        assertIterableEquals(List.of(firstOrder), orderService.findByClientId("CLIENT-ANNA"));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(firstOrder));

    }

    @Test
    @DisplayName("Первая привязка, несколько заказов и повтор")
    public void linkOrder_sameTicket_keepsLinkOrderWithoutDuplicates() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket firstAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        Order firstOrder = orderService.create("CLIENT-ANNA");
        Order secondOrder = orderService.create("CLIENT-ANNA");

        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1").isEmpty());
        assertNull(ticketOrderService.findSource(firstOrder));
        assertNull(ticketOrderService.findSource(secondOrder));

        ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-1", secondOrder);
        ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-1", firstOrder);
        ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-1", secondOrder);

        assertIterableEquals(List.of(secondOrder, firstOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(firstOrder));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(secondOrder));
        assertIterableEquals(List.of(firstOrder, secondOrder), orderService.findByClientId("CLIENT-ANNA"));
    }

    @Test
    @DisplayName("Два создания из одного обращения")
    public void createFromTicket_sameTicketTwice_createsTwoDifferentOrders() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket firstAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        Order firstOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");
        Order secondOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");

        assertNotSame(firstOrder, secondOrder);
        assertIterableEquals(List.of(firstOrder, secondOrder), orderService.findByClientId("CLIENT-ANNA"));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(firstOrder));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(secondOrder));
        assertIterableEquals(List.of(firstOrder, secondOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
    }

    @Test
    @DisplayName("Заказ другого клиента")
    public void linkOrder_otherClient_throwsWithoutAddingSource() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Order firstOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");
        Order foreignOrder = orderService.create("CLIENT-BORIS");

        assertNull(ticketOrderService.findSource(foreignOrder));
        assertThrows(IllegalStateException.class, () -> {
            ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-1", foreignOrder);
        });
        assertIterableEquals(List.of(firstOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
        assertNull(ticketOrderService.findSource(foreignOrder));
        assertIterableEquals(List.of(firstOrder), orderService.findByClientId("CLIENT-ANNA"));
    }

    @Test
    @DisplayName("Разные обращения и сохранение источника")
    public void linkOrder_anotherTicket_rejectsSourceChangeAndKeepsSelectionsSeparate() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket firstAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        Order firstOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");
        annaDialog.assignTicket("TICKET-ANNA-1", "OPERATOR-1");
        annaDialog.closeTicket("TICKET-ANNA-1", "OPERATOR-1");

        dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-2");
        Ticket secondAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-2");
        Dialog otherAnnaDialog = dialogService.open("DIALOG-ANNA-2", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket sameIdTicketInOtherDialog = otherAnnaDialog.getTicketById("TICKET-ANNA-1");

        assertEquals(StatusTicket.CLOSED, firstAnnaTicket.getStatusTicket());
        assertEquals("DIALOG-ANNA-1", secondAnnaTicket.getDialogId());
        assertEquals("DIALOG-ANNA-2", sameIdTicketInOtherDialog.getDialogId());

        assertEquals(List.of(firstOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-2").isEmpty());
        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-2", "TICKET-ANNA-1").isEmpty());

        Order secondOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-2");
        assertEquals(List.of(secondOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-2"));
        Throwable throwable = assertThrows(IllegalStateException.class, () -> {
            ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-2", firstOrder);
        });
        assertEquals("Этот заказ принадлежит другому тикету", throwable.getMessage());

        assertEquals(List.of(firstOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
        assertEquals(List.of(secondOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-2"));
        assertSame(firstAnnaTicket, ticketOrderService.findSource(firstOrder));
        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-2", "TICKET-ANNA-1").isEmpty());
        assertEquals(StatusTicket.CLOSED, firstAnnaTicket.getStatusTicket());

    }

    @Test
    @DisplayName("Неизвестные Dialog и Ticket")
    public void operations_unknownDialogOrTicket_throwWithoutCreatingOrdersOrLinks() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Order firstOrder = orderService.create("CLIENT-ANNA");

        assertThrows(NoSuchElementException.class, () -> {
            ticketOrderService.createFromTicket("DIALOG-MISSING", "TICKET-ANNA-1");
        });
        assertThrows(NoSuchElementException.class, () -> {
            ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-MISSING");
        });
        assertThrows(NoSuchElementException.class, () -> {
            ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-MISSING", firstOrder);
        });
        assertThrows(NoSuchElementException.class, () -> {
            ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-MISSING");
        });

        assertEquals(List.of(firstOrder), orderService.findByClientId("CLIENT-ANNA"));
        assertNull(ticketOrderService.findSource(firstOrder));
        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1").isEmpty());
        assertFalse(dialogService.getDialogList().containsKey("DIALOG-MISSING"));
    }

    @Test
    @DisplayName("Недопустимые аргументы")
    public void operations_invalidInput_throwWithoutCreatingOrdersOrLinks() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Order firstOrder = orderService.create("CLIENT-ANNA");
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket(null, "TICKET-ANNA-1");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket("", "TICKET-ANNA-1");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket("  ", "TICKET-ANNA-1");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket("DIALOG-ANNA-1", null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket("DIALOG-ANNA-1", "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.createFromTicket("DIALOG-ANNA-1", "  ");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.linkOrder("DIALOG-ANNA-1", "TICKET-ANNA-1", null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ticketOrderService.findSource(null);
        });

        assertEquals(List.of(firstOrder), orderService.findByClientId("CLIENT-ANNA"));
        assertNull(ticketOrderService.findSource(firstOrder));
        assertTrue(ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1").isEmpty());
    }

    @Test
    @DisplayName("Защита списка и снимок состава")
    public void findOrders_laterLinksAndOrderChanges_keepSnapshotAndOriginalOrders() {
        DialogListService dialogService = new DialogListService();
        OrderService orderService = new OrderService();
        TicketOrderService ticketOrderService = new TicketOrderService(dialogService, orderService);

        Dialog annaDialog = dialogService.open("DIALOG-ANNA-1", "CLIENT-ANNA", "TICKET-ANNA-1");
        Ticket firstAnnaTicket = annaDialog.getTicketById("TICKET-ANNA-1");
        Order firstOrder = ticketOrderService.createFromTicket(" DIALOG-ANNA-1 ", " TICKET-ANNA-1 ");
        List<Order> savedOrders = ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1");

        assertEquals(List.of(firstOrder), savedOrders);
        assertThrows(UnsupportedOperationException.class, () -> {
            savedOrders.add(firstOrder);
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            savedOrders.clear();
        });

        Order secondOrder = ticketOrderService.createFromTicket("DIALOG-ANNA-1", "TICKET-ANNA-1");
        assertEquals(List.of(firstOrder), savedOrders);
        assertEquals(List.of(firstOrder, secondOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));

        firstOrder.cancel();
        assertEquals(StatusOrder.CANCELLED, savedOrders.get(0).getStatusOrder());
        assertSame(firstAnnaTicket, ticketOrderService.findSource(firstOrder));
        assertEquals(List.of(firstOrder, secondOrder), ticketOrderService.findOrders("DIALOG-ANNA-1", "TICKET-ANNA-1"));
    }
}
/*


- assertEquals(expected, actual) — сравнить ожидаемый статус или значение с фактическим.
- assertSame(expected, actual) — проверить тот же объект, например сохранённый firstAnnaTicket и результат findSource(firstOrder).
- assertNotSame(firstOrder, secondOrder) — проверить, что два вызова создания вернули разные объекты. Совпадение клиента и товаров этой проверке не мешает.
- assertIterableEquals(List.of(firstOrder, secondOrder), actualOrders) — сравнить весь состав и порядок списка. Первый аргумент — ожидаемый результат, второй — фактический. Проверка не заменяет отдельное чтение статуса, когда оно требуется сценарием.
- assertNull(actualSource) — проверить отсутствие источника.
- assertThrows(ExceptionType.class, () -> operation()) — выполнить один вызов и проверить тип его исключения. Проверки сохранности после отказа выполняются отдельно, как указано в сценарии.
*/