package com.nexacro17.xeni.extend;

import com.nexacro17.xeni.util.XeniProperties;

public class XeniExcelDataStorageFactory {
   private static final String DEFAULT_XENI_EXTEND = "com.nexacro17.xeni.extend.XeniExcelDataStorageDef";
   private static XeniExcelDataStorageBase xeniExt = null;

   private XeniExcelDataStorageFactory() {
   }

   public static XeniExcelDataStorageBase getExtendClass(String var0) {
      return xeniExt;
   }

   static {
      String var0 = XeniProperties.getStringProperty("xeni.exportimport.storage", "com.nexacro17.xeni.extend.XeniExcelDataStorageDef");

      try {
         ClassLoader var1 = Thread.currentThread().getContextClassLoader();
         Class var2 = Class.forName(var0, true, var1);
         xeniExt = (XeniExcelDataStorageBase)var2.newInstance();
      } catch (Throwable var3) {
      }
   }
}
