package dev.stockflow.core;

import dev.stockflow.core.model.Dialog;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Ticket;
import dev.stockflow.core.service.DialogListService;
import dev.stockflow.core.service.OrderService;
import dev.stockflow.core.service.TicketOrderService;
import dev.stockflow.core.state.StatusOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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