package xenforo;

import utils.SelfData;
import utils.console.Logs;
import utils.tools.GTools;
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
import xenforo.events.TicketEvent;
import xenforo.objects.Alert;

import java.util.*;

public class Xenforo {

    public static void login() {
        // Create request
        Request r = RequestBuilder.newRequest(RequestType.AUTHENTICATE)
                .addParam(RequestParam.AUTH_USER, "Information")
                .addParam(RequestParam.AUTH_PASS, "3q3USp6kj2SnQqB4")
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

    public static void startTicketPolling() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long lastTicketRefreshTime = SelfData.get().getLastTicketRefreshTime();

                Request r = RequestBuilder.newRequest(RequestType.GET_ALERTS)
                        .addParam(RequestParam.VALUE_STRING, "Information")
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
        }, 1000 * 10, 1000 * 60 );

    }



}
