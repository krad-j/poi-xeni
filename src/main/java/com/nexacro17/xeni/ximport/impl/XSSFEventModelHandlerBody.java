package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.PlatformException;
import com.nexacro17.xeni.extend.XeniBuiltinFormats;
import com.nexacro17.xeni.extend.XeniDataFormatter;
import com.nexacro17.xeni.extend.XeniReadOnlySharedStringsTable;
import com.nexacro17.xeni.extend.XeniStylesTable;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.util.XeniProperties;
import com.nexacro17.xeni.ximport.ExcelDimensionInfo;
import com.nexacro17.xeni.ximport.GridImportContext;
import com.nexacro17.xeni.ximport.POIEventModelException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.xssf.model.StylesTable;
import org.apache.poixeni.xssf.usermodel.XSSFCellStyle;
import org.apache.poixeni.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XSSFEventModelHandlerBody extends DefaultHandler {
   private static final Log logger = LogFactory.getLog(XSSFEventModelHandlerBody.class);
   private StylesTable stylesTable;
   private XeniStylesTable xeniStylesTable;
   private XeniReadOnlySharedStringsTable sharedStringsTable;
   private boolean vIsOpen;
   private boolean fIsOpen;
   private boolean isIsOpen;
   private boolean hfIsOpen;
   private XSSFEventModelHandlerBody.xssfDataType nextDataType;
   private short formatIndex;
   private String formatString;
   private final XeniDataFormatter formatter;
   private boolean formulasNotResults;
   private int nCurrExlRow = -1;
   private int nCurrDsRow = -1;
   private int[] currCellRef = new int[]{-1, -1};
   private StringBuffer value = new StringBuffer();
   private StringBuffer formula = new StringBuffer();
   private StringBuffer headerFooter = new StringBuffer();
   private DataSet dsResult = null;
   private DataSet dsInfo = null;
   private boolean bHancell = false;
   private long maxCellCount;
   private GridImportContext importContext;
   private ExcelDimensionInfo dimInfo;

   public XSSFEventModelHandlerBody(
      DataSet var1,
      ExcelDimensionInfo var2,
      StylesTable var3,
      XeniStylesTable var4,
      XeniReadOnlySharedStringsTable var5,
      boolean var6,
      boolean var7,
      GridImportContext var8,
      DataSet var9
   ) {
      this.dsResult = var1;
      this.dimInfo = var2;
      this.stylesTable = var3;
      this.xeniStylesTable = var4;
      this.sharedStringsTable = var5;
      this.formulasNotResults = var6;
      this.formatter = new XeniDataFormatter();
      this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.NUMBER;
      this.bHancell = var7;
      this.importContext = var8;
      this.maxCellCount = Long.parseLong(XeniProperties.getStringProperty("xeni.import.cell.max", "-1"));
      if (this.dimInfo.getUserStartColumn() >= 0) {
         this.dimInfo.setStartColumn(this.dimInfo.getUserStartColumn());
      }

      if (this.dimInfo.getUserEndColumn() >= 0) {
         this.dimInfo.setEndColumn(this.dimInfo.getUserEndColumn());
      }

      if (this.dimInfo.getUserStartRow() >= 0) {
         this.dimInfo.setStartRow(this.dimInfo.getUserStartRow());
      }

      if (this.dimInfo.getUserEndRow() >= 0) {
         this.dimInfo.setEndRow(this.dimInfo.getUserEndRow());
      }

      this.dsInfo = var9;
   }

   private boolean isTextTag(String var1) {
      if ("v".equals(var1)) {
         return true;
      } else {
         return "inlineStr".equals(var1) ? true : "t".equals(var1) && this.isIsOpen;
      }
   }

   @Override
   public void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      if ("dimension".equals(var2)) {
         String var5 = var4.getValue("ref");
         this.setDimension(var5);
      } else if (this.isTextTag(var2)) {
         this.vIsOpen = true;
         this.value.setLength(0);
      } else if ("is".equals(var2)) {
         this.isIsOpen = true;
      } else if ("f".equals(var2)) {
         this.setStartFormula(var4);
      } else if ("oddHeader".equals(var2)
         || "evenHeader".equals(var2)
         || "firstHeader".equals(var2)
         || "firstFooter".equals(var2)
         || "oddFooter".equals(var2)
         || "evenFooter".equals(var2)) {
         this.hfIsOpen = true;
         this.headerFooter.setLength(0);
      } else if ("row".equals(var2)) {
         int var6 = Integer.parseInt(var4.getValue("r")) - 1;
         if (var6 > this.dimInfo.getEndRow()) {
            if (this.importContext.isPartData()) {
               this.writePartDataset(this.dsResult);
            }

            throw new SAXException(new POIEventModelException("Stop parsing the document."));
         }

         this.setStartRowIndex(var6);
      } else if ("c".equals(var2)) {
         this.setStartCellValue(var4);
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) throws SAXException {
      if (this.isTextTag(var2)) {
         this.vIsOpen = false;
         if (this.isValidDimension()) {
            this.setCellValue(this.getCellValue());
         }
      } else if ("f".equals(var2)) {
         this.fIsOpen = false;
      } else if ("is".equals(var2)) {
         this.isIsOpen = false;
      } else if ("row".equals(var2)) {
         this.setEndRow();
      } else if ("oddHeader".equals(var2) || "evenHeader".equals(var2) || "firstHeader".equals(var2)) {
         this.hfIsOpen = false;
         this.setHeaderFooter(this.headerFooter.toString(), true, var2);
      } else if ("oddFooter".equals(var2) || "evenFooter".equals(var2) || "firstFooter".equals(var2)) {
         this.hfIsOpen = false;
         this.setHeaderFooter(this.headerFooter.toString(), false, var2);
      } else if ("c".equals(var2)) {
         this.value.setLength(0);
      } else if ("sheetData".equals(var2)) {
         if (this.importContext.isPartData()) {
            this.writePartDataset(this.dsResult);
         }

         throw new SAXException(new POIEventModelException("Stop parsing the document."));
      }
   }

   @Override
   public void characters(char[] var1, int var2, int var3) throws SAXException {
      if (this.vIsOpen) {
         this.value.append(var1, var2, var3);
      }

      if (this.fIsOpen) {
         this.formula.append(var1, var2, var3);
      }

      if (this.hfIsOpen) {
         this.headerFooter.append(var1, var2, var3);
      }
   }

   private void setDimension(String var1) throws SAXException {
      if (this.maxCellCount > 0L) {
         long var2 = (long)(this.dimInfo.getEndColumn() - this.dimInfo.getStartColumn() + 1)
            * (long)(this.dimInfo.getEndRow() - this.dimInfo.getStartRow() + 1);
         if (var2 > this.maxCellCount) {
            throw new SAXException("Maximum number of cells exceeded. (" + var2 + ":" + this.maxCellCount + ")");
         }
      }
   }

   private void setStartRowIndex(int var1) {
      this.sendPartDataset();
      if (this.nCurrExlRow >= 0 && var1 - this.nCurrExlRow > 1) {
         for (int var2 = this.nCurrExlRow + 1; var2 < var1; var2++) {
            if (this.dimInfo.getStartRow() <= var2) {
               this.dsResult.newRow();
            }
         }
      }

      if (this.dimInfo.getStartRow() <= var1) {
         this.nCurrDsRow = this.dsResult.newRow();
      }

      this.nCurrExlRow = var1;
   }

   private void setEndRow() {
      this.nCurrDsRow = -1;
   }

   private void setCellValue(String var1) throws SAXException {
      if (var1 != null) {
         if (this.importContext.getCorsResponseType() == 1) {
            var1 = var1.replaceAll("\n", "\\\\n");
            var1 = var1.replaceAll("\"", "\\\\\"");
         }

         this.dsResult.set(this.nCurrDsRow, this.currCellRef[0] - this.dimInfo.getStartColumn(), var1);
      }
   }

   private void setHeaderFooter(String var1, boolean var2, String var3) {
   }

   private void setStartCellValue(Attributes var1) {
      CommUtil.getRangeIndex(var1.getValue("r"), this.currCellRef);
      this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.NUMBER;
      this.formatIndex = -1;
      this.formatString = null;
      String var2 = var1.getValue("t");
      String var3 = var1.getValue("s");
      if ("b".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.BOOLEAN;
      } else if ("e".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.ERROR;
      } else if ("inlineStr".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.INLINE_STRING;
      } else if ("s".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.SST_STRING;
      } else if ("str".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.FORMULA;
      } else if (var3 != null) {
         int var4 = Integer.parseInt(var3);
         if (!this.bHancell) {
            XSSFCellStyle var5 = this.stylesTable.getStyleAt(var4);
            if (var5 != null) {
               this.formatIndex = var5.getDataFormat();
               if (this.formatIndex == 14) {
                  this.formatString = XeniBuiltinFormats.getBuiltinFormat(this.formatIndex, "");
               } else {
                  this.formatString = var5.getDataFormatString();
               }
            }
         } else {
            this.formatIndex = this.xeniStylesTable.getDataFormat(var4);
            if (this.formatIndex == 14) {
               this.formatString = XeniBuiltinFormats.getBuiltinFormat(this.formatIndex, "");
            } else {
               this.formatString = this.xeniStylesTable.getDataFormatString(this.formatIndex);
            }
         }

         if (this.formatString == null) {
            this.formatString = XeniBuiltinFormats.getBuiltinFormat(this.formatIndex, "");
         }
      }
   }

   private String getCellValue() {
      String var1 = null;
      switch (this.nextDataType) {
         case BOOLEAN:
            char var2 = this.value.charAt(0);
            var1 = var2 == '0' ? "FALSE" : "TRUE";
            break;
         case ERROR:
            var1 = "ERROR:" + this.value.toString();
         case FORMULA:
            if (this.formulasNotResults) {
               var1 = this.formula.toString();
            } else {
               var1 = this.value.toString();
               if (!this.importContext.isRawNumberValue() || this.importContext.getCompatibleMode() > 0) {
                  try {
                     double var12 = Double.parseDouble(var1);
                     var1 = this.formatter
                        .formatRawCellContents(
                           var12, this.formatIndex, this.formatString, false, this.importContext.isRawDateValue(), this.importContext.isRawNumberValue()
                        );
                     var1 = var1.replaceAll("[\"*? ]", "");
                  } catch (NumberFormatException var7) {
                  }
               }
            }
            break;
         case INLINE_STRING:
            if (!this.bHancell) {
               XSSFRichTextString var11 = new XSSFRichTextString(this.value.toString());
               var1 = var11.toString();
            } else {
               var1 = this.value.toString();
            }
            break;
         case SST_STRING:
            String var3 = this.value.toString();

            try {
               int var14 = Integer.parseInt(var3);
               if (!this.bHancell) {
                  XSSFRichTextString var5 = new XSSFRichTextString(this.sharedStringsTable.getEntryAt(var14));
                  var1 = var5.toString();
               } else {
                  var1 = this.sharedStringsTable.getEntryAt(var14);
               }
            } catch (NumberFormatException var8) {
               NumberFormatException var13 = var8;
               if (logger.isErrorEnabled()) {
                  try {
                     logger.error("Failed to parse SST index '" + URLEncoder.encode(var3, "UTF-8") + "': " + URLEncoder.encode(var13.toString(), "UTF-8"));
                  } catch (UnsupportedEncodingException var6) {
                     logger.error("endElement: UnsupportedEncodingException");
                  }
               }
            }
            break;
         case NUMBER:
            double var4 = Double.parseDouble(this.value.toString());
            var1 = this.formatter
               .formatRawCellContents(
                  var4, this.formatIndex, this.formatString, false, this.importContext.isRawDateValue(), this.importContext.isRawNumberValue()
               );
            var1 = var1.replaceAll("[\"*? ]", "");
            break;
         default:
            var1 = "(Unexpected type: " + this.nextDataType + ")";
      }

      return var1;
   }

   private void setStartFormula(Attributes var1) {
      this.formula.setLength(0);
      if (this.nextDataType == XSSFEventModelHandlerBody.xssfDataType.NUMBER) {
         this.nextDataType = XSSFEventModelHandlerBody.xssfDataType.FORMULA;
      }

      String var2 = var1.getValue("t");
      if (var2 != null && "shared".equals(var2)) {
         String var3 = var1.getValue("ref");
         if (var3 != null) {
            this.fIsOpen = true;
         } else if (this.formulasNotResults && logger.isErrorEnabled()) {
            logger.error("Warning - shared formulas not yet supported!");
         }
      } else {
         this.fIsOpen = true;
      }
   }

   private boolean isValidDimension() {
      return this.nCurrExlRow < this.dimInfo.getStartRow()
         ? false
         : this.currCellRef[0] >= this.dimInfo.getStartColumn() && this.currCellRef[0] <= this.dimInfo.getEndColumn();
   }

   private void writePartDataset(DataSet var1) {
      try {
         if (this.importContext.getPartIndex() <= 2) {
            VariableList var2 = new VariableList();
            var2.add("ErrorCode", 0);
            var2.add("ErrorMsg", "SUCCESS");
            StringWriter var3 = new StringWriter();
            CommUtil.writePartDataVariableList(var3, this.importContext, var2);
            this.importContext.getResponseWriter().write(var3.toString());
            var3.getBuffer().delete(0, var3.getBuffer().length());
            this.importContext.setPartIndex(3);
            if (this.dsInfo != null) {
               this.dsInfo.set(0, "importid", this.importContext.getImportId());
               this.writePartDataset(var1);
               this.importContext.setPartIndex(4);
            }
         }

         StringWriter var5 = new StringWriter();
         CommUtil.writePartDataDataset(var5, this.importContext, var1);
         this.importContext.getResponseWriter().write(var5.toString());
         this.importContext.getResponseWriter().flush();
         this.importContext.setPartIndex(4);
         var5.getBuffer().delete(0, var5.getBuffer().length());
      } catch (PlatformException | IOException var4) {
         logger.error(var4.getMessage());
      }
   }

   private void sendPartDataset() {
      if (this.importContext.isPartData() && this.dsResult.getRowCount() >= this.importContext.getPartSize()) {
         this.writePartDataset(this.dsResult);
      }
   }

   static enum xssfDataType {
      BOOLEAN,
      ERROR,
      FORMULA,
      INLINE_STRING,
      SST_STRING,
      NUMBER;
   }
}
