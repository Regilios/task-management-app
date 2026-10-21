package dev.stockflow.core.service;

import dev.stockflow.core.model.Dialog;
import dev.stockflow.core.model.Ticket;
import dev.stockflow.core.state.StatusTicket;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DialogListService {
    private Map<String, Dialog> dialogsList = new LinkedHashMap<>();
    private List<Ticket> allTicketsQuery = new ArrayList<>();

    public Dialog open(String dialogId, String clientId, String ticketId) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор диалога");
        }
        if (Objects.isNull(clientId) || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор клиента");
        }
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор обращения");
        }

        String dID = dialogId.trim();
        String cID = clientId.trim();
        String tID = ticketId.trim();

        Dialog d = dialogsList.computeIfAbsent(dID, key -> this.create(dID, cID, tID));
        if (!d.getClientId().equals(cID)) {
            throw new IllegalStateException("ID переданного клиента не соответсвует этому диалогу");
        }

        if (d.getTicketsList().getLast().getStatusTicket().equals(StatusTicket.CLOSED)) {
            Ticket t = d.openTicket(tID);
            allTicketsQuery.add(t);
        }

        return d;
    }

    private Dialog create(String dialogId, String clientId, String ticketId) {
        Dialog dialog = new Dialog(dialogId, clientId, ticketId);
        allTicketsQuery.add(dialog.getTicketsList().getLast());
        return dialog;
    }

    public Map<String, Dialog> getDialogList() {
        return Collections.unmodifiableMap(dialogsList);
    }

    public Dialog getDialog(String dialogId) {
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор диалога");
        }
        String dID = dialogId.trim();
        if (!dialogsList.containsKey(dID)) {
            throw new NoSuchElementException("Диалога с таким id не найдено");
        }
        return dialogsList.get(dID);
    }

    public List<Ticket> getQueryTicket() {
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : allTicketsQuery) {
            if (ticket.getStatusTicket() == StatusTicket.OPEN) {
                tickets.add(ticket);
            }
        }
        return Collections.unmodifiableList(tickets);
    }
}


