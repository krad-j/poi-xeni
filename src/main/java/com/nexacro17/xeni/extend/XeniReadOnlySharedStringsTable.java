package com.nexacro17.xeni.extend;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.openxml4j.opc.PackagePart;
import org.apache.poixeni.openxml4j.opc.PackageRelationship;
import org.apache.poixeni.xssf.usermodel.XSSFRelation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XeniReadOnlySharedStringsTable extends DefaultHandler {
   private int count;
   private int uniqueCount;
   public List<String> strings;
   private StringBuffer characters;
   private boolean tIsOpen;
   private boolean isPhonetic = false;

   public List<String> getStrings() {
      return this.strings;
   }

   public void setStrings(String var1) {
      this.strings.add(var1);
   }

   public void setCount(int var1) {
      this.count = var1;
   }

   public void setUniqueCount(int var1) {
      this.uniqueCount = var1;
   }

   public XeniReadOnlySharedStringsTable(OPCPackage var1) throws IOException, SAXException {
      ArrayList var2 = var1.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
      if (var2.size() > 0) {
         PackagePart var3 = (PackagePart)var2.get(0);
         this.readFrom(var3.getInputStream());
      }
   }

   public XeniReadOnlySharedStringsTable(PackagePart var1, PackageRelationship var2) throws IOException, SAXException {
      this.readFrom(var1.getInputStream());
   }

   public void readFrom(InputStream var1) throws IOException, SAXException {
      InputSource var2 = new InputSource(var1);
      SAXParserFactory var3 = SAXParserFactory.newInstance();

      try {
         SAXParser var4 = var3.newSAXParser();
         XMLReader var5 = var4.getXMLReader();
         var5.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         var5.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         var5.setFeature("http://xml.org/sax/features/external-general-entities", false);
         var5.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         var5.setContentHandler(this);
         var5.parse(var2);
      } catch (ParserConfigurationException var6) {
         throw new RuntimeException("SAX parser appears to be broken - " + var6.getMessage());
      }
   }

   public int getCount() {
      return this.count;
   }

   public int getUniqueCount() {
      return this.uniqueCount;
   }

   public String getEntryAt(int var1) {
      return this.strings.get(var1);
   }

   public List<String> getItems() {
      return this.strings;
   }

   public StringBuffer getCharacters() {
      return this.characters;
   }

   public void setCharacters(StringBuffer var1) {
      this.characters = var1;
   }

   public boolean istIsOpen() {
      return this.tIsOpen;
   }

   public void settIsOpen(boolean var1) {
      this.tIsOpen = var1;
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
            this.count = Integer.parseInt(var6);
         }

         String var7 = var4.getValue("uniqueCount");
         if (var7 != null) {
            this.uniqueCount = Integer.parseInt(var7);
         }

         this.strings = new ArrayList<>(this.uniqueCount);
         this.characters = new StringBuffer();
      } else if ("si".equals(var3)) {
         this.characters.setLength(0);
      } else if ("t".equals(var3)) {
         this.tIsOpen = true;
      } else if ("rPh".equals(var3)) {
         this.isPhonetic = true;
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) throws SAXException {
      int var4 = var3.indexOf(":");
      if (var4 > -1) {
         var3 = var3.substring(var4 + 1, var3.length());
      }

      if ("si".equals(var3)) {
         this.strings.add(this.characters.toString());
      } else if ("t".equals(var3)) {
         this.tIsOpen = false;
      } else if ("rPh".equals(var3)) {
         this.isPhonetic = false;
      }
   }

   @Override
   public void characters(char[] var1, int var2, int var3) throws SAXException {
      if (this.tIsOpen && !this.isPhonetic) {
         this.characters.append(var1, var2, var3);
      }
   }
}
