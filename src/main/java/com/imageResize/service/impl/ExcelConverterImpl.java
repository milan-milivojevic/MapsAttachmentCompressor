package com.imageResize.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.imageResize.service.ExcelConverter;

@Service
public class ExcelConverterImpl implements ExcelConverter {

	@Autowired
	private Environment env;

	@Override
	public List<Integer> readFile() {
		List<Integer> result = new ArrayList<Integer>();
		
		File file = new File(env.getProperty("file.nodes.path"));
		System.out.println(file.getName());
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start
			// from first few rows
			for (int i = 0; i < 10 || i < rows; i++) {
				row = sheet.getRow(i);
				if (row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if (tmp > cols)
						cols = tmp;
				}
			}

			for (int r = 1; r < rows; r++) {
				row = sheet.getRow(r);
				if (row != null) {
					HSSFCell firstCell = row.getCell((short) 0);
					if (firstCell != null) {
						Double value = firstCell.getNumericCellValue();
						result.add(value.intValue());
						System.out.println(value.intValue());
					}
				}
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		return result;
	}

}
