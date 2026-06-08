package dev.jgunsett.inmobiliaria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.security.bootstrap-admin.enabled=false",
		"spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=none"
})
class InmobiliariaApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
