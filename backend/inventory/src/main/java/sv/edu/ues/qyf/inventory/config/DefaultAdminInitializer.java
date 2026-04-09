package sv.edu.ues.qyf.inventory.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.service.DefaultAdminSeederService;

@Component
@Order(0)
public class DefaultAdminInitializer implements ApplicationRunner {

    private final DefaultAdminSeederService defaultAdminSeederService;

    public DefaultAdminInitializer(DefaultAdminSeederService defaultAdminSeederService) {
        this.defaultAdminSeederService = defaultAdminSeederService;
    }

    @Override
    public void run(ApplicationArguments args) {
        defaultAdminSeederService.ensureDefaultAdmin();
    }
}
