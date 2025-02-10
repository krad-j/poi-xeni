package com.nexacro17.xeni.data;

import com.nexacro17.xeni.data.importformats.ImportFormat;
import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GridImportFormatFactory extends DefaultHandler {
   private static final Log oLogger = LogFactory.getLog(GridImportFormatFactory.class);
   private static GridImportFormatFactory oGridImportFormatFactory;
   private ImportFormat importformat;

   public static GridImportFormatFactory getInstance() {
      if (oGridImportFormatFactory == null) {
         oGridImportFormatFactory = new GridImportFormatFactory();
      }

      return oGridImportFormatFactory;
   }

   public ImportFormat readFormatString(String var1) {
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
         var6.printStackTrace();
         return null;
      }

      return this.importformat;
   }

   @Override
   public void startElement(String var1, String var2, String var3, Attributes var4) {
      if ("Sheet".equals(var3)) {
         this.startSheet(var4);
      } else if ("Import".equals(var3)) {
         this.startData();
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) {
      if (!"Sheet".equals(var3) && "Import".equals(var3)) {
      }
   }

   @Override
   public void characters(char[] var1, int var2, int var3) {
   }

   protected void startData() {
      this.setData(new ImportFormat());
   }

   protected void setData(ImportFormat var1) {
      this.importformat = var1;
   }

   protected ImportFormat getData() {
      return this.importformat;
   }

   protected void startSheet(Attributes var1) {
      String var2 = null;
      String var3 = null;
      String var4 = null;
      String var5 = null;

      for (int var6 = 0; var6 < var1.getLength(); var6++) {
         String var7 = var1.getQName(var6);
         String var8 = var1.getValue(var6);
         if ("command".equalsIgnoreCase(var7)) {
            var2 = var8;
         }

         if ("output".equalsIgnoreCase(var7)) {
            var3 = var8;
         }

         if ("head".equalsIgnoreCase(var7)) {
            var4 = var8;
         }

         if ("body".equalsIgnoreCase(var7)) {
            var5 = var8;
         }
      }

      this.getData().addSheet(var2, var3, var4, var5);
   }
}
