package com.nexacro17.xeni.data;

import com.nexacro17.xeni.data.exportformats.ExportFormat;
import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GridExportFormatFactory extends DefaultHandler {
   private static final Log oLogger = LogFactory.getLog(GridExportFormatFactory.class);
   private ExportFormat format;
   private int currIndex;
   protected static final int INDEX_UNDEFINED = 0;
   protected static final int INDEX_COLUMNS = 1;
   protected static final int INDEX_ROWS = 2;
   protected static final int INDEX_DATA_HEAD = 3;
   protected static final int INDEX_DATA_BODY = 4;
   protected static final int INDEX_DATA_SUMMARY = 5;

   public ExportFormat readFormatString(String var1) {
      InputSource var2 = new InputSource(new StringReader(var1));
      SAXParserFactory var3 = SAXParserFactory.newInstance();
      var3.setNamespaceAware(true);

      try {
         SAXParser var4 = var3.newSAXParser();
         XMLReader var5 = var4.getXMLReader();
         var5.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         var5.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         var5.setFeature("http://xml.org/sax/features/external-general-entities", false);
         var5.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         var4.parse(var2, this);
      } catch (Exception var6) {
         oLogger.error("Fail to create grid format : " + var6.getMessage());
         return null;
      }

      return this.format;
   }

   @Override
   public void startElement(String var1, String var2, String var3, Attributes var4) {
      if ("Cell".equals(var3)) {
         this.startCell(var4);
      } else if ("Summary".equals(var3)) {
         this.setCurrentIndex(5);
      } else if ("Body".equals(var3)) {
         this.setCurrentIndex(4);
      } else if ("Head".equals(var3)) {
         this.setCurrentIndex(3);
      } else if ("Row".equals(var3)) {
         this.startRow(var4);
      } else if ("Column".equals(var3)) {
         this.startColumn(var4);
      } else if ("Format".equals(var3)) {
         this.startFormat(var4);
      } else if ("Formats".equals(var3)) {
         this.startData();
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) {
      if (!"Cell".equals(var3)) {
         if ("Summary".equals(var3)) {
            this.setCurrentIndex(0);
         } else if ("Body".equals(var3)) {
            this.setCurrentIndex(0);
         } else if ("Head".equals(var3)) {
            this.setCurrentIndex(0);
         } else if ("Formats".equals(var3)) {
            this.endData();
         }
      }
   }

   @Override
   public void characters(char[] var1, int var2, int var3) {
   }

   protected void startData() {
      this.setData(new ExportFormat());
   }

   protected void endData() {
      this.setCurrentIndex(0);
   }

   protected void setData(ExportFormat var1) {
      this.format = var1;
   }

   protected ExportFormat getData() {
      return this.format;
   }

   protected void setCurrentIndex(int var1) {
      this.currIndex = var1;
   }

   protected int getCurrentIndex() {
      return this.currIndex;
   }

   protected void startFormat(Attributes var1) {
      String var2 = var1.getValue("id");
      this.getData().setId(var2);
   }

   protected void startColumn(Attributes var1) {
      String var2 = var1.getValue("size");
      this.getData().addColumn(var2);
   }

   protected void startRow(Attributes var1) {
      String var2 = var1.getValue("size");
      String var3 = var1.getValue("band");
      this.getData().addRow(var2, var3);
   }

   protected void startCell(Attributes var1) {
      int var2 = this.getCurrentIndex();
      if (var2 == 3 || var2 == 4 || var2 == 5) {
         this.getData()
            .addCell(
               var2,
               var1.getValue("row"),
               var1.getValue("col"),
               var1.getValue("rowspan"),
               var1.getValue("colspan"),
               var1.getValue("style"),
               var1.getValue("style1"),
               var1.getValue("style2"),
               var1.getValue("text"),
               var1.getValue("displaytype"),
               var1.getValue("edittype"),
               var1.getValue("combodisplay"),
               var1.getValue("type")
            );
      }
   }
}
