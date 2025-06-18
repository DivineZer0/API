package plantime.ru.API.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO для передачи данных о должности сотрудников.
 * Используется для передачи данных между слоями приложения и валидации входящего запроса.
 */
public class EmployeePostDTO {

    /**
     * Уникальный идентификатор должности.
     */
    private Integer idEmployeePost;

    /**
     * Название должности.
     */
    @NotBlank(message = "Название должности обязательно для заполнения.")
    @Size(min = 3, max = 40, message = "Название должности должно содержать от 3 до 40 символов.")
    private String post;

    /**
     * Идентификатор уровня прав доступа, связанного с должностью.
     */
    @NotNull(message = "Идентификатор уровня прав доступа обязателен.")
    private Integer idEmployeePermission;

    /**
     * Конструктор по умолчанию.
     */
    public EmployeePostDTO() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param idEmployeePost       Идентификатор должности.
     * @param post                 Название должности.
     * @param idEmployeePermission Идентификатор уровня прав доступа.
     */
    public EmployeePostDTO(Integer idEmployeePost, String post, Integer idEmployeePermission) {
        this.idEmployeePost = idEmployeePost;
        this.post = post;
        this.idEmployeePermission = idEmployeePermission;
    }

    /**
     * Получить идентификатор должности.
     */
    public Integer getIdEmployeePost() {
        return idEmployeePost;
    }

    /**
     * Установить идентификатор должности.
     */
    public void setIdEmployeePost(Integer idEmployeePost) {
        this.idEmployeePost = idEmployeePost;
    }

    /**
     * Получить название должности.
     */
    public String getPost() {
        return post;
    }

    /**
     * Установить название должности.
     */
    public void setPost(String post) {
        this.post = post;
    }

    /**
     * Получить идентификатор уровня прав доступа.
     */
    public Integer getIdEmployeePermission() {
        return idEmployeePermission;
    }

    /**
     * Установить идентификатор уровня прав доступа.
     */
    public void setIdEmployeePermission(Integer idEmployeePermission) {
        this.idEmployeePermission = idEmployeePermission;
    }
}