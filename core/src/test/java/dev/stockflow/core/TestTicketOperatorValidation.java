package dev.stockflow.core;

import dev.stockflow.core.model.Ticket;
import dev.stockflow.core.state.StatusTicket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestTicketOperatorValidation {

    @DisplayName("assign отклоняет недопустимый ID оператора и сохраняет OPEN без оператора")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void assign_invalidOperatorId_throwsWithoutChangingOpenTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");

        assertThrows(IllegalArgumentException.class, () -> ticket.assign(operatorId));
        assertTicketState(ticket, StatusTicket.OPEN, null);
    }

    @DisplayName("setOperatorId отклоняет недопустимый ID до записи в обращение")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void setOperatorId_invalidOperatorId_throwsWithoutChangingOpenTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");

        assertThrows(IllegalArgumentException.class, () -> ticket.setOperatorId(operatorId));
        assertTicketState(ticket, StatusTicket.OPEN, null);
    }

    @DisplayName("close отклоняет недопустимый ID и сохраняет назначение")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void close_invalidOperatorId_throwsWithoutChangingAssignedTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");
        ticket.assign("O-1");

        assertThrows(IllegalArgumentException.class, () -> ticket.close(operatorId));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O-1");
    }

    @Test
    @DisplayName("assign нормализует внешние пробелы и допускает повтор через обе команды назначения")
    void assign_outerSpacesAndEqualStrings_preservesSameOperatorAcrossAssignmentCommands() {
        Ticket ticket = new Ticket("T-1", "D-1");

        ticket.assign(" O 1 ");
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.assign(new String("O 1"));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.setOperatorId(new String(" O 1 "));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");
    }

    @Test
    @DisplayName("setOperatorId согласован с assign; close нормализует ID и допускает повтор владельцем")
    void setOperatorId_outerSpacesAndEqualStrings_allowsAssignmentAndClosingRepeats() {
        Ticket ticket = new Ticket("T-1", "D-1");

        ticket.setOperatorId(" O 1 ");
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.setOperatorId(new String("O 1"));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.assign(new String(" O 1 "));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.close(new String(" O 1 "));
        assertTicketState(ticket, StatusTicket.CLOSED, "O 1");

        ticket.close(new String("O 1"));
        assertTicketState(ticket, StatusTicket.CLOSED, "O 1");
    }

    @DisplayName("assign не считает другой регистр или внутренние пробелы тем же оператором")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @ValueSource(strings = {"o 1", "O1"})
    void assign_differentCaseOrInternalSpaces_throwsWithoutChangingAssignedTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");
        ticket.assign("O 1");

        assertThrows(IllegalStateException.class, () -> ticket.assign(operatorId));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");
    }

    @DisplayName("setOperatorId отклоняет чужой ID без замены прежнего оператора")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @ValueSource(strings = {"o 1", "O1"})
    void setOperatorId_differentCaseOrInternalSpaces_throwsWithoutChangingAssignedTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");
        ticket.assign("O 1");

        assertThrows(IllegalStateException.class, () -> ticket.setOperatorId(operatorId));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");
    }

    @DisplayName("close отклоняет чужой ID до и после закрытия владельцем")
    @ParameterizedTest(name = "{displayName}: [{0}]")
    @ValueSource(strings = {"o 1", "O1"})
    void close_differentCaseOrInternalSpaces_throwsWithoutChangingAssignedOrClosedTicket(String operatorId) {
        Ticket ticket = new Ticket("T-1", "D-1");
        ticket.assign("O 1");

        assertThrows(IllegalStateException.class, () -> ticket.close(operatorId));
        assertTicketState(ticket, StatusTicket.ASSIGNED, "O 1");

        ticket.close("O 1");

        assertThrows(IllegalStateException.class, () -> ticket.close(operatorId));
        assertTicketState(ticket, StatusTicket.CLOSED, "O 1");
    }

    @Test
    @DisplayName("Обе команды назначения отклоняют CLOSED даже для прежнего оператора")
    void assignmentCommands_closedTicket_throwWithoutChangingFinalOperator() {
        Ticket ticket = new Ticket("T-1", "D-1");
        ticket.assign("O 1");
        ticket.close("O 1");

        assertThrows(IllegalStateException.class, () -> ticket.assign(" O 1 "));
        assertTicketState(ticket, StatusTicket.CLOSED, "O 1");

        assertThrows(IllegalStateException.class, () -> ticket.setOperatorId(" O 1 "));
        assertTicketState(ticket, StatusTicket.CLOSED, "O 1");
    }

    private static void assertTicketState(Ticket ticket, StatusTicket expectedStatus, String expectedOperatorId) {
        assertEquals(expectedStatus, ticket.getStatusTicket());
        if (expectedOperatorId == null) {
            assertNull(ticket.getOperatorId());
        } else {
            assertEquals(expectedOperatorId, ticket.getOperatorId());
        }
    }
}
