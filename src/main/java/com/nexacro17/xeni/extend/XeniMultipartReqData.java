package com.nexacro17.xeni.extend;

import com.nexacro17.xapi.data.PlatformData;
import java.io.InputStream;

public class XeniMultipartReqData {
   private String sFileName = null;
   private PlatformData platformData = null;
   private InputStream fileStream = null;

   public String getFileName() {
      return this.sFileName;
   }

   public PlatformData getPlatformData() {
      return this.platformData;
   }

   public InputStream getFileStream() {
      return this.fileStream;
   }

   public void setFileName(String var1) {
      this.sFileName = var1;
   }

   public void setPlatformData(PlatformData var1) {
      this.platformData = var1;
   }

   public void setFileStream(InputStream var1) {
      this.fileStream = var1;
   }
}
