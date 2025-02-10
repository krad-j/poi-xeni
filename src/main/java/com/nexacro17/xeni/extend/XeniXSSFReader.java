package com.nexacro17.xeni.extend;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poixeni.ooxml.POIXMLException;
import org.apache.poixeni.openxml4j.exceptions.InvalidFormatException;
import org.apache.poixeni.openxml4j.exceptions.OpenXML4JException;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.openxml4j.opc.PackagePart;
import org.apache.poixeni.openxml4j.opc.PackagePartName;
import org.apache.poixeni.openxml4j.opc.PackageRelationship;
import org.apache.poixeni.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poixeni.openxml4j.opc.PackagingURIHelper;
import org.apache.poixeni.xssf.eventusermodel.XSSFReader;
import org.apache.poixeni.xssf.model.CommentsTable;
import org.apache.poixeni.xssf.model.SharedStringsTable;
import org.apache.poixeni.xssf.model.StylesTable;
import org.apache.poixeni.xssf.model.ThemesTable;
import org.apache.poixeni.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeansxeni.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument.Factory;

public class XeniXSSFReader extends XSSFReader {
   private OPCPackage pkg;
   private PackagePart workbookPart;

   public XeniXSSFReader(OPCPackage var1) throws IOException, OpenXML4JException {
      super(var1);
      this.pkg = var1;
      PackageRelationship var2 = this.pkg
         .getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument")
         .getRelationship(0);
      this.workbookPart = this.pkg.getPart(var2);
   }

   public SharedStringsTable getSharedStringsTable() throws IOException, InvalidFormatException {
      ArrayList var1 = this.pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
      return var1.size() == 0 ? null : new SharedStringsTable((PackagePart)var1.get(0));
   }

   public StylesTable getStylesTable() throws IOException, InvalidFormatException {
      ArrayList var1 = this.pkg.getPartsByContentType(XSSFRelation.STYLES.getContentType());
      if (var1.size() == 0) {
         return null;
      } else {
         StylesTable var2 = new StylesTable((PackagePart)var1.get(0));
         var1 = this.pkg.getPartsByContentType(XSSFRelation.THEME.getContentType());
         if (var1.size() != 0) {
            var2.setTheme(new ThemesTable((PackagePart)var1.get(0)));
         }

         return var2;
      }
   }

   public InputStream getSharedStringsData() throws IOException, InvalidFormatException {
      return XSSFRelation.SHARED_STRINGS.getContents(this.workbookPart);
   }

   public InputStream getStylesData() throws IOException, InvalidFormatException {
      return XSSFRelation.STYLES.getContents(this.workbookPart);
   }

   public InputStream getThemesData() throws IOException, InvalidFormatException {
      return XSSFRelation.THEME.getContents(this.workbookPart);
   }

   public InputStream getWorkbookData() throws IOException, InvalidFormatException {
      return this.workbookPart.getInputStream();
   }

   public InputStream getSheet(String var1) throws IOException, InvalidFormatException {
      PackageRelationship var2 = this.workbookPart.getRelationship(var1);
      if (var2 == null) {
         throw new IllegalArgumentException("No Sheet found with r:id " + var1);
      } else {
         PackagePartName var3 = PackagingURIHelper.createPartName(var2.getTargetURI());
         PackagePart var4 = this.pkg.getPart(var3);
         if (var4 == null) {
            throw new IllegalArgumentException("No data found for Sheet with r:id " + var1);
         } else {
            return var4.getInputStream();
         }
      }
   }

   public Iterator<InputStream> getSheetsData() throws IOException, InvalidFormatException {
      return new XeniXSSFReader.SheetIterator(this.workbookPart);
   }

   public static class SheetIterator implements Iterator<InputStream> {
      private Map<String, PackagePart> sheetMap;
      private CTSheet ctSheet;
      private Iterator<CTSheet> sheetIterator;

      private SheetIterator(PackagePart var1) throws IOException {
         try {
            this.sheetMap = new HashMap<>();

            for (PackageRelationship var3 : var1.getRelationships()) {
               if (var3.getRelationshipType().equals(XSSFRelation.WORKSHEET.getRelation())
                  || var3.getRelationshipType().equals(XSSFRelation.CHARTSHEET.getRelation())) {
                  PackagePartName var4 = PackagingURIHelper.createPartName(var3.getTargetURI());
                  this.sheetMap.put(var3.getId(), var1.getPackage().getPart(var4));
               }
            }

            CTWorkbook var7 = Factory.parse(var1.getInputStream()).getWorkbook();
            this.sheetIterator = var7.getSheets().getSheetList().iterator();
         } catch (InvalidFormatException var5) {
            throw new POIXMLException(var5);
         } catch (XmlException var6) {
            throw new POIXMLException(var6);
         }
      }

      @Override
      public boolean hasNext() {
         return this.sheetIterator.hasNext();
      }

      public InputStream next() {
         this.ctSheet = this.sheetIterator.next();
         String var1 = this.ctSheet.getId();

         try {
            PackagePart var2 = this.sheetMap.get(var1);
            return var2.getInputStream();
         } catch (IOException var3) {
            throw new POIXMLException(var3);
         }
      }

      public String getSheetName() {
         return this.ctSheet.getName();
      }

      public CommentsTable getSheetComments() {
         PackagePart var1 = this.getSheetPart();

         try {
            PackageRelationshipCollection var2 = var1.getRelationshipsByType(XSSFRelation.SHEET_COMMENTS.getRelation());
            if (var2.size() > 0) {
               PackageRelationship var3 = var2.getRelationship(0);
               PackagePartName var4 = PackagingURIHelper.createPartName(var3.getTargetURI());
               PackagePart var5 = var1.getPackage().getPart(var4);
               return new CommentsTable(var5);
            } else {
               return null;
            }
         } catch (InvalidFormatException var6) {
            return null;
         } catch (IOException var7) {
            return null;
         }
      }

      public PackagePart getSheetPart() {
         String var1 = this.ctSheet.getId();
         return this.sheetMap.get(var1);
      }

      @Override
      public void remove() {
         throw new IllegalStateException("Not supported");
      }
   }
}
