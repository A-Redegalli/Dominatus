package it.aredegalli.dominatus.service.user;

import it.aredegalli.dominatus.dto.user.UserCreateDto;
import it.aredegalli.dominatus.dto.user.UserDto;
import it.aredegalli.dominatus.model.User;
import it.aredegalli.dominatus.repository.UserRepository;
import it.aredegalli.dominatus.security.encryption.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserCreateDto userCreateDto) {
        return createUser(userCreateDto.getEmail(), userCreateDto.getPlainPassword(), userCreateDto.getFirstName(), userCreateDto.getLastName());
    }

    @Override
    public User createUser(String email, String plainPassword, String firstName, String lastName) {
        String encryptedFirstName = encryptionService.encrypt(firstName);
        String encryptedLastName = encryptionService.encrypt(lastName);
        String hashedPassword = passwordEncoder.encode(plainPassword);

        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .firstName(encryptedFirstName)
                .lastName(encryptedLastName)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public Optional<UserDto> getDecryptedUser(UUID userId) {
        return userRepository.findById(userId).map(user -> {
            user.setFirstName(encryptionService.decrypt(user.getFirstName()));
            user.setLastName(encryptionService.decrypt(user.getLastName()));
            return user.map(user);
        });
    }

    @Override
    public Optional<User> getDecryptedUserByEmail(String email) {
        return userRepository.findByEmail(email).map(user -> {
            user.setFirstName(encryptionService.decrypt(user.getFirstName()));
            user.setLastName(encryptionService.decrypt(user.getLastName()));
            return user;
        });
    }

}
