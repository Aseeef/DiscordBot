package Utils;

import Utils.tools.GTools;
import Utils.console.Logs;
import me.cadox8.xenapi.XenAPI;
import me.cadox8.xenapi.exceptions.ArgsErrorException;
import me.cadox8.xenapi.reply.AuthenticateReply;
import me.cadox8.xenapi.request.Request;
import me.cadox8.xenapi.request.RequestBuilder;
import me.cadox8.xenapi.request.RequestParam;
import me.cadox8.xenapi.request.RequestType;
import me.cadox8.xenapi.utils.Callback;

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
                e.printStackTrace();
            }
        });
    }

    public static void handleTickets() {
        Request r = RequestBuilder.newRequest(RequestType.GET_ALERTS).createRequest();
        // Handle Callback

    }



}
