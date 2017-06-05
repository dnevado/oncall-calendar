package com.guardias.excel;
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */



import org.apache.poi.xssf.usermodel.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guardias.Guardias;
import com.guardias.Medico;
import com.guardias.Util;
import com.guardias.database.MedicoDBImpl;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * A  monthly calendar created using Apache POI. Each month is on a separate sheet.
 * <pre>
 * Usage:
 * CalendarDemo -xls|xlsx <year>
 * </pre>
 *
 * @author Yegor Kozlov
 */
public class CalendarToExcel {

	/* public CalendarToExcel()
	{
		
	}*/
	
    private static final String[] days = {
            "LUNES", "MARTES", "MIÉRCOLES",
            "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};

    private static final String[]  months = {
            "January", "February", "March","April", "May", "June","July", "August",
            "September","October", "November", "December"};

    public  static  void GenerateExcel(String RutaFile, Calendar calendar, String JSONContenidos) throws IOException
	{
    	
    	
    	 Guardias[] lGuardias ;
 		
	    Gson gson = new GsonBuilder().create();
	   
	    lGuardias = gson.fromJson(JSONContenidos, Guardias[].class); 
    	

        boolean xlsx = true;        
        int year = calendar.get(Calendar.YEAR);
        int  month =  calendar.get(Calendar.MONTH);
        
        DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
        
        
        

        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();

        Map<String, CellStyle> styles = createStyles(wb);
        
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            //create a sheet for each month
            Sheet sheet = wb.createSheet(_format.format(calendar.getTime()));
            
            
            
            CellStyle styleBORDER = wb.createCellStyle();
            styleBORDER.setBorderRight(CellStyle.BORDER_THICK);
            styleBORDER.setBorderBottom(CellStyle.BORDER_THICK);
            styleBORDER.setBorderTop(CellStyle.BORDER_THICK);
            styleBORDER.setBorderLeft(CellStyle.BORDER_THICK);
            styleBORDER.setRightBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            styleBORDER.setLeftBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            styleBORDER.setTopBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            styleBORDER.setBottomBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            //
            
            //turn off gridlines
            sheet.setDisplayGridlines(true);
            sheet.autoSizeColumn(0);
            sheet.setPrintGridlines(true);
            sheet.setFitToPage(true);
            sheet.setHorizontallyCenter(true);
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);

         
            //header with month titles
            Row monthRow = sheet.createRow(1);
            Font fontH =  wb.createFont();
    		CellStyle CStyleH =  wb.createCellStyle();
    		CStyleH.setBorderRight(CellStyle.BORDER_THICK);
    		CStyleH.setRightBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
    		fontH.setBold(true);
            CStyleH.setFont(fontH);
            for (int i = 0; i < days.length; i++) {
         
                Cell monthCell = monthRow.createCell(i);
                
                monthCell.setCellStyle(CStyleH);
                monthCell.setCellValue(days[i]);
                sheet.autoSizeColumn(i);
         
            }

            int cnt = 1, day=1;
            int rownum = 2;
            for (int j = 0; j < 6; j++) {
                Row row = sheet.createRow(rownum++);                
                Row rowGuardias;
                boolean bRowsCreated = false;
                
               // row.setHeightInPoints(100);
                for (int i = 0; i < days.length; i++) {
                    Cell dayCell_1 = row.createCell(i);
                  //  Cell dayCell_2 = row.createCell(i*2 + 1);
                    
                    
                    int currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 7 - calendar.getFirstDayOfWeek()) % 7;		 	
                    //int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);  
                    if(cnt > currentDayOfWeek && calendar.get(Calendar.MONTH) == month) {
                    	
                    	Font font =  wb.createFont();
                		CellStyle CStyle =  wb.createCellStyle();
                		short colorI = HSSFColor.AQUA.index;  // presencia
                		//font.set(colorI);
                		CStyle.setFillForegroundColor(colorI);
                		CStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                		//CStyle.setBorderBottom( colorBorder);
                		CStyle.setBorderRight(CellStyle.BORDER_THICK);
                		CStyle.setBorderBottom(CellStyle.BORDER_THICK);
                		CStyle.setBorderTop(CellStyle.BORDER_THICK);
                		CStyle.setBorderLeft(CellStyle.BORDER_THICK);
                		CStyle.setRightBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
                		CStyle.setLeftBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
                		CStyle.setTopBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
                		CStyle.setBottomBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
                		//CStyle.setFont(font);

                        dayCell_1.setCellValue(day);
                        dayCell_1.setCellStyle(CStyle);
                        
                		
                        sheet.autoSizeColumn(i);
                        

                        String _Dia = _format.format(calendar.getTime());
                                                
                        
                        int DataRowCont = 1; // esto sirve para coger la fila de los datos de cada dia
                        for (int d=0;d<lGuardias.length;d++)
                        {
                        	
                        	Guardias oGuardias = lGuardias[d];
                        	if(oGuardias.getDiaGuardia().equals(_Dia))
                        	{
                        
                        		
                        		if (!bRowsCreated)                        		
                        			rowGuardias = sheet.createRow(rownum++);        		
                        		else
                        			rowGuardias = sheet.getRow(row.getRowNum()+DataRowCont); 
                        			
                                Cell dayCell_1_GUARDIAS = rowGuardias.createCell(i);
                            //    Cell dayCell_2_GUARDIAS = rowGuardias.createCell(i*2 + 1);
                        		
                        		List<Medico> _lMedico = MedicoDBImpl.getMedicos(oGuardias.getIdMedico());
                        		
                        		Medico _oMedico = _lMedico.get(0);
                        		
                        		font =  wb.createFont();
                        		 CStyle =  wb.createCellStyle();
                        		// PRESENCIA 
                        		// LOCALIZADA
                        		//XSSFRichTextString richString = new HSSFRichTextString(_oMedico.getApellidos() + " " + _oMedico.getNombre());
                        		colorI = HSSFColor.LIGHT_ORANGE.index;  // presencia								
								if (oGuardias.getTipo().equals(Util.eTipoGuardia.LOCALIZADA.toString().toLowerCase()))
									colorI = HSSFColor.GREEN.index;
								else
									if (oGuardias.getTipo().equals(Util.eTipoGuardia.REFUERZO.toString().toLowerCase()))
										colorI = HSSFColor.BLUE.index;
									else
										if (oGuardias.getTipo().equals(""))  // residente
											colorI = HSSFColor.RED.index;
								
								
								font.setColor(colorI);
								
								CStyle.setFont(font);
								//CStyle.setBorderBottom( colorBorder);
								CStyle.setBorderRight(CellStyle.BORDER_THICK);
		                		CStyle.setRightBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
								
								

                        		
								 dayCell_1_GUARDIAS.setCellValue(_oMedico.getApellidos() + " " + _oMedico.getNombre() + "[" + _oMedico.getIDMEDICO() + "]");
								 dayCell_1_GUARDIAS.setCellStyle(CStyle);
                        		
                        		
								 DataRowCont++;
                        		
                        	}
                                               
                        
                        }
                        bRowsCreated= true;
                        
                        
                        
                  //      dayCell_1_GUARDIAS.setCellValue(TextoGuardias.toString());
                        
                        calendar.set(Calendar.DAY_OF_MONTH, ++day);

                        /*if(i == 0 || i == days.length-1) {
                            dayCell_1.setCellStyle(styles.get("weekend_left"));
                            dayCell_2.setCellStyle(styles.get("weekend_right"));
                        } else {
                            dayCell_1.setCellStyle(styles.get("workday_left"));
                            dayCell_2.setCellStyle(styles.get("workday_right"));
                        }
                    } else {
                        dayCell_1.setCellStyle(styles.get("grey_left"));
                        dayCell_2.setCellStyle(styles.get("grey_right"));*/
                    }
                    cnt++;
                }
                if(calendar.get(Calendar.MONTH) > month) break;
            }
      //  }

        // Write the output to a file
        String file = RutaFile;        
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();
        
        wb.close();
    	
		
	}
    
    public static void main(String[] args) throws Exception {

    	
    	CalendarToExcel cE = new CalendarToExcel();
    	Calendar c = Calendar.getInstance();
    			c.set(2017, 2, 1);
    	CalendarToExcel.GenerateExcel("d:\\c2.xls", c,"[{\"DiaGuardia\":\"2017-01-01\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-01\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-01\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-02\",\"IdMedico\":\"15\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-02\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-02\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-03\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-03\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-03\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-04\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-04\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-04\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-05\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-05\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-05\",\"IdMedico\":\"5\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-06\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-06\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-06\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-07\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-07\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-07\",\"IdMedico\":\"5\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-08\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-08\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-08\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-09\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-09\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-09\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-10\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-10\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-10\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-11\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-11\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-11\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-12\",\"IdMedico\":\"15\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-12\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-12\",\"IdMedico\":\"5\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-13\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-13\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-13\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-14\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-14\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-14\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-15\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-15\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-15\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-16\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-16\",\"IdMedico\":\"15\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-16\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-17\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-17\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-17\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-18\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-18\",\"IdMedico\":\"15\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-18\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-19\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-19\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-19\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-20\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-20\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-20\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-21\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-21\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-21\",\"IdMedico\":\"4\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-22\",\"IdMedico\":\"15\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-22\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-22\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-23\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-23\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-23\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-24\",\"IdMedico\":\"7\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-24\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-24\",\"IdMedico\":\"1\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-25\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-25\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-25\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-26\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-26\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-26\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-27\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-27\",\"IdMedico\":\"6\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"localizada\"},{\"DiaGuardia\":\"2017-01-27\",\"IdMedico\":\"2\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-28\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-28\",\"IdMedico\":\"8\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-28\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-29\",\"IdMedico\":\"12\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-29\",\"IdMedico\":\"9\",\"adjunto\":1,\"Festivo\":1,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-29\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":1,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-30\",\"IdMedico\":\"13\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-30\",\"IdMedico\":\"10\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-30\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"\"},{\"DiaGuardia\":\"2017-01-31\",\"IdMedico\":\"14\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"presencia\"},{\"DiaGuardia\":\"2017-01-31\",\"IdMedico\":\"11\",\"adjunto\":1,\"Festivo\":0,\"Tipo\":\"refuerzo\"},{\"DiaGuardia\":\"2017-01-31\",\"IdMedico\":\"3\",\"adjunto\":0,\"Festivo\":0,\"Tipo\":\"}]");
    	
    	
    	
           }

    /**
     * cell styles used for formatting calendar sheets
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

        short borderColor = IndexedColors.GREY_50_PERCENT.getIndex();

        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)48);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(titleFont);
        styles.put("title", style);

        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)12);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        monthFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(monthFont);
        styles.put("month", style);

        Font dayFont = wb.createFont();
        dayFont.setFontHeightInPoints((short)14);
        dayFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(dayFont);
        styles.put("weekend_left", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("weekend_right", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderLeft(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setLeftBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        style.setFont(dayFont);
        styles.put("workday_left", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("workday_right", style);

        style = wb.createCellStyle();
        style.setBorderLeft(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("grey_left", style);

        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);
        styles.put("grey_right", style);

        return styles;
    }
}