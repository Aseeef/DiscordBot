package net.grandtheftmc.discordbot.utils.web.clickup;

import net.grandtheftmc.discordbot.commands.bugs.BugReport;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;

import javax.annotation.Nullable;
import java.util.*;

public class ClickUpPollTask implements Runnable {

    private static final String URL = "https://api.clickup.com/api/v2/list/126053131/task?include_closed=true";
    private static final String COMMENT_URL = "https://api.clickup.com/api/v2/task/INSERT_TASK_ID/comment/";

    public ClickUpPollTask() {
        ThreadUtil.runTaskTimer(this, 1000 * 10, 1000L * Config.get().getClickUpRefreshFrequency());
    }

    @Override @SuppressWarnings("unchecked")
    public void run() {

        HashMap<String,Long> dataMap = BotData.CLICKUP_PENDING_BUGS.getData(HashMap.class);

        // process pending bugs: if the updated bug reports haven't had any further updates for 30+ seconds, we process them
        dataMap.entrySet().removeIf((pair) -> {
            System.out.println("[Debug] [ClickUpPollTask] Finishing processing bug report id=" + pair.getKey());
            try {
                BugReport bugReport = (BugReport) Data.obtainData(Data.BUG_REPORTS, pair.getKey());
                long lastUpdated = pair.getValue();
                if (lastUpdated < System.currentTimeMillis() - (1000L * Config.get().getClickUpWaitDuration())) {

                    CUComment comment = getLastComment(bugReport.getId());
                    bugReport.sendUpdate(comment == null ? null : comment.getComment());

                    return true;
                } else return false;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        });

        String rawResponse = null;

        // check if any new updates have occurred to bug reports on clickup
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(URL);
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Authorization", Config.get().getClickUpKey());
            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();
            rawResponse = Utils.convertStreamToString(responseEntity.getContent());
            JSONArray json = new JSONObject(rawResponse).getJSONArray("tasks");
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
                        BotData.save();
                    }
                }

                if (entryLastUpdated > botLastUpdate && entryCreated != entryLastUpdated) {
                    try {
                        System.out.println("[Debug] [ClickUpPollTask] Received an update for bug report id " + id + "!");

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

                        dataMap.put(bugReport.getId(), entryLastUpdated);

                    } catch (NumberFormatException | JSONException ignored) {}
                }

            });
        }  catch (Exception ex) {
            if (rawResponse != null) {
                System.err.println("An error occurred while processing the following http response:");
                System.err.println(rawResponse);
            }
            ex.printStackTrace();
        }

        BotData.LAST_CLICKUP_REFRESH.setValue(System.currentTimeMillis());
        BotData.save();
    }

    private static @Nullable CUComment getLastComment(String task) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(COMMENT_URL.replace("INSERT_TASK_ID", task));
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Authorization", Config.get().getClickUpKey());
            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();
            JSONArray json = new JSONObject(Utils.convertStreamToString(responseEntity.getContent())).getJSONArray("comments");

            System.out.println("[Debug] [ClickUpPollTask] " + json.length() + " report comments found for bug report id " + task + "! Processing latest...");
            for (Object o : json) {
                JSONObject jo = (JSONObject) o;
                String id = jo.getString("id");
                String comment = jo.getString("comment_text");
                String commenter = jo.getJSONObject("user").getString("username");
                long commentTime = jo.getLong("date");

                return new CUComment(id, comment, commentTime, commenter);
            }
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
