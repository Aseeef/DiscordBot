package utils.web.clickup;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class CUComment {

    private String commentId;
    private String comment;
    private long time;
    private String commenter;

}
