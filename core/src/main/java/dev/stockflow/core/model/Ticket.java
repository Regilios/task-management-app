package dev.stockflow.core.model;

import dev.stockflow.core.exceprion.TicketException;
import dev.stockflow.core.state.StatusTicket;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Ticket {
    private final Instant openedAt;
    private String ticketId;
    private String operatorId;
    private String dialogId;
    private StatusTicket statusTicket = StatusTicket.OPEN;

    public Ticket(String ticketId, String dialogId, Instant openedAt) {
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new TicketException("Не указан или неверный идентификатор обращения");
        }
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new TicketException("Не указан id диалога");
        }
        if (Objects.isNull(openedAt)) {
            throw new IllegalArgumentException("Не указано время открытия");
        }

        this.ticketId = ticketId.trim();
        this.openedAt = openedAt;
        this.dialogId = dialogId.trim();
    }

    public Ticket(String ticketId, String dialogId) {
        if (Objects.isNull(ticketId) || ticketId.trim().isEmpty()) {
            throw new TicketException("Не указан или неверный идентификатор обращения");
        }
        if (Objects.isNull(dialogId) || dialogId.trim().isEmpty()) {
            throw new TicketException("Не указан id диалога");
        }

        this.ticketId = ticketId.trim();
        this.dialogId = dialogId.trim();
        this.openedAt = Instant.now();
    }


    public void setOperatorId(String operatorId) {
        if (Objects.isNull(operatorId)) {
            throw new IllegalArgumentException("ID оператора не назначен");
        }
        if (operatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID оператора не назначен");
        }
        String oID = operatorId.trim();
        if (!Objects.equals(this.operatorId, oID) && Objects.nonNull(this.operatorId)) {
            throw new IllegalStateException("Нельзя переназначать оператора");
        }

        this.operatorId = oID;
        this.assign(oID);
    }

    public void close(String operatorId) {
        if (Objects.isNull(this.operatorId)) {
            throw new IllegalStateException("Оператор не назначен для этого обращения");
        }
        if (Objects.isNull(operatorId)) {
            throw new IllegalArgumentException("Оператор не назначен для этого обращения");
        }
        if (operatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Оператор не назначен для этого обращения");
        }
        String oID = operatorId.trim();
        if (!this.operatorId.equals(oID)) {
            throw new IllegalStateException("Операция не доступна для этого оператора");
        }
        if (this.getStatusTicket() == StatusTicket.ASSIGNED) {
            this.statusTicket = StatusTicket.CLOSED;
        }
    }

    public void assign(String operatorId) {

        if (this.getStatusTicket() == StatusTicket.CLOSED) {
            throw new IllegalStateException("Нельзя назначить закрытое обращение");
        }
        if (Objects.isNull(operatorId)) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор оператора");
        }
        String oID = operatorId.trim();
        if (oID.isEmpty()) {
            throw new IllegalArgumentException("Не указан или неверный идентификатор оператора");
        }
        if (!Objects.equals(this.operatorId, oID) && Objects.nonNull(this.operatorId)) {
            throw new IllegalStateException("Нельзя переназначать оператора");
        }

        this.operatorId = oID;

        if (this.getStatusTicket() == StatusTicket.OPEN) {
            this.statusTicket = StatusTicket.ASSIGNED;
        }
    }
}
