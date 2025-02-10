package com.nexacro17.xeni.extend;

import com.nexacro17.xeni.util.XeniProperties;

public class XeniDataValidationFactory {
   private static XeniDataValidationBase dataValidator = null;

   private XeniDataValidationFactory() {
   }

   public static XeniDataValidationBase getDataValidator() {
      return dataValidator;
   }

   static {
      try {
         String var0 = XeniProperties.getStringProperty("xeni.data.validation");
         if (var0 != null && var0.length() > 0) {
            ClassLoader var1 = Thread.currentThread().getContextClassLoader();
            Class var2 = Class.forName(var0, true, var1);
            dataValidator = (XeniDataValidationBase)var2.newInstance();
         }
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }
}
