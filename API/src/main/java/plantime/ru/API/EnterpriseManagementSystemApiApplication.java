package plantime.ru.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения PlanTime API.
 */
@SpringBootApplication
public class EnterpriseManagementSystemApiApplication {

    /**
     * Точка входа в приложение.
     * Инициализирует и запускает Spring Boot приложение с переданными аргументами командной строки.
     *
     * @param args Аргументы командной строки, передаваемые при запуске приложения.
     */
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseManagementSystemApiApplication.class, args);
    }
}