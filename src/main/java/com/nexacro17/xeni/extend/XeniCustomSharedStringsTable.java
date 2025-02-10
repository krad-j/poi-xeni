package com.nexacro17.xeni.extend;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XeniCustomSharedStringsTable extends XeniReadOnlySharedStringsTable {
   private StringBuffer characters;
   private boolean tIsOpen;

   public XeniCustomSharedStringsTable(OPCPackage var1) throws IOException, SAXException {
      super(var1);
   }

   @Override
   public void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      int var5 = var3.indexOf(":");
      if (var5 > -1) {
         var3 = var3.substring(var5 + 1, var3.length());
      }

      if ("sst".equals(var3)) {
         String var6 = var4.getValue("count");
         if (var6 != null) {
            this.setCount(Integer.parseInt(var6));
         }

         String var7 = var4.getValue("uniqueCount");
         if (var7 != null) {
            this.setUniqueCount(Integer.parseInt(var7));
         }

         this.strings = new ArrayList<>(this.getUniqueCount());
         this.characters = new StringBuffer();
      } else if ("si".equals(var3)) {
         this.characters.setLength(0);
      } else if ("t".equals(var3)) {
         this.tIsOpen = true;
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) throws SAXException {
      int var4 = var3.indexOf(":");
      if (var4 > -1) {
         var3 = var3.substring(var4 + 1, var3.length());
      }

      if ("si".equals(var3)) {
         this.setStrings(this.characters.toString());
      } else if ("t".equals(var3)) {
         this.tIsOpen = false;
      }
   }

   @Override
   public void characters(char[] var1, int var2, int var3) throws SAXException {
      if (this.tIsOpen) {
         this.characters.append(var1, var2, var3);
      }
   }
}
