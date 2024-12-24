package org.start2do.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.start2do.dto.BusinessException;

@Slf4j
@UtilityClass
public class ExcelUtil {


    @SneakyThrows
    public String toCsv(List list, Class clazz) {
        StringJoiner joiner = new StringJoiner("\r\n");
        StringJoiner titleStr = new StringJoiner(",");
        LinkedList<Field> fields = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelSetting setting = field.getDeclaredAnnotation(ExcelSetting.class);
            String title = null;
            if (setting == null) {
                continue;
            } else {
                if (setting.skin()) {
                    continue;
                }
                title = setting.value();

            }
            field.setAccessible(true);
            titleStr.add(title);
            fields.add(field);
        }
        joiner.add(titleStr.toString());
        for (Object o : list) {
            StringJoiner line = new StringJoiner(",");
            for (Field field : fields) {
                Object obj = field.get(o);
                if (obj == null) {
                    line.add("");
                } else {
                    line.add(obj.toString());
                }
            }
            joiner.add(line.toString());
        }
        return joiner.toString();
    }


    public static <T> void write(OutputStream outputStream, Class<T> tClass, String sheetName, List<T> pojos) {
        XSSFWorkbook workbook = newWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        //使用反射获取所有字段名称,并且获取ExcelSetting注解

        // 使用反射获取所有字段名称,并且获取ExcelSetting注解
        List<Field> fields = new LinkedList<>();
        for (Field field : tClass.getDeclaredFields()) {
            ExcelSetting setting = field.getAnnotation(ExcelSetting.class);
            if (setting != null && !setting.skin()) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        // 填充数据
        for (int rowIndex = 0; rowIndex < pojos.size(); rowIndex++) {
            Row row = createRow(sheet, rowIndex);
            T pojo = pojos.get(rowIndex);
            for (int colIndex = 0; colIndex < fields.size(); colIndex++) {
                Field field = fields.get(colIndex);
                ExcelSetting setting = field.getAnnotation(ExcelSetting.class);
                Cell cell = createCell(row, colIndex);
                if (rowIndex == 0) {
                    if (setting != null) {
                        if (setting.skin()) {
                            continue;
                        }
                        cell.setCellValue(setting.value());
                    } else {
                        cell.setCellValue(field.getName());
                    }
                    continue;
                }
                try {
                    Object value = field.get(pojo);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // 写入输出流
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 辅助方法，将行列坐标转换为单元格地址
     */
    private static String convertToCellAddress(int row, int col) {
        // 将列坐标转换为字母
        StringBuilder colLetters = new StringBuilder();
        while (col >= 0) {
            int modulo = (col % 26) + 65;
            colLetters.insert(0, (char) modulo);
            col = (col / 26) - 1;
        }

        // 行坐标转换为数字
        String rowNum = String.valueOf(row + 1);

        // 组合行列坐标为单元格地址
        return colLetters.toString() + rowNum;
    }

    public static void setBorderedCellStyle(Sheet sheet, int firstRow, int lastRow, int firstCell, int lastCell,
        Consumer<Cell> consumer) {
        for (int rowIndex = firstRow; rowIndex <= lastCell; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            for (int colIndex = firstCell; colIndex <= lastCell; colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                    cell.setCellValue("");
                }
                consumer.accept(cell);
            }
        }
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExcelSetting {

        /**
         * 标题
         */
        String value();

        boolean skin() default false;

    }

    public static void resetCellMaxTextLength() {
        SpreadsheetVersion excel2007 = SpreadsheetVersion.EXCEL2007;
        if (Integer.MAX_VALUE != excel2007.getMaxTextLength()) {
            Field field;
            try {
                field = excel2007.getClass().getDeclaredField("_maxTextLength");
                field.setAccessible(true);
                field.set(excel2007, Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void createCell(Row row, int beginIndex, int endIndex,
        BiFunction<Integer, Cell, CellStyle> function) {
        for (int i = beginIndex; i < endIndex; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                continue;
            }
            cell = row.createCell(i);
            if (function != null) {
                CellStyle style = function.apply(i, cell);
                if (style != null) {
                    cell.setCellStyle(style);
                }
            }
        }
    }

    public static void addMergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    public static XSSFWorkbook newWorkbook() {
        return new XSSFWorkbook();
    }

    public static XSSFFont createFont(XSSFWorkbook workbook, String fontName, int size, boolean isBold) {
        XSSFFont font = workbook.createFont();
        font.setFontName(fontName);
        font.setFontHeightInPoints((short) size);
        font.setBold(isBold);
        return font;
    }

    public static CellStyle createCellStyle(XSSFWorkbook workbook, XSSFFont font, HorizontalAlignment alignment) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(alignment);
        if (font != null) {
            cellStyle.setFont(font);
        }
        return cellStyle;

    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class HeaderInfo {

        private String property;
        private String address;
        private String text;
        private CellStyle cellStyle;
        private CellRangeAddress rangeAddress;
        private String defaultValue = "";

        public HeaderInfo(String property, String address, String text, CellStyle cellStyle,
            CellRangeAddress rangeAddress) {
            this.property = property;
            this.address = address;
            this.text = text;
            this.cellStyle = cellStyle;
            this.rangeAddress = rangeAddress;
        }
    }

    public static int[] convertToCoordinates(String cellAddress) {
        int row = Integer.parseInt(cellAddress.replaceAll("[^0-9]", ""));
        String columnString = cellAddress.replaceAll("[0-9]", "");
        int column = columnStringToNumber(columnString);
        return new int[]{row - 1, column};
    }

    // 将列字母转换为数字
    private static int columnStringToNumber(String column) {
        int result = 0;
        for (char ch : column.toCharArray()) {
            result *= 26;
            result += (ch - 'A');
        }
        return result;
    }

    public static Row createRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    public static Cell createCell(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            cell = row.createCell(cellNum);
        }
        return cell;
    }

    public static void writeData(Workbook workbook, Sheet sheet, List<HeaderInfo> headers,
        List<Map<String, Object>> data, CellStyle baseStyle) {
        for (int p = 0; p < headers.size(); p++) {
            HeaderInfo header = headers.get(p);
            int height = 0;
            int[] ints = convertToCoordinates(header.getAddress());
            int rowNum = ints[0];
            int cellNum = ints[1];
            CellRangeAddress rangeAddress = header.getRangeAddress();
            if (rangeAddress != null) {
                sheet.addMergedRegion(rangeAddress);
                height = rangeAddress.getLastRow() - rangeAddress.getFirstRow();
                if (height >= 1) {
                    for (int i1 = 0; i1 < height; i1++) {
                        createRow(sheet, rowNum + i1);
                    }
                }
            }
            Row row = createRow(sheet, rowNum);
            Cell cell = createCell(row, cellNum);
            if (header.getCellStyle() != null) {
                cell.setCellStyle(header.getCellStyle());
            } else {
                if (cell.getCellStyle() == null) {
                    cell.setCellStyle(baseStyle);
                }
            }
            cell.setCellValue(header.getText());
            //准备填充数据
            if (StringUtils.isNotEmpty(header.getProperty())) {
                int rowHeight = 1;
                if (rangeAddress != null) {
                    rowHeight = (rangeAddress.getLastRow() - rangeAddress.getFirstRow()) + 1;
                }
                for (int i = 0; i < data.size(); i++) {
                    Map<String, Object> map = data.get(i);
                    int dataRowNum = rowNum + rowHeight + i;
                    Row dataBaseRow = sheet.getRow(dataRowNum);
                    if (dataBaseRow == null) {
                        dataBaseRow = sheet.createRow(dataRowNum);
                    }
                    Object object = map.get(header.getProperty());

                    Cell valueCell = dataBaseRow.getCell(cellNum);
                    if (valueCell == null) {
                        valueCell = dataBaseRow.createCell(cellNum);
                    }
                    if (baseStyle != null) {
                        valueCell.setCellStyle(baseStyle);
                    }
                    if (object == null) {
                        valueCell.setCellValue(header.getDefaultValue());
                    } else {
                        valueCell.setCellValue(object.toString());
                    }
                }
            }

        }

    }

    public static CellRangeAddress buildCellRangeAddress(String address, String address2) {
        int[] ints = convertToCoordinates(address);
        int[] int2 = convertToCoordinates(address2);
        return new CellRangeAddress(ints[0], int2[0], ints[1], int2[1]);
    }

    public static void read(InputStream inputStream, ReadListener listener) {
        read(inputStream, null, 1, listener);
    }

    public static void read(InputStream inputStream, String sheet, Integer headerRowNumber, ReadListener listener) {
        try {
            if (listener == null) {
                throw new BusinessException("回调函数不能为空");
            }
            XSSFWorkbook sheets = new XSSFWorkbook(inputStream);
            XSSFSheet rows = null;
            if (StringUtils.isEmpty(sheet)) {
                rows = sheets.getSheetAt(0);
            } else {
                rows = sheets.getSheet(sheet);
            }
            if (headerRowNumber == null) {
                headerRowNumber = 0;
            }
            for (int i = headerRowNumber; i < rows.getLastRowNum(); i++) {
                Row row = rows.getRow(i);
                Object[] objects = new Object[row.getLastCellNum()];
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    objects[j] = cell.getStringCellValue();
                }
                listener.invoke(i, objects);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("读取Excel失败");
        }

    }


    public interface ReadListener {

        /**
         * 设置表头
         */
        void setHeader(Map<Integer, String> headerMap);

        /**
         * 设置数据
         */
        void invoke(Integer rowNumber, Object[] objects);
    }
}
