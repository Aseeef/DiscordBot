package utils.web.clickup;

import commands.bugs.BugReport;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import utils.BotData;
import utils.confighelpers.Config;
import utils.Utils;
import utils.threads.ThreadUtil;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CUTask {

    private static final String CREATE_URL = "https://api.clickup.com/api/v2/list/126053131/task";
    private static final String EDIT_URL_BASE = "https://api.clickup.com/api/v2/task/INSERT_TASK_ID";

    private String title;
    private BugReport.ReportStatus status;
    private String submitterIGN;
    private long submitterDiscordId;
    private String content;
    private int priority;

    public CUTask(String submitterIGN, long submitterDiscordId, String content, BugReport.ReportStatus status, int priority) {
        this.submitterIGN = submitterIGN;
        this.submitterDiscordId = submitterDiscordId;
        this.content = content;
        this.status = status;
        this.priority = priority;

        this.title = submitterIGN + "'s Bug Report [" + this.submitterDiscordId + "]";
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<Void> editTask(String id, BugReport.ReportStatus status) {
        CompletableFuture<Void> futureComplete = new CompletableFuture<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut post = new HttpPut(EDIT_URL_BASE.replace("INSERT_TASK_ID", id));
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Authorization", Config.get().getClickUpKey());

            JSONObject json = new JSONObject()
                    .put("status", status.toString().replace("_", " "))
                    .put("notify_all", true);

            HttpEntity entity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(post);
            HttpEntity responseEntity = response.getEntity();
            JSONObject returnJson = new JSONObject(Utils.convertStreamToString(responseEntity.getContent()));

            if (returnJson.has("err"))
                throw new Exception(returnJson.toString());

            // add to click up ignore so our click up poll task will ignore this update to the bug report
            // because we already msged the player about this bug report update since it was triggered from
            // with in the bot
            HashMap<String, Long> ignoreMap = BotData.CLICKUP_TO_IGNORE.getData(HashMap.class);
            ignoreMap.put(id, returnJson.getLong("date_updated"));
            BotData.save();

            futureComplete.complete(null);
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        return futureComplete;
    }

    public CompletableFuture<String> createTask() {

        CompletableFuture<String> futureId = new CompletableFuture<>();

        ThreadUtil.runAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(CREATE_URL);
                post.addHeader("Content-Type", "application/json");
                post.addHeader("Authorization", Config.get().getClickUpKey());

                JSONObject json = new JSONObject()
                        .put("name", this.title)
                        .put("markdown_description", content)
                        .put("status", this.status.toString().replace("_", " "))
                        .put("priority", priority)
                        .put("notify_all", true);

                HttpEntity entity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
                post.setEntity(entity);
                CloseableHttpResponse response = httpClient.execute(post);
                HttpEntity responseEntity = response.getEntity();
                JSONObject returnJson = new JSONObject(Utils.convertStreamToString(responseEntity.getContent()));

                if (returnJson.has("err"))
                    throw new Exception(returnJson.toString());

                futureId.complete(returnJson.getString("id"));
            }  catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return futureId;

    }
}
