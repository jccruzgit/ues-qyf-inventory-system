package sv.edu.ues.qyf.inventory.service;

import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String DEFAULT_ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_ADMIN_ROLE_DESCRIPTION = "System administrator";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final boolean defaultAdminEnabled;
    private final String defaultAdminUsername;
    private final String defaultAdminEmail;
    private final String defaultAdminPassword;
    private final String defaultAdminFullName;

    public DefaultAdminSeederService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserService userService,
            @Value("${app.default-admin.enabled:false}") boolean defaultAdminEnabled,
            @Value("${app.default-admin.username:admin}") String defaultAdminUsername,
            @Value("${app.default-admin.email:admin@qyf.demo}") String defaultAdminEmail,
            @Value("${app.default-admin.password:}") String defaultAdminPassword,
            @Value("${app.default-admin.full-name:Administrador Demo}") String defaultAdminFullName) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.defaultAdminEnabled = defaultAdminEnabled;
        this.defaultAdminUsername = defaultAdminUsername.trim();
        this.defaultAdminEmail = defaultAdminEmail.trim();
        this.defaultAdminPassword = defaultAdminPassword;
        this.defaultAdminFullName = defaultAdminFullName.trim();
    }

    @Transactional
    public Optional<User> ensureDefaultAdmin() {
        return ensureDefaultAdmin(true);
    }

    @Transactional
    public Optional<User> ensureDefaultAdminSilently() {
        return ensureDefaultAdmin(false);
    }

    private Optional<User> ensureDefaultAdmin(boolean logWhenAlreadyExists) {
        if (!defaultAdminEnabled) {
            if (logWhenAlreadyExists) {
                LOGGER.info("Default admin seeding is disabled.");
            }
            return Optional.empty();
        }

        if (isInvalidConfiguration()) {
            LOGGER.warn("Default admin seeding skipped because required configuration is missing.");
            return Optional.empty();
        }

        Optional<User> existingByUsername = userRepository.findByUsername(defaultAdminUsername);
        if (existingByUsername.isPresent()) {
            return logExistingAdmin(existingByUsername.get(), "username", defaultAdminUsername, logWhenAlreadyExists);
        }

        Optional<User> existingByEmail = userRepository.findByEmail(defaultAdminEmail);
        if (existingByEmail.isPresent()) {
            return logExistingAdmin(existingByEmail.get(), "email", defaultAdminEmail, logWhenAlreadyExists);
        }

        Role adminRole = roleRepository.findByName(DEFAULT_ADMIN_ROLE)
                .orElseGet(this::createAdminRole);

        UserResponseDto createdAdmin = userService.createUser(new UserRequestDto(
                defaultAdminUsername,
                defaultAdminEmail,
                defaultAdminPassword,
                defaultAdminFullName,
                Boolean.TRUE,
                AccessScope.ALL_LABS,
                adminRole.getName()));

        User persistedAdmin = userRepository.findById(createdAdmin.getId())
                .orElseThrow(() -> new IllegalStateException("Default admin user was created but could not be reloaded"));

        LOGGER.info(
                "Default admin user created successfully with username '{}' and role '{}'.",
                persistedAdmin.getUsername(),
                persistedAdmin.getRole().getName());

        return Optional.of(persistedAdmin);
    }

    private Role createAdminRole() {
        Role adminRole = roleRepository.save(Role.builder()
                .name(DEFAULT_ADMIN_ROLE)
                .description(DEFAULT_ADMIN_ROLE_DESCRIPTION)
                .build());

        LOGGER.info("ADMIN role did not exist and was created automatically.");
        return adminRole;
    }

    private Optional<User> logExistingAdmin(User user, String matchType, String matchValue, boolean logWhenAlreadyExists) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "UNKNOWN";
        if (!DEFAULT_ADMIN_ROLE.equals(roleName.toUpperCase(Locale.ROOT))) {
            LOGGER.warn(
                    "Default admin seed skipped because a user with {} '{}' already exists, but its role is '{}'.",
                    matchType,
                    matchValue,
                    roleName);
            return Optional.of(user);
        }

        if (logWhenAlreadyExists) {
            LOGGER.info(
                    "Default admin user already exists with {} '{}'. Seeder skipped.",
                    matchType,
                    matchValue);
        }
        return Optional.of(user);
    }

    private boolean isInvalidConfiguration() {
        return defaultAdminUsername.isBlank()
                || defaultAdminEmail.isBlank()
                || defaultAdminPassword == null
                || defaultAdminPassword.isBlank()
                || defaultAdminFullName.isBlank();
    }
}
