package com.nexacro17.xeni.extend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class XeniBuiltinFormats {
   public static final int FIRST_USER_DEFINED_FORMAT_INDEX = 164;
   private static final String[] _formats;

   private static void putFormat(List<String> var0, int var1, String var2) {
      if (var0.size() != var1) {
         throw new IllegalStateException("index " + var1 + " is wrong");
      } else {
         var0.add(var2);
      }
   }

   /** @deprecated */
   public static Map<Integer, String> getBuiltinFormats() {
      LinkedHashMap var0 = new LinkedHashMap();

      for (int var1 = 0; var1 < _formats.length; var1++) {
         var0.put(var1, _formats[var1]);
      }

      return var0;
   }

   public static String[] getAll() {
      return (String[])_formats.clone();
   }

   public static String getBuiltinFormat(int var0, String var1) {
      if (var0 < 0 || var0 >= _formats.length) {
         return null;
      } else {
         return var0 == 14 ? getNumberFmtDateString("ko") : _formats[var0];
      }
   }

   public static int getBuiltinFormat(String var0) {
      String var1;
      if (var0.equalsIgnoreCase("TEXT")) {
         var1 = "@";
      } else {
         var1 = var0;
      }

      for (int var2 = 0; var2 < _formats.length; var2++) {
         if (var1.equals(_formats[var2])) {
            return var2;
         }
      }

      return -1;
   }

   public static String getNumberFmtDateString(String var0) {
      String var1 = "";
      if (var0.equals("ko")) {
         var1 = "yyyy-mm-dd";
      }

      return var1;
   }

   static {
      ArrayList var0 = new ArrayList();
      putFormat(var0, 0, "General");
      putFormat(var0, 1, "0");
      putFormat(var0, 2, "0.00");
      putFormat(var0, 3, "#,##0");
      putFormat(var0, 4, "#,##0.00");
      putFormat(var0, 5, "\"$\"#,##0_);(\"$\"#,##0)");
      putFormat(var0, 6, "\"$\"#,##0_);[Red](\"$\"#,##0)");
      putFormat(var0, 7, "\"$\"#,##0.00_);(\"$\"#,##0.00)");
      putFormat(var0, 8, "\"$\"#,##0.00_);[Red](\"$\"#,##0.00)");
      putFormat(var0, 9, "0%");
      putFormat(var0, 10, "0.00%");
      putFormat(var0, 11, "0.00E+00");
      putFormat(var0, 12, "# ?/?");
      putFormat(var0, 13, "# ??/??");
      putFormat(var0, 14, "m-d-yy");
      putFormat(var0, 15, "d-mmm-yy");
      putFormat(var0, 16, "d-mmm");
      putFormat(var0, 17, "mmm-yy");
      putFormat(var0, 18, "h:mm AM/PM");
      putFormat(var0, 19, "h:mm:ss AM/PM");
      putFormat(var0, 20, "h:mm");
      putFormat(var0, 21, "h:mm:ss");
      putFormat(var0, 22, "m/d/yy h:mm");

      for (int var1 = 23; var1 <= 36; var1++) {
         putFormat(var0, var1, "reserved-0x" + Integer.toHexString(var1));
      }

      putFormat(var0, 37, "#,##0_);(#,##0)");
      putFormat(var0, 38, "#,##0_);[Red](#,##0)");
      putFormat(var0, 39, "#,##0.00_);(#,##0.00)");
      putFormat(var0, 40, "#,##0.00_);[Red](#,##0.00)");
      putFormat(var0, 41, "_(* #,##0_);_(* (#,##0);_(* \"-\"_);_(@_)");
      putFormat(var0, 42, "_(\"$\"* #,##0_);_(\"$\"* (#,##0);_(\"$\"* \"-\"_);_(@_)");
      putFormat(var0, 43, "_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)");
      putFormat(var0, 44, "_(\"$\"* #,##0.00_);_(\"$\"* (#,##0.00);_(\"$\"* \"-\"??_);_(@_)");
      putFormat(var0, 45, "mm:ss");
      putFormat(var0, 46, "[h]:mm:ss");
      putFormat(var0, 47, "mm:ss.0");
      putFormat(var0, 48, "##0.0E+0");
      putFormat(var0, 49, "@");
      String[] var2 = new String[var0.size()];
      var0.toArray(var2);
      _formats = var2;
   }
}
