package com.nexacro17.xeni.extend;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.HttpPlatformResponse;
import com.nexacro17.xeni.util.CommUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.crypt.EncryptionInfo;
import org.apache.poixeni.poifs.crypt.EncryptionMode;
import org.apache.poixeni.poifs.crypt.Encryptor;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public class XeniExcelDataStorageDef implements XeniExcelDataStorageBase {
   @Override
   public InputStream loadTargetStream(String var1) throws Exception {
      File var2 = new File(var1);
      return new FileInputStream(var2);
   }

   @Override
   public String saveImportStream(VariableList var1, InputStream var2, String var3) throws Exception {
      int var4 = var3.lastIndexOf("/");
      String var5 = var3.substring(0, var4);
      File var6 = new File(var5);
      if (!var6.exists()) {
         var6.mkdirs();
      }

      FileOutputStream var7 = new FileOutputStream(var3);
      byte[] var8 = new byte[1024];
      int var9 = 0;

      while ((var9 = var2.read(var8)) > 0) {
         var7.write(var8, 0, var9);
      }

      var7.flush();
      var7.close();
      var2.close();
      return null;
   }

   @Override
   public int saveExportStream(VariableList var1, DataSet var2, ByteArrayOutputStream var3, String var4, String var5, HttpServletResponse var6) throws Exception {
      int var7 = var4.lastIndexOf("/");
      String var8 = var4.substring(0, var7);
      File var9 = new File(var8);
      if (!var9.exists()) {
         var9.mkdirs();
      }

      FileOutputStream var10 = new FileOutputStream(var4);
      String var11 = var2.getString(0, "password");
      if (var11 != null && var11.length() > 0) {
         int var12 = var2.getInt(0, "type");
         if (var12 != 288 && var12 != 1040) {
            var10.write(var3.toByteArray());
         } else {
            ByteArrayInputStream var13 = new ByteArrayInputStream(var3.toByteArray());
            OPCPackage var14 = OPCPackage.open(var13);
            POIFSFileSystem var15 = new POIFSFileSystem();
            EncryptionInfo var16 = new EncryptionInfo(EncryptionMode.agile);
            Encryptor var17 = var16.getEncryptor();
            var17.confirmPassword(var11);
            OutputStream var18 = var17.getDataStream(var15);
            var14.save(var18);
            var14.close();
            var18.close();
            var15.writeFilesystem(var10);
            var10.close();
         }
      } else {
         var10.write(var3.toByteArray());
      }

      var10.close();
      var3.close();
      DataSet var19 = CommUtil.getDatasetExportResponse(var2);
      PlatformData var20 = new PlatformData();
      VariableList var21 = var20.getVariableList();
      var21.add("ErrorCode", 0);
      var21.add("ErrorMsg", "SUCCESS");
      var19.set(0, "url", var5);
      var20.addDataSet(var19);
      HttpPlatformResponse var22 = new HttpPlatformResponse(var6, "PlatformSsv", "UTF-8");
      var22.setData(var20);
      var22.sendData();
      return 0;
   }

   @Override
   public DataSet saveExportStream(VariableList var1, DataSet var2, ByteArrayOutputStream var3, String var4, String var5) throws Exception {
      int var6 = var4.lastIndexOf("/");
      String var7 = var4.substring(0, var6);
      File var8 = new File(var7);
      if (!var8.exists()) {
         var8.mkdirs();
      }

      FileOutputStream var9 = new FileOutputStream(var4);
      var9.write(var3.toByteArray());
      var9.close();
      var3.close();
      DataSet var10 = CommUtil.getDatasetExportResponse(var2);
      var10.set(0, "url", var5);
      return var10;
   }
}
