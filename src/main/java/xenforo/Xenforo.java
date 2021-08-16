package xenforo;

import utils.BotData;
import utils.confighelpers.Config;
import utils.database.XenforoDAO;
import utils.threads.ThreadUtil;
import xenforo.events.DBTicketEvent;
import xenforo.objects.tickets.EventType;
import xenforo.objects.tickets.SupportTicket;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Information and docs related to the XenAPI can be found at https://xenapi.readthedocs.io/
 */
public class Xenforo {

    public static void dbPollTickets() {

        System.out.println("Initializing Xenforo ticket poll task...");

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                long lastTicketRefreshTime = BotData.LAST_TICKET_REFRESH.getData(Long.TYPE);

                List<SupportTicket> activeTickets = XenforoDAO.getPendingTickets();

                for (SupportTicket ticket : activeTickets) {

                    EventType type;

                    if (ticket.getOpenDate() * 1000L > lastTicketRefreshTime) {
                        type = EventType.NEW_TICKET;
                    } else if (ticket.getLastMessageDate() * 1000L > lastTicketRefreshTime) {
                        type = EventType.NEW_MESSAGE;
                    } else if (ticket.getLastUpdate() * 1000L > lastTicketRefreshTime) {
                        type = EventType.TICKET_UPDATE;
                    } else continue;

                    ThreadUtil.runAsync(() -> new DBTicketEvent().onTicketEvent(type, ticket));

                }

                BotData.LAST_TICKET_REFRESH.setValue(System.currentTimeMillis());

            }
        }, 1000 * 10, 1000L * Config.get().getTicketPollingRate());

    }

}
