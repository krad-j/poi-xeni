package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.PlatformException;
import com.nexacro17.xeni.data.importformats.ImportSheet;
import com.nexacro17.xeni.extend.XeniReadOnlySharedStringsTable;
import com.nexacro17.xeni.extend.XeniStylesTable;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.ximport.ExcelDimensionInfo;
import com.nexacro17.xeni.ximport.GridImportBase;
import com.nexacro17.xeni.ximport.GridImportContext;
import com.nexacro17.xeni.ximport.POIEventModelException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.openxml4j.exceptions.InvalidFormatException;
import org.apache.poixeni.openxml4j.exceptions.OpenXML4JException;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;
import org.apache.poixeni.xssf.eventusermodel.XSSFReader;
import org.apache.poixeni.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poixeni.xssf.model.StylesTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class GridImportExcelXSSFEvent implements GridImportBase {
   private static final Log logger = LogFactory.getLog(GridImportExcelXSSFEvent.class);
   private int nErrCode = 0;
   private boolean bHancell = false;
   private String sErrMessage;
   private OPCPackage opcPackage;
   private InputStream inputStream;
   private POIFSFileSystem poiFs = null;
   private GridImportContext importContext;
   private int partIndex;

   public GridImportExcelXSSFEvent() {
      this.inputStream = null;
      this.opcPackage = null;
      this.importContext = null;
      this.sErrMessage = "SUCCESS";
   }

   @Override
   public void setOPCPackage(OPCPackage var1) {
      this.opcPackage = var1;
   }

   @Override
   public void setPOIFileSystem(POIFSFileSystem var1) {
      this.poiFs = var1;
   }

   @Override
   public void setInputStream(InputStream var1) {
      this.inputStream = var1;
   }

   @Override
   public void setPartIndex(int var1) {
      this.partIndex = var1;
   }

   @Override
   public int getPartIndex() {
      return this.partIndex;
   }

   @Override
   public void setErrorMessage(String var1) {
      this.sErrMessage = var1;
      logger.error(var1);
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
   public void initialize(GridImportContext var1) {
      if (var1.getImportType() == 1040 || var1.getImportType() == 1056) {
         this.bHancell = true;
      }

      this.importContext = var1;
   }

   @Override
   public int executeImport() {
      if (this.importContext.getFileUrl() != null && !"".equals(this.importContext.getFileUrl())) {
         this.nErrCode = 0;
         DataSetList var1 = this.importContext.getPlatformData().getDataSetList();
         DataSet var2 = CommUtil.getDatasetImportResponse();
         var2.set(0, "filepath", this.importContext.getFileUrl());
         var1.add(var2);

         int var3;
         try {
            if ((this.nErrCode = this.sendDataHead()) >= 0) {
               XSSFReader var39 = new XSSFReader(this.opcPackage);

               for (int var4 = 0; var4 < this.importContext.getImportFormat().getSheetCount(); var4++) {
                  ImportSheet var5 = this.importContext.getImportFormat().getSheet(var4);
                  if (var5 != null) {
                     Object var6 = null;
                     if ("getsheetlist".equals(var5.getCommand())) {
                        var6 = this.getSheetList(var39, var5);
                        if (this.importContext.isPartData()) {
                           this.sendDataFirst(var2);
                           this.writePartDataset((DataSet)var6);
                        }
                     } else {
                        var6 = this.getSheetData(var39, var5, this.opcPackage, var2);
                     }

                     if (var6 != null && !this.importContext.isPartData()) {
                        var1.add((DataSet)var6);
                     }
                  }
               }

               this.sendDataTail();
               return this.nErrCode;
            }

            var3 = this.nErrCode;
         } catch (InvalidFormatException var30) {
            this.nErrCode = -2019;
            this.setErrorMessage(var30.getMessage());
            return this.nErrCode;
         } catch (OpenXML4JException var31) {
            this.nErrCode = -2001;
            this.setErrorMessage(var31.getMessage());
            return this.nErrCode;
         } catch (SAXException var32) {
            this.nErrCode = -2001;
            this.setErrorMessage(var32.getMessage());
            return this.nErrCode;
         } catch (MalformedURLException var33) {
            this.nErrCode = -2018;
            this.setErrorMessage(var33.getMessage());
            return this.nErrCode;
         } catch (PlatformException var34) {
            this.nErrCode = -3101;
            this.setErrorMessage(var34.getMessage());
            return this.nErrCode;
         } catch (IOException var35) {
            this.nErrCode = -2003;
            this.setErrorMessage(var35.getMessage());
            return this.nErrCode;
         } catch (GeneralSecurityException var36) {
            this.nErrCode = -2021;
            this.setErrorMessage(var36.getMessage());
            return this.nErrCode;
         } catch (Exception var37) {
            this.nErrCode = -2001;
            this.setErrorMessage(var37 + "");
            return this.nErrCode;
         } finally {
            try {
               if (this.opcPackage != null) {
                  this.opcPackage.close();
               }

               if (this.inputStream != null) {
                  this.inputStream.close();
               }

               if (this.poiFs != null) {
                  this.poiFs.close();
               }
            } catch (IOException var29) {
               this.nErrCode = -2003;
               this.setErrorMessage(var29.getMessage());
            }
         }

         return var3;
      } else {
         this.setErrorMessage("Import file path is empty...");
         return -2001;
      }
   }

   private DataSet getSheetList(XSSFReader var1, ImportSheet var2) throws Exception {
      String var3 = var2.getOutput();
      if (var3 == null || "".equals(var3)) {
         var3 = "SHEETS";
      }

      DataSet var4 = new DataSet(var3);
      var4.addColumn("number", 3);
      var4.addColumn("sheetname", 2);
      SheetIterator var5 = (SheetIterator)var1.getSheetsData();

      while (var5.hasNext()) {
         InputStream var6 = var5.next();
         int var7 = var4.newRow();
         var4.set(var7, "number", var7 + 1);
         var4.set(var7, "sheetname", var5.getSheetName());
         var6.close();
      }

      return var4;
   }

   private DataSet getSheetData(XSSFReader var1, ImportSheet var2, OPCPackage var3, DataSet var4) throws OpenXML4JException, SAXException, Exception {
      String var5 = var2.getOutput();
      if (var5 == null || "".equals(var5)) {
         var5 = "SHEETDATA";
      }

      DataSet var6 = new DataSet(var5);
      StylesTable var7 = null;
      XeniStylesTable var8 = new XeniStylesTable();

      try {
         var8.readFrom(var3);
      } catch (SAXException var21) {
         if (!(var21.getException() instanceof POIEventModelException)) {
            throw var21;
         }
      }

      this.bHancell = var8.isHancellStyle();
      if (!this.bHancell) {
         var7 = var1.getStylesTable();
      }

      XeniReadOnlySharedStringsTable var9 = new XeniReadOnlySharedStringsTable(var3);
      Object var10 = null;
      XMLReader var11 = this.createXMLReader();
      int[] var12 = new int[]{-1, -1};
      int[] var13 = new int[]{-1, -1};
      InputSource var14 = this.getSheetSource(var1, var2.getHead(), var12, var13);
      ExcelDimensionInfo var15 = new ExcelDimensionInfo();
      var15.setUserStartColumn(var12[0]);
      var15.setUserStartRow(var12[1]);
      var15.setUserEndColumn(var13[0]);
      var15.setUserEndRow(var13[1]);
      var10 = new XSSFEventModelHandlerHead(var6, var15, var7, var8, var9, false, this.bHancell, this.importContext);

      try {
         var11.setContentHandler((ContentHandler)var10);
         var11.parse(var14);
      } catch (SAXException var20) {
         if (!(var20.getException() instanceof POIEventModelException)) {
            throw var20;
         }
      }

      int[] var16 = new int[]{-1, -1};
      int[] var17 = new int[]{-1, -1};
      var14 = this.getSheetSource(var1, var2.getBody(), var16, var17);
      var15.setUserStartColumn(var16[0]);
      var15.setUserStartRow(var16[1]);
      var15.setUserEndColumn(var17[0]);
      var15.setUserEndRow(var17[1]);
      var10 = new XSSFEventModelHandlerBody(var6, var15, var7, var8, var9, false, this.bHancell, this.importContext, var4);

      try {
         var11.setContentHandler((ContentHandler)var10);
         var11.parse(var14);
      } catch (SAXException var19) {
         if (!(var19.getException() instanceof POIEventModelException)) {
            throw var19;
         }
      }

      return var6;
   }

   public InputSource getSheetSource(XSSFReader var1, String var2, int[] var3, int[] var4) throws OpenXML4JException, SAXException, InvalidFormatException, IOException {
      Object var5 = null;
      if (var2 != null && var2.length() > 0) {
         String var6 = CommUtil.getDataRange(var2, var3, var4);
         if (var6 != null && !"".equals(var6)) {
            var5 = this.getSheetStream(var1, var6);
            if (var5 == null) {
               this.setErrorMessage("Unable to process: Not found '" + var6 + "' sheet.");
               throw new IOException(this.sErrMessage);
            }
         } else {
            var5 = this.getSheetStream(var1, null);
         }
      } else {
         var5 = this.getSheetStream(var1, null);
      }

      return new InputSource((InputStream)var5);
   }

   private InputStream getSheetStream(XSSFReader var1, String var2) throws InvalidFormatException, IOException {
      InputStream var3 = null;

      for (SheetIterator var4 = (SheetIterator)var1.getSheetsData(); var4.hasNext(); var3 = null) {
         if (var2 == null) {
            var3 = var4.next();
            break;
         }

         var3 = var4.next();
         if (var2.equalsIgnoreCase(var4.getSheetName())) {
            break;
         }

         var3.close();
      }

      return var3;
   }

   private XMLReader createXMLReader() throws SAXException {
      XMLReader var1 = XMLReaderFactory.createXMLReader();
      var1.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      var1.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      var1.setFeature("http://xml.org/sax/features/external-general-entities", false);
      var1.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return var1;
   }

   private int sendDataHead() throws PlatformException, IOException {
      if (this.importContext.isPartData()) {
         if (this.importContext.getHttpReponse() == null) {
            this.setErrorMessage("Response is null.");
            return -2001;
         }

         this.importContext.setPartIndex(0);
         this.importContext.getHttpReponse().addHeader("Content-Type", "text/html;charset=UTF-8");
         this.importContext.setPartIndex(1);
         StringWriter var1 = new StringWriter();
         CommUtil.writePartDataHead(var1, this.importContext);
         this.importContext.getResponseWriter().write(var1.toString());
         this.importContext.setPartIndex(2);
      }

      return 0;
   }

   private int sendDataFirst(DataSet var1) throws PlatformException, IOException {
      if (this.importContext.isPartData()) {
         if (this.importContext.getHttpReponse() == null) {
            this.setErrorMessage("Response is null.");
            return -2001;
         }

         VariableList var2 = new VariableList();
         var2.add("ErrorCode", 0);
         var2.add("ErrorMsg", "SUCCESS");
         StringWriter var3 = new StringWriter();
         CommUtil.writePartDataVariableList(var3, this.importContext, var2);
         this.importContext.getResponseWriter().write(var3.toString());
         var3.getBuffer().delete(0, var3.getBuffer().length());
         this.importContext.setPartIndex(3);
         var1.set(0, "importid", this.importContext.getImportId());
         this.writePartDataset(var1);
         this.importContext.setPartIndex(4);
      }

      return 0;
   }

   private void writePartDataset(DataSet var1) throws IOException, PlatformException {
      StringWriter var2 = new StringWriter();
      CommUtil.writePartDataDataset(var2, this.importContext, var1);
      this.importContext.getResponseWriter().write(var2.toString());
      this.importContext.getResponseWriter().flush();
      var2.getBuffer().delete(0, var2.getBuffer().length());
   }

   private void sendDataTail() throws IOException, PlatformException {
      if (this.importContext.isPartData()) {
         StringWriter var1 = new StringWriter();
         CommUtil.writePartDataTail(var1, this.importContext);
         this.importContext.getResponseWriter().write(var1.toString());
         this.importContext.getResponseWriter().flush();
         var1.getBuffer().delete(0, var1.getBuffer().length());
         this.importContext.setPartIndex(6);
      }
   }
}
