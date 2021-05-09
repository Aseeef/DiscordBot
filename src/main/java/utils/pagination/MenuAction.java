package utils.pagination;

import net.dv8tion.jda.api.entities.User;

@FunctionalInterface
public interface MenuAction {

    void onAction(Type actionType, User user);

    enum Type {
        NEXT_PAGE,
        PREVIOUS_PAGE,
        DELETE,
    }

}
