package com.nexacro17.xeni.data;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xeni.data.exportformats.ExportFormat;
import com.nexacro17.xeni.services.GridExportImportServlet;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.hssf.usermodel.HSSFCell;
import org.apache.poixeni.hssf.usermodel.HSSFRow;
import org.apache.poixeni.hssf.usermodel.HSSFSheet;
import org.apache.poixeni.hssf.usermodel.HSSFWorkbook;

public class GridExportData {
   private DataSet dsCommand = null;
   private DataSet dsStyle = null;
   private DataSet dsCell = null;
   private ExportFormat gridformat = null;
   private String sAppendUrl = null;
   private boolean bAppendExport = false;
   private static final Log oLogger = LogFactory.getLog(GridExportImportServlet.class);
   private long lLastAccTime = 0L;

   public GridExportData() {
      this.dsCommand = new DataSet();
      this.dsStyle = new DataSet();
      this.dsCell = new DataSet();
      this.bAppendExport = false;
   }

   public void setAppendExport(String var1) {
      this.bAppendExport = true;
      this.sAppendUrl = var1;
   }

   public boolean isAppendExport() {
      return this.bAppendExport;
   }

   public String getAppendExportUrl() {
      return this.sAppendUrl;
   }

   public void setCmdDataset(DataSet var1) {
      if (var1 != null) {
         String var2 = this.dsCommand.getName();
         if (var2 == null) {
            this.dsCommand.setName(var1.getName());
            this.dsCommand.copyFrom(var1, true);
            String var3 = var1.getString(0, "format");
            GridExportFormatFactory var4 = new GridExportFormatFactory();
            this.gridformat = var4.readFormatString(var3);
         } else {
            this.dsCommand.copyDataFrom(var1);
         }
      }
   }

   public void setStyleDataset(DataSet var1) {
      if (var1 != null) {
         String var2 = this.dsStyle.getName();
         if (var2 == null) {
            this.dsStyle.setName(var1.getName());
            this.dsStyle.copyFrom(var1, true);
         } else {
            this.dsStyle.copyDataFrom(var1, true);
         }
      }
   }

   public void setCellDataset(DataSet var1) {
      if (var1 != null) {
         String var2 = this.dsCell.getName();
         if (var2 == null) {
            this.dsCell.setName(var1.getName());
            this.dsCell.copyFrom(var1, true);
         } else {
            this.dsCell.copyDataFrom(var1, true);
         }
      }
   }

   public String export(String var1, String var2) {
      HSSFWorkbook var3 = new HSSFWorkbook();
      HSSFSheet var4 = var3.createSheet("test sheet");
      HSSFRow var5 = var4.createRow(0);
      HSSFCell var6 = null;
      var6 = var5.createCell(0);
      var6.setCellValue("Grid");
      var6 = var5.createCell(1);
      var6.setCellValue("Export");
      var6 = var5.createCell(2);
      var6.setCellValue("Import");
      String var7 = this.dsCommand.getString(0, "item") + ".xls";
      String var8 = var1 + "export" + "/" + var7;
      File var9 = new File(var8);
      File var10 = var9.getParentFile();
      if (!var10.exists()) {
         var10.mkdirs();
      }

      try {
         FileOutputStream var11 = new FileOutputStream(var9);
         var3.write(var11);
         var11.close();
         if (oLogger.isInfoEnabled()) {
            oLogger.info("have been saved export file.....[ " + var9.getAbsolutePath() + " ]");
         }
      } catch (Exception var12) {
         if (oLogger.isInfoEnabled()) {
            oLogger.info(var12);
         }
      }

      return var2 + "export" + "/" + var7;
   }

   public DataSet getCmdDataset() {
      return this.dsCommand;
   }

   public DataSet getStyleDataset() {
      return this.dsStyle;
   }

   public DataSet getCellDataset() {
      return this.dsCell;
   }

   public void setLastAccTime(long var1) {
      this.lLastAccTime = var1;
   }

   public long getLastAccTime() {
      return this.lLastAccTime;
   }

   public ExportFormat getGridFormat() {
      return this.gridformat;
   }
}
