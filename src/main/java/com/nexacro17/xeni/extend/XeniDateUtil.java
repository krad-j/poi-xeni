package com.nexacro17.xeni.extend;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.poixeni.ss.usermodel.Cell;
import org.apache.poixeni.ss.usermodel.CellStyle;

public class XeniDateUtil {
   public static final int SECONDS_PER_MINUTE = 60;
   public static final int MINUTES_PER_HOUR = 60;
   public static final int HOURS_PER_DAY = 24;
   public static final int SECONDS_PER_DAY = 86400;
   private static final int BAD_DATE = -1;
   public static final long DAY_MILLISECONDS = 86400000L;
   private static final Pattern TIME_SEPARATOR_PATTERN = Pattern.compile("S");
   private static final Pattern date_ptrn1 = Pattern.compile("^\\[\\$\\-.*?\\]");
   private static final Pattern date_ptrn2 = Pattern.compile("^\\[[a-zA-Z]+\\]");
   private static final Pattern date_ptrn3 = Pattern.compile("^[\\[\\]yYmMdDhHsS\\-/,. :\"\\\\]+0*[ampAMP/]*$");
   private static final Pattern date_ptrn4 = Pattern.compile("^\\[([hH]+|[mM]+|[sS]+)\\]");
   private static final Pattern date_ptrn5 = Pattern.compile("^\\[\\$\\-.*?\\]*$");
   private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

   protected XeniDateUtil() {
   }

   public static double getExcelDate(Date var0) {
      return getExcelDate(var0, false);
   }

   public static double getExcelDate(Date var0, boolean var1) {
      GregorianCalendar var2 = new GregorianCalendar();
      var2.setTime(var0);
      return internalGetExcelDate(var2, var1);
   }

   public static double getExcelDate(Calendar var0, boolean var1) {
      return internalGetExcelDate((Calendar)var0.clone(), var1);
   }

   private static double internalGetExcelDate(Calendar var0, boolean var1) {
      if ((var1 || var0.get(1) >= 1900) && (!var1 || var0.get(1) >= 1904)) {
         double var2 = (double)(((var0.get(11) * 60 + var0.get(12)) * 60 + var0.get(13)) * 1000 + var0.get(14)) / 8.64E7;
         Calendar var4 = dayStart(var0);
         double var5 = var2 + (double)absoluteDay(var4, var1);
         if (!var1 && var5 >= 60.0) {
            var5++;
         } else if (var1) {
            var5--;
         }

         return var5;
      } else {
         return -1.0;
      }
   }

   public static Date getJavaDate(double var0, TimeZone var2) {
      return getJavaDate(var0, false, var2);
   }

   public static Date getJavaDate(double var0) {
      return getJavaDate(var0, (TimeZone)null);
   }

   public static Date getJavaDate(double var0, boolean var2, TimeZone var3) {
      return getJavaCalendar(var0, var2, var3).getTime();
   }

   public static Date getJavaDate(double var0, boolean var2) {
      return getJavaCalendar(var0, var2).getTime();
   }

   public static void setCalendar(Calendar var0, int var1, int var2, boolean var3) {
      short var4 = 1900;
      byte var5 = -1;
      if (var3) {
         var4 = 1904;
         var5 = 1;
      } else if (var1 < 61) {
         var5 = 0;
      }

      var0.set(var4, 0, var1 + var5, 0, 0, 0);
      var0.set(14, var2);
   }

   public static Calendar getJavaCalendar(double var0, boolean var2) {
      return getJavaCalendar(var0, var2, (TimeZone)null);
   }

   public static Calendar getJavaCalendarUTC(double var0, boolean var2) {
      return getJavaCalendar(var0, var2, TIMEZONE_UTC);
   }

   public static Calendar getJavaCalendar(double var0, boolean var2, TimeZone var3) {
      if (!isValidExcelDate(var0)) {
         return null;
      } else {
         int var4 = (int)Math.floor(var0);
         int var5 = (int)((var0 - (double)var4) * 8.64E7 + 0.5);
         GregorianCalendar var6;
         if (var3 != null) {
            var6 = new GregorianCalendar(var3);
         } else {
            var6 = new GregorianCalendar();
         }

         setCalendar(var6, var4, var5, var2);
         return var6;
      }
   }

   public static boolean isADateFormat(int var0, String var1) {
      if (isInternalDateFormat(var0)) {
         return true;
      } else if (var1 != null && var1.length() != 0) {
         String var2 = var1;
         StringBuilder var3 = new StringBuilder(var1.length());

         for (int var4 = 0; var4 < var2.length(); var4++) {
            char var5 = var2.charAt(var4);
            if (var4 < var2.length() - 1) {
               char var6 = var2.charAt(var4 + 1);
               if (var5 == '\\') {
                  switch (var6) {
                     case ' ':
                     case ',':
                     case '-':
                     case '.':
                     case '\\':
                        continue;
                  }
               }
            }

            var3.append(var5);
         }

         var2 = var3.toString();
         if (date_ptrn4.matcher(var2).matches()) {
            return true;
         } else {
            if (var2.length() > 1) {
               String var12 = var2.substring(var2.length() - 2);
               if (var12.equals(";@")) {
                  return true;
               }
            }

            if (date_ptrn5.matcher(var2).matches()) {
               var2 = date_ptrn1.matcher(var2).replaceAll("");
               var2 = date_ptrn2.matcher(var2).replaceAll("");
               return true;
            } else {
               var2 = date_ptrn1.matcher(var2).replaceAll("");
               var2 = date_ptrn2.matcher(var2).replaceAll("");
               if (var2.indexOf(59) > 0 && var2.indexOf(59) < var2.length() - 1) {
                  var2 = var2.substring(0, var2.indexOf(59));
               }

               return date_ptrn3.matcher(var2).matches();
            }
         }
      } else {
         return false;
      }
   }

   public static boolean isInternalDateFormat(int var0) {
      switch (var0) {
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 45:
         case 46:
         case 47:
            return true;
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         default:
            return false;
      }
   }

   public static boolean isCellDateFormatted(Cell var0) {
      if (var0 == null) {
         return false;
      } else {
         boolean var1 = false;
         double var2 = var0.getNumericCellValue();
         if (isValidExcelDate(var2)) {
            CellStyle var4 = var0.getCellStyle();
            if (var4 == null) {
               return false;
            }

            short var5 = var4.getDataFormat();
            String var6 = var4.getDataFormatString();
            var1 = isADateFormat(var5, var6);
         }

         return var1;
      }
   }

   public static boolean isCellInternalDateFormatted(Cell var0) {
      if (var0 == null) {
         return false;
      } else {
         boolean var1 = false;
         double var2 = var0.getNumericCellValue();
         if (isValidExcelDate(var2)) {
            CellStyle var4 = var0.getCellStyle();
            short var5 = var4.getDataFormat();
            var1 = isInternalDateFormat(var5);
         }

         return var1;
      }
   }

   public static boolean isValidExcelDate(double var0) {
      return var0 > -Double.MIN_VALUE;
   }

   protected static int absoluteDay(Calendar var0, boolean var1) {
      return var0.get(6) + daysInPriorYears(var0.get(1), var1);
   }

   private static int daysInPriorYears(int var0, boolean var1) {
      if ((var1 || var0 >= 1900) && (!var1 || var0 >= 1900)) {
         int var2 = var0 - 1;
         int var3 = var2 / 4 - var2 / 100 + var2 / 400 - 460;
         return 365 * (var0 - (var1 ? 1904 : 1900)) + var3;
      } else {
         throw new IllegalArgumentException("'year' must be 1900 or greater");
      }
   }

   private static Calendar dayStart(Calendar var0) {
      var0.get(11);
      var0.set(11, 0);
      var0.set(12, 0);
      var0.set(13, 0);
      var0.set(14, 0);
      var0.get(11);
      return var0;
   }

   public static double convertTime(String var0) {
      try {
         return convertTimeInternal(var0);
      } catch (XeniDateUtil.FormatException var3) {
         String var2 = "Bad time format '" + var0 + "' expected 'HH:MM' or 'HH:MM:SS' - " + var3.getMessage();
         throw new IllegalArgumentException(var2);
      }
   }

   private static double convertTimeInternal(String var0) throws XeniDateUtil.FormatException {
      int var1 = var0.length();
      if (var1 >= 4 && var1 <= 8) {
         String[] var2 = TIME_SEPARATOR_PATTERN.split(var0);
         String var3;
         switch (var2.length) {
            case 2:
               var3 = "00";
               break;
            case 3:
               var3 = var2[2];
               break;
            default:
               throw new XeniDateUtil.FormatException("Expected 2 or 3 fields but got (" + var2.length + ")");
         }

         String var4 = var2[0];
         String var5 = var2[1];
         int var6 = parseInt(var4, "hour", 24);
         int var7 = parseInt(var5, "minute", 60);
         int var8 = parseInt(var3, "second", 60);
         double var9 = (double)(var8 + (var7 + var6 * 60) * 60);
         return var9 / 86400.0;
      } else {
         throw new XeniDateUtil.FormatException("Bad length");
      }
   }

   public static Date parseYYYYMMDDDate(String var0) {
      try {
         return parseYYYYMMDDDateInternal(var0);
      } catch (XeniDateUtil.FormatException var3) {
         String var2 = "Bad time format " + var0 + " expected 'YYYY/MM/DD' - " + var3.getMessage();
         throw new IllegalArgumentException(var2);
      }
   }

   private static Date parseYYYYMMDDDateInternal(String var0) throws XeniDateUtil.FormatException {
      if (var0.length() != 10) {
         throw new XeniDateUtil.FormatException("Bad length");
      } else {
         String var1 = var0.substring(0, 4);
         String var2 = var0.substring(5, 7);
         String var3 = var0.substring(8, 10);
         int var4 = parseInt(var1, "year", -32768, 32767);
         int var5 = parseInt(var2, "month", 1, 12);
         int var6 = parseInt(var3, "day", 1, 31);
         GregorianCalendar var7 = new GregorianCalendar(var4, var5 - 1, var6, 0, 0, 0);
         var7.set(14, 0);
         return var7.getTime();
      }
   }

   private static int parseInt(String var0, String var1, int var2) throws XeniDateUtil.FormatException {
      return parseInt(var0, var1, 0, var2 - 1);
   }

   private static int parseInt(String var0, String var1, int var2, int var3) throws XeniDateUtil.FormatException {
      int var4;
      try {
         var4 = Integer.parseInt(var0);
      } catch (NumberFormatException var6) {
         throw new XeniDateUtil.FormatException("Bad int format '" + var0 + "' for " + var1 + " field");
      }

      if (var4 >= var2 && var4 <= var3) {
         return var4;
      } else {
         throw new XeniDateUtil.FormatException(var1 + " value (" + var4 + ") is outside the allowable range(0.." + var3 + ")");
      }
   }

   private static final class FormatException extends Exception {
      public FormatException(String var1) {
         super(var1);
      }
   }
}
