package plantime.ru.API.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для ответа на запрос проверки токена.
 * Содержит информацию о сотруднике, возвращаемую в ответ на запрос к /verify.
 */
public class VerifyResponse {

        /**
         * Фамилия сотрудника.
         */
        @NotNull(message = "Фамилия сотрудника обязательна")
        private final String surname;

        /**
         * Имя сотрудника.
         */
        @NotNull(message = "Имя сотрудника обязательна")
        private final String firstName;

        /**
         * Отчество сотрудника.
         * Может быть {@code null}, если не указано.
         */
        private final String patronymic;

        /**
         * Уровень прав доступа сотрудника, определяемая из EmployeePermission.
         */
        @NotNull(message = "Уровень прав доступа обязателен")
        private final String role;

        /**
         * Ссылка на фотографию профиля сотрудника.
         */
        @NotNull(message = "Ссылка на фотографию обязательна")
        private final String profilePicture;

        /**
         * Статус сотрудника.
         */
        @NotNull(message = "Статус сотрудника не может быть пустым")
        private final String status;

        /**
         * Конструктор для создания ответа с информацией о сотруднике.
         *
         * @param surname        Фамилия сотрудника.
         * @param firstName      Имя сотрудника.
         * @param patronymic     Отчество сотрудника.
         * @param role           Уровень прав доступа сотрудника.
         * @param profilePicture Ссылка на фотографию профиля.
         * @param status         Статус сотрудника.
         */
        public VerifyResponse(String surname, String firstName, String patronymic, String role,
                              String profilePicture, String status) {
                this.surname = surname;
                this.firstName = firstName;
                this.patronymic = patronymic;
                this.role = role;
                this.profilePicture = "/static/employee/" +  profilePicture;
                this.status = status;
        }

        /**
         * Возвращает фамилию сотрудника.
         *
         * @return Фамилия сотрудника.
         */
        public String getSurname() {
                return surname;
        }

        /**
         * Возвращает имя сотрудника.
         *
         * @return Имя сотрудника.
         */
        public String getFirstName() {
                return firstName;
        }

        /**
         * Возвращает отчество сотрудника.
         *
         * @return Отчество сотрудника или null, если не указано.
         */
        public String getPatronymic() {
                return patronymic;
        }

        /**
         * Возвращает уровень прав доступа сотрудника.
         *
         * @return Уровень прав доступа.
         */
        public String getRole() {
                return role;
        }

        /**
         * Возвращает ссылку на фотографию профиля сотрудника.
         *
         * @return Ссылка на фотографию профиля.
         */
        public String getProfilePicture() {
                return profilePicture;
        }

        /**
         * Возвращает статус сотрудника.
         *
         * @return Статус сотрудника.
         */
        public String getStatus() {
                return status;
        }
}