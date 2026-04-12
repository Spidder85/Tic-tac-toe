package app.datasource.mapper;

import app.datasource.model.UserData;
import app.domain.model.User;

public class UserDataMapper {
    public UserData toData(User user) {
        return new UserData(user.getId(), user.getLogin(), user.getPassword());
    }

    public User toDomain(UserData userData) {
        if (userData == null) {
            return null;
        }
        return new User(userData.getId(), userData.getLogin(), userData.getPassword());
    }
}
