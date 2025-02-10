package com.nexacro17.xeni.extend;

import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.tx.impl.PlatformXmlDataDeserializer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XeniMultipartProcDef implements XeniMultipartProcBase {
   private static final Log logger = LogFactory.getLog(XeniMultipartProcDef.class);

   @Override
   public XeniMultipartReqData getImportData(HttpServletRequest var1) throws Exception {
      ServletFileUpload var2 = new ServletFileUpload();
      XeniMultipartReqData var3 = new XeniMultipartReqData();
      var2.setHeaderEncoding("UTF-8");
      FileItemIterator var4 = var2.getItemIterator(var1);
      String var5 = null;

      while (var4.hasNext()) {
         FileItemStream var6 = var4.next();
         if (var6.isFormField()) {
            PlatformXmlDataDeserializer var7 = new PlatformXmlDataDeserializer();
            PlatformData var8 = var7.readData(var6.openStream(), null, "UTF-8");
            var3.setPlatformData(var8);
         } else {
            String var10 = var6.getName();
            var5 = var10.replaceAll("\\\\", "/");
            int var11 = var5.lastIndexOf(47);
            if (var11 >= 0) {
               var5 = var5.substring(var11 + 1);
            }

            var3.setFileName(var5);
            InputStream var12 = var6.openStream();
            ByteArrayInputStream var9 = new ByteArrayInputStream(IOUtils.toByteArray(var12));
            var3.setFileStream(var9);
            var12.close();
         }

         if (logger.isDebugEnabled()) {
            logger.debug("File field " + var6.getFieldName() + " with file name " + var5 + " detected.");
         }
      }

      return var3;
   }

   private boolean IsValidName(String var1) {
      boolean var2 = false;
      String var3 = "";
      int var4 = var1.lastIndexOf(46);
      if (var4 > 0) {
         var3 = var1.substring(var4 + 1);
      }

      if (var3.length() > 0
         && (
            "xls".equalsIgnoreCase(var3)
               || "xlsx".equalsIgnoreCase(var3)
               || "cell".equalsIgnoreCase(var3)
               || "csv".equalsIgnoreCase(var3)
               || "xlsm".equalsIgnoreCase(var3)
               || "pia".equalsIgnoreCase(var3)
         )) {
         var2 = true;
      }

      return var2;
   }
}
