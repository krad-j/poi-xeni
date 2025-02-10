package com.nexacro17.xeni.ximport.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xeni.data.importformats.ImportSheet;
import com.nexacro17.xeni.util.CommUtil;
import com.nexacro17.xeni.ximport.GridImportBase;
import com.nexacro17.xeni.ximport.GridImportContext;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public class GridImportCSV implements GridImportBase {
   private static final Log objLogger = LogFactory.getLog(GridImportCSV.class);
   private String sErrMessage = "";
   private int nErrCode = 0;
   private InputStream inputStream = null;
   private GridImportContext importContext;

   @Override
   public void setInputStream(InputStream var1) {
      this.inputStream = var1;
   }

   @Override
   public void setPartIndex(int var1) {
   }

   @Override
   public int getPartIndex() {
      return 0;
   }

   @Override
   public void setOPCPackage(OPCPackage var1) {
   }

   @Override
   public void setPOIFileSystem(POIFSFileSystem var1) {
   }

   @Override
   public String getErrorMessage() {
      return this.sErrMessage;
   }

   @Override
   public void setErrorMessage(String var1) {
      this.sErrMessage = var1;
      if (objLogger.isInfoEnabled()) {
         objLogger.info(var1);
      }
   }

   @Override
   public int getErrorCode() {
      return this.nErrCode;
   }

   @Override
   public void setErrorCode(int var1) {
      this.nErrCode = var1;
   }

   @Override
   public void initialize(GridImportContext var1) {
      this.importContext = var1;
   }

   @Override
   public int executeImport() {
      if (this.importContext.getFileUrl() != null && !"".equals(this.importContext.getFileUrl())) {
         this.nErrCode = 0;
         DataSetList var1 = this.importContext.getPlatformData().getDataSetList();
         DataSet var2 = CommUtil.getDatasetImportResponse();
         var2.set(0, "filepath", this.importContext.getFileUrl());
         var1.add(var2);

         for (int var3 = 0; var3 < this.importContext.getImportFormat().getSheetCount(); var3++) {
            ImportSheet var4 = this.importContext.getImportFormat().getSheet(var3);
            if (var4 != null && !"getsheetlist".equals(var4.getCommand())) {
               DataSet var5 = this.getCsvData(var4);
               if (var5 != null) {
                  var1.add(var5);
               }
            }
         }

         return this.nErrCode;
      } else {
         try {
            if (this.inputStream != null) {
               this.inputStream.close();
            }
         } catch (IOException var6) {
         }

         this.setErrorMessage("Import file path is empty.");
         return -2001;
      }
   }

   public int executeImport(DataSet var1, VariableList var2, VariableList var3, boolean var4, PlatformData var5) {
      return this.nErrCode;
   }

   private DataSet getCsvData(ImportSheet var1) {
      int var2 = 0;
      String var3 = var1.getOutput();
      if (var3 == null || "".equals(var3)) {
         var3 = "CSVDATA";
      }

      DataSet var4 = new DataSet(var3);
      BufferedReader var5 = null;
      InputStream var6 = null;

      try {
         var6 = this.inputStream;
         var5 = new BufferedReader(new InputStreamReader(var6, "UTF-8"));
         GridImportCSV.CSVReader var8 = new GridImportCSV.CSVReader(var5);
         int[] var9 = new int[]{-1, -1};
         int[] var10 = new int[]{-1, -1};
         this.getDataRange(var1.getHead(), var9, var10);
         int[] var11 = new int[]{-1, -1};
         int[] var12 = new int[]{-1, -1};
         this.getDataRange(var1.getBody(), var11, var12);
         var4.setChangeStructureWithData(true);
         if (objLogger.isDebugEnabled()) {
            objLogger.debug(
               "head start column : "
                  + var9[0]
                  + "\nhead start row : "
                  + var9[1]
                  + "\nhead end column : "
                  + var10[0]
                  + "\nhead end row : "
                  + var10[1]
                  + "\nbody start column : "
                  + var11[0]
                  + "\nbody start row : "
                  + var11[1]
                  + "\nbody end column : "
                  + var12[0]
                  + "\nbody end row : "
                  + var12[1]
            );
         }

         String[] var41;
         if (var9[0] < 0 && var9[1] < 0 && var10[0] < 0 && var10[1] < 0 && var11[0] < 0 && var11[1] < 0 && var12[0] < 0 && var12[1] < 0) {
            while ((var41 = var8.readNext(var2)) != null) {
               if (var2 == 0) {
                  for (int var43 = 0; var43 < var41.length; var43++) {
                     var4.addColumn("Column" + var43, 2, 256);
                  }
               } else {
                  int var42 = var4.getColumnCount();
                  if (var41.length > var42) {
                     for (int var45 = var42; var45 < var41.length; var45++) {
                        var4.addColumn("Column" + var45, 2, 256);
                     }
                  }
               }

               int var44 = var4.newRow();

               for (int var46 = 0; var46 < var41.length; var46++) {
                  var4.set(var44, var46, var41[var46]);
               }

               var2++;
            }
         } else {
            boolean var13 = false;
            int var14 = var9[0] < 0 ? 0 : var9[0];
            int var15 = var9[1] < 0 ? 0 : var9[1];
            int var16 = var11[0] < 0 ? 0 : var11[0];

            for (int var17 = var11[1] < 0 ? 0 : var11[1]; (var41 = var8.readNext(var2)) != null; var2++) {
               if (var2 == 0 && var9[0] < 0 && var9[1] < 0 && var10[0] < 0 && var10[1] < 0) {
                  for (int var47 = 0; var47 < var41.length; var47++) {
                     var4.addColumn("Column" + var47, 2, 256);
                  }

                  var13 = true;
               } else if (!var13) {
                  int var18 = var10[0] < 0 ? var41.length - 1 : var10[0];
                  if (var15 <= var2) {
                     for (int var19 = var14; var19 <= var18; var19++) {
                        var4.addColumn(var41[var19], 2, 256);
                     }

                     var13 = true;
                  }
               }

               if (var13) {
                  int var48 = var12[0] < 0 ? var41.length - 1 : var12[0];
                  int var49 = var12[1] < 0 ? var2 : var12[1];
                  if (var17 <= var2 && var2 <= var49) {
                     int var20 = var4.newRow();

                     for (int var21 = var16; var21 <= var48; var21++) {
                        var4.set(var20, var21 - var16, var41[var21]);
                     }
                  } else if (var2 > var49) {
                     break;
                  }
               }
            }
         }
      } catch (MalformedURLException var36) {
         this.nErrCode = -2018;
         this.setErrorMessage(var36.getMessage());
      } catch (FileNotFoundException var37) {
         this.nErrCode = -2020;
         this.setErrorMessage("No such file or directory");
      } catch (IOException var38) {
         this.setErrorMessage(var38.getMessage());
         this.nErrCode = -2003;
      } catch (Exception var39) {
         this.nErrCode = -2001;
         this.setErrorMessage(var39 + "");
      } finally {
         try {
            if (var5 != null) {
               var5.close();
            }

            if (var6 != null) {
               var6.close();
            }
         } catch (IOException var35) {
            this.nErrCode = -2003;
            this.setErrorMessage(var35.getMessage());
         }
      }

      return var4;
   }

   public void getDataRange(String var1, int[] var2, int[] var3) {
      if (var1 != null && !"".equals(var1)) {
         try {
            String[] var4 = var1.split("\\:");
            String[] var5 = var4[0].split("\\,");
            if (var5[0] != null && !"".equals(var5[0])) {
               var2[0] = Integer.parseInt(var5[0]);
            }

            if (var5.length > 1 && var5[1] != null && !"".equals(var5[1])) {
               var2[1] = Integer.parseInt(var5[1]);
            }

            if (var4.length > 1) {
               String[] var6 = var4[1].split("\\,");
               if (var6[0] != null && !"".equals(var6[0])) {
                  var3[0] = Integer.parseInt(var6[0]);
               }

               if (var6.length > 1 && var6[1] != null && !"".equals(var6[1])) {
                  var3[1] = Integer.parseInt(var6[1]);
               }
            }
         } catch (NumberFormatException var7) {
         }
      }
   }

   private class CSVReader {
      private BufferedReader bufreader;
      private boolean hasnextline = true;

      public CSVReader(Reader var2) {
         this.bufreader = new BufferedReader(var2);
      }

      public String[] readNext(int var1) throws IOException {
         String var2 = this.getNextLine();
         if (var2 == null) {
            return null;
         } else {
            if (var1 == 0 && var2.startsWith("\ufeff")) {
               var2 = var2.substring(1);
            }

            return this.hasnextline ? this.parseLine(var2) : null;
         }
      }

      private String getNextLine() throws IOException {
         String var1 = this.bufreader.readLine();
         if (var1 == null) {
            this.hasnextline = false;
         }

         return this.hasnextline ? var1 : null;
      }

      private String[] parseLine(String var1) throws IOException {
         boolean var2 = false;
         StringBuilder var3 = new StringBuilder();
         ArrayList<String> var4 = new ArrayList();

         do {
            if (var2) {
               var3.append("\n");
               var1 = this.getNextLine();
               if (var1 == null) {
                  break;
               }
            }

            for (int var5 = 0; var5 < var1.length(); var5++) {
               char var6 = var1.charAt(var5);
               if (var6 == '\\') {
                  if (this.isEscapable(var1, var2, var5)) {
                     var3.append(var1.charAt(var5 + 1));
                     var5++;
                  } else {
                     var3.append(var6);
                  }
               } else if (GridImportCSV.this.importContext.getQuoteChar().length() > 0 && var6 == GridImportCSV.this.importContext.getQuoteChar().charAt(0)) {
                  if (this.isEscapedQuote(var1, var2, var5)) {
                     var3.append(var1.charAt(var5 + 1));
                     var5++;
                  } else {
                     var2 = !var2;
                     if (var5 > 2
                        && var1.charAt(var5 - 1) != GridImportCSV.this.importContext.getSeparator()
                        && var1.length() > var5 + 1
                        && var1.charAt(var5 + 1) != GridImportCSV.this.importContext.getSeparator()) {
                        var3.append(var6);
                     }
                  }
               } else if (var6 == GridImportCSV.this.importContext.getSeparator() && !var2) {
                  var4.add(var3.toString());
                  var3 = new StringBuilder();
               } else {
                  var3.append(var6);
               }
            }
         } while (var2);

         var4.add(var3.toString());
         return var4.toArray(new String[0]);
      }

      private boolean isEscapedQuote(String var1, boolean var2, int var3) {
         return var2
            && var1.length() > var3 + 1
            && GridImportCSV.this.importContext.getQuoteChar().length() > 0
            && var1.charAt(var3 + 1) == GridImportCSV.this.importContext.getQuoteChar().charAt(0);
      }

      private boolean isEscapable(String var1, boolean var2, int var3) {
         return var2
            && var1.length() > var3 + 1
            && (
               GridImportCSV.this.importContext.getQuoteChar().length() > 0
                     && var1.charAt(var3 + 1) == GridImportCSV.this.importContext.getQuoteChar().charAt(0)
                  || var1.charAt(var3 + 1) == '\\'
            );
      }
   }
}
