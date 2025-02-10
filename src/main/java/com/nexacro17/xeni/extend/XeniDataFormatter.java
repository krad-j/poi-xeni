package com.nexacro17.xeni.extend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poixeni.ss.formula.eval.NotImplementedException;
import org.apache.poixeni.ss.usermodel.Cell;
import org.apache.poixeni.ss.usermodel.CellType;
import org.apache.poixeni.ss.usermodel.ExcelStyleDateFormatter;
import org.apache.poixeni.ss.usermodel.FormulaEvaluator;

public class XeniDataFormatter {
   private static final Pattern numPattern = Pattern.compile("[0#]+");
   private static final Pattern daysAsText = Pattern.compile("([d]{3,})", 2);
   private static final Pattern amPmPattern = Pattern.compile("((A|P)[M/P]*)", 2);
   private static final Pattern usMonthPattern = Pattern.compile("([M]{3,})");
   private static final Pattern localePatternGroup = Pattern.compile("(\\[\\$[^-\\]]*-[0-9A-Z]+\\])");
   private static final Pattern colorPattern = Pattern.compile(
      "(\\[BLACK\\])|(\\[BLUE\\])|(\\[CYAN\\])|(\\[GREEN\\])|(\\[MAGENTA\\])|(\\[RED\\])|(\\[WHITE\\])|(\\[YELLOW\\])|(\\[COLOR\\s*\\d\\])|(\\[COLOR\\s*[0-5]\\d\\])",
      2
   );
   private static final String invalidDateTimeString;
   private final DecimalFormatSymbols decimalSymbols;
   private final DateFormatSymbols dateSymbols;
   private final DateFormatSymbols usMonthSymbols;
   private final Format generalWholeNumFormat;
   private final Format generalDecimalNumFormat;
   private final Format xeniDecimalNumFormat;
   private Format defaultNumFormat;
   private final Map<String, Format> formats;
   private boolean emulateCsv = false;

   public XeniDataFormatter() {
      this(false);
   }

   public XeniDataFormatter(boolean var1) {
      this(Locale.getDefault());
      this.emulateCsv = var1;
   }

   public XeniDataFormatter(Locale var1, boolean var2) {
      this(var1);
      this.emulateCsv = var2;
   }

   public XeniDataFormatter(Locale var1) {
      this.dateSymbols = new DateFormatSymbols(var1);
      this.usMonthSymbols = new DateFormatSymbols(Locale.ENGLISH);
      this.decimalSymbols = new DecimalFormatSymbols(var1);
      this.generalWholeNumFormat = new DecimalFormat("#", this.decimalSymbols);
      this.generalDecimalNumFormat = new DecimalFormat("#.##########", this.decimalSymbols);
      this.xeniDecimalNumFormat = new DecimalFormat("#.######################", this.decimalSymbols);
      this.formats = new HashMap<>();
      Format var2 = XeniDataFormatter.ZipPlusFourFormat.instance;
      this.addFormat("00000\\-0000", var2);
      this.addFormat("00000-0000", var2);
      Format var3 = XeniDataFormatter.PhoneFormat.instance;
      this.addFormat("[<=9999999]###\\-####;\\(###\\)\\ ###\\-####", var3);
      this.addFormat("[<=9999999]###-####;(###) ###-####", var3);
      this.addFormat("###\\-####;\\(###\\)\\ ###\\-####", var3);
      this.addFormat("###-####;(###) ###-####", var3);
      Format var4 = XeniDataFormatter.SSNFormat.instance;
      this.addFormat("000\\-00\\-0000", var4);
      this.addFormat("000-00-0000", var4);
   }

   private Format getFormat(Cell var1) {
      if (var1.getCellStyle() == null) {
         return null;
      } else {
         short var2 = var1.getCellStyle().getDataFormat();
         String var3 = var1.getCellStyle().getDataFormatString();
         return var3 != null && var3.trim().length() != 0 ? this.getFormat(var1.getNumericCellValue(), var2, var3) : null;
      }
   }

   private Format getFormat(double var1, int var3, String var4) {
      String var5 = var4;
      int var6 = var4.indexOf(59);
      int var7 = var4.lastIndexOf(59);
      if (var6 != -1 && var6 != var7) {
         int var8 = var4.indexOf(59, var6 + 1);
         if (var8 == var7) {
            if (var1 == 0.0) {
               var5 = var4.substring(var7 + 1);
            } else {
               var5 = var4.substring(0, var7);
            }
         } else if (var1 == 0.0) {
            var5 = var4.substring(var8 + 1, var7);
         } else {
            var5 = var4.substring(0, var8);
         }
      }

      if (this.emulateCsv && var1 == 0.0 && var5.contains("#") && !var5.contains("0")) {
         var5 = var5.replaceAll("#", "");
      }

      Format var9 = this.formats.get(var5);
      if (var9 != null) {
         return var9;
      } else if (!"General".equalsIgnoreCase(var5) && !"@".equals(var5)) {
         var9 = this.createFormat(var1, var3, var5);
         this.formats.put(var5, var9);
         return var9;
      } else {
         return isWholeNumber(var1) ? this.generalWholeNumFormat : this.generalDecimalNumFormat;
      }
   }

   public Format createFormat(Cell var1) {
      short var2 = var1.getCellStyle().getDataFormat();
      String var3 = var1.getCellStyle().getDataFormatString();
      return this.createFormat(var1.getNumericCellValue(), var2, var3);
   }

   private Format createFormat(double var1, int var3, String var4) {
      String var5 = var4;
      Matcher var6 = colorPattern.matcher(var4);

      while (var6.find()) {
         String var7 = var6.group();
         int var8 = var5.indexOf(var7);
         if (var8 == -1) {
            break;
         }

         String var9 = var5.substring(0, var8) + var5.substring(var8 + var7.length());
         if (var9.equals(var5)) {
            break;
         }

         var5 = var9;
         var6 = colorPattern.matcher(var9);
      }

      for (Matcher var15 = localePatternGroup.matcher(var5); var15.find(); var15 = localePatternGroup.matcher(var5)) {
         String var16 = var15.group();
         String var18 = var16.substring(var16.indexOf(36) + 1, var16.indexOf(45));
         if (var18.indexOf(36) > -1) {
            StringBuffer var10 = new StringBuffer();
            var10.append(var18.substring(0, var18.indexOf(36)));
            var10.append('\\');
            var10.append(var18.substring(var18.indexOf(36), var18.length()));
            var18 = var10.toString();
         }

         var5 = var15.replaceAll(var18);
      }

      if (var5 == null || var5.trim().length() == 0) {
         return this.getDefaultFormat(var1);
      } else if (!"General".equalsIgnoreCase(var5) && !"@".equals(var5)) {
         if (XeniDateUtil.isADateFormat(var3, var5) && XeniDateUtil.isValidExcelDate(var1)) {
            return this.createDateFormat(var5, var1);
         } else {
            if (var5.indexOf("#/#") >= 0 || var5.indexOf("?/?") >= 0) {
               String var17 = var5.replaceAll("\\\\ ", " ").replaceAll("\\\\.", "").replaceAll("\"[^\"]*\"", " ");
               boolean var19 = true;

               for (String var13 : var17.split(";")) {
                  int var14 = this.indexOfFraction(var13);
                  if (var14 == -1 || var14 != this.lastIndexOfFraction(var13)) {
                     var19 = false;
                     break;
                  }
               }

               if (var19) {
                  return new XeniDataFormatter.FractionFormat(var17);
               }
            }

            if (numPattern.matcher(var5).find()) {
               return this.createNumberFormat(var5, var1);
            } else {
               return this.emulateCsv ? new XeniDataFormatter.ConstantStringFormat(this.cleanFormatForNumber(var5)) : null;
            }
         }
      } else {
         return isWholeNumber(var1) ? this.generalWholeNumFormat : this.generalDecimalNumFormat;
      }
   }

   private int indexOfFraction(String var1) {
      int var2 = var1.indexOf("#/#");
      int var3 = var1.indexOf("?/?");
      return var2 == -1 ? var3 : (var3 == -1 ? var2 : Math.min(var2, var3));
   }

   private int lastIndexOfFraction(String var1) {
      int var2 = var1.lastIndexOf("#/#");
      int var3 = var1.lastIndexOf("?/?");
      return var2 == -1 ? var3 : (var3 == -1 ? var2 : Math.max(var2, var3));
   }

   private Format createDateFormat(String var1, double var2) {
      String var4 = var1.replaceAll("\\\\-", "-");
      var4 = var4.replaceAll("\\\\,", ",");
      var4 = var4.replaceAll("\\\\\\.", ".");
      var4 = var4.replaceAll("\\\\ ", " ");
      var4 = var4.replaceAll("\\\\/", "/");
      var4 = var4.replaceAll(";@", "");
      var4 = var4.replaceAll("\"/\"", "/");
      boolean var5 = false;

      for (Matcher var6 = amPmPattern.matcher(var4); var6.find(); var6 = amPmPattern.matcher(var4)) {
         var4 = var6.replaceAll("@");
         var5 = true;
      }

      var4 = var4.replaceAll("@", "a");
      Matcher var7 = daysAsText.matcher(var4);
      if (var7.find()) {
         String var8 = var7.group(0);
         var4 = var7.replaceAll(var8.toUpperCase().replaceAll("D", "E"));
      }

      StringBuffer var26 = new StringBuffer();
      char[] var9 = var4.toCharArray();
      boolean var10 = true;
      ArrayList var11 = new ArrayList();
      boolean var12 = false;

      for (int var13 = 0; var13 < var9.length; var13++) {
         char var14 = var9[var13];
         if (var14 == '[' && !var12) {
            var12 = true;
            var10 = false;
            var26.append(var14);
         } else if (var14 == ']' && var12) {
            var12 = false;
            var26.append(var14);
         } else if (var12) {
            if (var14 == 'h' || var14 == 'H') {
               var26.append('H');
            } else if (var14 == 'm' || var14 == 'M') {
               var26.append('m');
            } else if (var14 != 's' && var14 != 'S') {
               var26.append(var14);
            } else {
               var26.append('s');
            }
         } else if (var14 == 'h' || var14 == 'H') {
            var10 = false;
            if (var5) {
               var26.append('h');
            } else {
               var26.append('H');
            }
         } else if (var14 != 'm' && var14 != 'M') {
            if (var14 == 's' || var14 == 'S') {
               var26.append('s');

               for (int var15 = 0; var15 < var11.size(); var15++) {
                  int var16 = (Integer)var11.get(var15);
                  if (var26.charAt(var16) == 'M') {
                     var26.replace(var16, var16 + 1, "m");
                  }
               }

               var10 = true;
               var11.clear();
            } else if (Character.isLetter(var14)) {
               var10 = true;
               var11.clear();
               if (var14 == 'y' || var14 == 'Y') {
                  var26.append('y');
               } else if (var14 != 'd' && var14 != 'D') {
                  var26.append(var14);
               } else {
                  var26.append('d');
               }
            } else {
               var26.append(var14);
            }
         } else if (var10) {
            var26.append('M');
            var11.add(var26.length() - 1);
         } else {
            var26.append('m');
         }
      }

      var4 = var26.toString();

      try {
         Matcher var27 = usMonthPattern.matcher(var4);
         if (var27.find()) {
            ExcelStyleDateFormatter var28 = new ExcelStyleDateFormatter(var4, Locale.ENGLISH);
            var28.setDateFormatSymbols(this.usMonthSymbols);
            return var28;
         } else {
            return new ExcelStyleDateFormatter(var4, this.dateSymbols);
         }
      } catch (IllegalArgumentException var17) {
         return this.getDefaultFormat(var2);
      }
   }

   private String cleanFormatForNumber(String var1) {
      StringBuffer var2 = new StringBuffer(var1);
      if (this.emulateCsv) {
         for (int var3 = 0; var3 < var2.length(); var3++) {
            char var4 = var2.charAt(var3);
            if ((var4 == '_' || var4 == '*' || var4 == '?') && (var3 <= 0 || var2.charAt(var3 - 1) != '\\')) {
               if (var4 == '?') {
                  var2.setCharAt(var3, ' ');
               } else if (var3 < var2.length() - 1) {
                  if (var4 == '_') {
                     var2.setCharAt(var3 + 1, ' ');
                  } else {
                     var2.deleteCharAt(var3 + 1);
                  }

                  var2.deleteCharAt(var3);
               }
            }
         }
      } else {
         for (int var5 = 0; var5 < var2.length(); var5++) {
            char var7 = var2.charAt(var5);
            if ((var7 == '_' || var7 == '*') && (var5 <= 0 || var2.charAt(var5 - 1) != '\\')) {
               if (var5 < var2.length() - 1) {
                  var2.deleteCharAt(var5 + 1);
               }

               var2.deleteCharAt(var5);
            }
         }
      }

      for (int var6 = 0; var6 < var2.length(); var6++) {
         char var8 = var2.charAt(var6);
         if (var8 == '\\' || var8 == '"') {
            var2.deleteCharAt(var6);
            var6--;
         } else if (var8 == '+' && var6 > 0 && var2.charAt(var6 - 1) == 'E') {
            var2.deleteCharAt(var6);
            var6--;
         }
      }

      return var2.toString();
   }

   private Format createNumberFormat(String var1, double var2) {
      String var4 = this.cleanFormatForNumber(var1);

      try {
         DecimalFormat var5 = new DecimalFormat(var4, this.decimalSymbols);
         setExcelStyleRoundingMode(var5);
         return var5;
      } catch (IllegalArgumentException var6) {
         return this.getDefaultFormat(var2);
      }
   }

   private static boolean isWholeNumber(double var0) {
      return var0 == Math.floor(var0);
   }

   public Format getDefaultFormat(Cell var1) {
      return this.getDefaultFormat(var1.getNumericCellValue());
   }

   private Format getDefaultFormat(double var1) {
      if (this.defaultNumFormat != null) {
         return this.defaultNumFormat;
      } else {
         return isWholeNumber(var1) ? this.generalWholeNumFormat : this.generalDecimalNumFormat;
      }
   }

   private String performDateFormatting(Date var1, Format var2) {
      return var2 != null ? var2.format(var1) : var1.toString();
   }

   private String getFormattedDateString(Cell var1) {
      Format var2 = this.getFormat(var1);
      if (var2 instanceof ExcelStyleDateFormatter) {
         ((ExcelStyleDateFormatter)var2).setDateToBeFormatted(var1.getNumericCellValue());
      }

      Date var3 = var1.getDateCellValue();
      return this.performDateFormatting(var3, var2);
   }

   private String getFormattedNumberString(Cell var1) {
      Format var2 = this.getFormat(var1);
      double var3 = var1.getNumericCellValue();
      return var2 == null ? String.valueOf(var3) : var2.format(new Double(var3));
   }

   public String formatRawCellContents(double var1, int var3, String var4) {
      return this.formatRawCellContents(var1, var3, var4, false, false, true);
   }

   public String formatRawCellContents(double var1, int var3, String var4, boolean var5, boolean var6, boolean var7) {
      var4 = this.getCustomFormatString(var3, var4);
      if (XeniDateUtil.isADateFormat(var3, var4)) {
         if (XeniDateUtil.isValidExcelDate(var1)) {
            if (!var6) {
               Format var8 = this.getFormat(var1, var3, var4);
               if (var8 instanceof ExcelStyleDateFormatter) {
                  ((ExcelStyleDateFormatter)var8).setDateToBeFormatted(var1);
               }

               Date var9 = XeniDateUtil.getJavaDate(var1, var5);
               return this.performDateFormatting(var9, var8);
            }

            return this.generalIntegerNumberFormat(var1, true);
         }

         if (this.emulateCsv) {
            return invalidDateTimeString;
         }
      }

      return var4 != null && !var7 ? this.formatNumberString(var1, var3, var4) : this.generalIntegerNumberFormat(var1, var7);
   }

   public String getCustomFormatString(int var1, String var2) {
      switch (var1) {
         case 30:
            var2 = "mm-dd-yy";
            break;
         case 31:
            var2 = "yyyy\"년\" mm\"월\" dd\"일\";@";
            break;
         case 32:
            var2 = "h\"시\" mm\"분\";@";
            break;
         case 33:
            var2 = "h\"시\" mm\"분\" ss\"초\";@";
            break;
         case 55:
            var2 = "yyyy/mm/dd";
            break;
         case 57:
            var2 = "yyyy\"年\" mm\"月\" dd\"日\";@";
            break;
         case 58:
            var2 = "mm-dd";
      }

      return var2;
   }

   public String formatCellValue(Cell var1) {
      return this.formatCellValue(var1, null);
   }

   public String formatNumberString(double var1, int var3, String var4) {
      Format var5 = this.getFormat(var1, var3, var4);
      if (var5 == null) {
         return String.valueOf(var1);
      } else if (var4.indexOf("%") > -1) {
         BigDecimal var13 = new BigDecimal(Double.toString(var1)).multiply(new BigDecimal("100"));
         return this.generalIntegerNumberFormat(var13.doubleValue(), true);
      } else {
         Format var6 = var5;
         if (var5 instanceof DecimalFormat && ("General".equalsIgnoreCase(var4) || "@".equalsIgnoreCase(var4))) {
            String var7 = this.xeniDecimalNumFormat.format(var1);
            int var8 = 11;
            if (var7.startsWith("-")) {
               var8++;
            }

            if (var7.length() > var8) {
               var7 = var7.substring(0, var8);
               String var9 = "#.#";
               int var10 = var7.indexOf(46);
               if (var10 > 0) {
                  String var11 = var7.substring(var10 + 1);
                  var10 = var11.length();

                  for (int var12 = 1; var12 < var10; var12++) {
                     var9 = var9 + "#";
                  }
               }

               var6 = new DecimalFormat(var9, this.decimalSymbols);
            }
         }

         String var15 = var6.format(new Double(var1));
         if (var15.contains("E") && !var15.contains("E-")) {
            var15 = var15.replaceFirst("E", "E+");
         }

         return var15;
      }
   }

   public String generalIntegerNumberFormat(double var1, boolean var3) {
      DecimalFormat var4 = new DecimalFormat("#.######################");
      String var5 = var4.format(var1);
      if (!var3) {
         int var6 = 11;
         if (var5.startsWith("-")) {
            var6++;
         }

         if (var5.length() > var6) {
            var5 = var5.substring(0, var6);
            String var7 = "#.#";
            int var8 = var5.indexOf(46);
            if (var8 > 0) {
               String var9 = var5.substring(var8 + 1);
               var8 = var9.length();

               for (int var10 = 1; var10 < var8; var10++) {
                  var7 = var7 + "#";
               }
            }

            var5 = new DecimalFormat(var7, this.decimalSymbols).format(var1);
         }
      }

      return var5;
   }

   public String formatCellValue(Cell var1, FormulaEvaluator var2) {
      if (var1 == null) {
         return "";
      } else {
         CellType var3 = var1.getCellType();
         if (var3 == CellType.FORMULA) {
            if (var2 == null) {
               return var1.getCellFormula();
            }

            var3 = var2.evaluateFormulaCell(var1);
         }

         switch (var3) {
            case NUMERIC:
               if (XeniDateUtil.isCellDateFormatted(var1)) {
                  return this.getFormattedDateString(var1);
               }

               return this.getFormattedNumberString(var1);
            case STRING:
               return var1.getRichStringCellValue().getString();
            case BOOLEAN:
               return String.valueOf(var1.getBooleanCellValue());
            case BLANK:
               return "";
            default:
               throw new RuntimeException("Unexpected celltype (" + var3 + ")");
         }
      }
   }

   public void setDefaultNumberFormat(Format var1) {
      for (Entry var3 : this.formats.entrySet()) {
         if (var3.getValue() == this.generalDecimalNumFormat || var3.getValue() == this.generalWholeNumFormat) {
            var3.setValue(var1);
         }
      }

      this.defaultNumFormat = var1;
   }

   public void addFormat(String var1, Format var2) {
      this.formats.put(var1, var2);
   }

   static DecimalFormat createIntegerOnlyFormat(String var0) {
      DecimalFormat var1 = new DecimalFormat(var0);
      var1.setParseIntegerOnly(true);
      return var1;
   }

   public static void setExcelStyleRoundingMode(DecimalFormat var0) {
      setExcelStyleRoundingMode(var0, RoundingMode.HALF_UP);
   }

   public static void setExcelStyleRoundingMode(DecimalFormat var0, RoundingMode var1) {
      try {
         Method var2 = var0.getClass().getMethod("setRoundingMode", RoundingMode.class);
         var2.invoke(var0, var1);
      } catch (NoSuchMethodException var3) {
      } catch (IllegalAccessException var4) {
         throw new RuntimeException("Unable to set rounding mode", var4);
      } catch (InvocationTargetException var5) {
         throw new RuntimeException("Unable to set rounding mode", var5);
      } catch (SecurityException var6) {
      }
   }

   static {
      StringBuilder var0 = new StringBuilder();

      for (int var1 = 0; var1 < 255; var1++) {
         var0.append('#');
      }

      invalidDateTimeString = var0.toString();
   }

   private static final class ConstantStringFormat extends Format {
      private static final DecimalFormat df = XeniDataFormatter.createIntegerOnlyFormat("##########");
      private final String str;

      public ConstantStringFormat(String var1) {
         this.str = var1;
      }

      @Override
      public StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3) {
         return var2.append(this.str);
      }

      @Override
      public Object parseObject(String var1, ParsePosition var2) {
         return df.parseObject(var1, var2);
      }
   }

   private static final class FractionFormat extends Format {
      private final String str;

      public FractionFormat(String var1) {
         this.str = var1;
      }

      public String format(Number var1) {
         double var2 = var1.doubleValue();
         String[] var4 = this.str.split(";");
         int var5 = var2 > 0.0 ? 0 : (var2 < 0.0 ? 1 : 2);
         String var6 = var5 < var4.length ? var4[var5] : var4[0];
         double var7 = Math.floor(Math.abs(var2));
         double var9 = Math.abs(var2) - var7;
         if (var7 + var9 == 0.0) {
            return "0";
         } else {
            if (var2 < 0.0) {
               var7 *= -1.0;
            }

            String[] var11 = var6.replaceAll("  *", " ").split(" ");
            String[] var12;
            if (var11.length == 2) {
               var12 = var11[1].split("/");
            } else {
               var12 = var6.split("/");
            }

            for (int var13 = 0; var13 < var12.length; var13++) {
               var12[var13] = var12[var13].replace('?', '#');
            }

            if (var12.length != 2) {
               throw new IllegalArgumentException("Fraction must have 2 parts, found " + var12.length + " for fraction format " + this.str);
            } else {
               int var24 = Math.min(this.countHashes(var12[1]), 4);
               double var14 = 1.0;
               double var16 = Math.pow(10.0, (double)var24) - 1.0;
               double var18 = 0.0;

               for (int var20 = (int)(Math.pow(10.0, (double)var24) - 1.0); var20 > 0; var20--) {
                  for (int var21 = (int)(Math.pow(10.0, (double)var24) - 1.0); var21 > 0; var21--) {
                     if (var14 >= Math.abs((double)var21 / (double)var20 - var9)) {
                        var16 = (double)var20;
                        var18 = (double)var21;
                        var14 = Math.abs((double)var21 / (double)var20 - var9);
                     }
                  }
               }

               DecimalFormat var25 = new DecimalFormat(var12[0]);
               DecimalFormat var26 = new DecimalFormat(var12[1]);
               if (var11.length == 2) {
                  DecimalFormat var22 = new DecimalFormat(var11[0]);
                  return var22.format(var7) + " " + var25.format(var18) + "/" + var26.format(var16);
               } else {
                  return var25.format(var18 + var16 * var7) + "/" + var26.format(var16);
               }
            }
         }
      }

      private int countHashes(String var1) {
         int var2 = 0;

         for (int var3 = var1.length() - 1; var3 >= 0; var3--) {
            if (var1.charAt(var3) == '#') {
               var2++;
            }
         }

         return var2;
      }

      @Override
      public StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3) {
         return var2.append(this.format((Number)var1));
      }

      @Override
      public Object parseObject(String var1, ParsePosition var2) {
         throw new NotImplementedException("Reverse parsing not supported");
      }
   }

   private static final class PhoneFormat extends Format {
      public static final Format instance = new XeniDataFormatter.PhoneFormat();
      private static final DecimalFormat df = XeniDataFormatter.createIntegerOnlyFormat("##########");

      public static String format(Number var0) {
         String var1 = df.format(var0);
         StringBuffer var2 = new StringBuffer();
         int var6 = var1.length();
         if (var6 <= 4) {
            return var1;
         } else {
            String var5 = var1.substring(var6 - 4, var6);
            String var4 = var1.substring(Math.max(0, var6 - 7), var6 - 4);
            String var3 = var1.substring(Math.max(0, var6 - 10), Math.max(0, var6 - 7));
            if (var3 != null && var3.trim().length() > 0) {
               var2.append('(').append(var3).append(") ");
            }

            if (var4 != null && var4.trim().length() > 0) {
               var2.append(var4).append('-');
            }

            var2.append(var5);
            return var2.toString();
         }
      }

      @Override
      public StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3) {
         return var2.append(format((Number)var1));
      }

      @Override
      public Object parseObject(String var1, ParsePosition var2) {
         return df.parseObject(var1, var2);
      }
   }

   private static final class SSNFormat extends Format {
      public static final Format instance = new XeniDataFormatter.SSNFormat();
      private static final DecimalFormat df = XeniDataFormatter.createIntegerOnlyFormat("000000000");

      public static String format(Number var0) {
         String var1 = df.format(var0);
         StringBuffer var2 = new StringBuffer();
         var2.append(var1.substring(0, 3)).append('-');
         var2.append(var1.substring(3, 5)).append('-');
         var2.append(var1.substring(5, 9));
         return var2.toString();
      }

      @Override
      public StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3) {
         return var2.append(format((Number)var1));
      }

      @Override
      public Object parseObject(String var1, ParsePosition var2) {
         return df.parseObject(var1, var2);
      }
   }

   private static final class ZipPlusFourFormat extends Format {
      public static final Format instance = new XeniDataFormatter.ZipPlusFourFormat();
      private static final DecimalFormat df = XeniDataFormatter.createIntegerOnlyFormat("000000000");

      public static String format(Number var0) {
         String var1 = df.format(var0);
         StringBuffer var2 = new StringBuffer();
         var2.append(var1.substring(0, 5)).append('-');
         var2.append(var1.substring(5, 9));
         return var2.toString();
      }

      @Override
      public StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3) {
         return var2.append(format((Number)var1));
      }

      @Override
      public Object parseObject(String var1, ParsePosition var2) {
         return df.parseObject(var1, var2);
      }
   }
}
