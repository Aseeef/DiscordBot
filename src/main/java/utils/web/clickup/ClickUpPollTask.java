package utils.web.clickup;

import commands.bugs.BugReport;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.BotData;
import utils.Data;
import utils.confighelpers.Config;
import utils.Utils;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ClickUpPollTask extends TimerTask {

    private static final String URL = "https://api.clickup.com/api/v2/list/126053131/task?include_closed=true";

    public ClickUpPollTask() {
        new Timer().scheduleAtFixedRate(this, 1000 * 10, 1000L * Config.get().getClickUpRefreshFrequency()); //todo config
    }

    @Override @SuppressWarnings("unchecked")
    public void run() {

        HashMap<String,Long> dataMap = BotData.CLICKUP_PENDING_BUGS.getData(HashMap.class);

        // process pending bugs: if the updated bug reports haven't had any further updates for 30+ seconds, we process them
        dataMap.entrySet().removeIf((pair) -> {

            BugReport bugReport = (BugReport) Data.obtainData(Data.BUG_REPORTS, pair.getKey());
            long lastUpdated = pair.getValue();
            if (lastUpdated < System.currentTimeMillis() - 1000 * 30) {
                bugReport.sendUpdate(null);
                return true;
            } else return false;
        });

        // check if any new updates have occurred to bug reports on clickup
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(URL);
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Authorization", Config.get().getClickUpKey());
            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();
            JSONArray json = new JSONObject(Utils.convertStreamToString(responseEntity.getContent())).getJSONArray("tasks");
            json.forEach(entry -> {
                JSONObject jo = (JSONObject) entry;
                String id = jo.getString("id");
                long entryLastUpdated = jo.getLong("date_updated");
                long entryCreated = jo.getLong("date_created");
                long botLastUpdate = BotData.LAST_CLICKUP_REFRESH.getData(Long.TYPE);

                HashMap<String, Long> ignoreMap = BotData.CLICKUP_TO_IGNORE.getData(HashMap.class);
                // if a task has the same id as our to-ignore map
                if (ignoreMap.containsKey(id)) {
                    // if the time on to ignore is greater than or equal to current update time, return
                    if (ignoreMap.get(id) >= entryLastUpdated) {
                        return;
                    }
                    // otherwise remove this entry and proceed as planned
                    else {
                        ignoreMap.remove(id);
                    }
                }

                if (entryLastUpdated > botLastUpdate && entryCreated != entryLastUpdated) {
                    try {
                        System.out.println("[Debug] [ClickUpPollTask] Received an update for bug report id " + id + "! Processing...");

                        BugReport.ReportStatus status = BugReport.ReportStatus.valueOf(jo.getJSONObject("status").getString("status").replace(" ", "_").toUpperCase());
                        JSONObject customFields = jo.getJSONArray("custom_fields").getJSONObject(0);
                        boolean hide = customFields.has("value") && customFields.getBoolean("value");
                        String description = jo.getString("text_content");
                        BugReport bugReport = (BugReport) Data.obtainData(Data.BUG_REPORTS, id);
                        if (bugReport == null) {
                            System.err.println("[Debug] [ClickUpPollTask] Failed to process Task Id = " + id);
                            return;
                        }
                        bugReport.setStatus(status);
                        bugReport.setHidden(hide);

                        // this null check is for a very special case that was a one time thing
                        if (bugReport.getReportMessage() == null) {
                            bugReport.setReportMessage(description);
                        }

                        dataMap.put(bugReport.getId(), System.currentTimeMillis());

                    } catch (NumberFormatException | JSONException ignored) {}
                }

            });
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        BotData.LAST_CLICKUP_REFRESH.setValue(System.currentTimeMillis());
        BotData.save();
    }

}
