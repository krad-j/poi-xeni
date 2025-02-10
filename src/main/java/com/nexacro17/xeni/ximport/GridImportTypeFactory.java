package com.nexacro17.xeni.ximport;

import com.nexacro17.xeni.extend.XeniExcelDataStorageBase;
import com.nexacro17.xeni.extend.XeniExcelDataStorageDef;
import com.nexacro17.xeni.extend.XeniExcelDataStorageFactory;
import com.nexacro17.xeni.ximport.impl.GridImportCSV;
import com.nexacro17.xeni.ximport.impl.GridImportExcelHSSFEvent;
import com.nexacro17.xeni.ximport.impl.GridImportExcelXSSFEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.UUID;
import org.apache.poixeni.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.crypt.Decryptor;
import org.apache.poixeni.poifs.crypt.EncryptionInfo;
import org.apache.poixeni.poifs.filesystem.FileMagic;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public class GridImportTypeFactory {
   private GridImportTypeFactory() {
   }

   public static GridImportBase getGridImporter(int var0) {
      if (var0 == 256 || var0 == 272) {
         return new GridImportExcelHSSFEvent();
      } else if (var0 == 288) {
         return new GridImportExcelXSSFEvent();
      } else if (var0 == 1024 || var0 == 1040) {
         return new GridImportExcelXSSFEvent();
      } else {
         return var0 == 1280 ? new GridImportCSV() : null;
      }
   }

   public static GridImportBase getGridImporter(int var0, String var1, String var2, String var3) throws Exception {
      InputStream var4 = null;
      String var5 = var2;
      if (var2.startsWith("http")) {
         int var6 = var2.lastIndexOf(47);
         String var7 = var2.substring(0, var6 + 1);
         String var8 = var2.substring(var6 + 1);
         URL var9 = new URL(var7 + URLEncoder.encode(var8, "UTF-8").replaceAll("\\+", "%20"));
         var4 = new BufferedInputStream(var9.openStream());
         var5 = var1 + "/" + UUID.randomUUID().toString() + "/" + URLEncoder.encode(var8, "UTF-8").replaceAll("\\+", "%20");
      } else {
         if (var2.startsWith("/")) {
            var5 = var1 + var2;
         }

         XeniExcelDataStorageBase var18 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
         if (!(var18 instanceof XeniExcelDataStorageDef)) {
            var4 = var18.loadTargetStream(var5);
            if (!new File(var5).exists() && var4 != null) {
               var5 = var1 + "/" + UUID.randomUUID().toString() + "/importfile";
            }
         }
      }

      if (var4 != null) {
         if (!var4.markSupported()) {
            var4 = new BufferedInputStream((InputStream)var4, 8);
         }

         var5 = var5 + ".xeni";
         saveImportStream((InputStream)var4, var5);
         var4.close();
         var4 = null;
      }

      Object var19 = null;

      Object var11;
      try {
         File var20 = new File(var5);
         if (FileMagic.valueOf(var20) != FileMagic.OLE2) {
            if (FileMagic.valueOf(var20) == FileMagic.OOXML) {
               var19 = new GridImportExcelXSSFEvent();
               OPCPackage var22 = OPCPackage.open(var20);
               ((GridImportBase)var19).setOPCPackage(var22);
            } else if (var0 == 1280 || var0 == 1296) {
               var19 = new GridImportCSV();
               ((GridImportBase)var19).setInputStream(new FileInputStream(var20));
               return (GridImportBase)var19;
            }

            return (GridImportBase)var19;
         }

         POIFSFileSystem var21 = new POIFSFileSystem(var20);
         if (!var21.getRoot().hasEntry("EncryptionInfo")) {
            var19 = new GridImportExcelHSSFEvent();
            if (var3 != null && var3.length() > 0) {
               Biff8EncryptionKey.setCurrentUserPassword(var3);
            }

            ((GridImportBase)var19).setPOIFileSystem(var21);
            return (GridImportBase)var19;
         }

         var19 = new GridImportExcelXSSFEvent();
         if (var3 == null || var3.length() == 0) {
            ((GridImportBase)var19).setErrorCode(-2021);
            ((GridImportBase)var19).setErrorMessage("Unable to process: Required password");
            var21.close();
            return (GridImportBase)var19;
         }

         EncryptionInfo var23 = new EncryptionInfo(var21);
         Decryptor var10 = Decryptor.getInstance(var23);
         if (var10.verifyPassword(var3)) {
            ((GridImportBase)var19).setPOIFileSystem(var21);
            InputStream var25 = var10.getDataStream(var21);
            ((GridImportBase)var19).setInputStream(var25);
            OPCPackage var12 = OPCPackage.open(var25);
            ((GridImportBase)var19).setOPCPackage(var12);
            return (GridImportBase)var19;
         }

         ((GridImportBase)var19).setErrorCode(-2021);
         ((GridImportBase)var19).setErrorMessage("Unable to process: Document is encrypted");
         var11 = var19;
      } catch (GeneralSecurityException var16) {
         if (var19 != null) {
            ((GridImportBase)var19).setErrorCode(-2021);
            ((GridImportBase)var19).setErrorMessage("Unable to process: Document is encrypted");
         }

         return (GridImportBase)var19;
      } finally {
         if (var4 != null) {
            var4.close();
         }
      }

      return (GridImportBase)var11;
   }

   private static void saveImportStream(InputStream var0, String var1) throws IOException {
      if (var0 != null) {
         int var2 = var1.lastIndexOf("/");
         File var3 = new File(var1.substring(0, var2));
         if (!var3.exists()) {
            var3.mkdirs();
         }

         byte[] var5 = new byte[8192];
         FileOutputStream var6 = new FileOutputStream(new File(var1));

         int var4;
         while ((var4 = var0.read(var5)) != -1) {
            var6.write(var5, 0, var4);
         }

         var6.close();
      }
   }
}
