package com.uetty.common.tool.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelOpe {

	/** 时间格式化对象. */
    private static SimpleDateFormat fullTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** workbook对象. */
    private Workbook wb;

    /** 文件输入流. */
    private FileInputStream fileInputStream;
    
    private DateFormat readDateFormat = null;

    /**
     * 仅xls格式成功过
     */
    public ExcelOpe(InputStream is) {
        POIFSFileSystem fs;
        try {
            fs = new POIFSFileSystem(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 先尝试xls，后尝试xlsx
        try {
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            try {
                wb = new XSSFWorkbook(is);
            } catch (IOException e1) {
                throw new RuntimeException(e);
            }
        }

    }

    public ExcelOpe(String filePath) {
        // excle的类型
        String readType = filePath.substring(filePath.lastIndexOf("."));
        try {
            fileInputStream = new FileInputStream(filePath);
            if (".xls".equalsIgnoreCase(readType)) {
                POIFSFileSystem fs = new POIFSFileSystem(fileInputStream);
                wb = new HSSFWorkbook(fs);
            } else {
                wb = new XSSFWorkbook(fileInputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + filePath, e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + filePath, e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + filePath, e);
        }
    }

    /**
     * The Class Point.
     */
    class Point {

        /**
         * E01
         */
        public Point(String cellPositionStr) {
            char[] chars = cellPositionStr.toCharArray();
            int i = 0;
            for (; i < chars.length; i++) {
                if (Character.isDigit(chars[i])) {
                    break;
                }
            }
            row = Integer.parseInt(cellPositionStr.substring(i)) - 1;
            col = cellNumStr2Int(cellPositionStr.substring(0, i));
        }

        /**
         * E, 1
         */
        public Point(int row, String colStr) {
            col = cellNumStr2Int(colStr);
            this.row = row;
        }

        /** 行. */
        private int row;

        /** 列. */
        private int col;
    }

    /**
     * 获取sheet数目。.
     * 
     * @return sheet数目
     */
    public int getSheetCnt() {
        return this.wb.getNumberOfSheets();
    }

    /**
     * 给Excel中的某个sheet的某个单元格赋值。.
     * 
     * @param cellPositionStr 位置参数如A12表示A列，12行。
     * @param sheetNo sheet编号
     * @param v cell的值
     * @return 单元格对象
     */
    public Cell setCellValue(int sheetNo, String cellPositionStr, Object v) {
        Point p = new Point(cellPositionStr);
        return setCellValue(sheetNo, p, v);
    }

    /**
     * 给Excel中的第0个sheet的某个单元格赋值。.
     * 
     * @param cellPositionStr the cell position str
     * @param v cell的值
     * @return 单元格对象
     */
    public Cell setCellValue(String cellPositionStr, Object v) {
        Point p = new Point(cellPositionStr);
        return setCellValue(0, p, v);
    }

    /**
     * 给Excel中的某个sheet的某个单元格赋值。.
     * 
     * @param colNumStr 哪一列
     * @param rowNum 哪一行
     * @param sheetNo sheet编号
     * @param v cell的值
     * @return 单元格对象
     */
    public Cell setCellValue(int sheetNo, int rowNum, String colNumStr, Object v) {
        Point p = new Point(rowNum, colNumStr);
        return setCellValue(sheetNo, p, v);
    }

    /**
     * Sets the cell value.
     * 
     * @param p point独享
     * @param sheetNo sheet编号
     * @param v cell的值
     * @return 单元格对象
     */
    public Cell setCellValue(int sheetNo, Point p, Object v) {
        return setCellValue(sheetNo, p.row, p.col, v);
    }

    /**
     * 给Excel中的某个sheet的某个单元格赋值。.
     * 
     * @param colNum 列号
     * @param rowNum 从0开始。
     * @param sheetNo 从0开始。
     * @param v cell的值
     * @return 单元各对象
     */
    public Cell setCellValue(int sheetNo, int rowNum, int colNum, Object v) {
        return setCellValue(sheetNo, rowNum, colNum, v, null);
    }

    /**
     * Creates the style.
     * 
     * @param color 颜色字符串
     * @return 单元格风格对象
     */
    public CellStyle createStyle(String color) {
        CellStyle cellStyle = wb.createCellStyle();
        Font font = wb.createFont();
        if ("red".equals(color)) {
            font.setColor(HSSFColor.RED.index); // 绿字
        }
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 给Excel中的某个sheet的某个单元格赋值。.
     */
    public Cell setCellValue(int sheetNo, int rowNum, int colNum, Object v, CellStyle cellStyle) {
        Cell cell = this.getCell(sheetNo, rowNum, colNum);
        if (v == null) {
            cell.setCellValue(new HSSFRichTextString(""));
            return cell;
        }

        if (v.getClass() == Boolean.class) {
            cell.setCellValue((Boolean) v);
        } else if (v.getClass() == Integer.class) {
            cell.setCellValue((Integer) v);
        } else if (v.getClass() == Double.class) {
            cell.setCellValue((Double) v);
        } else if (v.getClass() == Float.class) {
            cell.setCellValue((Float) v);
        } else if (v.getClass() == BigDecimal.class) {
            cell.setCellValue(((BigDecimal) v).doubleValue());
        } else if (v instanceof Date) {
            cell.setCellValue(new HSSFRichTextString(fullTimeFmt.format((Date) v)));
        } else if (v.getClass() == String.class) {
            String cellStr = (String) v;
            final int cellStrLen = 32766;
            if (cellStr.length() >= cellStrLen) {
                cellStr = cellStr.substring(0, cellStrLen - 1);
            }
            cell.setCellValue(new HSSFRichTextString(cellStr));
        } else {
            cell.setCellValue(new HSSFRichTextString(v.toString()));
        }
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    /**
     * 根据指定行列和sheet获取单元。.
     */
    public Cell getCell(int sheetNo, int rowNum, int colNum) {
        Row row = getRow(sheetNo, rowNum);
        Cell cell = row.getCell(colNum);
        if (cell == null) {
            cell = row.createCell(colNum);
        }
        return cell;
    }

    /**
     * Gets the cell.
     * 
     * @param colNumStr 列号
     * @param rowNum 行号
     * @param sheetNo sheet号
     * @return 单元格对象
     */
    public Cell getCell(int sheetNo, int rowNum, String colNumStr) {
        int colNum = cellNumStr2Int(colNumStr);
        return getCell(sheetNo, rowNum, colNum);
    }

    /**
     * Gets the cell.
     * 
     * @param cellPositionStr 单元格位置字符串
     * @param sheetNo sheet号
     * @return 单元格对象
     */
    public Cell getCell(int sheetNo, String cellPositionStr) {
        Point p = new Point(cellPositionStr);
        return getCell(sheetNo, p.row, p.col);
    }

    /**
     * Gets the sheet at.
     * 
     * @param num sheet号码
     * @return 返回sheet对象
     */
    public Sheet getSheetAt(int num) {
        return wb.getSheetAt(num);
    }

    /**
     * 合并.
     * 
     * @param sheetNum sheet号
     * @param firstRow 第一行
     * @param lastRow 最后一行
     * @param firstCol 第一列
     * @param lastCol 最后一列
     */
    public void addMergedRegion(int sheetNum, int firstRow, int lastRow, int firstCol, int lastCol) {
        Sheet sheet = getSheetAt(sheetNum);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));// 指定合并区域
    }

    /**
     * 自适应列宽.
     * 
     * @param col 列号
     */
    public void adjustColWidth(int col) {
        adjustColWidth(0, col);
    }

    /**
     * 自适应列宽.
     * 
     * @param sheetNum sheet页码
     * @param col 列号
     */
    public void adjustColWidth(int sheetNum, int col) {
        Sheet sheet = getSheetAt(sheetNum);
        sheet.autoSizeColumn(col, true);// 先写数据，后标题，以便一次性调整宽度
        final int colWidthX = 255;
        final int colWidthY = 256;
        if (sheet.getColumnWidth(col) < colWidthX * colWidthY) {// 最大为255*256宽
            final int wordWidth = 512;
            sheet.setColumnWidth(col, sheet.getColumnWidth(col) + wordWidth);// 汉字调整后总小1-2个字符，256为一个字符宽
        } else {
            sheet.setColumnWidth(col, colWidthX * colWidthY);
        }
    }

    /**
     * 获取某一行。.
     * 
     * @param rowNum the row num
     * @param sheetNo the sheet no
     * @return the row
     */
    public Row getRow(int sheetNo, int rowNum) {
        Sheet sheet = null;
        if (sheetNo >= wb.getNumberOfSheets()) {
            sheet = wb.createSheet("sheet-" + sheetNo);
        } else {
            sheet = wb.getSheetAt(sheetNo);
        }
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    /**
     * 将列的名称转换为数字。.
     * 
     * @param cellNumStr the cell num str
     * @return the int
     */
    public static int cellNumStr2Int(String cellNumStr) {
        cellNumStr = cellNumStr.toLowerCase();
        int cellNum = 0;
        char[] chars = cellNumStr.toCharArray();
        int j = 0;
        final int mathNum = 26;
        for (int i = chars.length - 1; i >= 0; i--) {
            cellNum += (chars[i] - 'a' + 1) * Math.pow(mathNum, j);
            j++;
        }
        return cellNum - 1;
    }

    /**
     * Cell num int to str.
     * 
     * @param colNum the col num
     * @return the string
     */
    public static String cellNumIntToStr(int colNum) {
        String colName = "";
        final int mathNum = 26;
        do {
            char c = (char) (colNum % mathNum + 'A');
            colName = c + colName;
            colNum = colNum / mathNum - 1;
        } while (colNum >= 0);
        return colName;
    }

    /**
     * 将excel写入到某个输出流中。.
     * 
     * @param out the out
     */
    public void write(OutputStream out) {
        try {
            wb.write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将Excel写入到某个文件中.
     * 
     * @param filePath the file path
     */
    public void save(String filePath) {
        try {
            OutputStream out = new FileOutputStream(new File(filePath));
            write(out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取某个单元格的值，并做一定的类型判断。.
     * 
     * @param cell the cell
     * @return the object
     */
    public Object getCellValue(Cell cell) {
        Object value = null;
        if (cell != null) {
            int cellType = cell.getCellType();
            CellStyle style = cell.getCellStyle();
            short format = style.getDataFormat();
            switch (cellType) {
                case Cell.CELL_TYPE_NUMERIC:
                    double numTxt = cell.getNumericCellValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = new Date(0);
                        date = HSSFDateUtil.getJavaDate(new Double(cell.getNumericCellValue()));
                        DateFormat sdf = fullTimeFmt;
                        if (this.readDateFormat != null) {
                        	sdf = this.readDateFormat;
                        }
                        value = sdf.format(date);

                    } else {
                        final int formatInt = 49;
                        final int formatTxt = 176;
                        if (format == formatInt || format == formatTxt) { // 整型或文本
                            value = String.valueOf(Math.round(numTxt));
                        } else {
                            // value = numTxt;
                            value = String.valueOf(Math.round(numTxt));
                        }
                    }// 全部当做文本
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    boolean booleanTxt = cell.getBooleanCellValue();
                    value = booleanTxt;
                    break;
                case Cell.CELL_TYPE_BLANK:
                    value = null;
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    FormulaEvaluator eval = null;
                    if (wb instanceof HSSFWorkbook) {
                        eval = new HSSFFormulaEvaluator((HSSFWorkbook) wb);
                    } else {
                        eval = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
                    }
                    eval.evaluateInCell(cell);
                    value = getCellValue(cell);
                    break;
                case Cell.CELL_TYPE_STRING:
                    RichTextString rtxt = cell.getRichStringCellValue();
                    if (rtxt == null) {
                        break;
                    }
                    String txt = rtxt.getString().replace("　", "").replace(" ", "");
                    value = txt;
                    break;
                default:
            }
        }
        return value;
    }

    /**
     * The Interface CellCallback.
     * 
     * @author qin
     */
    public interface CellCallback {

        /**
         * Handler.
         * 
         * @param cell the cell
         */
        void handler(Cell cell);
    }

    /**
     * 遍历所有的单元格。.
     * 
     * @param callback the callback
     * @param sheetNo the sheet no
     */
    public void iterator(CellCallback callback, int sheetNo) {
        Sheet sheet = wb.getSheetAt(sheetNo);
        if (sheet == null) {
            return;
        }
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = firstRowNum; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                callback.handler(cell);
            }
        }
    }

    /**
     * 读取某个excel，然后将其转化为List的List。.
     * 
     * @param sheetNo the sheet no
     * @return the list
     */
    public List<List<Object>> excelToListList(int sheetNo) {
        // 首先是讲excel的数据读入，然后根据导入到的数据库的结构和excel的结构来决定如何处理。
        Sheet sheet = wb.getSheetAt(sheetNo);
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        List<List<Object>> rows = new ArrayList<List<Object>>();
        for (int i = firstRowNum; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                // System.out.println("Excel.excelToListList()" + i + " filePath="
                // + filePath);
                continue;
            }
            List<Object> cellList = new ArrayList<Object>();
            for (int j = 0; j < row.getLastCellNum(); j++) {// row.getFirstCellNum()
                Object value = null;
                Cell cell = row.getCell(j);
                if (cell != null) {
                    value = getCellValue(cell);
                }

                cellList.add(value);
            }
            rows.add(cellList);
        }
        return rows;
    }

    /**
     * 读取某个excel，然后将其转化为List的List。.
     * 
     * @param sheetNo the sheet no
     * @param lastColNum the last col num
     * @return the list
     */
    public List<List<Object>> excelToListList(int sheetNo, int lastColNum) {
        // 首先是讲excel的数据读入，然后根据导入到的数据库的结构和excel的结构来决定如何处理。
        Sheet sheet = wb.getSheetAt(sheetNo);
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        List<List<Object>> rows = new ArrayList<List<Object>>();
        for (int i = firstRowNum; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                // System.out.println("Excel.excelToListList()" + i + " filePath="
                // + filePath);
                continue;
            }
            List<Object> cellList = new ArrayList<Object>();
            for (int j = 0; j < lastColNum; j++) {// row.getFirstCellNum()
                Object value = null;
                Cell cell = row.getCell(j);
                if (cell != null) {
                    value = getCellValue(cell);
                }

                cellList.add(value);
            }
            rows.add(cellList);
        }
        return rows;
    }

    /**
     * The Interface RowCallBack.
     */
    interface RowCallBack {

        /**
         * Handler.
         * 
         * @param m the m
         */
        void handler(Map<String, Object> m);
    }

    /**
     * 遍历行.
     * 
     * @param sheet sheet
     * @param callBack 行回调类
     * @param keyRowNoFrom 作为key的开始行号 （0,1,2...n）
     * @param keyRowNoTo 作为key的结束行号 （0,1,2...n）
     * @param dataStartRowNo 第一行数据的行号 （1,2...n）
     */
    public void iterateRows(Sheet sheet, RowCallBack callBack, int keyRowNoFrom, int keyRowNoTo, int dataStartRowNo) {
        final int keyListLen = 200;
        String[] keyList = new String[keyListLen];
        for (int i = keyRowNoFrom; i <= keyRowNoTo; i++) {
            Row mapKeyRow = sheet.getRow(i);
            String lstKey = null;
            for (int j = mapKeyRow.getFirstCellNum(); j < mapKeyRow.getLastCellNum(); j++) {
                Cell col = mapKeyRow.getCell(j);
                String key = col.getRichStringCellValue().getString();
                String keyx = keyList[j];
                if (key == null) {
                    key = keyx;
                } else if (keyx != null) {
                    key = keyx + key;
                }
                if (key == null || "".equals(key)) {
                    key = lstKey;
                }
                lstKey = key;

                keyList[j] = key;
            }
        }
        int lastRowNum = sheet.getLastRowNum();
        for (int i = dataStartRowNo; i <= lastRowNum; ++i) {
            Row dataRow = sheet.getRow(i);
            if (dataRow == null) {
                continue;
            }
            Map<String, Object> rowMap = new HashMap<String, Object>();
            for (int j = dataRow.getFirstCellNum(); j < dataRow.getLastCellNum(); ++j) {
                String key = keyList[j];
                if (key == null || "".equals(key)) {
                    continue;
                }
                Object value = getCellValue(dataRow.getCell(j));
                rowMap.put(key, value);
            }
            callBack.handler(rowMap);
        }
    }

    /**
     * 复制srcRowNum，然后在targetRowNum处添加一行。.
     * 
     * @param srcRowNum the src row num
     * @return the row
     */
    public Row createRow(int srcRowNum) {
        Sheet sheet = wb.getSheetAt(0);
        int targetRowNum = sheet.getLastRowNum() + 1;
        return createRow(sheet, sheet, srcRowNum, targetRowNum);
    }

    /**
     * 复制srcRowNum，然后在targetRowNum处添加一行。.
     * 
     * @param srcRowNum the src row num
     * @param targetRowNum the target row num
     * @return the row
     */
    public Row createRow(int srcRowNum, int targetRowNum) {
        Sheet sheet = wb.getSheetAt(0);
        return createRow(sheet, sheet, srcRowNum, targetRowNum);
    }

    /**
     * 复制srcRowNum，然后在targetRowNum处添加一行。.
     * 
     * @param srcSheet the src sheet
     * @param targetSheet the target sheet
     * @param srcRowNum the src row num
     * @param targetRowNum the target row num
     * @return the row
     */
    public Row createRow(Sheet srcSheet, Sheet targetSheet, int srcRowNum, int targetRowNum) {
        Row srcRow = srcSheet.getRow(srcRowNum);
        Row newRow = targetSheet.createRow(targetRowNum);
        newRow.setHeight(srcRow.getHeight());
        int i = 0;
        for (Iterator<Cell> cit = srcRow.cellIterator(); cit.hasNext();) {
            Cell hssfCell = cit.next();
            // Cell中的一些属性转移到Cell中
            Cell cell = newRow.createCell(i++);
            cell.setCellStyle(hssfCell.getCellStyle());
        }
        return newRow;
    }

    /**
     * 删除行.
     * 
     * @param rowNum 行号
     */
    public void deleteRow(int rowNum) {
        deleteRow(0, rowNum);
    }

    /**
     * 删除行.
     * 
     * @param sheetNo sheet次序号(0,1,2...n)
     * @param rowNum 行号
     */
    public void deleteRow(int sheetNo, int rowNum) {
        Sheet sheet = wb.getSheetAt(sheetNo);
        sheet.shiftRows(rowNum, sheet.getLastRowNum(), -1);
    }

    /**
     * 拷贝行粘帖到指定位置。.
     * 
     * @param sheet the sheet
     * @param srcRow the src row
     * @param targetRowNum the target row num
     * @return the row
     */
    public Row copyAndInsertRow(Sheet sheet, Row srcRow, int targetRowNum) {
        sheet.shiftRows(targetRowNum, sheet.getLastRowNum(), 1);
        Row newRow = sheet.getRow(targetRowNum);
        newRow.setHeight(srcRow.getHeight());
        int j = 0;
        for (Iterator<Cell> cit = srcRow.cellIterator(); cit.hasNext();) {
            Cell hssfCell = cit.next();
            // Cell中的一些属性转移到Cell中
            Cell cell = newRow.createCell(j++);
            cell.setCellStyle(hssfCell.getCellStyle());
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.getFirstRow() == srcRow.getRowNum() && region.getLastRow() == region.getFirstRow()) {
                sheet.addMergedRegion(new CellRangeAddress(targetRowNum, region.getFirstColumn(), targetRowNum, region
                        .getLastColumn()));
            }
        }
        return newRow;
    }

    /**
     * Copy and insert row.
     * 
     * @param sheetNo the sheet no
     * @param fromRowNum the from row num
     * @param targetRowNum the target row num
     * @return the row
     */
    public Row copyAndInsertRow(int sheetNo, int fromRowNum, int targetRowNum) {
        Sheet sheet = wb.getSheetAt(sheetNo);
        Row srcRow = sheet.getRow(fromRowNum);
        return copyAndInsertRow(sheet, srcRow, targetRowNum);
    }

    /**
     * Copy and insert row.
     * 
     * @param fromRowNum the from row num
     * @param targetRowNum the target row num
     * @return the row
     */
    public Row copyAndInsertRow(int fromRowNum, int targetRowNum) {
        return copyAndInsertRow(0, fromRowNum, targetRowNum);
    }

    /**
     * 获取工作薄对象.
     * 
     * @return the wb(
     */
    public Workbook getWb() {
        return wb;
    }

    /**
     * 设置工作薄对象.
     * 
     * @param wb the new wb
     */
    public void setWb(Workbook wb) {
        this.wb = wb;
    }

	public DateFormat getReadDateFormat() {
		return readDateFormat;
	}

	public void setReadDateFormat(DateFormat readDateFormat) {
		this.readDateFormat = readDateFormat;
	}
}
