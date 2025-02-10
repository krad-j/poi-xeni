package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.tx.PlatformException;
import com.nexacro17.xeni.data.importformats.ImportSheet;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.ximport.GridImportBase;
import com.nexacro17.xeni.ximport.GridImportContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poixeni.hssf.eventusermodel.HSSFRequest;
import org.apache.poixeni.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poixeni.openxml4j.exceptions.InvalidFormatException;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.filesystem.DocumentInputStream;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public class GridImportExcelHSSFEvent implements GridImportBase {
   private static final Log logger = LogFactory.getLog(GridImportExcelHSSFEvent.class);
   private int nErrCode = 0;
   private String sErrMessage = "SUCCESS";
   private POIFSFileSystem poifs = null;
   private GridImportContext importContext;

   @Override
   public int getPartIndex() {
      return 0;
   }

   @Override
   public void setPartIndex(int var1) {
   }

   @Override
   public int getErrorCode() {
      return this.nErrCode;
   }

   @Override
   public void setErrorCode(int var1) {
      this.nErrCode = var1;
   }

   @Override
   public String getErrorMessage() {
      return this.sErrMessage;
   }

   @Override
   public void setErrorMessage(String var1) {
      this.sErrMessage = var1;
      if (logger.isInfoEnabled()) {
         logger.info(this.sErrMessage);
      }
   }

   @Override
   public void initialize(GridImportContext var1) {
      this.importContext = var1;
   }

   @Override
   public int executeImport() {
      this.nErrCode = 0;
      if (this.importContext.getFileUrl() != null && !"".equals(this.importContext.getFileUrl())) {
         DataSetList var1 = this.importContext.getPlatformData().getDataSetList();
         DataSet var2 = CommUtil.getDatasetImportResponse();
         var2.set(0, "filepath", this.importContext.getFileUrl());
         var1.add(var2);
         POIFSFileSystem var3 = null;

         try {
            var3 = this.poifs;

            for (int var4 = 0; var4 < this.importContext.getImportFormat().getSheetCount(); var4++) {
               ImportSheet var5 = this.importContext.getImportFormat().getSheet(var4);
               if (var5 != null) {
                  Object var6 = null;
                  if ("getsheetlist".equals(var5.getCommand())) {
                     var6 = this.getSheetList(var5, var3);
                  } else {
                     var6 = this.getSheetData(var5, var3);
                  }

                  if (var6 != null) {
                     var1.add((DataSet)var6);
                  }
               }
            }

            if (this.nErrCode == 0 && this.importContext.getFilePassword() != null && this.importContext.getFilePassword().length() > 0) {
               Biff8EncryptionKey.setCurrentUserPassword(null);
            }
         } catch (InvalidFormatException var23) {
            this.nErrCode = -2019;
            this.setErrorMessage(var23.getMessage());
         } catch (MalformedURLException var24) {
            this.nErrCode = -2018;
            this.setErrorMessage(var24.getMessage());
         } catch (PlatformException var25) {
            this.nErrCode = -3101;
            this.setErrorMessage(var25.getMessage());
         } catch (IOException var26) {
            this.nErrCode = -2003;
            this.setErrorMessage(var26.getMessage());
         } catch (Exception var27) {
            this.nErrCode = -2001;
            this.setErrorMessage(var27 + "");
         } finally {
            if (var3 != null) {
               try {
                  var3.close();
               } catch (IOException var22) {
                  this.nErrCode = -2003;
                  this.setErrorMessage(var22.getMessage());
               }
            }
         }

         return this.nErrCode;
      } else {
         this.setErrorMessage("Import file path is empty...");
         return -2001;
      }
   }

   private DataSet getSheetList(ImportSheet var1, POIFSFileSystem var2) throws Exception {
      if (logger.isDebugEnabled()) {
         logger.debug("Get sheet list.");
      }

      String var3 = var1.getOutput();
      if (var3 == null || "".equals(var3)) {
         var3 = "SHEETS";
      }

      DataSet var4 = new DataSet(var3);
      var4.addColumn("number", 3);
      var4.addColumn("sheetname", 2);
      HSSFRequest var5 = new HSSFRequest();
      var5.addListenerForAllRecords(
         new HSSFEventModelHandler(
            var4,
            null,
            null,
            null,
            HSSFEventModelHandler.commandType.SHEETLIST,
            "",
            this.importContext.isRawDateValue(),
            this.importContext.isRawNumberValue(),
            this.importContext.getCorsResponseType()
         )
      );
      HSSFEventFactory var6 = new HSSFEventFactory();
      DocumentInputStream var7 = var2.createDocumentInputStream("Workbook");
      var6.processEvents(var5, var7);
      if (var7 != null) {
         var7.close();
      }

      return var4;
   }

   private DataSet getSheetData(ImportSheet var1, POIFSFileSystem var2) throws Exception {
      if (logger.isDebugEnabled()) {
         logger.debug("Get sheet data.");
      }

      String var3 = var1.getOutput();
      if (var3 == null || "".equals(var3)) {
         var3 = "SHEETDATA";
      }

      DataSet var4 = new DataSet(var3);
      int[] var5 = new int[]{-1, -1};
      int[] var6 = new int[]{-1, -1};
      String var7 = null;
      String var8 = var1.getHead();
      if (var8 != null && var8.length() > 0) {
         var7 = CommUtil.getDataRange(var8, var5, var6);
      }

      if (logger.isDebugEnabled()) {
         logger.debug("Import head range : " + var8);
      }

      HSSFEventModelHandler var9 = new HSSFEventModelHandler(
         var4,
         var7,
         var5,
         var6,
         HSSFEventModelHandler.commandType.HEAD,
         "",
         this.importContext.isRawDateValue(),
         this.importContext.isRawNumberValue(),
         this.importContext.getCorsResponseType()
      );
      var9.excute(var2);
      if (!var9.isFoundSheet()) {
         this.setErrorMessage("Unable to process: Not found '" + var7 + "' sheet.");
         throw new IOException(this.sErrMessage);
      } else {
         int[] var10 = new int[]{-1, -1};
         int[] var11 = new int[]{-1, -1};
         var8 = var1.getBody();
         if (var8 != null && var8.length() > 0) {
            var7 = CommUtil.getDataRange(var8, var10, var11);
         }

         if (logger.isDebugEnabled()) {
            logger.debug("Import body range : " + var8);
         }

         HSSFEventModelHandler var12 = new HSSFEventModelHandler(
            var4,
            var7,
            var10,
            var11,
            HSSFEventModelHandler.commandType.BODY,
            "",
            this.importContext.isRawDateValue(),
            this.importContext.isRawNumberValue(),
            this.importContext.getCorsResponseType()
         );
         var12.excute(var2);
         if (!var12.isFoundSheet()) {
            this.setErrorMessage("Unable to process: Not found '" + var7 + "' sheet.");
            throw new IOException(this.sErrMessage);
         } else {
            return var4;
         }
      }
   }

   @Override
   public void setPOIFileSystem(POIFSFileSystem var1) {
      this.poifs = var1;
   }

   @Override
   public void setOPCPackage(OPCPackage var1) {
   }

   @Override
   public void setInputStream(InputStream var1) {
   }
}
