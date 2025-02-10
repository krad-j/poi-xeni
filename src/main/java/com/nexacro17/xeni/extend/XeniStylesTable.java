package com.nexacro17.xeni.extend;

import com.nexacro17.xeni.ximport.POIEventModelException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.openxml4j.opc.PackagePart;
import org.apache.poixeni.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeansxeni.XmlException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XeniStylesTable extends DefaultHandler {
   private boolean bHancell = false;
   private Boolean bIsCTXF = false;
   private final List<XeniCTXf> styleXfs = new ArrayList<>();
   private final List<XeniCTXf> xfs = new ArrayList<>();
   private XeniCTXf ctXf = null;
   private XeniCTXf styleCtXf = null;

   public XeniStylesTable() {
   }

   public XeniStylesTable(OPCPackage var1) throws IOException, SAXException, XmlException {
      ArrayList var2 = var1.getPartsByContentType(XSSFRelation.STYLES.getContentType());
      if (var2.size() > 0) {
         this.bHancell = true;
         PackagePart var3 = (PackagePart)var2.get(0);
         this.readFrom(var3.getInputStream());
      }
   }

   public void readFrom(OPCPackage var1) throws IOException, SAXException {
      ArrayList var2 = var1.getPartsByContentType(XSSFRelation.STYLES.getContentType());
      if (var2.size() > 0) {
         this.bHancell = true;
         PackagePart var3 = (PackagePart)var2.get(0);
         this.readFrom(var3.getInputStream());
      }
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

   public List<XeniCTXf> getArrCtxf() {
      return this.xfs;
   }

   public short getDataFormat(int var1) {
      return this.xfs.get(var1).getNumFmtId();
   }

   public String getDataFormatString(Short var1) {
      String var2 = null;

      for (XeniCTXf var5 : this.styleXfs) {
         if (var5.getNumFmtId() == var1) {
            var2 = var5.getFormatCode();
            break;
         }
      }

      return var2;
   }

   @Override
   public void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
      if (var3.equals("x:numFmt") || var3.equals("numFmt")) {
         this.styleCtXf = new XeniCTXf();
         this.styleCtXf.setNumFmtId(Short.parseShort(var4.getValue("numFmtId")));
         this.styleCtXf.setFormatCode(var4.getValue("formatCode"));
      } else if (var3.equals("x:cellXfs") || var3.equals("cellXfs")) {
         this.bIsCTXF = true;
      } else if (var3.equals("mc:Choice") || var3.equals("Choice")) {
         this.bIsCTXF = false;
      } else if (!var3.equals("mc:Fallback") && !var3.equals("Fallback")) {
         if (!this.bIsCTXF || !var3.equals("x:xf") && !var3.equals("xf")) {
            if (var3.equals("styleSheet") && var4.getIndex("xmlns:x") < 0) {
               this.bHancell = false;
               throw new SAXException(new POIEventModelException("Stop parsing the document."));
            }
         } else {
            this.ctXf = new XeniCTXf();
            this.ctXf.setNumFmtId(Short.parseShort(var4.getValue("numFmtId")));
         }
      } else {
         this.bIsCTXF = true;
      }
   }

   @Override
   public void endElement(String var1, String var2, String var3) throws SAXException {
      if (var3.equals("x:cellXfs") || var3.equals("cellXfs")) {
         this.bIsCTXF = false;
      } else if (var3.equals("x:numFmt") || var3.equals("numFmt")) {
         this.styleXfs.add(this.styleCtXf);
      } else if (this.bIsCTXF && (var3.equals("x:xf") || var3.equals("xf"))) {
         this.xfs.add(this.ctXf);
      }
   }

   public boolean isHancellStyle() {
      return this.bHancell;
   }
}
