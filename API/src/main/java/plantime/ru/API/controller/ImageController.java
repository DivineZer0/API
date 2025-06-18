package plantime.ru.API.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plantime.ru.API.dto.ErrorResponse;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Контроллер для обработки запросов на получение изображений.
 */
@RestController
@RequestMapping("/images/employee")
public class ImageController {

    /**
     * Логер для записи сообщений об ошибках и событиях.
     */
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    /**
     * Базовый путь к директории с изображениями сотрудников в ресурсах.
     */
    private static final String IMAGE_DIR = "static/employee/";

    /**
     * Возвращает изображение сотрудника по имени файла.
     *
     * @param filename Имя файла изображения, указанное в запросе.
     * @return Ответ с изображением в случае успеха, статус 404, если файл не найден,
     *         или статус 400 с сообщением об ошибке в случае некорректного запроса.
     */
    @GetMapping("/{filename}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        try {
            if (filename.contains("..") || filename.contains("/")) {
                logger.error("Некорректное имя файла: {}", filename);
                return ResponseEntity
                        .status(400)
                        .body(new ErrorResponse("Некорректное имя файла", "Неверный запрос", 400));
            }

            Path filePath = Paths.get(IMAGE_DIR, filename).normalize();
            Resource resource = new ClassPathResource(filePath.toString());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                logger.warn("Файл не найден: {}", filename);
                return ResponseEntity
                        .status(404)
                        .body(new ErrorResponse("Изображение не найдено", "Файл отсутствует", 404));
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении изображения {}: {}", filename, e.getMessage());
            return ResponseEntity
                    .status(400)
                    .body(new ErrorResponse("Ошибка при получении изображения: " + e.getMessage(), "Неверный запрос", 400));
        }
    }

    /**
     * Определяет Тип изображения на основе расширения файла.
     *
     * @param filename Имя файла изображения.
     * @return Тип изображения или "image/jpeg" по умолчанию.
     */
    private String determineContentType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFilename.endsWith(".webp")) {
            return "image/webp";
        }
        logger.warn("Неизвестное расширение файла {}, используется image/jpeg по умолчанию", filename);
        return "image/jpeg";
    }
}