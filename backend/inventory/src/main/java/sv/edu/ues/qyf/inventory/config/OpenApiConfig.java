package sv.edu.ues.qyf.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UES QYF Inventory System API")
                        .description("Base backend for the Faculty of Chemistry and Pharmacy inventory system.")
                        .version("v1")
                        .contact(new Contact().name("UES QYF")));
    }
}
