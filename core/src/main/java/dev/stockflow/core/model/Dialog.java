package dev.stockflow.core.model;

import dev.stockflow.core.exceprion.DialogException;
import dev.stockflow.core.exceprion.TicketException;
import dev.stockflow.core.service.MessageListService;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

public class Dialog {
    @Getter
    private String dialogId;
    @Getter
    private String clientId;
    @Getter
    MessageListService messageListService = new MessageListService();
    List<Ticket> ticketsList = new ArrayList<>();

    public Dialog(String dialogId, String clientId, String ticketId, Instant openedAt) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new DialogException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(clientId) || clientId.trim().isEmpty()) {
            throw new DialogException("Не указан или неверный идентификатор клиента");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new TicketException("Не указан или неверный идентификатор обращения");
        }
        if (Objects.isNull(openedAt) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указано время открытия");
        }
        this.dialogId = dialogId.trim();
        this.clientId = clientId.trim();
        this.openTicket(ticketId.trim(), openedAt);
    }

    public Dialog(String dialogId, String clientId, String ticketId) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new DialogException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(clientId) || clientId.trim().isEmpty()) {
            throw new DialogException("Не указан или неверный идентификатор клиента");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new TicketException("Не указан или неверный идентификатор обращения");
        }
        this.dialogId = dialogId.trim();
        this.clientId = clientId.trim();
        this.openTicket(ticketId.trim());
    }

    public Ticket getTicketById(String ticketId) {
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new TicketException("Не указан или неверный идентификатор обращения");
        }
        Ticket ticket = null;
        String tId = ticketId.trim();
        for (Ticket t : ticketsList) {
            if (tId.equals(t.getTicketId())) {
                ticket = t;
            }
        }
        return ticket;
    }

    public List<Ticket> getTicketsList() {
        List<Ticket> tList = List.copyOf(ticketsList);
        return tList;
    }

    public void addMessage(Message message) {
        messageListService.addMessage(message);
    }

    public Ticket openTicket(String ticketId, Instant openedAt) {
        Ticket ticket = new Ticket(ticketId, dialogId, openedAt);
        ticketsList.add(ticket);
        return ticket;
    }

    public Ticket openTicket(String ticketId) {
        Ticket ticket = new Ticket(ticketId, dialogId);
        ticketsList.add(ticket);
        return ticket;
    }

    public void closeTicket(String ticketId, String operatorId) {
        if (Objects.isNull(operatorId) || operatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор оператора");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор обращения");
        }
        String oID = operatorId.trim();
        String tID = ticketId.trim();
        Ticket ticket = null;
        for (Ticket t : ticketsList) {
            if (tID.equals(t.getTicketId())) {
                ticket = t;
            }
        }
        ticket.close(oID);
    }

    public void assignTicket(String ticketId, String operatorId) {
        if (Objects.isNull(operatorId) || operatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор оператора");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор обращения");
        }
        String oID = operatorId.trim();
        String tID = ticketId.trim();
        Ticket ticket = null;
        for (Ticket t : ticketsList) {
            if (tID.equals(t.getTicketId())) {
                ticket = t;
            }
        }
        ticket.assign(oID);
    }
}
