package net.grandtheftmc.discordbot.xenforo;

import net.grandtheftmc.discordbot.xenforo.objects.tickets.EventType;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.database.XenforoDAO;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.xenforo.events.DBTicketEvent;
import net.grandtheftmc.discordbot.xenforo.objects.tickets.SupportTicket;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Information and docs related to the XenAPI can be found at <a href="https://xenapi.readthedocs.io/">...</a>
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
