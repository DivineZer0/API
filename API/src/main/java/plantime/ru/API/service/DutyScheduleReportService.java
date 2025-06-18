package plantime.ru.API.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import plantime.ru.API.dto.DutyScheduleDTO;
import plantime.ru.API.entity.DutySchedule;

import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DutyScheduleReportService {

    // Календарь дежурств и отпусков
    public byte[] generateDutyCalendar(LocalDate periodStart, LocalDate periodEnd, List<DutyScheduleDTO> schedules) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Календарь дежурств");
            int rowNum = 0;

            // Шапка
            Row header = sheet.createRow(rowNum++);
            header.createCell(0).setCellValue("Календарь дежурств и отпусков");
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Дата формирования: " + LocalDate.now());
            Row periodRow = sheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("Период: " + periodStart + " - " + periodEnd);

            // Заголовки столбцов: ФИО, 1, 2, ..., 31 (числа месяца)
            Row colHeader = sheet.createRow(rowNum++);
            colHeader.createCell(0).setCellValue("ФИО");
            int days = periodEnd.getDayOfMonth();
            for (int d = 1; d <= days; d++) {
                colHeader.createCell(d).setCellValue(d);
            }

            // Построим map: ФИО -> [день -> причина/тип]
            Map<String, Map<Integer, DutyScheduleDTO>> empCalendar = new LinkedHashMap<>();
            for (DutyScheduleDTO dto : schedules) {
                String fio = dto.getEmployeeName();
                empCalendar.putIfAbsent(fio, new HashMap<>());
                LocalDate start = dto.getDateStart();
                LocalDate end = dto.getDateEnd();
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    int day = date.getDayOfMonth();
                    empCalendar.get(fio).put(day, dto);
                }
            }

            // Цвета: отпуск, дежурство, больничный и т.д.
            Map<String, IndexedColors> colorMap = new HashMap<>();
            colorMap.put("отпуск", IndexedColors.LIGHT_ORANGE);
            colorMap.put("дежурство", IndexedColors.LIGHT_YELLOW);
            colorMap.put("больничный", IndexedColors.LIGHT_BLUE);
            colorMap.put("отгул", IndexedColors.LIGHT_GREEN);
            colorMap.put("прогул", IndexedColors.ROSE);

            // Стиль по типу
            Map<String, CellStyle> styleMap = new HashMap<>();
            for (Map.Entry<String, IndexedColors> entry : colorMap.entrySet()) {
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(entry.getValue().getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setAlignment(HorizontalAlignment.CENTER);
                styleMap.put(entry.getKey(), style);
            }

            // Заполнение строк сотрудников
            for (Map.Entry<String, Map<Integer, DutyScheduleDTO>> entry : empCalendar.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey()); // ФИО
                Map<Integer, DutyScheduleDTO> daysMap = entry.getValue();
                for (int d = 1; d <= days; d++) {
                    DutyScheduleDTO dto = daysMap.get(d);
                    if (dto != null) {
                        String reason = dto.getTypeOfAbsence().toLowerCase();
                        // Краткое обозначение (фамилия или первая буква)
                        String mark = entry.getKey().split(" ")[0];
                        Cell cell = row.createCell(d);
                        cell.setCellValue(mark);
                        // Цвет по типу
                        CellStyle style = styleMap.getOrDefault(reason, null);
                        if (style != null) cell.setCellStyle(style);
                    }
                }
            }

            // Автоширина
            for (int i = 0; i <= days; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка генерации календаря", e);
        }
    }

    // Заполнение шаблона Test.xlsx
    public byte[] generateFromTemplate(LocalDate periodStart, LocalDate periodEnd, List<DutyScheduleDTO> schedules) {
        try (InputStream template = new ClassPathResource("templates/Test.xlsx").getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(template)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Шапка
            sheet.getRow(12).getCell(148).setCellValue(LocalDate.now().toString()); // ES13
            sheet.getRow(12).getCell(175).setCellValue(periodStart.toString()); // FT13
            sheet.getRow(12).getCell(182).setCellValue(periodEnd.toString()); // GG13

            // Пример заполнения сотрудников (A24 и далее)
            int rowIdx = 23; // A24 - 24-я строка, индекс 23
            int num = 1;
            for (DutyScheduleDTO dto : schedules) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(num++); // A
                row.createCell(8).setCellValue(dto.getEmployeeName()); // I
                row.createCell(35).setCellValue("Должность, таб.номер"); // AJ
                // ... дальнейшее заполнение согласно вашему описанию и шаблону
                rowIdx += 4; // шаг как у вас в примере (A24, A28, ...)
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка генерации по шаблону", e);
        }
    }

    /**
     * Генерирует Excel-файл на основе шаблона с расписанием дежурств и отпусков сотрудников.
     * @param start     Начало периода (начало месяца)
     * @param end       Конец периода (конец месяца)
     * @param schedules Список расписаний (только дежурство и отпуск)
     * @return Массив байтов с заполненным Excel-файлом
     */
    public byte[] generateVacationCalendar(LocalDate start, LocalDate end, List<DutyScheduleDTO> schedules) {
        try (InputStream templateStream = new ClassPathResource("templates/Schedule of on-call and absence of employees.xlsx").getInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Locale locale = new Locale("ru");
            LocalDate now = LocalDate.now();

            // 1. Ячейка C3 — номер месяца (например, Май - 5)
            Cell c3 = getOrCreateCell(sheet, 2, 2); // индексация с нуля (C3 -> row 2, col 2)
            c3.setCellValue(start.getMonthValue());

            // 2. Ячейка D3 — текущая дата ДД.ММ.ГГГГ
            Cell d3 = getOrCreateCell(sheet, 2, 3);
            d3.setCellValue(now.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")));

            // 3. Ячейка A6 — название месяца буквами
            Cell a5 = getOrCreateCell(sheet, 4, 0);
            String monthName = start.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale);
            if (!monthName.isEmpty()) {
                monthName = monthName.substring(0, 1).toUpperCase(locale) + monthName.substring(1);
            }
            a5.setCellValue(monthName);

            // 4. Проставляем числа дней месяца по неделям (A8-G8, A13-G13 и т.д.)
            int month = start.getMonthValue();
            int year = start.getYear();
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int daysInMonth = firstDay.lengthOfMonth();

            int weekBlockHeight = 5; // 1 строка дней + 4 строки под сотрудников
            int tableStartRow = 7;   // A8 (индекс 7)
            int currRow = tableStartRow;
            int currDay = 1;
            Map<Integer, Integer> dayToCol = new HashMap<>(); // dayOfWeek (1-7) -> col (0-6)

            // Записываем недели
            while (currDay <= daysInMonth) {
                // 1. Заполняем строку с днями (A...G)
                Row weekRow = getOrCreateRow(sheet, currRow);
                Arrays.fill(new Object[7], null); // очистка

                // Определяем первый день недели
                LocalDate day = LocalDate.of(year, month, currDay);
                int dow = day.getDayOfWeek().getValue(); // 1 - Monday, ..., 7 - Sunday
                int col = dow == 7 ? 6 : dow - 1; // 0 - Sunday, 1 - Monday, ..., 6 - Saturday
                dayToCol.clear();
                for (int i = col; i < 7 && currDay <= daysInMonth; i++) {
                    Cell cell = getOrCreateCell(weekRow, i);
                    cell.setCellValue(currDay);
                    dayToCol.put(currDay, i);
                    currDay++;
                }

                // 2. Список событий для этой недели (по датам из dayToCol)
                List<LocalDate> weekDates = dayToCol.keySet().stream()
                        .sorted()
                        .map(dayNum -> LocalDate.of(year, month, dayNum))
                        .collect(Collectors.toList());

                List<ScheduleCellData> weekEvents = new ArrayList<>();
                for (LocalDate d : weekDates) {
                    int colIdx = dayToCol.get(d.getDayOfMonth());
                    // Группируем все события этого дня по сотрудникам
                    List<DutyScheduleDTO> eventsForDay = schedules.stream()
                            .filter(s -> !s.getDateStart().isAfter(d) && !s.getDateEnd().isBefore(d))
                            .collect(Collectors.toList());
                    for (DutyScheduleDTO event : eventsForDay) {
                        weekEvents.add(new ScheduleCellData(event, d, colIdx));
                    }
                }
                // Группируем по сотрудникам и датам
                Map<Integer, List<ScheduleCellData>> colToEvents = weekEvents.stream()
                        .collect(Collectors.groupingBy(c -> c.col));

                // 3. Заполняем строки ниже днями (A9-G9:A12-G12)
                int maxRows = colToEvents.values().stream().mapToInt(List::size).max().orElse(1);
                int rowsNeeded = Math.max(4, maxRows);

// Получаем стиль из шаблонной строки
                Row templateRow = getOrCreateRow(sheet, currRow + 1);

// 1. Обрабатываем строку с датами (верхняя граница недели)
                Row datesRow = getOrCreateRow(sheet, currRow);
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    Cell cell = getOrCreateCell(datesRow, colIdx);
                    CellStyle style = workbook.createCellStyle();
                    style.cloneStyleFrom(cell.getCellStyle());
                    style.setBorderTop(BorderStyle.THIN); // Верхняя граница для строки с датами
                    cell.setCellStyle(style);
                }

// 2. Создаем строки с сотрудниками
                for (int rowOffset = 1; rowOffset <= rowsNeeded; rowOffset++) {
                    Row staffRow = getOrCreateRow(sheet, currRow + rowOffset);

                    for (int colIdx = 0; colIdx < 7; colIdx++) {
                        Cell cell = getOrCreateCell(staffRow, colIdx);
                        Cell templateCell = templateRow.getCell(colIdx);

                        if (templateCell != null) {
                            CellStyle newStyle = workbook.createCellStyle();
                            newStyle.cloneStyleFrom(templateCell.getCellStyle());
                            cell.setCellStyle(newStyle);
                        }
                        cell.setCellValue("");
                    }
                }

// 3. Обрабатываем последнюю строку недели (нижняя граница)
                Row lastWeekRow = getOrCreateRow(sheet, currRow + rowsNeeded);
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    Cell cell = getOrCreateCell(lastWeekRow, colIdx);
                    CellStyle style = workbook.createCellStyle();
                    style.cloneStyleFrom(cell.getCellStyle());
                    style.setBorderBottom(BorderStyle.THIN); // Нижняя граница для последней строки недели
                    cell.setCellStyle(style);
                }

// Заполняем данные
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    List<ScheduleCellData> colEvents = colToEvents.getOrDefault(colIdx, Collections.emptyList());
                    for (int rowOffset = 1; rowOffset <= colEvents.size(); rowOffset++) {
                        Row staffRow = getOrCreateRow(sheet, currRow + rowOffset);
                        ScheduleCellData data = colEvents.get(rowOffset - 1);
                        cellFill(cellFor(staffRow, colIdx), data);
                    }
                }

// Перемещаем указатель текущей строки
                currRow += weekBlockHeight + Math.max(0, rowsNeeded - 4);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка генерации VacationCalendar.xlsx: " + ex.getMessage(), ex);
        }
    }

    private static Row getOrCreateRow(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        return row;
    }
    private static Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        return getOrCreateCell(getOrCreateRow(sheet, rowIdx), colIdx);
    }
    private static Cell getOrCreateCell(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        return cell;
    }
    private static Cell cellFor(Row row, int colIdx) {
        return getOrCreateCell(row, colIdx);
    }

    // Цвета для типов отсутствий (hex -> Excel IndexedColor)
    private static final Map<String, Short> absenceTypeToColor = Map.of(
            "выходной", IndexedColors.GREY_25_PERCENT.getIndex(),
            "дежурство", IndexedColors.YELLOW.getIndex(),
            "отгул", IndexedColors.LIGHT_GREEN.getIndex(),
            "прогул", IndexedColors.RED.getIndex(),
            "больничный", IndexedColors.LIGHT_BLUE.getIndex(),
            "больничный лист", IndexedColors.LIGHT_BLUE.getIndex(),
            "отпуск", IndexedColors.ORANGE.getIndex()
    );

    private void cellFill(Cell cell, ScheduleCellData data) {
        // ФИО сотрудника (Фамилия И. О.)
        String[] fioParts = data.dto.getEmployeeName().split(" ");
        String fioShort = fioParts[0];
        if (fioParts.length > 1) fioShort += " " + fioParts[1].charAt(0) + ".";
        if (fioParts.length > 2) fioShort += " " + fioParts[2].charAt(0) + ".";
        String text = fioShort;

        String type = data.dto.getTypeOfAbsence().toLowerCase();

        cell.setCellValue(text);

        Workbook wb = cell.getSheet().getWorkbook();
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        Short colorIdx = absenceTypeToColor.getOrDefault(type, IndexedColors.WHITE.getIndex());
        style.setFillForegroundColor(colorIdx);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }

    private static class ScheduleCellData {
        DutyScheduleDTO dto;
        LocalDate date;
        int col;
        ScheduleCellData(DutyScheduleDTO dto, LocalDate date, int col) {
            this.dto = dto;
            this.date = date;
            this.col = col;
        }
    }
}