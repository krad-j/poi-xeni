package com.nexacro17.xeni.export.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xeni.util.CommUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.hssf.usermodel.HSSFPalette;
import org.apache.poixeni.hssf.usermodel.HSSFWorkbook;
import org.apache.poixeni.hssf.util.HSSFColor;
import org.apache.poixeni.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poixeni.ss.usermodel.BorderStyle;
import org.apache.poixeni.ss.usermodel.BuiltinFormats;
import org.apache.poixeni.ss.usermodel.CellStyle;
import org.apache.poixeni.ss.usermodel.FillPatternType;
import org.apache.poixeni.ss.usermodel.Font;
import org.apache.poixeni.ss.usermodel.HorizontalAlignment;
import org.apache.poixeni.ss.usermodel.VerticalAlignment;
import org.apache.poixeni.ss.usermodel.Workbook;
import org.apache.poixeni.xssf.usermodel.XSSFCellStyle;
import org.apache.poixeni.xssf.usermodel.XSSFColor;
import org.apache.poixeni.xssf.usermodel.XSSFFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;

public class GridCellStyleInfo {
   private String sErrMessage = "";
   private static final Log oLogger = LogFactory.getLog(GridCellStyleInfo.class);
   private List<GridCellStyleInfoExt> listStyle = new ArrayList<>();
   private HashMap<String, Integer> oColors = new HashMap<>();
   private short nOffset = 8;
   private boolean bAppended = false;

   public int getStyleInfo(Workbook var1, DataSet var2, boolean var3, boolean var4, boolean var5) {
      this.bAppended = var5;

      for (int var6 = 0; var6 < var2.getRowCount(); var6++) {
         String var7 = var2.getString(var6, "type");
         if (var7.equals("style") && this.createCellStyle(var1, var2, var3, var4, var6) < 0) {
            return -2009;
         }
      }

      return 0;
   }

   private void setErrorMessage(String var1) {
      this.sErrMessage = var1;
      if (oLogger.isInfoEnabled()) {
         oLogger.info(var1);
      }
   }

   public String getErrorMessage() {
      return this.sErrMessage;
   }

   private int createCellStyle(Workbook var1, DataSet var2, boolean var3, boolean var4, int var5) {
      GridCellStyleInfoExt var6 = new GridCellStyleInfoExt();
      CellStyle var7 = var1.createCellStyle();
      this.setDefaultStyle(var7, var4);
      Font var8 = null;
      String var9 = var2.getString(var5, "name");
      String var10 = var2.getString(var5, "value");
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Create style[ " + var9 + " - " + var10 + " ]");
      }

      String[] var11 = var10.split("\\,");

      for (int var12 = 0; var12 < var11.length; var12++) {
         if (!var11[var12].equals("")) {
            String[] var13 = var11[var12].split("\\:");
            if (var13.length < 2) {
               this.setErrorMessage("Can't find style type [ " + var9 + " - " + var11[var12] + " ]");
            } else {
               String var14 = var13[0].trim();
               String[] var15 = this.getStyleValue(var2, var14, var13[1].trim());
               String var16 = var15[1];
               String var17 = var15[0];
               if (var17 == null || var17.length() < 1) {
                  this.setErrorMessage("Can't find style type [ " + var9 + " - " + var11[var12] + " ]");
               } else if (var14.equals("align")) {
                  this.setStyleAlign(var7, var17);
               } else if (var14.equals("font")) {
                  if (var8 == null) {
                     var8 = var1.createFont();
                  }

                  this.setStyleFont(var17, var8);
               } else if (var14.equals("background")) {
                  this.setStyleBackground(var1, var7, var17, var3);
               } else if (var14.equals("color")) {
                  if (var8 == null) {
                     var8 = var1.createFont();
                  }

                  this.setStyleForeground(var1, var17, var3, var8);
               } else if (var14.equals("line")) {
                  this.setStyleBorder(var1, var7, var17, var3);
               } else if (var14.equals("type")) {
                  this.setStyleDataType(var1, var7, var17, var6, var16);
               } else if (!var14.equals("rowsuppress") && !var14.equals("merge")) {
                  if (!var14.equals("colsuppress")) {
                     this.setErrorMessage("Can't find style type [ " + var9 + " - " + var11[var12] + " ]");
                     return -1;
                  }

                  this.setStyleSuppress(var1, var7, var17, var6, false);
               } else {
                  this.setStyleSuppress(var1, var7, var17, var6, true);
               }
            }
         }
      }

      if (var8 != null) {
         var7.setFont(var8);
      }

      var6.addStyle(var9, var7.getIndex());
      this.listStyle.add(var6);
      return 0;
   }

   private void setDefaultStyle(CellStyle var1, boolean var2) {
      if (var2) {
         var1.setWrapText(true);
      }
   }

   private String[] getStyleValue(DataSet var1, String var2, String var3) {
      String[] var4 = new String[2];

      for (int var5 = 0; var5 < var1.getRowCount(); var5++) {
         String var6 = var1.getString(var5, "type");
         String var7 = var1.getString(var5, "name");
         if (var2.equals(var6.trim()) && var3.equals(var7.trim())) {
            if ("type".equals(var2)) {
               var4[1] = var1.getString(var5, "locale");
            }

            var4[0] = var1.getString(var5, "value");
            return var4;
         }
      }

      return null;
   }

   private void setStyleAlign(CellStyle var1, String var2) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style align [ " + var2 + " ]");
      }

      String[] var3 = var2.split("\\,");
      if (var3.length >= 1) {
         HorizontalAlignment var4 = HorizontalAlignment.LEFT;
         VerticalAlignment var5 = VerticalAlignment.CENTER;
         if (var3[0].equals("left")) {
            var4 = HorizontalAlignment.LEFT;
         } else if (var3[0].equals("center")) {
            var4 = HorizontalAlignment.CENTER;
         } else if (var3[0].equals("right")) {
            var4 = HorizontalAlignment.RIGHT;
         }

         var1.setAlignment(var4);
         if (var3.length > 1) {
            if (var3[1].equals("top")) {
               var5 = VerticalAlignment.TOP;
            } else if (var3[1].equals("middle")) {
               var5 = VerticalAlignment.CENTER;
            } else if (var3[1].equals("bottom")) {
               var5 = VerticalAlignment.BOTTOM;
            }
         }

         var1.setVerticalAlignment(var5);
      }
   }

   private void setStyleFont(String var1, Font var2) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style font [ " + var1 + " ]");
      }

      String[] var3 = var1.split("\\,");
      if (var3.length >= 1) {
         boolean var4 = false;
         boolean var5 = false;
         byte var6 = 0;
         boolean var7 = false;

         for (int var8 = 0; var8 < var3.length; var8++) {
            String var9 = var3[var8].trim();
            var9 = var9.replaceAll("\"", "");
            if (!"".equals(var9)) {
               if (var9.equals("bold")) {
                  var4 = true;
               } else if (var9.equals("italic")) {
                  var5 = true;
               } else if (var9.equals("underline")) {
                  var6 = 1;
               } else if (var9.equals("strikeout")) {
                  var7 = true;
               } else if (!var9.equals("antialias")) {
                  if (CommUtil.isNumber(var9)) {
                     var2.setFontHeight((short)(Integer.parseInt(var9) * 20));
                  } else {
                     var2.setFontName(var9);
                  }
               }
            }
         }

         var2.setBold(var4);
         var2.setItalic(var5);
         var2.setUnderline(var6);
         var2.setStrikeout(var7);
      }
   }

   private void setStyleBackground(Workbook var1, CellStyle var2, String var3, boolean var4) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style background [ " + var3 + " ]");
      }

      String[] var5 = var3.split("\\,");
      if (var5.length >= 3) {
         var2.setFillForegroundColor(HSSFColorPredefined.WHITE.getIndex());
         var2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         if (var4) {
            XSSFColor var6 = this.getXSSFRGBColor(var5[0], var5[1], var5[2], false);
            if (var6 != null) {
               ((XSSFCellStyle)var2).setFillForegroundColor(var6);
            }
         } else {
            short var7 = this.getHSSFRGBColor(var5[0], var5[1], var5[2], var1);
            var2.setFillForegroundColor(var7);
         }
      }
   }

   private void setStyleForeground(Workbook var1, String var2, boolean var3, Font var4) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style foreground [ " + var2 + " ]");
      }

      String[] var5 = var2.split("\\,");
      if (var5.length >= 3) {
         var4.setColor((short)32767);
         if (var3) {
            XSSFColor var6 = this.getXSSFRGBColor(var5[0], var5[1], var5[2], true);
            if (var6 != null) {
               CTFont var7 = ((XSSFFont)var4).getCTFont();
               var7.setColorArray(0, var6.getCTColor());
            }
         } else {
            short var8 = this.getHSSFRGBColor(var5[0], var5[1], var5[2], var1);
            var4.setColor(var8);
         }
      }
   }

   private void setStyleBorder(Workbook var1, CellStyle var2, String var3, boolean var4) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style border [ " + var3 + " ]");
      }

      String[] var5 = var3.split("\\:");
      if (var5.length >= 4) {
         for (int var6 = 0; var6 < var5.length; var6++) {
            if (!var5[var6].equals("") && !var5[var6].equals("empty")) {
               String[] var7 = var5[var6].split("\\,");
               BorderStyle var8 = BorderStyle.THIN;
               if (var7.length > 3) {
                  var8 = BorderStyle.MEDIUM;
                  if (var7[3].equals("double")) {
                     var8 = BorderStyle.DOUBLE;
                  } else if (var7[3].equals("dotted")) {
                     var8 = BorderStyle.DOTTED;
                  } else if (var7[3].equals("dashed")) {
                     var8 = BorderStyle.DASHED;
                  }
               }

               if (var4) {
                  XSSFColor var9 = this.getXSSFRGBColor(var7[0], var7[1], var7[2], false);
                  if (var9 != null) {
                     if (var6 == 0) {
                        var2.setBorderLeft(var8);
                        ((XSSFCellStyle)var2).setLeftBorderColor(var9);
                     } else if (var6 == 1) {
                        var2.setBorderTop(var8);
                        ((XSSFCellStyle)var2).setTopBorderColor(var9);
                     } else if (var6 == 2) {
                        var2.setBorderRight(var8);
                        ((XSSFCellStyle)var2).setRightBorderColor(var9);
                     } else if (var6 == 3) {
                        var2.setBorderBottom(var8);
                        ((XSSFCellStyle)var2).setBottomBorderColor(var9);
                     }
                  }
               } else {
                  short var10 = this.getHSSFRGBColor(var7[0], var7[1], var7[2], var1);
                  if (var6 == 0) {
                     var2.setBorderLeft(var8);
                     var2.setLeftBorderColor(var10);
                  } else if (var6 == 1) {
                     var2.setBorderTop(var8);
                     var2.setTopBorderColor(var10);
                  } else if (var6 == 2) {
                     var2.setBorderRight(var8);
                     var2.setRightBorderColor(var10);
                  } else if (var6 == 3) {
                     var2.setBorderBottom(var8);
                     var2.setBottomBorderColor(var10);
                  }
               }
            }
         }
      }
   }

   private void setStyleDataType(Workbook var1, CellStyle var2, String var3, GridCellStyleInfoExt var4, String var5) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style data type [ " + var3 + " ]");
      }

      Object var6 = null;
      String var7 = null;
      int var8 = var3.indexOf(58);
      if (var8 <= 0) {
         var6 = var3;
      } else {
         var6 = var3.substring(0, var8);
         var7 = var3.substring(var8 + 1);
      }

      int var9 = this.getDataTypeNumber((String)var6);
      var4.setDataType(var9);
      if (var9 == 1) {
         if (var7 != null) {
            if (var7.startsWith("!") || var7.startsWith("+") || var7.startsWith("-")) {
               var7 = var7.substring(1);
            }

            if (var7.contains("9")) {
               var7 = var7.replace('9', '#');
            }

            var7 = var7.replace("#.", "0.");
            var7 = var7.replace(".#", ".0");
            if (var7.contains("%")) {
               var4.setbPercentage(true);
            } else {
               var7 = var7 + "_ ";
            }

            var2.setDataFormat(var1.createDataFormat().getFormat(var7));
            var4.setDataFormat(var7);
         } else {
            var2.setDataFormat(var1.createDataFormat().getFormat("#,##0_ "));
         }
      } else if (var9 == 2) {
         if (var7 != null) {
            String var10 = var7;
            if (var7.contains("sss")) {
               var10 = var7.replace("sss", ".000");
            }

            if (var10.contains("/")) {
               var10 = var10.replaceAll("\\/", "\\\\/");
            }

            var10 = this.getFormatForLanguage(var10, var5);
            var2.setDataFormat(var1.createDataFormat().getFormat(var10));
            var10 = var7;
            if (var7.contains("dddd")) {
               var10 = var7.replace("dddd", "E");
            } else if (var7.contains("ddd")) {
               var10 = var7.replace("ddd", "E");
            }

            if (var10.contains("hh")) {
               var10 = var10.replace("hh", "HH");
            } else if (var10.contains("h")) {
               var10 = var10.replace("h", "H");
            }

            if (var10.contains("sss")) {
               var10 = var10.replace("sss", "SSS");
            }

            var4.setDataFormat(var10);
         }
      } else if (var9 == 3) {
         if (var7 != null && var7.startsWith("image")) {
            var4.setImageData(true);
            String[] var16 = var7.split("\\,");
            if (var16.length > 1) {
               var4.setImageStretch(var16[1]);
            }
         } else {
            var2.setDataFormat((short)BuiltinFormats.getBuiltinFormat("TEXT"));
         }
      }
   }

   private void setStyleSuppress(Workbook var1, CellStyle var2, String var3, GridCellStyleInfoExt var4, boolean var5) {
      if (oLogger.isDebugEnabled()) {
         oLogger.debug("Set style suppress [ " + var3 + " ]");
      }

      int var6 = Integer.parseInt(var3);
      if (var6 > 1) {
         if (var5) {
            var4.setiRowSuppressCount(var6);
         } else {
            var4.setiColSuppressCount(var6);
         }
      }
   }

   private XSSFColor getXSSFRGBColor(String var1, String var2, String var3, boolean var4) {
      int var5 = Integer.parseInt(var1);
      int var6 = Integer.parseInt(var2);
      int var7 = Integer.parseInt(var3);
      byte[] var8 = new byte[]{0, (byte)var5, (byte)var6, (byte)var7};
      XSSFColor var9 = new XSSFColor();
      var9.setRGB(var8);
      return var9;
   }

   private short getHSSFRGBColor(String var1, String var2, String var3, Workbook var4) {
      if (!this.bAppended) {
         String var14 = var1 + var2 + var3;
         if (this.oColors.containsKey(var14)) {
            return (short)this.oColors.get(var14).intValue();
         } else {
            int var15 = Integer.parseInt(var1);
            int var16 = Integer.parseInt(var2);
            int var17 = Integer.parseInt(var3);
            short var18 = this.nOffset;
            HSSFPalette var10 = ((HSSFWorkbook)var4).getCustomPalette();
            if (this.nOffset > 64) {
               this.setErrorMessage("User color is at the limit of 56. The color is fixed last user color.");
               HSSFColor var11 = var10.findSimilarColor(var15, var16, var17);
               return var11 != null ? var11.getIndex() : 64;
            } else {
               var10.setColorAtIndex(var18, (byte)var15, (byte)var16, (byte)var17);
               this.nOffset++;
               this.oColors.put(var14, Integer.valueOf(var18));
               return var18;
            }
         }
      } else {
         HSSFColor var5 = null;
         HSSFPalette var6 = ((HSSFWorkbook)var4).getCustomPalette();
         byte var7 = (byte)Integer.parseInt(var1);
         byte var8 = (byte)Integer.parseInt(var2);
         byte var9 = (byte)Integer.parseInt(var3);

         try {
            var5 = var6.findColor(var7, var8, var9);
            if (var5 == null) {
               var5 = var6.addColor(var7, var8, var9);
            }
         } catch (RuntimeException var12) {
            var5 = var6.findSimilarColor(var7, var8, var9);
         }

         return var5.getIndex();
      }
   }

   public GridCellStyleInfoExt getCellStyle(String var1) {
      GridCellStyleInfoExt var2 = null;

      for (int var3 = 0; var3 < this.listStyle.size(); var3++) {
         var2 = this.listStyle.get(var3);
         if (var1.equals(var2.getStyleName())) {
            return var2;
         }
      }

      return null;
   }

   public CellStyle getCellStyle(Workbook var1, String var2) {
      if (var2 != null && var2.length() >= 1) {
         CellStyle var3 = null;
         GridCellStyleInfoExt var4 = this.getCellStyle(var2);
         if (var4 != null) {
            var3 = var1.getCellStyleAt(var4.getStyleIndex());
         }

         return var3;
      } else {
         return null;
      }
   }

   public void clear() {
      this.listStyle.clear();
      this.oColors.clear();
   }

   public short getPaletteOffset() {
      return this.nOffset;
   }

   public void setPaletteOffset(short var1) {
      this.nOffset = var1;
   }

   public void appendStyle(String var1, short var2, int var3, String var4, boolean var5) {
      GridCellStyleInfoExt var6 = new GridCellStyleInfoExt();
      var6.addStyle(var1, var2);
      var6.setDataType(var3);
      var6.setDataFormat(var4);
      var6.setImageData(var5);
      this.listStyle.add(var6);
   }

   private int getDataTypeNumber(String var1) {
      byte var2 = 3;
      if ("normal".equals(var1)) {
         var2 = 0;
      } else if ("number".equals(var1)) {
         var2 = 1;
      } else if ("date".equals(var1)) {
         var2 = 2;
      }

      return var2;
   }

   private String getFormatForLanguage(String var1, String var2) {
      if (var2 == null || "".equals(var2)) {
         Locale var3 = Locale.getDefault();
         var2 = var3.getLanguage() + "_" + var3.getCountry();
      }

      String var4 = "";
      if ("ko_KR".equals(var2)) {
         var4 = "[$-412]";
      } else if ("ja_JP".equals(var2)) {
         var4 = "[$-411]";
      } else if ("zh_CN".equals(var2)) {
         var4 = "[$-804]";
      } else if ("zh_HK".equals(var2)) {
         var4 = "[$-C04]";
      } else if ("zh_SG".equals(var2)) {
         var4 = "[$-1004]";
      } else if ("zh_TW".equals(var2)) {
         var4 = "[$-404]";
      } else if ("aa_DJ".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("aa_ER".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("aa_ET".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("af_ZA".equals(var2)) {
         var4 = "[$-436]";
      } else if ("ar_AE".equals(var2)) {
         var4 = "[$-3801]";
      } else if ("ar_BH".equals(var2)) {
         var4 = "[$-3C01]";
      } else if ("ar_DZ".equals(var2)) {
         var4 = "[$-1401]";
      } else if ("ar_EG".equals(var2)) {
         var4 = "[$-c01]";
      } else if ("ar_IN".equals(var2)) {
         var4 = "[$-1]";
      } else if ("ar_IQ".equals(var2)) {
         var4 = "[$-801]";
      } else if ("ar_JO".equals(var2)) {
         var4 = "[$-2C01]";
      } else if ("ar_KW".equals(var2)) {
         var4 = "[$-3401]";
      } else if ("ar_LB".equals(var2)) {
         var4 = "[$-3001]";
      } else if ("ar_LY".equals(var2)) {
         var4 = "[$-1001]";
      } else if ("ar_MA".equals(var2)) {
         var4 = "[$-1801]";
      } else if ("ar_OM".equals(var2)) {
         var4 = "[$-2001]";
      } else if ("ar_QA".equals(var2)) {
         var4 = "[$-4001]";
      } else if ("ar_SA".equals(var2)) {
         var4 = "[$-401]";
      } else if ("ar_SD".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("ar_SY".equals(var2)) {
         var4 = "[$-2801]";
      } else if ("ar_TN".equals(var2)) {
         var4 = "[$-1C01]";
      } else if ("ar_YE".equals(var2)) {
         var4 = "[$-2401]";
      } else if ("as_IN".equals(var2)) {
         var4 = "[$-44D]";
      } else if ("az_AZ".equals(var2)) {
         var4 = "[$-2C]";
      } else if ("be_BY".equals(var2)) {
         var4 = "[$-423]";
      } else if ("bg_BG".equals(var2)) {
         var4 = "[$-402]";
      } else if ("bn_BD".equals(var2)) {
         var4 = "[$-845]";
      } else if ("bn_IN".equals(var2)) {
         var4 = "[$-445]";
      } else if ("br_FR".equals(var2)) {
         var4 = "[$-47E]";
      } else if ("bs_BA".equals(var2)) {
         var4 = "[$-781A]";
      } else if ("ca_AD".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("ca_ES".equals(var2)) {
         var4 = "[$-403]";
      } else if ("ca_FR".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("ca_IT".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("cs_CZ".equals(var2)) {
         var4 = "[$-405]";
      } else if ("cy_GB".equals(var2)) {
         var4 = "[$-452]";
      } else if ("da_DK".equals(var2)) {
         var4 = "[$-406]";
      } else if ("de_AT".equals(var2)) {
         var4 = "[$-C07]";
      } else if ("de_BE".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("de_CH".equals(var2)) {
         var4 = "[$-807]";
      } else if ("de_DE".equals(var2)) {
         var4 = "[$-407]";
      } else if ("de_LU".equals(var2)) {
         var4 = "[$-1007]";
      } else if ("dz_BT".equals(var2)) {
         var4 = "[$-C51]";
      } else if ("el_CY".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("el_GR".equals(var2)) {
         var4 = "[$-408]";
      } else if ("en_AU".equals(var2)) {
         var4 = "[$-C09]";
      } else if ("en_BW".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("en_CA".equals(var2)) {
         var4 = "[$-1009]";
      } else if ("en_DK".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("en_GB".equals(var2)) {
         var4 = "[$-809]";
      } else if ("en_HK".equals(var2)) {
         var4 = "[$-3C09]";
      } else if ("en_IE".equals(var2)) {
         var4 = "[$-1809]";
      } else if ("en_IN".equals(var2)) {
         var4 = "[$-4009]";
      } else if ("en_NZ".equals(var2)) {
         var4 = "[$-1409]";
      } else if ("en_PH".equals(var2)) {
         var4 = "[$-3409]";
      } else if ("en_SG".equals(var2)) {
         var4 = "[$-4809]";
      } else if ("en_US".equals(var2)) {
         var4 = "[$-409]";
      } else if ("en_ZA".equals(var2)) {
         var4 = "[$-1C09]";
      } else if ("en_ZW".equals(var2)) {
         var4 = "[$-3009]";
      } else if ("es_AR".equals(var2)) {
         var4 = "[$-2C0A]";
      } else if ("es_BO".equals(var2)) {
         var4 = "[$-400A]";
      } else if ("es_CL".equals(var2)) {
         var4 = "[$-340A]";
      } else if ("es_CO".equals(var2)) {
         var4 = "[$-240A]";
      } else if ("es_CR".equals(var2)) {
         var4 = "[$-140A]";
      } else if ("es_DO".equals(var2)) {
         var4 = "[$-1c0A]";
      } else if ("es_EC".equals(var2)) {
         var4 = "[$-300A]";
      } else if ("es_ES".equals(var2)) {
         var4 = "[$-0c0A]";
      } else if ("es_GT".equals(var2)) {
         var4 = "[$-100A]";
      } else if ("es_HN".equals(var2)) {
         var4 = "[$-480A]";
      } else if ("es_MX".equals(var2)) {
         var4 = "[$-80A]";
      } else if ("es_NI".equals(var2)) {
         var4 = "[$-4C0A]";
      } else if ("es_PA".equals(var2)) {
         var4 = "[$-180A]";
      } else if ("es_PE".equals(var2)) {
         var4 = "[$-280A]";
      } else if ("es_PR".equals(var2)) {
         var4 = "[$-500A]";
      } else if ("es_PY".equals(var2)) {
         var4 = "[$-3C0A]";
      } else if ("es_SV".equals(var2)) {
         var4 = "[$-440A]";
      } else if ("es_US".equals(var2)) {
         var4 = "[$-540A]";
      } else if ("es_UY".equals(var2)) {
         var4 = "[$-380A]";
      } else if ("es_VE".equals(var2)) {
         var4 = "[$-200A]";
      } else if ("et_EE".equals(var2)) {
         var4 = "[$-425]";
      } else if ("eu_ES".equals(var2)) {
         var4 = "[$-42D]";
      } else if ("fa_IR".equals(var2)) {
         var4 = "[$-429]";
      } else if ("fi_FI".equals(var2)) {
         var4 = "[$-40B]";
      } else if ("fo_FO".equals(var2)) {
         var4 = "[$-438]";
      } else if ("fr_BE".equals(var2)) {
         var4 = "[$-80C]";
      } else if ("fr_CA".equals(var2)) {
         var4 = "[$-c0C]";
      } else if ("fr_CH".equals(var2)) {
         var4 = "[$-100C]";
      } else if ("fr_FR".equals(var2)) {
         var4 = "[$-40C]";
      } else if ("fr_LU".equals(var2)) {
         var4 = "[$-140C]";
      } else if ("fy_NL".equals(var2)) {
         var4 = "[$-462]";
      } else if ("ga_IE".equals(var2)) {
         var4 = "[$-83C]";
      } else if ("gd_GB".equals(var2)) {
         var4 = "[$-491]";
      } else if ("gl_ES".equals(var2)) {
         var4 = "[$-456]";
      } else if ("gu_IN".equals(var2)) {
         var4 = "[$-447]";
      } else if ("gv_GB".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("he_IL".equals(var2)) {
         var4 = "[$-40D]";
      } else if ("hi_IN".equals(var2)) {
         var4 = "[$-439]";
      } else if ("hr_HR".equals(var2)) {
         var4 = "[$-41A]";
      } else if ("hu_HU".equals(var2)) {
         var4 = "[$-40E]";
      } else if ("hy_AM".equals(var2)) {
         var4 = "[$-42B]";
      } else if ("id_ID".equals(var2)) {
         var4 = "[$-421]";
      } else if ("is_IS".equals(var2)) {
         var4 = "[$-40F]";
      } else if ("it_CH".equals(var2)) {
         var4 = "[$-810]";
      } else if ("it_IT".equals(var2)) {
         var4 = "[$-410]";
      } else if ("iw_IL".equals(var2)) {
         var4 = "[$-40D]";
      } else if ("ka_GE".equals(var2)) {
         var4 = "[$-437]";
      } else if ("kk_KZ".equals(var2)) {
         var4 = "[$-43F]";
      } else if ("kl_GL".equals(var2)) {
         var4 = "[$-46F]";
      } else if ("km_KH".equals(var2)) {
         var4 = "[$-453]";
      } else if ("kn_IN".equals(var2)) {
         var4 = "[$-44B]";
      } else if ("ku_TR".equals(var2)) {
         var4 = "[$-92]";
      } else if ("kw_GB".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("ky_KG".equals(var2)) {
         var4 = "[$-440]";
      } else if ("lg_UG".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("lo_LA".equals(var2)) {
         var4 = "[$-454]";
      } else if ("lt_LT".equals(var2)) {
         var4 = "[$-427]";
      } else if ("lv_LV".equals(var2)) {
         var4 = "[$-426]";
      } else if ("mg_MG".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("mi_NZ".equals(var2)) {
         var4 = "[$-481]";
      } else if ("mk_MK".equals(var2)) {
         var4 = "[$-42F]";
      } else if ("ml_IN".equals(var2)) {
         var4 = "[$-44C]";
      } else if ("mn_MN".equals(var2)) {
         var4 = "[$-450]";
      } else if ("mr_IN".equals(var2)) {
         var4 = "[$-44E]";
      } else if ("ms_MY".equals(var2)) {
         var4 = "[$-43E]";
      } else if ("mt_MT".equals(var2)) {
         var4 = "[$-43A]";
      } else if ("nb_NO".equals(var2)) {
         var4 = "[$-414]";
      } else if ("ne_NP".equals(var2)) {
         var4 = "[$-461]";
      } else if ("nl_BE".equals(var2)) {
         var4 = "[$-813]";
      } else if ("nl_NL".equals(var2)) {
         var4 = "[$-413]";
      } else if ("nn_NO".equals(var2)) {
         var4 = "[$-814]";
      } else if ("no_NO".equals(var2)) {
         var4 = "[$-414]";
      } else if ("nr_ZA".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("oc_FR".equals(var2)) {
         var4 = "[$-482]";
      } else if ("om_ET".equals(var2)) {
         var4 = "[$-472]";
      } else if ("om_KE".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("or_IN".equals(var2)) {
         var4 = "[$-448]";
      } else if ("pa_IN".equals(var2)) {
         var4 = "[$-446]";
      } else if ("pa_PK".equals(var2)) {
         var4 = "[$-46]";
      } else if ("pl_PL".equals(var2)) {
         var4 = "[$-415]";
      } else if ("pt_BR".equals(var2)) {
         var4 = "[$-416]";
      } else if ("pt_PT".equals(var2)) {
         var4 = "[$-816]";
      } else if ("ro_RO".equals(var2)) {
         var4 = "[$-418]";
      } else if ("ru_RU".equals(var2)) {
         var4 = "[$-419]";
      } else if ("ru_UA".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("rw_RW".equals(var2)) {
         var4 = "[$-487]";
      } else if ("se_NO".equals(var2)) {
         var4 = "[$-43B]";
      } else if ("si_LK".equals(var2)) {
         var4 = "[$-45B]";
      } else if ("sk_SK".equals(var2)) {
         var4 = "[$-41B]";
      } else if ("sl_SI".equals(var2)) {
         var4 = "[$-424]";
      } else if ("so_DJ".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("so_ET".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("so_KE".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("so_SO".equals(var2)) {
         var4 = "[$-477]";
      } else if ("sq_AL".equals(var2)) {
         var4 = "[$-41C]";
      } else if ("sr_CS".equals(var2)) {
         var4 = "[$-C1A]";
      } else if ("sr_ME".equals(var2)) {
         var4 = "[$-301A]";
      } else if ("sr_RS".equals(var2)) {
         var4 = "[$-281A]";
      } else if ("ss_ZA".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("st_ZA".equals(var2)) {
         var4 = "[$-430]";
      } else if ("sv_FI".equals(var2)) {
         var4 = "[$-81D]";
      } else if ("sv_SE".equals(var2)) {
         var4 = "[$-41D]";
      } else if ("ta_IN".equals(var2)) {
         var4 = "[$-449]";
      } else if ("te_IN".equals(var2)) {
         var4 = "[$-44A]";
      } else if ("tg_TJ".equals(var2)) {
         var4 = "[$-428]";
      } else if ("th_TH".equals(var2)) {
         var4 = "[$-41E]";
      } else if ("ti_ER".equals(var2)) {
         var4 = "[$-873]";
      } else if ("ti_ET".equals(var2)) {
         var4 = "[$-473]";
      } else if ("tl_PH".equals(var2)) {
         var4 = "[$-464]";
      } else if ("tn_ZA".equals(var2)) {
         var4 = "[$-432]";
      } else if ("tr_CY".equals(var2)) {
         var4 = "[$-1000]";
      } else if ("tr_TR".equals(var2)) {
         var4 = "[$-41F]";
      } else if ("ts_ZA".equals(var2)) {
         var4 = "[$-431]";
      } else if ("tt_RU".equals(var2)) {
         var4 = "[$-444]";
      } else if ("uk_UA".equals(var2)) {
         var4 = "[$-422]";
      } else if ("ur_PK".equals(var2)) {
         var4 = "[$-420]";
      } else if ("uz_UZ".equals(var2)) {
         var4 = "[$-843]";
      } else if ("ve_ZA".equals(var2)) {
         var4 = "[$-433]";
      } else if ("vi_VN".equals(var2)) {
         var4 = "[$-42A]";
      } else if ("wa_BE".equals(var2)) {
         var4 = "[$-C]";
      } else if ("xh_ZA".equals(var2)) {
         var4 = "[$-434]";
      } else if ("yi_US".equals(var2)) {
         var4 = "[$-409]";
      } else if ("zu_ZA".equals(var2)) {
         var4 = "[$-435]";
      }

      return var4 + var1;
   }
}
