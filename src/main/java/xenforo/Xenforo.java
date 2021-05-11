package xenforo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.cadox8.xenapi.XenAPI;
import me.cadox8.xenapi.exceptions.ArgsErrorException;
import me.cadox8.xenapi.reply.AuthenticateReply;
import me.cadox8.xenapi.request.Request;
import me.cadox8.xenapi.request.RequestBuilder;
import me.cadox8.xenapi.request.RequestParam;
import me.cadox8.xenapi.request.RequestType;
import me.cadox8.xenapi.utils.Callback;
import org.json.JSONObject;
import utils.SelfData;
import utils.confighelpers.Config;
import utils.console.Logs;
import utils.database.XenforoDAO;
import utils.tools.GTools;
import xenforo.events.DBTicketEvent;
import xenforo.events.TicketEvent;
import xenforo.objects.Alert;
import xenforo.objects.tickets.EventType;
import xenforo.objects.tickets.SupportTicket;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Information and docs related to the XenAPI can be found at https://xenapi.readthedocs.io/
 */
public class Xenforo {

    public static void login() {
        // Create request
        Request r = RequestBuilder.newRequest(RequestType.AUTHENTICATE)
                .addParam(RequestParam.AUTH_USER, Config.get().getDummyAccountUsername())
                .addParam(RequestParam.AUTH_PASS, Config.get().getDummyAccountPassword())
                .createRequest();
        // Handle Callback
        XenAPI.getInstance().getReply(r, (Callback<AuthenticateReply>) (failCause, result) -> {
            try {
                result.checkError();
                if (failCause != null) {
                    GTools.printStackError(failCause);
                } else {
                    Logs.log("Xenforo login successful!");
                    Logs.log(result.toString());
                }
            } catch (ArgsErrorException e) {
                GTools.printStackError(e);
            }
        });
    }

    public static void dbPollTickets() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                long lastTicketRefreshTime = SelfData.get().getLastTicketRefreshTime();

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

                    GTools.runAsync(() -> new DBTicketEvent().onTicketEvent(type, ticket));

                }

                SelfData.get().setLastTicketRefreshTime(System.currentTimeMillis());

            }
        }, 1000 * 10, 1000L * Config.get().getTicketPollingRate());

    }

    @Deprecated public static void startTicketPolling() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long lastTicketRefreshTime = SelfData.get().getLastTicketRefreshTime();

                Request r = RequestBuilder.newRequest(RequestType.GET_ALERTS)
                        .addParam(RequestParam.VALUE_STRING, Config.get().getDummyAccountUsername())
                        .addParam(RequestParam.TYPE_STRING, "fetchRecent")
                        .createRequest();

                String url = r.getURL(XenAPI.getInstance());

                SelfData.get().setLastTicketRefreshTime(System.currentTimeMillis());
                JSONObject rawReply = GTools.getJsonFromApi(url);

                if (rawReply != null) {
                    JSONObject reply = rawReply.getJSONObject("alerts");
                    Set<String> alertsSet = reply.keySet();

                    alertsSet.forEach( (s) -> {
                        String contentType = reply.getJSONObject(s).getString("content_type");
                        long alertTime = reply.getJSONObject(s).getLong("event_date") * 1000;

                        if (alertTime > lastTicketRefreshTime) {
                            if (contentType.equals("support_ticket")) {
                                Object alertObject = reply.get(s);
                                try {
                                    Alert alert = new ObjectMapper().readValue(alertObject.toString(), Alert.class);
                                    new TicketEvent().onTicketEvent(alert);
                                } catch (JsonProcessingException e) {
                                    GTools.printStackError(e);
                                }
                            }
                        }
                    });
                }

            }
        }, 1000 * 10, 1000L * Config.get().getTicketPollingRate());

    }



}
