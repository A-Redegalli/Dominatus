package it.aredegalli.dominatus.service.user;

import it.aredegalli.dominatus.dto.user.UserCreateDto;
import it.aredegalli.dominatus.dto.user.UserDto;
import it.aredegalli.dominatus.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * Create a new user
     *
     * @param userCreateDto
     * @return
     */
    User createUser(UserCreateDto userCreateDto);

    /**
     * Create a new user
     *
     * @param email
     * @param plainPassword
     * @param firstName
     * @param lastName
     * @return
     */
    User createUser(String email, String plainPassword, String firstName, String lastName);

    /**
     * Get a user with decrypted first and last name
     *
     * @param userId
     * @return
     */
    Optional<UserDto> getDecryptedUser(UUID userId);

    /**
     * Get a user with decrypted first and last name
     *
     * @param email
     * @return
     */
    Optional<User> getDecryptedUserByEmail(String email);
}
