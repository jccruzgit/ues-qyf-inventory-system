package sv.edu.ues.qyf.inventory.service;

import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.UserRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserResponseDto;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.RoleRepository;
import sv.edu.ues.qyf.inventory.repository.UserRepository;

@Service
public class DefaultAdminSeederService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdminSeederService.class);

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@qyf.local";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123*";
    private static final String DEFAULT_ADMIN_FULL_NAME = "Administrador del sistema";
    private static final String DEFAULT_ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_ADMIN_ROLE_DESCRIPTION = "System administrator";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;

    public DefaultAdminSeederService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Transactional
    public User ensureDefaultAdmin() {
        return ensureDefaultAdmin(true);
    }

    @Transactional
    public User ensureDefaultAdminSilently() {
        return ensureDefaultAdmin(false);
    }

    private User ensureDefaultAdmin(boolean logWhenAlreadyExists) {
        Optional<User> existingByUsername = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME);
        if (existingByUsername.isPresent()) {
            return logExistingAdmin(existingByUsername.get(), "username", DEFAULT_ADMIN_USERNAME, logWhenAlreadyExists);
        }

        Optional<User> existingByEmail = userRepository.findByEmail(DEFAULT_ADMIN_EMAIL);
        if (existingByEmail.isPresent()) {
            return logExistingAdmin(existingByEmail.get(), "email", DEFAULT_ADMIN_EMAIL, logWhenAlreadyExists);
        }

        Role adminRole = roleRepository.findByName(DEFAULT_ADMIN_ROLE)
                .orElseGet(this::createAdminRole);

        UserResponseDto createdAdmin = userService.createUser(new UserRequestDto(
                DEFAULT_ADMIN_USERNAME,
                DEFAULT_ADMIN_EMAIL,
                DEFAULT_ADMIN_PASSWORD,
                DEFAULT_ADMIN_FULL_NAME,
                Boolean.TRUE,
                AccessScope.ALL_LABS,
                adminRole.getName()));

        User persistedAdmin = userRepository.findById(createdAdmin.getId())
                .orElseThrow(() -> new IllegalStateException("Default admin user was created but could not be reloaded"));

        LOGGER.info(
                "Default admin user created successfully with username '{}' and role '{}'.",
                persistedAdmin.getUsername(),
                persistedAdmin.getRole().getName());

        return persistedAdmin;
    }

    private Role createAdminRole() {
        Role adminRole = roleRepository.save(Role.builder()
                .name(DEFAULT_ADMIN_ROLE)
                .description(DEFAULT_ADMIN_ROLE_DESCRIPTION)
                .build());

        LOGGER.info("ADMIN role did not exist and was created automatically.");
        return adminRole;
    }

    private User logExistingAdmin(User user, String matchType, String matchValue, boolean logWhenAlreadyExists) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "UNKNOWN";
        if (!DEFAULT_ADMIN_ROLE.equals(roleName.toUpperCase(Locale.ROOT))) {
            LOGGER.warn(
                    "Default admin seed skipped because a user with {} '{}' already exists, but its role is '{}'.",
                    matchType,
                    matchValue,
                    roleName);
            return user;
        }

        if (logWhenAlreadyExists) {
            LOGGER.info(
                    "Default admin user already exists with {} '{}'. Seeder skipped.",
                    matchType,
                    matchValue);
        }
        return user;
    }
}
