package com.nexacro17.xeni.services;

import com.nexacro17.xeni.data.GridExportData;
import com.nexacro17.xeni.data.GridExportDataFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExportImportFileManager extends TimerTask {
   private static final Log oLogger = LogFactory.getLog(ExportImportFileManager.class);
   private static ExportImportFileManager INSTANCE = null;
   private static Timer TIMER = null;
   private String sExportPath = null;
   private String sImportPath = null;
   private int m_iStorageTime = 0;
   SimpleDateFormat m_oSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private boolean bFileManage = true;

   public static synchronized ExportImportFileManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ExportImportFileManager();
      }

      return INSTANCE;
   }

   public synchronized Timer newTimerInstance() {
      if (TIMER == null) {
         TIMER = new Timer();
      }

      return TIMER;
   }

   public Timer getTimer() {
      return TIMER;
   }

   public void setServiceDir(String var1, String var2, boolean var3) {
      this.sExportPath = var1;
      this.sImportPath = var2 + '/';
      this.bFileManage = var3;
   }

   public void setServiceDir(String var1, boolean var2) {
      this.sExportPath = var1;
      this.bFileManage = var2;
   }

   public void setStorageTime(int var1) {
      this.m_iStorageTime = var1;
   }

   @Override
   public void run() {
      if (oLogger.isInfoEnabled()) {
         oLogger.info("run file manager [ " + this.m_oSdf.format(Long.valueOf(System.currentTimeMillis())) + " ]");
      }

      this.removeUncompletedChunkedData();
      if (this.bFileManage) {
         this.deleteExportFile();
         this.deleteImportFile();
      }
   }

   public void deleteExportFile() {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Find file has been exported.");
      }

      File var1 = new File(this.sExportPath);
      if (!var1.exists()) {
         if (oLogger.isInfoEnabled()) {
            oLogger.info("The directory does not exist [ " + this.sExportPath + " ]");
         }

         boolean var2 = var1.mkdir();
         if (!var2) {
            if (oLogger.isWarnEnabled()) {
               oLogger.warn("Fail to creat directory [ " + this.sExportPath + " ]");
               oLogger.warn("File Monitor Thread Terminated.");
            }

            this.getTimer().cancel();
            return;
         }

         if (oLogger.isInfoEnabled()) {
            oLogger.info("'Export' path has been created.");
         }
      }

      this.deleteFile(var1);
   }

   private void deleteFile(File var1) {
      boolean var2 = false;
      File[] var3 = var1.listFiles();
      if (var3 != null) {
         for (File var7 : var3) {
            if (var7.isDirectory()) {
               this.deleteFile(var7);
            } else if (!var2) {
               Calendar var8 = Calendar.getInstance();
               Date var9 = var8.getTime();
               var8.setTimeInMillis(var7.lastModified());
               var8.add(13, this.m_iStorageTime);
               Date var10 = var8.getTime();
               if (var9.getTime() >= var10.getTime()) {
                  if (var7.delete()) {
                     var2 = true;
                     if (oLogger.isInfoEnabled()) {
                        oLogger.info("Succeeded in deleting files [ " + var7.getName() + " ]");
                     }
                  } else if (oLogger.isWarnEnabled()) {
                     oLogger.warn("Failed to delete the file is in using [ " + var7.getName() + " ]");
                  }
               }
            } else {
               var7.delete();
            }
         }
      }

      if (var2) {
         var1.delete();
      }
   }

   public void removeUncompletedChunkedData() {
      HashMap var1 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Find chunk data uncompleted");
      }

      Set var2 = var1.keySet();
      Iterator var3 = var2.iterator();

      while (var3.hasNext()) {
         String var4 = (String)var3.next();
         GridExportData var5 = (GridExportData)var1.get(var4);
         Calendar var6 = Calendar.getInstance();
         Date var7 = var6.getTime();
         long var8 = var5.getLastAccTime();
         var6.setTimeInMillis(var8);
         Date var10 = var6.getTime();
         var6.add(13, this.m_iStorageTime);
         Date var11 = var6.getTime();
         if (var7.getTime() >= var11.getTime()) {
            var3.remove();
            if (oLogger.isInfoEnabled()) {
               oLogger.info("Succeeded in deleting uncompleted chunk data [ " + var4 + " ]");
            }
         }
      }
   }

   public void deleteImportFile() {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Find file has been imported.");
      }

      File var1 = new File(this.sImportPath);
      if (!var1.exists()) {
         if (oLogger.isInfoEnabled()) {
            oLogger.info("The directory does not exist [ " + this.sImportPath + " ]");
         }

         boolean var2 = var1.mkdir();
         if (!var2) {
            if (oLogger.isWarnEnabled()) {
               oLogger.warn("Fail to creat directory [ " + this.sImportPath + " ]");
               oLogger.warn("File Monitor Thread Terminated.");
            }

            this.getTimer().cancel();
            return;
         }

         if (oLogger.isInfoEnabled()) {
            oLogger.info("'Import' path has been created.");
         }
      }

      this.deleteFile(var1);
   }
}
