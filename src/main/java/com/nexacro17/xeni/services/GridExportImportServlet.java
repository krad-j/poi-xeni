package com.nexacro17.xeni.services;

import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.HttpPlatformResponse;
import com.nexacro17.xapi.tx.PlatformException;
import java.io.IOException;
import java.util.Timer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GridExportImportServlet extends HttpServlet {
   private static final long serialVersionUID = -8877137699076711995L;
   private static final Log objLogger = LogFactory.getLog(GridExportImportServlet.class);
   private static final String EXPORT_PATH = "exportpath";
   private static final String IMPORT_PATH = "importpath";
   private static final String DOWNLOAD_URL = "downloadurl";
   private static String sExportUserPath = null;
   private static String sImportUserPath = null;
   private static String sExportUserUrl = null;
   private static int iRunPeriodSec = 1800;
   private static int iStorageTimeSec = 600;
   private static boolean isEnableMonitor = true;
   private static String sNumberFmt_lang = "ko";
   private static boolean isCsvQuote = true;
   private static boolean isCsvBom = true;
   private static boolean isImportTempName = false;
   private static boolean bOutExportPath = false;
   private static boolean bOutImportPath = false;

   public void init() throws ServletException {
      super.init();
      ServletContext var1 = this.getServletContext();
      sExportUserPath = var1.getInitParameter("export-path");
      if (sExportUserPath != null && !sExportUserPath.equalsIgnoreCase("")) {
         if (sExportUserPath.startsWith("file://")) {
            sExportUserPath = sExportUserPath.substring(7);
            bOutExportPath = true;
         } else if (sExportUserPath.startsWith("/")) {
            sExportUserPath = sExportUserPath.substring(1);
         }

         if (!sExportUserPath.endsWith("/")) {
            sExportUserPath = sExportUserPath + "/";
         }
      } else {
         sExportUserPath = "export/";
      }

      sExportUserUrl = var1.getInitParameter("export-url");
      sImportUserPath = var1.getInitParameter("import-path");
      if (sImportUserPath == null || sImportUserPath.equals("")) {
         sImportUserPath = "import";
      } else if (sImportUserPath.startsWith("file://")) {
         sImportUserPath = sImportUserPath.substring(7);
         if (!sImportUserPath.endsWith("/")) {
            sImportUserPath = sImportUserPath + "/";
         }

         bOutImportPath = true;
      } else {
         if (sImportUserPath.startsWith("/")) {
            sImportUserPath = sImportUserPath.substring(1);
         }

         if (sImportUserPath.endsWith("/")) {
            int var2 = sImportUserPath.lastIndexOf(47);
            sImportUserPath = sImportUserPath.substring(0, var2);
         }
      }

      String var8 = var1.getInitParameter("monitor-enabled");
      if (var8 != null && "false".equalsIgnoreCase(var8)) {
         isEnableMonitor = false;
      }

      String var3 = var1.getInitParameter("monitor-cycle-time");
      if (var3 != null && var3.length() > 0) {
         int var4 = var3.indexOf(47);
         if (var4 > 0) {
            String[] var5 = var3.split("/");
            if (var5.length > 1 && var5[1].equalsIgnoreCase("sec")) {
               iRunPeriodSec = Integer.parseInt(var5[0]);
            } else {
               iRunPeriodSec = Integer.parseInt(var5[0]) * 60;
            }
         } else {
            iRunPeriodSec = Integer.parseInt(var3) * 60;
         }
      }

      var3 = var1.getInitParameter("file-storage-time");
      if (var3 != null && var3.length() > 0) {
         int var10 = var3.indexOf(47);
         if (var10 > 0) {
            String[] var12 = var3.split("/");
            if (var12.length > 1 && var12[1].equalsIgnoreCase("sec")) {
               iStorageTimeSec = Integer.parseInt(var12[0]);
            } else {
               iStorageTimeSec = Integer.parseInt(var12[0]) * 60;
            }
         } else {
            iStorageTimeSec = Integer.parseInt(var3) * 60;
         }
      }

      String var11 = var1.getInitParameter("numFmt-lang");
      if (var11 != null && var11.length() > 0) {
         sNumberFmt_lang = var11;
      }

      String var13 = var1.getInitParameter("csv-quote");
      if (var13 != null && "false".equalsIgnoreCase(var13)) {
         isCsvQuote = false;
      }

      String var6 = var1.getInitParameter("csv-bom");
      if (var6 != null && "false".equalsIgnoreCase(var6)) {
         isCsvBom = false;
      }

      String var7 = var1.getInitParameter("import-temp-name");
      if (var7 != null && "true".equalsIgnoreCase(var7)) {
         isImportTempName = true;
      }
   }

   public void destroy() {
      ExportImportFileManager var1 = ExportImportFileManager.getInstance();
      if (var1 != null) {
         Timer var2 = var1.getTimer();
         if (var2 != null) {
            var2.cancel();
         }
      }

      super.destroy();
   }

   public void doGet(HttpServletRequest var1, HttpServletResponse var2) throws ServletException, IOException {
      try {
         VariableList var3 = new VariableList();
         this.setPath(var1, var3);
         GridExportImportAgent var4 = new GridExportImportAgent();
         int var5 = var4.sendExportFileStream(var1, var2, var3.getString("exportpath"));
         if (var5 < 0) {
            this.sendErrorMessage(var2, var5, var4.getErrorMessage());
         }
      } catch (Exception var6) {
         this.sendErrorMessage(var2, -2001, var6 + "");
         var6.printStackTrace();
      }
   }

   public void doPost(HttpServletRequest var1, HttpServletResponse var2) throws ServletException, IOException {
      try {
         this.processRequest(var1, var2);
      } catch (PlatformException var4) {
         this.sendErrorMessage(var2, -3101, "Platform Error ( " + var4.getMessage() + " )");
         var4.printStackTrace();
      } catch (IllegalArgumentException var5) {
         this.sendErrorMessage(var2, -3201, "Illegal Argument ( " + var5.getMessage() + " )");
         var5.printStackTrace();
      } catch (IndexOutOfBoundsException var6) {
         this.sendErrorMessage(var2, -3301, "Index Out Of Bounds ( " + var6.getMessage() + " )");
         var6.printStackTrace();
      } catch (IllegalStateException var7) {
         this.sendErrorMessage(var2, -3401, "Illegal State ( " + var7.getMessage() + " )");
         var7.printStackTrace();
      } catch (NullPointerException var8) {
         this.sendErrorMessage(var2, -2004, var8 + "");
         var8.printStackTrace();
      } catch (IOException var9) {
         this.sendErrorMessage(var2, -2003, var9 + "");
         var9.printStackTrace();
      } catch (Exception var10) {
         this.sendErrorMessage(var2, -2001, var10 + "");
         var10.printStackTrace();
      }
   }

   private void sendErrorMessage(HttpServletResponse var1, int var2, String var3) {
      PlatformData var4 = new PlatformData();
      VariableList var5 = var4.getVariableList();
      var5.add("ErrorCode", var2);
      var5.add("ErrorMsg", var3);
      HttpPlatformResponse var6 = new HttpPlatformResponse(var1, "PlatformSsv", "UTF-8");
      var6.setData(var4);
      if (objLogger.isInfoEnabled()) {
         objLogger.info(var3);
      }

      if (objLogger.isDebugEnabled()) {
         objLogger.debug("RESPONSE DATA XML ============================ \n" + var4.saveXml());
      }

      try {
         var6.sendData();
      } catch (PlatformException var8) {
         if (objLogger.isInfoEnabled()) {
            objLogger.info(var8.getMessage());
         }
      }
   }

   private void processRequest(HttpServletRequest var1, HttpServletResponse var2) throws Exception {
      Object var3 = null;
      int var4 = 0;
      long var5 = System.currentTimeMillis();
      VariableList var7 = new VariableList();
      this.setPath(var1, var7);
      if (isEnableMonitor) {
         this.executeTimerSchedule(var7);
      }

      GridExportImportAgent var8 = new GridExportImportAgent();
      var8.initialize();
      var7.add("csv-quote", isCsvQuote);
      var7.add("import-temp-name", isImportTempName);
      var7.add("csv-bom", isCsvBom);
      boolean var9 = ServletFileUpload.isMultipartContent(var1);
      if (var9) {
         var3 = "import";
         var4 = var8.gridImport(var7.getString("importpath"), sNumberFmt_lang, isEnableMonitor, var1, var2, var7);
      } else {
         var3 = "export";
         var4 = var8.gridExport(var7.getString("exportpath"), var7.getString("downloadurl"), bOutExportPath, var1, var2, var7);
      }

      if (var4 < 0) {
         this.sendErrorMessage(var2, var4, var8.getErrorMessage());
      }

      if (objLogger.isInfoEnabled()) {
         long var10 = System.currentTimeMillis();
         long var12 = var10 - var5;
         objLogger.info(var3 + " running time.......[ " + var12 + " ]");
      }
   }

   private void setPath(HttpServletRequest var1, VariableList var2) throws Exception {
      String var3 = null;
      if (var1.getSession() == null) {
         objLogger.info("Request session is null");
      } else if (var1.getSession().getServletContext() == null) {
         objLogger.info("Servlet context is null");
      } else {
         var3 = var1.getSession().getServletContext().getRealPath("/");
      }

      if (var3 == null) {
         if (!bOutExportPath || !bOutImportPath) {
            String var4 = "Unable to get real path of the context. Set absolute paths for 'export-path' and 'import-path'";
            objLogger.error(var4);
            throw new Exception(var4);
         }
      } else {
         var3 = var3.replaceAll("\\\\", "/");
         if (!var3.endsWith("/")) {
            var3 = var3 + "/";
         }
      }

      if (bOutExportPath) {
         var2.add("exportpath", sExportUserPath);
      } else {
         var2.add("exportpath", var3 + sExportUserPath);
      }

      if (bOutImportPath) {
         var2.add("importpath", sImportUserPath);
      } else {
         var2.add("importpath", var3 + sImportUserPath);
      }

      String var7 = null;
      if (sExportUserUrl != null && !"".equals(sExportUserUrl)) {
         var7 = sExportUserUrl;
      } else {
         var7 = var1.getRequestURL().toString();
      }

      if (bOutExportPath) {
         var2.add("downloadurl", var7);
      } else {
         int var5 = var7.lastIndexOf("/");
         String var6 = var7.substring(0, var5 + 1);
         var2.add("downloadurl", var6 + sExportUserPath);
      }
   }

   private void executeTimerSchedule(VariableList var1) {
      ExportImportFileManager var2 = ExportImportFileManager.getInstance();
      Timer var3 = var2.getTimer();
      if (var3 == null) {
         var3 = var2.newTimerInstance();
         var2.setServiceDir(var1.getString("exportpath"), var1.getString("importpath"), isEnableMonitor);
         var2.setStorageTime(iStorageTimeSec);
         var3.scheduleAtFixedRate(var2, (long)(iRunPeriodSec * 1000), (long)(iRunPeriodSec * 1000));
      }
   }
}
