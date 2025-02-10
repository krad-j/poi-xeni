package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xeni.extend.XeniBuiltinFormats;
import com.nexacro17.xeni.extend.XeniDataFormatter;
import com.nexacro17.xeni.extend.XeniReadOnlySharedStringsTable;
import com.nexacro17.xeni.extend.XeniStylesTable;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.ximport.ExcelDimensionInfo;
import com.nexacro17.xeni.ximport.GridImportContext;
import com.nexacro17.xeni.ximport.POIEventModelException;
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

public class XSSFEventModelHandlerHead extends DefaultHandler {
   private static final Log logger = LogFactory.getLog(XSSFEventModelHandlerHead.class);
   private StylesTable stylesTable;
   private XeniStylesTable xeniStylesTable;
   private XeniReadOnlySharedStringsTable sharedStringsTable;
   private boolean vIsOpen;
   private boolean fIsOpen;
   private boolean isIsOpen;
   private boolean hfIsOpen;
   private XSSFEventModelHandlerHead.xssfDataType nextDataType;
   private short formatIndex;
   private String formatString;
   private final XeniDataFormatter formatter;
   private boolean formulasNotResults;
   private int[] currCellRef = new int[]{-1, -1};
   private StringBuffer value = new StringBuffer();
   private StringBuffer formula = new StringBuffer();
   private StringBuffer headerFooter = new StringBuffer();
   private ExcelDimensionInfo dimInfo;
   private int nCurrDsCol = 0;
   private int nPreColIndex = 0;
   private int nLastColIndex = 0;
   private boolean bHancell = false;
   private boolean bHasUserDim = false;
   private DataSet dsResult = null;
   private GridImportContext importContext;

   public XSSFEventModelHandlerHead(
      DataSet var1,
      ExcelDimensionInfo var2,
      StylesTable var3,
      XeniStylesTable var4,
      XeniReadOnlySharedStringsTable var5,
      boolean var6,
      boolean var7,
      GridImportContext var8
   ) {
      this.dsResult = var1;
      this.dimInfo = var2;
      this.stylesTable = var3;
      this.xeniStylesTable = var4;
      this.sharedStringsTable = var5;
      this.formulasNotResults = var6;
      this.formatter = new XeniDataFormatter();
      this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.NUMBER;
      this.bHancell = var7;
      this.importContext = var8;
      this.bHasUserDim = var2.isUserDimension();
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
      if (!"dimension".equals(var2)) {
         if (this.isTextTag(var2)) {
            this.vIsOpen = true;
            this.value.setLength(0);
         } else if ("is".equals(var2)) {
            this.isIsOpen = true;
         } else if ("f".equals(var2)) {
            this.formula.setLength(0);
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
            int var5 = Integer.parseInt(var4.getValue("r")) - 1;
            this.setStartRowIndex(var5);
         } else if ("c".equals(var2)) {
            this.setStartCellValue(var4);
         }
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) throws SAXException {
      if (this.isTextTag(var2)) {
         this.vIsOpen = false;
         if (this.bHasUserDim && this.isValidDimension()) {
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
         if (!this.bHasUserDim) {
            this.addColumn();
         } else {
            this.addEmptyColumn();
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

   private void setStartFormula(Attributes var1) {
      if (this.nextDataType == XSSFEventModelHandlerHead.xssfDataType.NUMBER) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.FORMULA;
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

   private void setStartRowIndex(int var1) {
      this.nPreColIndex = 0;
      if (this.dimInfo.getStartRow() < 0) {
         this.dimInfo.setStartRow(var1);
      }

      this.dimInfo.setEndRow(var1);
   }

   private void setEndRow() {
   }

   private void setStartCellValue(Attributes var1) {
      CommUtil.getRangeIndex(var1.getValue("r"), this.currCellRef);
      if (this.dimInfo.getStartColumn() < 0) {
         this.dimInfo.setStartColumn(this.currCellRef[0]);
      } else if (this.dimInfo.getStartColumn() > this.currCellRef[0]) {
         this.dimInfo.setStartColumn(this.currCellRef[0]);
      }

      if (this.dimInfo.getEndColumn() < 0) {
         this.dimInfo.setEndColumn(this.currCellRef[0]);
      } else if (this.dimInfo.getEndColumn() < this.currCellRef[0]) {
         this.dimInfo.setEndColumn(this.currCellRef[0]);
      }

      this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.NUMBER;
      this.formatIndex = -1;
      this.formatString = null;
      String var2 = var1.getValue("t");
      String var3 = var1.getValue("s");
      if ("b".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.BOOLEAN;
      } else if ("e".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.ERROR;
      } else if ("inlineStr".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.INLINE_STRING;
      } else if ("s".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.SST_STRING;
      } else if ("str".equals(var2)) {
         this.nextDataType = XSSFEventModelHandlerHead.xssfDataType.FORMULA;
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
               if (!this.importContext.isRawNumberValue()) {
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

   private void setCellValue(String var1) throws SAXException {
      if (var1 != null && this.importContext.getCorsResponseType() == 1) {
         var1 = var1.replaceAll("\n", "\\\\n");
         var1 = var1.replaceAll("\"", "\\\\\"");
      }

      int var2 = this.currCellRef[0] - this.nPreColIndex;
      if (this.nCurrDsCol == 0 && var2 > 0) {
         int var7 = this.dimInfo.getUserStartColumn() >= 0 ? this.dimInfo.getUserStartColumn() : this.dimInfo.getStartColumn();
         int var4 = this.currCellRef[0];

         for (int var5 = this.nPreColIndex; var5 < this.currCellRef[0]; var5++) {
            if (var5 >= var7 && var5 <= var4) {
               this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
               this.nCurrDsCol++;
            }
         }
      } else if (this.nCurrDsCol != 0 && var2 > 1) {
         for (int var3 = 1; var3 < var2; var3++) {
            this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
            this.nCurrDsCol++;
         }
      }

      this.nLastColIndex = this.nPreColIndex = this.currCellRef[0];
      if (var1 != null && !"".equals(var1)) {
         this.dsResult.addColumn(var1, 2, 256);
      } else {
         this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
      }

      this.nCurrDsCol++;
   }

   private boolean isValidDimension() {
      if (this.dimInfo.getEndRow() >= 0 && (this.dimInfo.getUserStartRow() < 0 || this.dimInfo.getUserStartRow() <= this.dimInfo.getEndRow())) {
         if (this.dimInfo.getUserEndRow() >= 0 && this.dimInfo.getUserEndRow() < this.dimInfo.getEndRow()) {
            return false;
         } else {
            return this.dimInfo.getUserStartColumn() >= 0 && this.dimInfo.getUserStartColumn() > this.currCellRef[0]
               ? false
               : this.dimInfo.getUserEndColumn() < 0 || this.dimInfo.getUserEndColumn() >= this.currCellRef[0];
         }
      } else {
         return false;
      }
   }

   private void setHeaderFooter(String var1, boolean var2, String var3) {
   }

   private void addColumn() {
      for (int var1 = 0; var1 <= this.dimInfo.getEndColumn() - this.dimInfo.getStartColumn(); var1++) {
         this.dsResult.addColumn("Column" + var1, 2, 256);
      }
   }

   private void addEmptyColumn() {
      int var1 = this.currCellRef[0] - this.nLastColIndex;
      if (this.nCurrDsCol != 0 && var1 > 0) {
         int var2 = this.dimInfo.getUserStartColumn() >= 0 ? this.dimInfo.getUserStartColumn() : this.dimInfo.getStartColumn();
         int var3 = this.dimInfo.getUserEndColumn() >= 0 ? this.dimInfo.getUserEndColumn() : this.dimInfo.getEndColumn();

         for (int var4 = this.nLastColIndex + 1; var4 <= this.nLastColIndex + var1; var4++) {
            if (var2 <= var4 && var4 <= var3) {
               this.dsResult.addColumn("Column" + this.nCurrDsCol, 2, 256);
               this.nCurrDsCol++;
            }
         }
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
