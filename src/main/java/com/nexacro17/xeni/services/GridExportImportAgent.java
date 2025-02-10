package com.nexacro17.xeni.services;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.DataSerializer;
import com.nexacro17.xapi.tx.HttpPlatformRequest;
import com.nexacro17.xapi.tx.HttpPlatformResponse;
import com.nexacro17.xapi.tx.PlatformException;
import com.nexacro17.xapi.tx.impl.PlatformSsvDataSerializer;
import com.nexacro17.xeni.data.GridExportData;
import com.nexacro17.xeni.data.GridExportDataFactory;
import com.nexacro17.xeni.data.GridImportFormatFactory;
import com.nexacro17.xeni.export.GridExportBase;
import com.nexacro17.xeni.export.GridExportTypeFactory;
import com.nexacro17.xeni.extend.XeniExcelDataStorageBase;
import com.nexacro17.xeni.extend.XeniExcelDataStorageFactory;
import com.nexacro17.xeni.extend.XeniMultipartProcBase;
import com.nexacro17.xeni.extend.XeniMultipartProcFactory;
import com.nexacro17.xeni.extend.XeniMultipartReqData;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.util.XeniProperties;
import com.nexacro17.xeni.ximport.GridImportBase;
import com.nexacro17.xeni.ximport.GridImportContext;
import com.nexacro17.xeni.ximport.GridImportTypeFactory;
import com.nexacro17.xeni.ximport.PlatformCsvDataSerializer;
import com.nexacro17.xeni.ximport.impl.GridImportExcelXSSFEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.openxml4j.util.ZipSecureFile;

public class GridExportImportAgent {
   private static final Log logger = LogFactory.getLog(GridExportImportAgent.class);
   private String sErrMsg = "";
   private String securityPattern = "";

   public GridExportImportAgent() {
      this.securityPattern = XeniProperties.getStringProperty(
         "xeni.security.pattern",
         "'|\"|:|;|<|>|`|=|#|\\$|%|&|\\?|!|@|\\*|\t|\\||%27|%22|%3a|%3b|%28|%29|%3c|%3e|%5b|%5d|%7b|%7d|%60|%3d|%23|%24|%25|%26|%3f|%21|%40|%2a|%09|%7c|&#x|27;|&#x22;|&#x3a;|&#x3b;|&#x28;|&#x29;|&#x3c;|&#x3e;|&#x5b;|&#x5d;|&#x7b;|&#x7d;|&#x60;|&#x3d;|&#x23;|&#x24;|&#x25;|&#x26;|&#x3f;|&#x21;|&#x40;|&#x2a;|&#x09;|&#x7c;|script|javascript|vbscript|livescript|iframe|mocha|applet|img|embed|object|marquee|qss|body|input|form|div|style|table|isindex|meta|http-equiv|xss|href"
      );
   }

   public void initialize() {
      String var1 = XeniProperties.getStringProperty("xeni.guard.maxentry.size", null);
      String var2 = XeniProperties.getStringProperty("xeni.guard.maxtext.size", null);
      String var3 = XeniProperties.getStringProperty("xeni.guard.mininflate.ratio", null);
      if (var1 != null) {
         ZipSecureFile.setMaxEntrySize(Long.parseLong(var1));
      }

      if (var2 != null) {
         ZipSecureFile.setMaxTextSize(Long.parseLong(var2));
      }

      if (var3 != null) {
         ZipSecureFile.setMinInflateRatio(Double.parseDouble(var3));
      }
   }

   public String getErrorMessage() {
      return this.sErrMsg;
   }

   public int gridExport(String var1, String var2, boolean var3, HttpServletRequest var4, HttpServletResponse var5, VariableList var6) throws PlatformException {
      HttpPlatformRequest var7 = new HttpPlatformRequest(var4, "PlatformSsv", "UTF-8");
      var7.receiveData();
      PlatformData var8 = var7.getData();
      DataSet var9 = null;
      String var10 = "";
      String var11 = "";
      DataSetList var12 = var8.getDataSetList();
      VariableList var13 = var8.getVariableList();

      try {
         var9 = var12.get("COMMAND");
         var10 = var9.getString(0, "command");
         var11 = var9.getString(0, "instanceid");
      } catch (NullPointerException var15) {
         this.sErrMsg = "Input data does not exist. Check the 'data-type' option of transaction";
         return -2006;
      }

      if (!var10.equals("export")) {
         this.sErrMsg = "Not export type. The type is " + var10;
         return -2006;
      } else {
         if (var11 == null || var11.equals("")) {
            var11 = this.createInstanceId(var4);
         }

         return this.gridExport(var13, var12, var11, var1, var2, var3, var5, var6);
      }
   }

   private int gridExport(VariableList var1, DataSetList var2, String var3, String var4, String var5, boolean var6, HttpServletResponse var7, VariableList var8) throws PlatformException {
      int var9 = 0;
      HashMap var10 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      DataSet var11 = var2.get("COMMAND");
      DataSet var12 = var2.get("STYLE");
      DataSet var13 = var2.get("CELL");
      GridExportData var14 = null;
      if (logger.isInfoEnabled()) {
         logger.info(
            "Input export data : [ Item = "
               + var11.getString(0, "item")
               + ", Seq. = "
               + var11.getString(0, "seq")
               + ", EOF = "
               + var11.getString(0, "eof")
               + ", Url = "
               + var11.getString(0, "url")
               + ", Inst.Id = "
               + var11.getString(0, "instanceid")
               + " ]"
         );
      }

      if (var10.containsKey(var3)) {
         var14 = (GridExportData)var10.get(var3);
         var14.setCmdDataset(var11);
         var14.setStyleDataset(var12);
         var14.setCellDataset(var13);
      } else {
         var11.set(0, "instanceid", var3);
         var14 = new GridExportData();
         var14.setCmdDataset(var11);
         var14.setStyleDataset(var12);
         var14.setCellDataset(var13);
         String var15 = var11.getString(0, "url");
         if (var15 != null && var15.length() > 0) {
            var14.setAppendExport(var15);
         }

         var10.put(var3, var14);
      }

      var14.setLastAccTime(System.currentTimeMillis());
      boolean var21 = var11.getBoolean(0, "eof");
      if (var21) {
         var14 = (GridExportData)var10.get(var3);
         if (var14 != null) {
            int var16 = var11.getInt(0, "type");
            GridExportBase var17 = GridExportTypeFactory.getGridExporter(var16);
            if (var17 == null) {
               String var18 = "Could not create Grid Exporter : export type = [ 0x" + Integer.toHexString(var16) + " ]";
               if (logger.isErrorEnabled()) {
                  logger.error(var18);
               }

               return -2008;
            }

            var17.setExportData(var14);
            var17.setExportFilePath(var4, var5, false);
            var9 = var17.executeExport(var1, var6, var7, var8);
            if (var9 != 0) {
               this.sErrMsg = var17.getErrorMessage();
            }

            var10.remove(var3);
         }
      } else {
         PlatformData var22 = new PlatformData();
         VariableList var23 = var22.getVariableList();
         var23.add("ErrorCode", 0);
         var23.add("ErrorMsg", "SUCCESS");
         var22.addDataSet(CommUtil.getDatasetExportResponse(var11));
         HttpPlatformResponse var24 = new HttpPlatformResponse(var7, "PlatformSsv", "UTF-8");
         var24.setData(var22);
         var24.sendData();
      }

      return var9;
   }

   public int gridImport(String var1, String var2, boolean var3, HttpServletRequest var4, HttpServletResponse var5, VariableList var6) throws IOException, FileUploadException, PlatformException, Exception {
      XeniMultipartProcBase var7 = XeniMultipartProcFactory.getMultipartProc("xeni.multipart.proc");
      if (var7 == null) {
         this.sErrMsg = "Could not create extend class : " + XeniProperties.getStringProperty("xeni.multipart.proc");
         return -3101;
      } else {
         XeniMultipartReqData var8 = var7.getImportData(var4);
         if (var8 == null) {
            this.sErrMsg = "Request data is null.";
            return -3101;
         } else {
            PlatformData var9 = var8.getPlatformData();
            if (var9 == null) {
               this.sErrMsg = "Command data is null.";
               return -3101;
            } else {
               String var10 = var8.getFileName();
               if (var6.getBoolean("import-temp-name")) {
                  String var11 = "";
                  int var12 = var10.lastIndexOf(46);
                  if (var12 > 0) {
                     var11 = var10.substring(var12);
                  }

                  var10 = "import_temp" + var11;
               }

               InputStream var23 = var8.getFileStream();
               VariableList var24 = var9.getVariableList();
               DataSetList var13 = var9.getDataSetList();
               DataSet var14 = var13.get("COMMAND");
               String var15 = var14.getString(0, "command");
               if (logger.isInfoEnabled()) {
                  logger.info(
                     "Input import data : [ Mode = "
                        + var14.getString(0, "filemode")
                        + ", Url = "
                        + var14.getString(0, "url")
                        + ", Html tag = "
                        + var14.getString(0, "usehtmltag")
                        + ", Raw num = "
                        + var14.getString(0, "rawnumbervalue")
                        + " ]"
                  );
               }

               GridImportContext var16 = this.getGridImportContext(var14, var24, var6);
               var16.setFilePath(var1);
               var16.setFileMonitor(var3);
               if (!var15.equals("import")) {
                  this.sErrMsg = "Not import type. The type is " + var15;
                  return this.setImportError(var5, -2006, this.sErrMsg, var16.getUserDomain(), var16.getCorsResponseType(), var16.getResponseContentType());
               } else {
                  if (!var16.isServerMode() && var23 != null) {
                     if (!this.IsValidName(var10)) {
                        this.sErrMsg = "File name " + var10 + " is not valid.";
                        return this.setImportError(
                           var5, -3101, this.sErrMsg, var16.getUserDomain(), var16.getCorsResponseType(), var16.getResponseContentType()
                        );
                     }

                     String var17 = this.containSecureCharacter2(var10);
                     if (var17.length() > 0) {
                        this.sErrMsg = "The file name contains characters that violate security. [" + var17 + "]";
                        return this.setImportError(
                           var5, -3101, this.sErrMsg, var16.getUserDomain(), var16.getCorsResponseType(), var16.getResponseContentType()
                        );
                     }

                     XeniExcelDataStorageBase var18 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
                     if (var18 == null) {
                        this.sErrMsg = "Could not create extend class : " + XeniProperties.getStringProperty("xeni.exportimport.storage");
                        return this.setImportError(
                           var5, -3101, this.sErrMsg, var16.getUserDomain(), var16.getCorsResponseType(), var16.getResponseContentType()
                        );
                     }

                     String var19 = "/" + this.createInstanceId(var4) + "/" + var10;
                     String var20 = var18.saveImportStream(var24, var23, var1 + var19);
                     if (var20 != null) {
                        var19 = var20;
                     }

                     var16.setFileUrl(var19);
                  }

                  GridImportBase var25 = null;

                  try {
                     var25 = GridImportTypeFactory.getGridImporter(var16.getImportType(), var1, var16.getFileUrl(), var16.getFilePassword());
                     if (var25 == null || var25.getErrorCode() < 0) {
                        this.sErrMsg = "Could not create Grid Importer.";
                        if (var25 != null) {
                           this.sErrMsg = this.sErrMsg + " [" + var25.getErrorMessage() + "]";
                        } else {
                           this.sErrMsg = this.sErrMsg + " [Unsupported excel format]";
                        }

                        if (logger.isErrorEnabled()) {
                           logger.error(this.sErrMsg);
                        }

                        if (var3 && !var16.isServerMode()) {
                           CommUtil.deleteDir(var1 + var16.getFileUrl());
                        }

                        return -2005;
                     }
                  } catch (FileNotFoundException var21) {
                     this.sErrMsg = "File or directory not found. [" + var16.getFileUrl() + "]";
                     return -2003;
                  } catch (Exception var22) {
                     this.sErrMsg = "" + var22;
                     return -2005;
                  }

                  var16.setPartData(var25 instanceof GridImportExcelXSSFEvent && var16.getResponseContentType() == null);
                  if (var16.isPartData()) {
                     var16.setHttpResponse(var5);
                     var16.initPartDataSerializer();
                  }

                  int var27 = this.gridImport(var25, var16, var5);
                  if (var3 && !var16.isServerMode()) {
                     CommUtil.deleteDir(var1 + var16.getFileUrl());
                  }

                  return var27 < 0
                     ? this.setImportError(var5, var27, this.sErrMsg, var16.getUserDomain(), var16.getCorsResponseType(), var16.getResponseContentType())
                     : var27;
               }
            }
         }
      }
   }

   private int gridImport(GridImportBase var1, GridImportContext var2, HttpServletResponse var3) throws Exception {
      PlatformData var4 = new PlatformData();
      var2.setPlatformData(var4);
      var1.initialize(var2);
      int var5 = var1.executeImport();
      String var6 = var2.getResponseContentType();
      String var7 = var2.getUserDomain();
      VariableList var8 = var4.getVariableList();
      if (var5 == 0) {
         if (var2.isPartData()) {
            return 0;
         }

         var8.add("ErrorCode", 0);
         var8.add("ErrorMsg", "SUCCESS");
         DataSet var9 = var4.getDataSet("IMPORTFILES");
         var9.set(0, "importid", var2.getImportId());

         try {
            if (var2.getCorsResponseType() == 1) {
               this.corsReviseResponse(var3, var4, var6);
               return var5;
            }

            if (var7 != null && !"".equals(var7)) {
               CommUtil.sendDomainResponse(var3, var4, var7, var6);
            } else if (var6 == null && !"csv".equalsIgnoreCase(var6)) {
               String var15 = "PlatformSsvExt";
               if (!var2.isUseHtmlTag()) {
                  var15 = "PlatformSsv";
               }

               HttpPlatformResponse var16 = new HttpPlatformResponse(var3, var15, "UTF-8");
               var16.setData(var4);
               var16.sendData();
            } else {
               var3.addHeader("Content-Type", "text/html;charset=UTF-8");
               PrintWriter var10 = var3.getWriter();
               if (var2.isUseHtmlTag()) {
                  var10.write("<!--[if lt IE 9]><comment><![endif]--><noscript>");
               }

               PlatformCsvDataSerializer var11 = new PlatformCsvDataSerializer();
               var11.writeData(var10, var4, null, "UTF-8");
               if (var2.isUseHtmlTag()) {
                  var10.write("</noscript></comment>");
               }

               var10.flush();
               var10.close();
            }
         } catch (PlatformException var12) {
            var5 = -3101;
            this.sErrMsg = var12.getMessage();
         } catch (Exception var13) {
            var5 = -2001;
            this.sErrMsg = "" + var13;
         }
      } else {
         this.sErrMsg = var1.getErrorMessage();
         var8.add("ErrorCode", var5);
         var8.add("ErrorMsg", this.sErrMsg);
         int var14 = var2.getPartIndex();
         if (var2.isPartData() && var14 >= 1) {
            this.sendPartData(var8, var2, var14);
            return 0;
         }

         if (var2.getCorsResponseType() == 1) {
            this.corsReviseResponse(var3, var4, var6);
            return 0;
         }
      }

      return var5;
   }

   private String createInstanceId(HttpServletRequest var1) {
      return UUID.randomUUID().toString();
   }

   private GridImportContext getGridImportContext(DataSet var1, VariableList var2, VariableList var3) throws UnsupportedEncodingException {
      GridImportContext var4 = new GridImportContext();
      Object var5 = null;
      var4.setImportType(var1.getInt(0, "type"));
      var4.setFilePassword(var1.getString(0, "password"));
      var4.setCsvQuote(var3.getBoolean("csv-quote"));
      var4.setSeparator(var1.getString(0, "separator"));
      var4.setQuoteChar(var1.getString(0, "quotechar"));
      var4.setImportId(var1.getString(0, "importid"));
      var4.setSentData(false);
      var4.setUserDomain(var2.getString("domain"));
      var4.setResponseContentType(var2.getString("contenttype"));
      var5 = var1.getString(0, "filemode");
      if ("local".equals(var5)) {
         var4.setServerMode(false);
      } else {
         var4.setServerMode(true);
         var4.setFileUrl(URLDecoder.decode(var1.getString(0, "url"), "UTF-8"));
      }

      var5 = var1.getString(0, "responsetype");
      if (var5 != null) {
         var4.setCorsResponseType(Integer.parseInt((String)var5));
      }

      var5 = var1.getString(0, "format");
      GridImportFormatFactory var6 = new GridImportFormatFactory();
      var4.setImportFormat(var6.readFormatString((String)var5));
      var5 = var1.getString(0, "rawdatevalue");
      if (var5 != null && "true".equals(var5)) {
         var4.setRawDateValue(true);
      }

      var5 = var1.getString(0, "usehtmltag");
      if (var5 == null || "false".equals(var5)) {
         var4.setUseHtmlTag(false);
      }

      var5 = var1.getString(0, "rawnumbervalue");
      if (var5 != null && "false".equals(var5)) {
         var4.setRawNumberValue(false);
      }

      return var4;
   }

   public int sendExportFileStream(HttpServletRequest var1, HttpServletResponse var2, String var3) {
      Map var4 = this.getQueryParameters(var1.getQueryString());
      if (var4 == null) {
         this.sErrMsg = "Invalid export file parameter.";
         return -3201;
      } else {
         short var5 = 0;
         String var6 = (String)var4.get("command");
         if (var6 != null && var6.equals("export")) {
            String var7 = (String)var4.get("key");
            if (var7 != null && !var7.contains(".") && !var7.contains("/")) {
               String var8 = (String)var4.get("name");
               String var9 = "";

               try {
                  var9 = URLDecoder.decode(var8, "UTF-8");
               } catch (UnsupportedEncodingException var29) {
                  if (this.isContainSecureCharacter(var9)) {
                     this.sErrMsg = "Could not decode the name.";
                  } else {
                     this.sErrMsg = "Could not decode the name. (" + var9 + ")";
                  }

                  return -3201;
               }

               if (var9 != null && !var9.contains("./") && !var9.contains("/")) {
                  String var10 = (String)var4.get("type");
                  if (var10 == null) {
                     this.sErrMsg = "Invalid export file type. (null)";
                     return -3201;
                  } else {
                     int var11 = Integer.parseInt(var10);
                     String var12 = "";
                     String var13 = "";
                     if (var11 == 0) {
                        var12 = ".xls";
                        var13 = "application/vnd.ms-excel";
                     } else if (var11 == 1) {
                        var12 = ".xlsx";
                        var13 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                     } else if (var11 == 2) {
                        var12 = ".cell";
                        var13 = "application/vnd.ms-excel";
                     } else if (var11 == 3) {
                        var12 = ".cell";
                        var13 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                     } else if (var11 == 4) {
                        var12 = ".csv";
                        var2.setContentType("text/csv");
                     } else {
                        if (var11 != 5) {
                           this.sErrMsg = "Invalid export file type. (" + var11 + ")";
                           return -3201;
                        }

                        var12 = ".txt";
                        var2.setContentType("text/csv");
                     }

                     String var14 = var3 + var7 + "/" + var9 + var12;
                     FileInputStream var15 = null;
                     ServletOutputStream var16 = null;

                     try {
                        var15 = new FileInputStream(var14);
                        var2.setContentType(var13);
                        var2.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(var9, "UTF-8").replaceAll("\\+", "%20") + var12);
                        var16 = var2.getOutputStream();

                        int var17;
                        while ((var17 = var15.read()) != -1) {
                           var16.write(var17);
                        }
                     } catch (FileNotFoundException var30) {
                        if (!this.isContainSecureCharacter(var7) && !this.isContainSecureCharacter(var9)) {
                           this.sErrMsg = var7 + "/" + var9 + var12 + " (File not found)";
                        } else {
                           this.sErrMsg = "File not found.";
                        }

                        var5 = -2001;
                     } catch (IOException var31) {
                        this.sErrMsg = var31.getMessage();
                        var5 = -2003;
                     } finally {
                        try {
                           if (var15 != null) {
                              var15.close();
                           }

                           if (var16 != null) {
                              var16.close();
                           }
                        } catch (IOException var28) {
                           this.sErrMsg = var28.getMessage();
                           var5 = -2003;
                        }
                     }

                     if (var5 == 0) {
                        CommUtil.deleteDir(var3 + var7);
                     }

                     return var5;
                  }
               } else {
                  if (this.isContainSecureCharacter(var9)) {
                     this.sErrMsg = "Invalid export file name.";
                  } else {
                     this.sErrMsg = "Invalid export file name. (" + var9 + ")";
                  }

                  return -3201;
               }
            } else {
               if (this.isContainSecureCharacter(var7)) {
                  this.sErrMsg = "Invalid export file key.";
               } else {
                  this.sErrMsg = "Invalid export file key. (" + var7 + ")";
               }

               return -3201;
            }
         } else {
            if (var6 != null && this.isContainSecureCharacter(var6)) {
               this.sErrMsg = "Could not excute command.";
            } else {
               this.sErrMsg = "Could not excute command. (" + var6 + ")";
            }

            return -3201;
         }
      }
   }

   private Map<String, String> getQueryParameters(String var1) {
      if (var1 != null && var1.length() > 0) {
         String[] var2 = var1.split("&");
         if (var2 != null && var2.length > 0) {
            HashMap var3 = new HashMap();

            for (String var7 : var2) {
               String[] var8 = var7.split("=");
               String var9 = var8[0];
               String var10 = "";
               if (var8.length > 1) {
                  var10 = var8[1];
               }

               var3.put(var9, var10);
            }

            return var3;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean isContainSecureCharacter(String var1) {
      Pattern var2 = Pattern.compile("(?i)(" + this.securityPattern + "){1,}");
      Matcher var3 = var2.matcher(var1);
      return var3.find();
   }

   private String containSecureCharacter2(String var1) {
      Pattern var2 = Pattern.compile("(?i)(" + this.securityPattern + "){1,}");
      Matcher var3 = var2.matcher(var1);
      String var4 = "";
      if (var3.find()) {
         if (var4.length() > 0) {
            var4 = var4 + ", ";
         }

         var4 = var4 + var3.group();
      }

      return var4;
   }

   private int setImportError(HttpServletResponse var1, int var2, String var3, String var4, int var5, String var6) throws Exception {
      if (var5 == 1) {
         PlatformData var9 = new PlatformData();
         VariableList var10 = var9.getVariableList();
         var10.add("ErrorCode", var2);
         var10.add("ErrorMsg", var3);
         this.corsReviseResponse(var1, var9, var6);
         return 0;
      } else if (var4 != null && !"".equals(var4)) {
         PlatformData var7 = new PlatformData();
         VariableList var8 = var7.getVariableList();
         var8.add("ErrorCode", var2);
         var8.add("ErrorMsg", var3);
         CommUtil.sendDomainResponse(var1, var7, var4, var6);
         return 0;
      } else {
         return var2;
      }
   }

   private void corsReviseResponse(HttpServletResponse var1, PlatformData var2, String var3) throws Exception {
      DataSerializer var4 = null;
      if (var3 != null && "csv".equalsIgnoreCase(var3)) {
         var4 = new PlatformCsvDataSerializer();
      } else {
         var4 = new PlatformSsvDataSerializer();
      }

      var1.addHeader("Content-Type", "text/html;charset=UTF-8");
      PrintWriter var5 = var1.getWriter();
      var5.write("<script type=\"text/javascript\">");
      var5.write("var retValue = \"\";");
      var5.write("window.onload = function() { ");
      var5.write("if (window.addEventListener) { ");
      var5.write("window.addEventListener (\"message\", OnMessage, false); }");
      var5.write("else { ");
      var5.write("if (window.attachEvent) { ");
      var5.write("window.attachEvent(\"onmessage\", OnMessage); }");
      var5.write(" } }; ");
      var5.write("function OnMessage (event) { ");
      var5.write("message = `");
      var4.writeData(var5, var2, null, "UTF-8");
      var5.write("`;");
      var5.write("message = event.data + message;");
      var5.write("event.source.postMessage (message, event.origin); }");
      var5.write("</script>");
      var5.flush();
      var5.close();
   }

   private boolean IsValidName(String var1) {
      boolean var2 = false;
      String var3 = "";
      int var4 = var1.lastIndexOf(46);
      if (var4 > 0) {
         var3 = var1.substring(var4 + 1);
      }

      if (var3.length() > 0
         && (
            "xls".equalsIgnoreCase(var3)
               || "xlsx".equalsIgnoreCase(var3)
               || "cell".equalsIgnoreCase(var3)
               || "csv".equalsIgnoreCase(var3)
               || "xlsm".equalsIgnoreCase(var3)
               || "txt".equalsIgnoreCase(var3)
         )) {
         var2 = true;
      }

      return var2;
   }

   private void sendPartData(VariableList var1, GridImportContext var2, int var3) throws IOException, PlatformException {
      if (var3 < 2) {
         StringWriter var4 = new StringWriter();
         CommUtil.writePartDataHead(var4, var2);
         var2.getResponseWriter().write(var4.toString());
      }

      StringWriter var5 = new StringWriter();
      CommUtil.writePartDataVariableList(var5, var2, var1);
      if (var2.getCorsResponseType() == 1) {
         var2.getResponseWriter().write(var5.toString().replace("`", "\\`").replace("${", "\\${"));
         var2.getResponseWriter().write("`;");
         var2.getResponseWriter().write("message = event.data + message;");
         var2.getResponseWriter().write("event.source.postMessage (message, event.origin); }");
         var2.getResponseWriter().write("</script>");
      } else {
         var2.getResponseWriter().write(var5.toString());
         if (var2.getUserDomain() != null) {
            var2.getResponseWriter().write("</html>");
         } else if (var2.isUseHtmlTag()) {
            var2.getResponseWriter().write("</noscript></comment>");
         }
      }

      var5.getBuffer().delete(0, var5.getBuffer().length());
   }
}
