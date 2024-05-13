package com.linkcircle.dbmanager.util;

import com.linkcircle.boot.common.exception.BusinessException;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @description:
 * @author: yangyonglian
 * @time: 2021/11/10 10:28
 */
public class ExcelUtil {
    public static ByteArrayOutputStream createExcel(List<List<String>> lists){
        try(XSSFWorkbook workbook = new XSSFWorkbook()){
            // 创建工作薄
            // 创建工作表
            XSSFSheet sheet = workbook.createSheet("sheet1");
            List<String> widthList = lists.get(0);
            for(int i=0;i<widthList.size();i++){
                sheet.setColumnWidth(i,Integer.parseInt(widthList.get(i))*20);
            }

            //设置数据
            for (int row = 1; row < lists.size(); row++) {
                XSSFRow sheetRow = sheet.createRow(row-1);
                for (int column = 0; column < lists.get(row).size(); column++) {
                    XSSFCell xssfCell = sheetRow.createCell(column);
                    xssfCell.setCellValue(lists.get(row).get(column));
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos;
        }catch (Exception e){
            throw new BusinessException("excel写入失败",e);
        }
    }
}
