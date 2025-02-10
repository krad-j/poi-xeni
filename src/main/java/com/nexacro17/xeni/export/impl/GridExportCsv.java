package com.nexacro17.xeni.export.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xeni.data.GridExportData;
import com.nexacro17.xeni.data.exportformats.ExportFormat;
import com.nexacro17.xeni.data.exportformats.FormatCell;
import com.nexacro17.xeni.export.GridExportBase;
import com.nexacro17.xeni.extend.XeniDataValidationBase;
import com.nexacro17.xeni.extend.XeniDataValidationFactory;
import com.nexacro17.xeni.extend.XeniExcelDataStorageBase;
import com.nexacro17.xeni.extend.XeniExcelDataStorageFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GridExportCsv implements GridExportBase {
   private String errmsg = "";
   private String fileurl = "";
   private String filepath = "";
   private GridExportData exportdata = null;
   private boolean csvquote = true;
   private boolean csvbom = true;
   private boolean addedrow = false;
   private char separator = ',';
   private String quoteCharStr = "";
   private static final Log logger = LogFactory.getLog(GridExportCsv.class);
   private XeniDataValidationBase validator;

   public void GridExportCsv() {
   }

   @Override
   public void setExportType(String var1) {
   }

   private void setErrMessage(String var1) {
      this.errmsg = var1;
      if (logger.isInfoEnabled()) {
         logger.info(var1);
      }
   }

   @Override
   public String getErrorMessage() {
      return this.errmsg;
   }

   @Override
   public void setExportData(GridExportData var1) {
      this.exportdata = var1;
   }

   @Override
   public void setExportFilePath(String var1, String var2, boolean var3) {
      this.filepath = var1;
      this.fileurl = var2;
   }

   @Override
   public String getExportFileUrl() {
      return this.fileurl;
   }

   @Override
   public int executeExport(VariableList var1, boolean var2, HttpServletResponse var3, VariableList var4) {
      if (logger.isDebugEnabled()) {
         logger.debug("Exceute export : CSV");
      }

      if (this.exportdata == null) {
         this.setErrMessage("Export data is null.");
         return -2010;
      } else {
         this.validator = XeniDataValidationFactory.getDataValidator();
         DataSet var5 = this.exportdata.getCmdDataset();
         DataSet var6 = this.exportdata.getStyleDataset();
         DataSet var7 = this.exportdata.getCellDataset();
         ExportFormat var8 = this.exportdata.getGridFormat();
         if (var5 != null && var6 != null && var7 != null && var8 != null) {
            String var9 = var5.getString(0, "exporthead");
            boolean var10 = var9.contains("nohead");
            boolean var11 = var9.contains("nosumm");
            boolean var12 = "top".equals(var5.getString(0, "summarytype"));
            this.csvquote = var4.getBoolean("csv-quote");
            this.csvbom = var4.getBoolean("csv-bom");
            int var13 = var5.getInt(0, "type");
            String var14 = var5.getString(0, "separator");
            if (var13 == 1296 && var14 != null && var14.length() > 0) {
               this.separator = (char)Integer.parseInt(var14.substring(2), 16);
            }

            String var15 = var5.getString(0, "quotechar");
            if (var15 != null && !"default".equalsIgnoreCase(var15)) {
               if ("none".equalsIgnoreCase(var15)) {
                  this.quoteCharStr = "";
               } else {
                  this.quoteCharStr = "" + var15.charAt(0);
               }
            } else if (this.csvquote) {
               this.quoteCharStr = "\"";
            } else {
               this.quoteCharStr = "";
            }

            if (logger.isDebugEnabled()) {
               logger.debug(
                  "nohead : "
                     + var10
                     + "\nnosummary : "
                     + var11
                     + "\nsummary top : "
                     + var12
                     + "\ncsv-quote : "
                     + this.csvquote
                     + "\nexport type : "
                     + var13
                     + "\nseparator : "
                     + this.separator
                     + "\nquotechar : "
                     + this.quoteCharStr
               );
            }

            try {
               int var16 = this.makeExportFilepath(var5, var2, var13);
               if (var16 < 0) {
                  this.setErrMessage("Fail to create export file path.");
                  return var16;
               } else {
                  ByteArrayOutputStream var17 = this.getOutputStream();
                  if (var17 == null) {
                     this.setErrMessage("Fail to create output stream.");
                     return -2012;
                  } else {
                     StringBuilder var18 = new StringBuilder();
                     if (!var10 && var8.getRowCountOfEachBand("head") > 0) {
                        this.setHead(var18, var8);
                     }

                     if (var12 && !var11 && var8.getRowCountOfEachBand("summ") > 0) {
                        this.setSummary(var18, var8);
                     }

                     if (var8.getRowCountOfEachBand("body") > 0) {
                        int var19 = this.setBody(var18, var8, var7);
                        if (var19 < 0) {
                           return var19;
                        }
                     }

                     if (!var12 && !var11 && var8.getRowCountOfEachBand("summ") > 0) {
                        this.setSummary(var18, var8);
                     }

                     XeniExcelDataStorageBase var24 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
                     if (var24 == null) {
                        this.setErrMessage("Could not create extend class : xeni.exportimport.storage");
                        return -2004;
                     } else {
                        if (!this.exportdata.isAppendExport() && this.csvbom) {
                           var17.write(239);
                           var17.write(187);
                           var17.write(191);
                        }

                        var17.write(var18.toString().getBytes("UTF-8"));
                        int var20 = var24.saveExportStream(var1, var5, var17, this.filepath, this.fileurl, var3);
                        return var20 < 0 ? var20 : 0;
                     }
                  }
               }
            } catch (UnsupportedEncodingException var21) {
               this.setErrMessage(var21.getMessage());
               return -2012;
            } catch (IOException var22) {
               this.setErrMessage(var22.getMessage());
               return -2003;
            } catch (Exception var23) {
               this.setErrMessage(var23 + "");
               return -2001;
            }
         } else {
            this.setErrMessage(
               "Dataset is null. \nFILENAME : "
                  + (var5 == null ? "null" : var5.getString(0, "exportfilename"))
                  + "\nCOMMAND : "
                  + (var5 == null ? "null" : var5.toString())
                  + "\nSTYLE : "
                  + (var6 == null ? "null" : var6.toString())
                  + "\nCELL : "
                  + (var7 == null ? "null" : var7.toString())
                  + "\nFORMAT : "
                  + (var8 == null ? "null" : var8.toString())
            );
            return -2011;
         }
      }
   }

   int makeExportFilepath(DataSet var1, boolean var2, int var3) throws UnsupportedEncodingException {
      if (!this.exportdata.isAppendExport()) {
         String var4 = var1.getString(0, "instanceid");
         if (var4.contains(".") || var4.contains("/")) {
            return -3201;
         }

         String var5 = var1.getString(0, "exportfilename");
         if (var5 == null || var5.length() < 1) {
            var5 = var1.getString(0, "item");
            if (var5 == null || var5.length() < 1) {
               var5 = "TEMP";
            }
         }

         if (var5.contains(".") || var5.contains("/")) {
            return -3201;
         }

         String var6 = ".csv";
         String var7 = "4";
         if (var3 == 1296) {
            var6 = ".txt";
            var7 = "5";
         }

         String var8 = var5 + var6;
         this.filepath = this.filepath + var4 + "/" + var8;
         if (var2) {
            this.fileurl = this.fileurl + "?command=export&key=" + var4 + "&name=" + URLEncoder.encode(var5, "UTF-8") + "&type=" + var7;
         } else {
            this.fileurl = this.fileurl + var4 + "/" + var8;
         }
      } else {
         String var10 = this.exportdata.getAppendExportUrl();
         this.fileurl = var10;
         if (var2) {
            int var11 = var10.indexOf("key=");
            String var14 = var10.substring(var11 + 4, var10.indexOf(38, var11));
            if (var14 == null || var14.contains(".") || var14.contains("/")) {
               return -3201;
            }

            var11 = var10.indexOf("name=", var11);
            String var16 = var10.substring(var11 + 5, var10.indexOf(38, var11));
            var16 = URLDecoder.decode(var16, "UTF-8");
            if (var16 == null || var16.contains(".") || var16.contains("/")) {
               return -3201;
            }

            String var19 = ".csv";
            if (var3 == 1296) {
               var19 = ".txt";
            }

            this.filepath = this.filepath + var14 + "/" + var16 + var19;
         } else {
            int var13 = var10.lastIndexOf("/");
            String var15 = var10.substring(var13);
            int var18 = var15.lastIndexOf(46);
            String var20 = var15.substring(1, var18);
            if (var20 == null || var20.contains(".") || var20.contains("/")) {
               return -3201;
            }

            String var9 = var10.substring(0, var13);
            this.filepath = this.filepath + var9.substring(var9.lastIndexOf("/")) + var15;
         }
      }

      if (logger.isDebugEnabled()) {
         logger.debug("export file path : " + this.filepath);
         logger.debug("export file url : " + this.fileurl);
      }

      return 0;
   }

   ByteArrayOutputStream getOutputStream() throws Exception {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();
      if (this.exportdata.isAppendExport()) {
         InputStream var2 = null;
         XeniExcelDataStorageBase var3 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
         var2 = var3.loadTargetStream(this.filepath);
         IOUtils.copy(var2, var1);
         if (var2 != null) {
            var2.close();
         }
      }

      return var1;
   }

   void setHead(StringBuilder var1, ExportFormat var2) throws IOException {
      int var3 = 0;
      int var4 = 0;
      int var5 = var2.getHeadCellCount();
      if (logger.isDebugEnabled()) {
         logger.debug("Export head( format count : " + var5 + " )");
      }

      if (this.exportdata.isAppendExport()) {
         var1.append("\r\n");
      }

      if (var5 > 0) {
         this.addedrow = true;
      }

      for (int var6 = 0; var6 < var5; var6++) {
         FormatCell var7 = var2.getHeadCell(var6);
         int var8 = Integer.parseInt(var7.getRow());
         int var9 = Integer.parseInt(var7.getCol());
         if (var8 > var3) {
            var1.append("\r\n");
            var4 = 0;
         }

         if (var6 > 0) {
            for (int var10 = 0; var10 < var9 - var4; var10++) {
               var1.append(this.separator);
            }
         }

         this.setCsvValue(var1, var7.getText());
         var3 = var8;
         var4 = var9;
      }
   }

   void setSummary(StringBuilder var1, ExportFormat var2) throws IOException {
      int var3 = 0;
      int var4 = 0;
      int var5 = var2.getSummaryCellCount();
      if (logger.isDebugEnabled()) {
         logger.debug("Export summary( format count : " + var5 + " )");
      }

      if (var5 > 0 && this.addedrow) {
         var1.append("\r\n");
      }

      if (var5 > 0) {
         this.addedrow = true;
      }

      for (int var6 = 0; var6 < var5; var6++) {
         FormatCell var7 = var2.getSummary(var6);
         int var8 = Integer.parseInt(var7.getRow());
         int var9 = Integer.parseInt(var7.getCol());
         if (var8 > var3) {
            var1.append("\r\n");
            var4 = 0;
         }

         if (var6 > 0) {
            for (int var10 = 0; var10 < var9 - var4; var10++) {
               var1.append(this.separator);
            }
         }

         this.setCsvValue(var1, var7.getText());
         var3 = var8;
         var4 = var9;
      }
   }

   int setBody(StringBuilder var1, ExportFormat var2, DataSet var3) throws IOException {
      int var4 = var3.getRowCount();
      int var5 = var3.getColumnCount();
      int var6 = var2.getBodyCellCount();
      if (logger.isDebugEnabled()) {
         logger.debug("Export body( format count : " + var6 + ", row : " + var4 + ", col : " + var5 + " )");
      }

      if (var5 != var6) {
         this.setErrMessage("The body format is fault : [ colum count of CELL dataset = " + var5 + ", body cell count of FORMAT  = " + var6 + " ]");
         return -2014;
      } else {
         for (int var7 = 0; var7 < var4; var7++) {
            int var8 = 0;
            if (var6 > 0 && this.addedrow) {
               var1.append("\r\n");
            }

            if (var6 > 0) {
               this.addedrow = true;
            }

            for (int var9 = 0; var9 < var6; var9++) {
               FormatCell var10 = var2.getBodyCell(var9);
               int var11 = Integer.parseInt(var10.getRow());
               int var12 = Integer.parseInt(var10.getColspan());
               if (var11 > var8) {
                  var1.append("\r\n");
               } else if (var9 > 0) {
                  var1.append(this.separator);
               }

               String var13 = var3.getString(var7, var9);
               if (var13 != null && var13.length() > 0) {
                  String[] var14 = var13.split(String.valueOf('\u001d'));
                  if (var14.length > 0) {
                     this.setCsvValue(var1, var14[0]);
                  }
               }

               for (int var15 = 1; var15 < var12; var15++) {
                  var1.append(this.separator);
               }

               var8 = var11;
            }
         }

         return 0;
      }
   }

   private void setCsvValue(StringBuilder var1, String var2) {
      if (this.validator != null) {
         var2 = this.validator.checkData(var2);
      }

      var1.append(this.quoteCharStr + var2 + this.quoteCharStr);
   }

   @Override
   public void receiveExportData(DataSetList var1) {
   }

   @Override
   public boolean isLastExportData(DataSetList var1) {
      return false;
   }

   @Override
   public DataSet getResponseCommand() {
      return null;
   }

   @Override
   public int createWorkbook() {
      return 0;
   }

   @Override
   public int createWorkbook(boolean var1) {
      return 0;
   }

   @Override
   public int appendBody(DataSet var1) {
      return 0;
   }

   @Override
   public int appendBody(List<Map<String, Object>> var1) {
      return 0;
   }

   @Override
   public DataSet disposeWorkbook() {
      return null;
   }

   @Override
   public DataSet disposeWorkbook(VariableList var1) {
      return null;
   }

   @Override
   public int disposeWorkbook(VariableList var1, HttpServletResponse var2) {
      return 0;
   }
}
