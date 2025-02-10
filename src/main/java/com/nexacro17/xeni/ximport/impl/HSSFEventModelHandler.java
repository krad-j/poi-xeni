package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xeni.extend.XeniBuiltinFormats;
import com.nexacro17.xeni.extend.XeniDataFormatter;
import com.nexacro17.xeni.util.XeniProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.hssf.eventusermodel.AbortableHSSFListener;
import org.apache.poixeni.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poixeni.hssf.eventusermodel.HSSFRequest;
import org.apache.poixeni.hssf.eventusermodel.HSSFUserException;
import org.apache.poixeni.hssf.model.HSSFFormulaParser;
import org.apache.poixeni.hssf.record.BOFRecord;
import org.apache.poixeni.hssf.record.BlankRecord;
import org.apache.poixeni.hssf.record.BoolErrRecord;
import org.apache.poixeni.hssf.record.BoundSheetRecord;
import org.apache.poixeni.hssf.record.CellValueRecordInterface;
import org.apache.poixeni.hssf.record.DimensionsRecord;
import org.apache.poixeni.hssf.record.ExtendedFormatRecord;
import org.apache.poixeni.hssf.record.FormatRecord;
import org.apache.poixeni.hssf.record.FormulaRecord;
import org.apache.poixeni.hssf.record.LabelRecord;
import org.apache.poixeni.hssf.record.LabelSSTRecord;
import org.apache.poixeni.hssf.record.NumberRecord;
import org.apache.poixeni.hssf.record.Record;
import org.apache.poixeni.hssf.record.SSTRecord;
import org.apache.poixeni.hssf.record.StringRecord;
import org.apache.poixeni.hssf.usermodel.HSSFDataFormat;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public class HSSFEventModelHandler extends AbortableHSSFListener {
   private static final Log logger = LogFactory.getLog(HSSFEventModelHandler.class);
   private int nextRow;
   private int nextColumn;
   private SSTRecord sstRecord;
   private boolean outputNextStringRecord;
   private XeniDataFormatter formatter;
   private HSSFEventModelHandler.commandType cmdType;
   private boolean outputFormulaValues = true;
   private String sSheetName = null;
   private int[] nStartRange = new int[]{-1, -1};
   private int[] nEndRange = new int[]{-1, -1};
   private int nStartCol = 0;
   private int nEndCol = 0;
   private int nStartRow = 0;
   private int nEndRow = 0;
   private int nCurrDsCol = 0;
   private int nPreColIndex = 0;
   private DataSet dsResult = null;
   private int nSheetIdx = 0;
   private boolean bFoundSheet = false;
   private String numberFmtLang = null;
   private boolean bRawDateValue = false;
   private boolean bRawNumValue = true;
   private int responseType = 0;
   private long maxCellCount;
   private List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();
   private final Map<Integer, FormatRecord> customFormatRecords = new Hashtable<>();
   private final List<ExtendedFormatRecord> extFormatRecords = new ArrayList<>();

   public HSSFEventModelHandler(
      DataSet var1, String var2, int[] var3, int[] var4, HSSFEventModelHandler.commandType var5, String var6, boolean var7, boolean var8, int var9
   ) {
      this.dsResult = var1;
      this.sSheetName = var2;
      this.nStartRange = var3;
      this.nEndRange = var4;
      this.cmdType = var5;
      this.formatter = new XeniDataFormatter();
      this.numberFmtLang = var6;
      this.bRawDateValue = var7;
      this.bRawNumValue = var8;
      this.responseType = var9;
      this.maxCellCount = Long.parseLong(XeniProperties.getStringProperty("xeni.import.cell.max", "-1"));
   }

   public void excute(POIFSFileSystem var1) throws IOException {
      HSSFEventFactory var2 = new HSSFEventFactory();
      HSSFRequest var3 = new HSSFRequest();
      var3.addListenerForAllRecords(this);
      var2.processWorkbookEvents(var3, var1.getRoot());
   }

   public short abortableProcessRecord(Record var1) throws HSSFUserException {
      short var2 = var1.getSid();
      if (!this.bFoundSheet && var2 != 1054 && var2 != 224 && var2 != 133 && var2 != 2057 && var2 != 10 && var2 != 252) {
         return 0;
      } else {
         short var3 = 0;
         int var4 = -1;
         int var5 = -1;
         String var6 = null;
         switch (var2) {
            case 6:
               FormulaRecord var12 = (FormulaRecord)var1;
               var4 = var12.getRow();
               var5 = var12.getColumn();
               if (this.outputFormulaValues) {
                  int var19 = var12.getCachedResultType();
                  if (var19 == 1) {
                     this.outputNextStringRecord = true;
                     this.nextRow = var12.getRow();
                     this.nextColumn = var12.getColumn();
                  } else if (var19 == 4) {
                     var6 = var12.getCachedBooleanValue() ? "TRUE" : "FALSE";
                  } else if (var19 == 5) {
                     var6 = "ERROR:" + var12.getCachedErrorValue();
                  } else {
                     var6 = this.formatNumberDateCell(var12, var12.getValue());
                  }
               } else {
                  var6 = HSSFFormulaParser.toFormulaString(null, var12.getParsedExpression());
               }
               break;
            case 10:
               if (this.cmdType == HSSFEventModelHandler.commandType.SHEETLIST) {
                  var3 = this.getSheetList();
               } else if (this.bFoundSheet) {
                  var3 = -1;
               }
               break;
            case 133:
               this.boundSheetRecords.add((BoundSheetRecord)var1);
               break;
            case 224:
               ExtendedFormatRecord var8 = (ExtendedFormatRecord)var1;
               this.extFormatRecords.add(var8);
               break;
            case 252:
               this.sstRecord = (SSTRecord)var1;
               break;
            case 253:
               LabelSSTRecord var14 = (LabelSSTRecord)var1;
               var4 = var14.getRow();
               var5 = var14.getColumn();
               if (this.sstRecord == null) {
                  var6 = "";
               } else {
                  var6 = this.sstRecord.getString(var14.getSSTIndex()).toString();
               }
               break;
            case 512:
               DimensionsRecord var10 = (DimensionsRecord)var1;
               var3 = this.setDimension(var10);
               break;
            case 513:
               BlankRecord var11 = (BlankRecord)var1;
               var4 = var11.getRow();
               var5 = var11.getColumn();
               var6 = "";
               break;
            case 515:
               NumberRecord var15 = (NumberRecord)var1;
               var4 = var15.getRow();
               var5 = var15.getColumn();
               var6 = this.formatNumberDateCell(var15, var15.getValue());
               break;
            case 516:
               LabelRecord var18 = (LabelRecord)var1;
               var4 = var18.getRow();
               var5 = var18.getColumn();
               var6 = var18.getValue();
               break;
            case 517:
               BoolErrRecord var16 = (BoolErrRecord)var1;
               var4 = var16.getRow();
               var5 = var16.getColumn();
               byte var17 = var16.getErrorValue();
               var6 = var17 == 0 ? "FALSE" : "TRUE";
               break;
            case 519:
               if (this.outputNextStringRecord) {
                  StringRecord var13 = (StringRecord)var1;
                  var6 = var13.getString();
                  var4 = this.nextRow;
                  var5 = this.nextColumn;
                  this.outputNextStringRecord = false;
               }
               break;
            case 1054:
               FormatRecord var7 = (FormatRecord)var1;
               this.customFormatRecords.put(var7.getIndexCode(), var7);
               break;
            case 2057:
               BOFRecord var9 = (BOFRecord)var1;
               if (var9.getType() == 16) {
                  this.findSheet();
               }
         }

         if (var3 > -1 && var4 >= 0 && var5 >= 0) {
            var3 = this.setCellValue(var4, var5, var6);
         }

         return var3;
      }
   }

   private void findSheet() {
      if (this.sSheetName != null && !"".equals(this.sSheetName)) {
         String var1 = this.boundSheetRecords.get(this.nSheetIdx).getSheetname();
         if (var1.equalsIgnoreCase(this.sSheetName)) {
            this.bFoundSheet = true;
         }
      } else {
         this.bFoundSheet = true;
      }

      this.nSheetIdx++;
   }

   public boolean isFoundSheet() {
      return this.bFoundSheet;
   }

   private String formatNumberDateCell(CellValueRecordInterface var1, double var2) {
      int var4 = this.getFormatIndex(var1);
      String var5 = this.getFormatString(var4);
      String var6 = "";
      var6 = this.formatter.formatRawCellContents(var2, var4, var5, false, this.bRawDateValue, this.bRawNumValue);
      return var6.replaceAll("[\"*? ]", "");
   }

   private short setDimension(DimensionsRecord var1) throws HSSFUserException {
      if (logger.isDebugEnabled() && var1 != null) {
         logger.debug("Set dimension[ " + var1.getFirstCol() + ":" + var1.getFirstRow() + "-" + (var1.getLastCol() - 1) + ":" + (var1.getLastRow() - 1) + " ]");
      }

      if (this.cmdType == HSSFEventModelHandler.commandType.HEAD
         && this.nStartRange[0] < 0
         && this.nStartRange[1] < 0
         && this.nEndRange[0] < 0
         && this.nEndRange[1] < 0) {
         if (this.maxCellCount > 0L) {
            long var5 = (long)(var1.getLastCol() - var1.getFirstCol()) * (long)(var1.getLastRow() - var1.getFirstRow());
            if (var5 > this.maxCellCount) {
               throw new HSSFUserException("Maximum number of cells exceeded. (" + var5 + ":" + this.maxCellCount + ")");
            }
         }

         int var6 = var1.getLastCol() - 1 - var1.getFirstCol();

         for (int var3 = 0; var3 <= var6; var3++) {
            this.dsResult.addColumn("Column" + var3, 2, 256);
         }

         return -1;
      } else {
         this.nStartCol = this.nStartRange[0] < 0 ? var1.getFirstCol() : this.nStartRange[0];
         this.nStartRow = this.nStartRange[1] < 0 ? var1.getFirstRow() : this.nStartRange[1];
         this.nEndCol = this.nEndRange[0] < 0 ? var1.getLastCol() - 1 : this.nEndRange[0];
         this.nEndRow = this.nEndRange[1] < 0 ? var1.getLastRow() - 1 : this.nEndRange[1];
         if (this.maxCellCount > 0L) {
            long var2 = (long)(this.nEndCol - this.nStartCol + 1) * (long)(this.nEndRow - this.nStartRow + 1);
            if (var2 > this.maxCellCount) {
               throw new HSSFUserException("Maximum number of cells exceeded. (" + var2 + ":" + this.maxCellCount + ")");
            }
         }

         if (this.cmdType == HSSFEventModelHandler.commandType.BODY) {
            for (int var4 = 0; var4 <= this.nEndRow - this.nStartRow; var4++) {
               this.dsResult.newRow();
            }
         }

         return 0;
      }
   }

   private String getFormatString(CellValueRecordInterface var1) {
      int var2 = this.getFormatIndex(var1);
      return var2 == -1 ? null : this.getFormatString(var2);
   }

   private int getFormatIndex(CellValueRecordInterface var1) {
      ExtendedFormatRecord var2 = this.extFormatRecords.get(var1.getXFIndex());
      return var2 == null ? -1 : var2.getFormatIndex();
   }

   private String getFormatString(int var1) {
      if (var1 == -1) {
         return null;
      } else {
         String var2 = null;
         if (var1 == 14) {
            var2 = XeniBuiltinFormats.getBuiltinFormat(var1, this.numberFmtLang);
         } else if (var1 >= HSSFDataFormat.getNumberOfBuiltinBuiltinFormats()) {
            FormatRecord var3 = this.customFormatRecords.get(var1);
            if (var3 != null) {
               var2 = var3.getFormatString();
            }
         } else {
            var2 = XeniBuiltinFormats.getBuiltinFormat(var1, this.numberFmtLang);
         }

         if (var2 == null) {
            var2 = XeniBuiltinFormats.getBuiltinFormat(var1, this.numberFmtLang);
         }

         return var2;
      }
   }

   private short getSheetList() {
      for (int var1 = 0; var1 < this.boundSheetRecords.size(); var1++) {
         int var2 = this.dsResult.newRow();
         BoundSheetRecord var3 = this.boundSheetRecords.get(var1);
         this.dsResult.set(var2, "number", var1 + 1);
         this.dsResult.set(var2, "sheetname", var3 == null ? "" : var3.getSheetname());
      }

      return -1;
   }

   private short setCellValue(int var1, int var2, String var3) {
      if (var1 > this.nEndRow) {
         return -1;
      } else if (var1 >= this.nStartRow && var2 >= this.nStartCol && var2 <= this.nEndCol) {
         if (this.responseType == 1 && var3 != null) {
            var3 = var3.replaceAll("\n", "\\\\n");
            var3 = var3.replaceAll("\"", "\\\\\"");
         }

         if (this.cmdType == HSSFEventModelHandler.commandType.HEAD) {
            int var4 = var2 - this.nPreColIndex;
            this.nPreColIndex = var2;
            if (this.nCurrDsCol != 0 && var4 > 1) {
               for (int var5 = 1; var5 < var4; var5++) {
                  this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
                  this.nCurrDsCol++;
               }
            }

            if (var3 != null && !"".equals(var3)) {
               this.dsResult.addColumn(var3, 2, 256);
            } else {
               this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
            }

            this.nCurrDsCol++;
         } else if (this.cmdType == HSSFEventModelHandler.commandType.BODY && var3 != null) {
            this.dsResult.set(var1 - this.nStartRow, var2 - this.nStartCol, var3);
         }

         return 0;
      } else {
         return 0;
      }
   }

   static enum commandType {
      SHEETLIST,
      HEAD,
      BODY;
   }
}
