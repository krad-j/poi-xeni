package com.nexacro17.xeni.extend;

import com.nexacro17.xeni.util.XeniProperties;

public class XeniMultipartProcFactory {
   private static final String DEFAULT_MULTIPART_PROC = "com.nexacro17.xeni.extend.XeniMultipartProcDef";
   private static XeniMultipartProcBase multipartProc = null;

   private XeniMultipartProcFactory() {
   }

   public static XeniMultipartProcBase getMultipartProc(String var0) {
      return multipartProc;
   }

   static {
      String var0 = XeniProperties.getStringProperty("xeni.multipart.proc", "com.nexacro17.xeni.extend.XeniMultipartProcDef");

      try {
         ClassLoader var1 = Thread.currentThread().getContextClassLoader();
         Class var2 = Class.forName(var0, true, var1);
         multipartProc = (XeniMultipartProcBase)var2.newInstance();
      } catch (Throwable var3) {
      }
   }
}
