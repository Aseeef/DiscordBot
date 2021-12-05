package utils.database.redis.bitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.BotData;
import utils.Utils;
import utils.confighelpers.Config;
import utils.database.redis.bitbucket.wrappers.Commit;
import utils.database.redis.bitbucket.wrappers.Repository;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BitbucketPushes {

    protected final String BASE_URL = "https://stash.grandtheftmc.net/rest/api/1.0/";

    private String projKey;
    private String repoSlug;
    private int authorId;
    private String branchRef;

    public BitbucketPushes(String projKey, String repoSlug, int authorId, String branchRef) {
        this.projKey = projKey;
        this.repoSlug = repoSlug;
        this.authorId = authorId;
        this.branchRef = branchRef;
    }

    public List<Commit> getCommits() {
        // Get all commits matching the parameters
        Commit[] commits = getRecentCommits(this.projKey, this.repoSlug, this.branchRef, 50);
        // Get all commits pushed after the time we last checked
        long time = BotData.LAST_COMMIT_POLL.getData(Long.TYPE);
        BotData.LAST_COMMIT_POLL.setValue(System.currentTimeMillis());
        return Arrays.stream(commits)
                .filter(c -> c.getCommitterTimestamp() > time && c.getCommitter().getId() == this.authorId)
                .collect(Collectors.toList());
    }

    protected String getProjectURL(String project, @Nullable String repo) {
        return BASE_URL + "projects/" + project + "/repos" + (repo == null ? "" : ("/" + repo + "/"));
    }

    public Commit[] getRecentCommits(String project, String repo, String branchRef, int limit) {
        try {

            JSONObject json = executeJsonGet(getProjectURL(project, repo) + "commits?until=" + branchRef + "&limit=" + limit);
            JSONArray vals = json.getJSONArray("values");

            Commit[] commits = new Commit[vals.length()];
            int i = 0;
            for (Object commitJson : vals) {
                ObjectMapper mapper = new ObjectMapper();
                commits[i] = mapper.readValue(commitJson.toString(), Commit.class);
                i++;
            }

            return commits;

        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        return new Commit[0];
    }

    public Repository[] getAllRepos(String project, int limit) {
        try {
            JSONObject json = executeJsonGet(getProjectURL(project, null) + "?limit=" + limit);
            JSONArray vals = json.getJSONArray("values");

            Repository[] repos = new Repository[vals.length()];
            int i = 0;
            for (Object reposJson : vals) {
                ObjectMapper mapper = new ObjectMapper();
                repos[i] = mapper.readValue(reposJson.toString(), Repository.class);
                i++;
            }

            return repos;
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        return new Repository[0];
    }

    /*
    {
         "id":"refs/heads/master",
         "displayId":"master",
         "type":"BRANCH",
         "latestCommit":"7e66f400401dd74ff1af65a65f3bf04beddadeda",
         "latestChangeset":"7e66f400401dd74ff1af65a65f3bf04beddadeda",
         "isDefault":true
      },
     */

    public JSONObject executeJsonGet(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Authorization", "Bearer " + Config.get().getBitbucketToken());

            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();

            return new JSONObject(Utils.convertStreamToString(responseEntity.getContent()));
        }
    }

}
