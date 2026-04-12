package app.web.mapper;

import app.domain.model.User;
import app.web.model.UserDto;

public class UserWebMapper {
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getLogin());
    }
}
