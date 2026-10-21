package dev.stockflow.core.service;

import dev.stockflow.core.model.Dialog;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Ticket;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketOrderService {
    Map<Ticket, List<Order>> ticketList = new LinkedHashMap<>();

    private final DialogListService dialogListService;
    private final OrderService orderService;

    public TicketOrderService(DialogListService dialogListService, OrderService orderService) {
        this.dialogListService = dialogListService;
        this.orderService = orderService;
    }

    public Order createFromTicket(String dialogId, String ticketId) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор тикета");
        }
        String dID = dialogId.trim();
        String cID = ticketId.trim();

        Dialog linkDialog = this.dialogListService.getDialog(dID);
        Ticket linkTicket = linkDialog.getTicketById(cID);
        if (Objects.isNull(linkTicket)) {
            throw new NoSuchElementException("Не найден тикет с таким Id");
        }

        String clientId = linkDialog.getClientId();
        Order order = this.orderService.create(clientId);
        if (this.ticketList.containsKey(linkTicket)) {
            this.ticketList.get(linkTicket).add(order);
        } else {
            this.ticketList.put(linkTicket, new ArrayList<>(Arrays.asList(order)));
        }

        return order;
    }

    public void linkOrder(String dialogId, String ticketId, Order order) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор тикета");
        }
        if (Objects.isNull(order)) {
            throw new IllegalArgumentException("Не указан заказ");
        }
        String dID = dialogId.trim();
        String tID = ticketId.trim();

        Dialog linkDialog = this.dialogListService.getDialog(dID);
        Ticket linkTicket = linkDialog.getTicketById(tID);
        if (Objects.isNull(linkTicket)) {
            throw new NoSuchElementException("Не найден тикет с таким Id");
        }
        if (!linkDialog.getClientId().equals(order.getClientId())) {
            throw new IllegalStateException("id клиента в диалоге не совпадает с id клиента в заказе");
        }
        Ticket link = this.findSource(order);
        if (Objects.nonNull(link) && !link.equals(linkTicket)) {
            throw new IllegalStateException("Этот заказ принадлежит другому тикету");
        }

        if (this.ticketList.containsKey(linkTicket)) {
            if (!this.ticketList.get(linkTicket).contains(order)) {
                this.ticketList.get(linkTicket).add(order);
            }
        } else {
            this.ticketList.put(linkTicket, new ArrayList<>(Arrays.asList(order)));
        }
    }

    public List<Order> findOrders(String dialogId, String ticketId) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор тикета");
        }
        String dID = dialogId.trim();
        String tID = ticketId.trim();

        List<Order> listOrders = new ArrayList<>();
        Dialog linkDialog = this.dialogListService.getDialog(dID);
        Ticket linkTicket = linkDialog.getTicketById(tID);

        if (Objects.isNull(linkTicket)) {
            throw new NoSuchElementException("В диалоге с id:" + dID + " Не найден тикет с таким id: " + tID);
        }
        this.ticketList.forEach((ticket, orders) -> {
            if (ticket.getDialogId().equals(dID) && ticket.getTicketId().equals(tID)) {
                listOrders.addAll(orders);
            }
        });
        return List.copyOf(listOrders);
    }


    public Ticket findSource(Order order) {
        if (Objects.isNull(order)) {
            throw new IllegalArgumentException("Не указан заказ");
        }

        Ticket link = null;
        for (Map.Entry<Ticket, List<Order>> entry : this.ticketList.entrySet()) {
            if (entry.getValue().contains(order)) {
                link = entry.getKey();
            }
        }

        return link;
    }
}
