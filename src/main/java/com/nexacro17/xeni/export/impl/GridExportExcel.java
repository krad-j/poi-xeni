package com.nexacro17.xeni.export.impl;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xeni.data.GridExportData;
import com.nexacro17.xeni.data.GridExportDataFactory;
import com.nexacro17.xeni.data.exportformats.ExportFormat;
import com.nexacro17.xeni.data.exportformats.FormatCell;
import com.nexacro17.xeni.data.exportformats.FormatColumn;
import com.nexacro17.xeni.data.exportformats.FormatRow;
import com.nexacro17.xeni.export.GridExportBase;
import com.nexacro17.xeni.extend.XeniDataValidationBase;
import com.nexacro17.xeni.extend.XeniDataValidationFactory;
import com.nexacro17.xeni.extend.XeniExcelDataStorageBase;
import com.nexacro17.xeni.extend.XeniExcelDataStorageFactory;
import com.nexacro17.xeni.services.ExportImportFileManager;
import com.nexacro17.xeni.util.CommUtil;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poixeni.hssf.usermodel.HSSFPictureData;
import org.apache.poixeni.hssf.usermodel.HSSFWorkbook;
import org.apache.poixeni.openxml4j.exceptions.InvalidFormatException;
import org.apache.poixeni.poifs.crypt.Decryptor;
import org.apache.poixeni.poifs.crypt.EncryptionInfo;
import org.apache.poixeni.poifs.filesystem.FileMagic;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;
import org.apache.poixeni.ss.usermodel.BuiltinFormats;
import org.apache.poixeni.ss.usermodel.Cell;
import org.apache.poixeni.ss.usermodel.CellStyle;
import org.apache.poixeni.ss.usermodel.ClientAnchor;
import org.apache.poixeni.ss.usermodel.Comment;
import org.apache.poixeni.ss.usermodel.CreationHelper;
import org.apache.poixeni.ss.usermodel.Drawing;
import org.apache.poixeni.ss.usermodel.HorizontalAlignment;
import org.apache.poixeni.ss.usermodel.Picture;
import org.apache.poixeni.ss.usermodel.PictureData;
import org.apache.poixeni.ss.usermodel.RichTextString;
import org.apache.poixeni.ss.usermodel.Row;
import org.apache.poixeni.ss.usermodel.Sheet;
import org.apache.poixeni.ss.usermodel.VerticalAlignment;
import org.apache.poixeni.ss.usermodel.Workbook;
import org.apache.poixeni.ss.usermodel.WorkbookFactory;
import org.apache.poixeni.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poixeni.ss.util.CellAddress;
import org.apache.poixeni.ss.util.CellRangeAddress;
import org.apache.poixeni.ss.util.CellReference;
import org.apache.poixeni.ss.util.ImageUtils;
import org.apache.poixeni.util.IOUtils;
import org.apache.poixeni.xssf.streaming.SXSSFWorkbook;
import org.apache.poixeni.xssf.usermodel.XSSFPictureData;
import org.apache.poixeni.xssf.usermodel.XSSFSheet;
import org.apache.poixeni.xssf.usermodel.XSSFWorkbook;

public class GridExportExcel implements GridExportBase {
   private String sErrMessage = "";
   private String sExportFileUrl = "";
   private String sExportFilePath = "";
   private int nStartRow = 0;
   private int nStartCol = 0;
   private int nRowCntAll = 0;
   private int nAddedRow = 0;
   private int nNextExlRowIdx = 0;
   private String sSheetName;
   private String sInstanceId = null;
   private boolean bSetWidth = true;
   private boolean bSetHeight = false;
   private boolean bNoHead = true;
   private boolean bNoSummary = true;
   private DataSet dsCmd = null;
   private int iRunPeriodMinute = 30;
   private int iStorageTimeMinute = 10;
   private GridExportData objExportData = null;
   private final int nMax2003 = 65536;
   private final int nMax2007 = 1048576;
   private final int nMaxHcell = 1048576;
   private final float PX_MODIFIED = 36.56F;
   private final float PX_DEFAULT = 32.0F;
   private final int PX_POW = 15;
   private final int PIXEL_DPI = 96;
   private final int POINT_DPI = 72;
   private final int PX_PADDING = 2;
   private static final Log objLogger = LogFactory.getLog(GridExportExcel.class);
   private boolean bIsExcel2007 = false;
   private char cGS = 29;
   private Workbook objWorkbook = null;
   private ExportFormat objGridFmt = null;
   private GridCellStyleInfo objStyleInfo = null;
   Drawing objDrawing = null;
   private HashMap<String, Integer> objPictures = new HashMap<>();
   private XeniDataValidationBase validator;

   private void setErrorMessage(String var1) {
      this.sErrMessage = var1;
      if (objLogger.isInfoEnabled()) {
         objLogger.info(var1);
      }
   }

   @Override
   public void setExportType(String var1) {
   }

   @Override
   public void setExportData(GridExportData var1) {
      this.objExportData = var1;
   }

   @Override
   public String getExportFileUrl() {
      return this.sExportFileUrl;
   }

   @Override
   public String getErrorMessage() {
      return this.sErrMessage;
   }

   @Override
   public void setExportFilePath(String var1, String var2, boolean var3) {
      this.sExportFilePath = var1;
      this.sExportFileUrl = var2;
      if (var3) {
         this.enableFileMonitor(var1, this.iRunPeriodMinute, this.iStorageTimeMinute);
      }
   }

   private void enableFileMonitor(String var1, int var2, int var3) {
      ExportImportFileManager var4 = ExportImportFileManager.getInstance();
      Timer var5 = var4.getTimer();
      if (var5 == null) {
         var5 = var4.newTimerInstance();
         var4.setServiceDir(var1, true);
         var4.setStorageTime(var3 * 60);
         var5.scheduleAtFixedRate(var4, (long)(var2 * 60 * 1000), (long)(var2 * 60 * 1000));
      }
   }

   @Override
   public void receiveExportData(DataSetList var1) {
      DataSet var2 = var1.get("COMMAND");
      DataSet var3 = var1.get("STYLE");
      DataSet var4 = var1.get("CELL");
      this.sInstanceId = var2.getString(0, "instanceid");
      if (this.sInstanceId == null || this.sInstanceId.equals("")) {
         this.sInstanceId = CommUtil.generateSerialNo();
      }

      HashMap var5 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      if (var5.containsKey(this.sInstanceId)) {
         this.objExportData = (GridExportData)var5.get(this.sInstanceId);
         this.objExportData.setCmdDataset(var2);
         this.objExportData.setStyleDataset(var3);
         this.objExportData.setCellDataset(var4);
      } else {
         var2.set(0, "instanceid", this.sInstanceId);
         this.objExportData = new GridExportData();
         this.objExportData.setCmdDataset(var2);
         this.objExportData.setStyleDataset(var3);
         this.objExportData.setCellDataset(var4);
         String var6 = var2.getString(0, "url");
         if (var6 != null && var6.length() > 0) {
            this.objExportData.setAppendExport(var6);
         }

         var5.put(this.sInstanceId, this.objExportData);
      }

      this.objExportData.setLastAccTime(System.currentTimeMillis());
   }

   @Override
   public boolean isLastExportData(DataSetList var1) {
      DataSet var2 = var1.get("COMMAND");
      return var2.getBoolean(0, "eof");
   }

   @Override
   public DataSet getResponseCommand() {
      return CommUtil.getDatasetExportResponse(this.objExportData.getCmdDataset());
   }

   @Override
   public int executeExport(VariableList var1, boolean var2, HttpServletResponse var3, VariableList var4) {
      if (objLogger.isDebugEnabled()) {
         objLogger.debug("Exceute export : Excel");
      }

      int var5 = 0;
      if (this.objExportData == null) {
         this.setErrorMessage("Export data is null.");
         return -2010;
      } else {
         this.validator = XeniDataValidationFactory.getDataValidator();
         DataSet var6 = this.objExportData.getCmdDataset();
         DataSet var7 = this.objExportData.getStyleDataset();
         DataSet var8 = this.objExportData.getCellDataset();
         this.objGridFmt = this.objExportData.getGridFormat();
         if (var6 != null && var7 != null && var8 != null && this.objGridFmt != null) {
            int var9 = var6.getInt(0, "type");
            if (var9 != 288 && var9 != 1040) {
               this.bIsExcel2007 = false;
            } else {
               this.bIsExcel2007 = true;
            }

            String var10 = var6.getString(0, "range");
            if (objLogger.isDebugEnabled()) {
               objLogger.debug("Export range : " + var10);
            }

            if (var10 != null && !var10.equals("")) {
               var10 = this.getRangInfo(var10);
            } else {
               var10 = "Sheet";
            }

            String var11 = var6.getString(0, "exporthead");
            boolean var12 = false;
            boolean var13 = false;
            if (var11 != null) {
               var12 = var11.contains("nohead");
               var13 = var11.contains("nosumm");
            }

            String var14 = var6.getString(0, "summarytype");
            boolean var15 = false;
            if ("top".equals(var14)) {
               var15 = true;
            }

            int var16 = var8.getRowCount();
            int var17 = this.nStartRow + this.objGridFmt.getRowCountOfEachBand("body") * var16;
            if (!var12) {
               var17 += this.objGridFmt.getRowCountOfEachBand("head");
            }

            if (!var13) {
               var17 += this.objGridFmt.getRowCountOfEachBand("summ");
            }

            int var18 = 0;
            int var19 = 1;
            if (this.bIsExcel2007) {
               var18 = 1048576;
            } else if (var9 != 1024 && var9 != 1040) {
               var18 = 65536;
            } else {
               var18 = 1048576;
            }

            if (var17 >= var18) {
               var19 = var17 / var18;
               if (var17 % var18 > 0) {
                  var19++;
               }
            }

            String var20 = var6.getString(0, "exportsize");
            boolean var21 = true;
            boolean var22 = false;
            if ("both".equals(var20)) {
               var22 = true;
            } else if ("height".equals(var20)) {
               var21 = false;
               var22 = true;
            }

            int var23 = var1.getInt("accesswindowsize");
            if (var23 <= 0) {
               var23 = 100;
            }

            Map var24 = this.getUserHeight(var1);
            var5 = this.makeExportFilePath(var6, var2);
            if (var5 < 0) {
               this.setErrorMessage("Fail to create export file path.");
               return var5;
            } else {
               this.objWorkbook = this.createExcelWorkbook(var6, var23);
               if (this.objWorkbook == null) {
                  this.setErrorMessage("Fail to create workbook!");
                  return -2012;
               } else {
                  boolean var25 = true;
                  if (var6.containsColumn("wraptext")) {
                     var25 = var6.getBoolean(0, "wraptext");
                  }

                  boolean var26 = var6.getBoolean(0, "righttoleft");
                  if (objLogger.isDebugEnabled()) {
                     objLogger.debug(
                        "nohead : "
                           + var12
                           + "\nnosummary : "
                           + var13
                           + "\nsummary top : "
                           + var15
                           + "\nexport type : "
                           + var9
                           + "\nset width : "
                           + var21
                           + "\nset height : "
                           + var22
                           + "\naccess window size : "
                           + var23
                           + "\nwrap text : "
                           + var25
                           + "\nright to left : "
                           + var26
                     );
                  }

                  this.objStyleInfo = new GridCellStyleInfo();
                  var5 = this.objStyleInfo.getStyleInfo(this.objWorkbook, var7, this.bIsExcel2007, var25, this.objExportData.isAppendExport());
                  if (var5 < 0) {
                     this.setErrorMessage(this.objStyleInfo.getErrorMessage());
                     return var5;
                  } else {
                     if (var19 == 1) {
                        Sheet var27 = null;
                        if (this.objExportData.isAppendExport()) {
                           var27 = this.objWorkbook.getSheet(var10);
                        }

                        if (var27 == null) {
                           var27 = this.objWorkbook.createSheet(var10);
                        }

                        if (var21) {
                           this.setColumnLayout(var27);
                        }

                        int var28 = 0;
                        int var29 = this.nStartRow;
                        this.objDrawing = var27.createDrawingPatriarch();
                        if (!var12) {
                           var28 = this.createEachBandLayout(var27, "head", var29, var22);
                           if (var28 > 0) {
                              var5 = this.setHead(var27, var29);
                              if (var5 < 0) {
                                 return var5;
                              }
                           }

                           var29 += var28;
                        }

                        if (var15 && !var13) {
                           var28 = this.createEachBandLayout(var27, "summ", var29, var22);
                           if (var28 > 0) {
                              var5 = this.setSummary(var27, var29);
                              if (var5 < 0) {
                                 return var5;
                              }
                           }

                           var29 += var28;
                        }

                        if (objLogger.isDebugEnabled()) {
                           objLogger.debug(
                              "Export body( format count : "
                                 + this.objGridFmt.getBodyCellCount()
                                 + ", row : "
                                 + var16
                                 + ", col : "
                                 + var8.getColumnCount()
                                 + " )"
                           );
                        }

                        for (int var30 = 0; var30 < var16; var30++) {
                           var28 = this.createEachBandLayout(var27, "body", var29, var22);
                           var5 = this.setBody(var27, var8, var30, var29);
                           if (var5 < 0) {
                              return var5;
                           }

                           var29 += var28;
                        }

                        if (!var15 && !var13) {
                           var28 = this.createEachBandLayout(var27, "summ", var29, var22);
                           if (var28 > 0) {
                              var5 = this.setSummary(var27, var29);
                              if (var5 < 0) {
                                 return var5;
                              }
                           }

                           var29 += var28;
                        }

                        if (var24 != null) {
                           this.setUserHeight(var27, var24);
                        }

                        this.UpdateDimension(var27, var10, this.nStartCol, this.nStartRow + 1, this.objGridFmt.getColumnCount() - 1 + this.nStartCol, var29);
                        if (var26) {
                           var27.setRightToLeft(true);
                        }
                     } else {
                        var5 = this.createMultiSheet(var8, var10, var19, var12, var13, var21, var22, var18, var15, var26);
                        if (var5 < 0) {
                           return var5;
                        }
                     }

                     return this.saveExportFile(var1, var6, var3);
                  }
               }
            }
         } else {
            this.setErrorMessage(
               "Dataset is null. \nFILENAME : "
                  + (var6 == null ? "null" : var6.getString(0, "exportfilename"))
                  + "\nCOMMAND : "
                  + (var6 == null ? "null" : var6.toString())
                  + "\nSTYLE : "
                  + (var7 == null ? "null" : var7.toString())
                  + "\nCELL : "
                  + (var8 == null ? "null" : var8.toString())
                  + "\nFORMAT : "
                  + (this.objGridFmt == null ? "null" : this.objGridFmt.toString())
            );
            return -2011;
         }
      }
   }

   @Override
   public int createWorkbook() {
      return this.createWorkbook(false);
   }

   @Override
   public int createWorkbook(boolean var1) {
      int var2 = 0;
      HashMap var3 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      this.objExportData = (GridExportData)var3.get(this.sInstanceId);
      if (this.objExportData == null) {
         this.setErrorMessage("Export data is null.");
         return -2010;
      } else {
         this.dsCmd = this.objExportData.getCmdDataset();
         DataSet var4 = this.objExportData.getStyleDataset();
         this.objGridFmt = this.objExportData.getGridFormat();
         if (this.dsCmd != null && var4 != null && this.objGridFmt != null) {
            this.bIsExcel2007 = true;
            this.sSheetName = this.dsCmd.getString(0, "range");
            if (this.sSheetName != null && !this.sSheetName.equals("")) {
               this.sSheetName = this.getRangInfo(this.sSheetName);
            } else {
               this.sSheetName = "Sheet";
            }

            String var5 = this.dsCmd.getString(0, "exporthead");
            this.bNoHead = var5.contains("nohead");
            this.bNoSummary = var5.contains("nosumm");
            String var6 = this.dsCmd.getString(0, "summarytype");
            boolean var7 = false;
            if ("top".equals(var6)) {
               var7 = true;
            }

            this.nRowCntAll = this.nStartRow + this.objGridFmt.getRowCountOfEachBand("body");
            if (!this.bNoHead) {
               this.nRowCntAll = this.nRowCntAll + this.objGridFmt.getRowCountOfEachBand("head");
            }

            if (!this.bNoSummary) {
               this.nRowCntAll = this.nRowCntAll + this.objGridFmt.getRowCountOfEachBand("summ");
            }

            int var8 = 0;
            int var9 = 1;
            if (this.bIsExcel2007) {
               var8 = 1048576;
            }

            if (this.nRowCntAll >= var8) {
               var9 = this.nRowCntAll / var8;
               if (this.nRowCntAll % var8 > 0) {
                  var9++;
               }
            }

            String var10 = this.dsCmd.getString(0, "exportsize");
            if (var10.equals("both")) {
               this.bSetHeight = true;
            } else if (var10.equals("height")) {
               this.bSetWidth = false;
               this.bSetHeight = true;
            }

            var2 = this.makeExportFilePath(this.dsCmd, var1);
            if (var2 < 0) {
               this.setErrorMessage("Fail to create export file path.");
               return var2;
            } else {
               this.objWorkbook = this.createExcelWorkbook(this.dsCmd, 100);
               if (this.objWorkbook == null) {
                  this.setErrorMessage("Fail to create workbook!");
                  return -2012;
               } else {
                  boolean var11 = true;
                  if (this.dsCmd.containsColumn("wraptext")) {
                     var11 = this.dsCmd.getBoolean(0, "wraptext");
                  }

                  this.objStyleInfo = new GridCellStyleInfo();
                  var2 = this.objStyleInfo.getStyleInfo(this.objWorkbook, var4, this.bIsExcel2007, var11, this.objExportData.isAppendExport());
                  if (var2 < 0) {
                     this.setErrorMessage(this.objStyleInfo.getErrorMessage());
                     return var2;
                  } else {
                     if (var9 == 1) {
                        Sheet var12 = null;
                        if (this.objExportData.isAppendExport()) {
                           var12 = this.objWorkbook.getSheet(this.sSheetName);
                        }

                        if (var12 == null) {
                           var12 = this.objWorkbook.createSheet(this.sSheetName);
                        }

                        if (this.bSetWidth) {
                           this.setColumnLayout(var12);
                        }

                        this.nNextExlRowIdx = this.nStartRow;
                        this.objDrawing = var12.createDrawingPatriarch();
                        if (!this.bNoHead) {
                           this.nAddedRow = this.createEachBandLayout(var12, "head", this.nNextExlRowIdx, this.bSetHeight);
                           if (this.nAddedRow > 0) {
                              var2 = this.setHead(var12, this.nNextExlRowIdx);
                              if (var2 < 0) {
                                 return var2;
                              }
                           }

                           this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
                        }

                        if (var7 && !this.bNoSummary) {
                           this.nAddedRow = this.createEachBandLayout(var12, "summ", this.nNextExlRowIdx, this.bSetHeight);
                           if (this.nAddedRow > 0) {
                              var2 = this.setSummary(var12, this.nNextExlRowIdx);
                              if (var2 < 0) {
                                 return var2;
                              }
                           }

                           this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
                        }
                     } else {
                        var2 = this.createMultiSheetHead(this.sSheetName, var9, this.bNoHead, this.bNoSummary, this.bSetWidth, this.bSetHeight, var8, var7);
                        if (var2 < 0) {
                           return var2;
                        }
                     }

                     return var2;
                  }
               }
            }
         } else {
            this.setErrorMessage(
               "Dataset is null. \nFILENAME : "
                  + (this.dsCmd == null ? "null" : this.dsCmd.getString(0, "exportfilename"))
                  + "\nCOMMAND : "
                  + (this.dsCmd == null ? "null" : this.dsCmd.toString())
                  + "\nSTYLE : "
                  + (var4 == null ? "null" : var4.toString())
                  + "\nFORMAT : "
                  + (this.objGridFmt == null ? "null" : this.objGridFmt.toString())
            );
            return -2011;
         }
      }
   }

   private int appendBody(Object var1) {
      int var2 = 0;
      if (this.objWorkbook == null) {
         this.setErrorMessage("Fail to create workbook!");
         return -2012;
      } else {
         Sheet var3 = this.objWorkbook.getSheet(this.sSheetName);
         if (var1 == null) {
            this.setErrorMessage("CellData object is null. \n");
            return -2011;
         } else {
            GridExportExcel.CellDataObject var4 = null;
            if (var1 instanceof DataSet) {
               var4 = new GridExportExcel.CellDataObject((DataSet)var1);
            } else {
               if (!(var1 instanceof List)) {
                  this.setErrorMessage("CellData object type unsupported.");
                  return -1;
               }

               var4 = new GridExportExcel.CellDataObject((List<Map<String, Object>>)var1);
            }

            this.nRowCntAll = this.nRowCntAll + var4.getRowCount();
            int var5 = 0;
            int var6 = 1;
            if (this.bIsExcel2007) {
               var5 = 1048576;
            }

            if (this.nRowCntAll >= var5) {
               var6 = this.nRowCntAll / var5;
               if (this.nRowCntAll % var5 > 0) {
                  var6++;
               }
            }

            if (var6 == 1) {
               int var7 = var4.getRowCount();

               for (int var8 = 0; var8 < var7; var8++) {
                  this.nAddedRow = this.createEachBandLayout(var3, "body", this.nNextExlRowIdx, this.bSetHeight);
                  var2 = this.setBody(var3, var4, var8, this.nNextExlRowIdx);
                  if (var2 < 0) {
                     return var2;
                  }

                  this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
               }

               if (!this.bNoSummary) {
                  this.nAddedRow = this.createEachBandLayout(var3, "summ", this.nNextExlRowIdx, this.bSetHeight);
                  if (this.nAddedRow > 0) {
                     var2 = this.setSummary(var3, this.nNextExlRowIdx);
                     if (var2 < 0) {
                        return var2;
                     }
                  }

                  this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
               }

               this.UpdateDimension(
                  var3, this.sSheetName, this.nStartCol, this.nStartRow + 1, this.objGridFmt.getColumnCount() - 1 + this.nStartCol, this.nNextExlRowIdx
               );
            } else {
               var2 = this.createMultiSheetBody(var4, this.sSheetName, var6, this.bNoHead, this.bNoSummary, this.bSetWidth, this.bSetHeight, var5);
               if (var2 < 0) {
                  return var2;
               }
            }

            return var2;
         }
      }
   }

   @Override
   public int appendBody(DataSet var1) {
      return this.appendBody((Object)var1);
   }

   @Override
   public int appendBody(List<Map<String, Object>> var1) {
      return this.appendBody(var1);
   }

   @Override
   public DataSet disposeWorkbook() {
      HashMap var1 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      var1.remove(this.sInstanceId);
      return this.saveExportFile(null, this.dsCmd);
   }

   @Override
   public int disposeWorkbook(VariableList var1, HttpServletResponse var2) {
      HashMap var3 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      var3.remove(this.sInstanceId);
      return this.saveExportFile(var1, this.dsCmd, var2);
   }

   @Override
   public DataSet disposeWorkbook(VariableList var1) {
      HashMap var2 = GridExportDataFactory.getExportDataFactoryInstance().getExportDataFactory();
      var2.remove(this.sInstanceId);
      return this.saveExportFile(var1, this.dsCmd);
   }

   private String getRangInfo(String var1) {
      this.nStartCol = 0;
      this.nStartRow = 0;
      String[] var3 = var1.split("\\!");
      if (var3.length == 1) {
         return var1;
      } else {
         int[] var4 = new int[]{0, 0};
         CommUtil.getRangeIndex(var3[1], var4);
         this.nStartCol = var4[0];
         this.nStartRow = var4[1];
         return var3[0].length() < 1 ? "Sheet" : var3[0];
      }
   }

   private int makeExportFilePath(DataSet var1, boolean var2) {
      try {
         if (this.objExportData.isAppendExport()) {
            String var10 = this.objExportData.getAppendExportUrl();
            this.sExportFileUrl = var10;
            label99:
            if (var2) {
               int var12 = var10.indexOf("key=");
               String var16 = var10.substring(var12 + 4, var10.indexOf(38, var12));
               if (var16 != null && !var16.contains(".") && !var16.contains("/")) {
                  var12 = var10.indexOf("name=", var12);
                  String var18 = var10.substring(var12 + 5, var10.indexOf(38, var12));
                  var18 = URLDecoder.decode(var18, "UTF-8");
                  if (var18 != null && !var18.contains(".") && !var16.contains("/")) {
                     var12 = var10.indexOf("type=", var12);
                     int var21 = Integer.parseInt(var10.substring(var12 + 5));
                     String var22 = ".xls";
                     if (var21 == 1) {
                        var22 = ".xlsx";
                     } else if (var21 != 2 && var21 != 3) {
                        if (var21 != 0) {
                           return -3201;
                        }
                     } else {
                        var22 = ".cell";
                     }

                     this.sExportFilePath = this.sExportFilePath + var16 + "/" + var18 + var22;
                     break label99;
                  }

                  return -3201;
               }

               return -3201;
            } else {
               int var11 = var10.lastIndexOf("/");
               String var15 = var10.substring(var11);
               int var17 = var15.lastIndexOf(46);
               String var20 = var15.substring(1, var17);
               if (var20 == null || var20.contains(".") || var20.contains("/")) {
                  return -3201;
               }

               String var8 = var10.substring(0, var11);
               this.sExportFilePath = this.sExportFilePath + var8.substring(var8.lastIndexOf("/")) + var15;
            }
         } else {
            label124: {
               String var3 = var1.getString(0, "instanceid");
               if (!var3.contains(".") && !var3.contains("/")) {
                  String var4 = var1.getString(0, "exportfilename");
                  if (var4 == null || var4.length() < 1) {
                     var4 = var1.getString(0, "item");
                     if (var4 == null || var4.length() < 1) {
                        var4 = "TEMP";
                     }
                  }

                  if (!var4.contains(".") && !var4.contains("/")) {
                     int var5 = var1.getInt(0, "type");
                     byte var7 = 0;
                     String var6;
                     if (var5 == 1024) {
                        var6 = var4 + ".cell";
                        var7 = 2;
                     } else if (var5 == 1040) {
                        var6 = var4 + ".cell";
                        var7 = 3;
                     } else {
                        var6 = var4 + ".xls";
                        if (this.bIsExcel2007) {
                           var6 = var6 + "x";
                           var7 = 1;
                        }
                     }

                     this.sExportFilePath = this.sExportFilePath + var3 + "/" + var6;
                     if (var2) {
                        this.sExportFileUrl = this.sExportFileUrl
                           + "?command=export&key="
                           + var3
                           + "&name="
                           + URLEncoder.encode(var4, "UTF-8")
                           + "&type="
                           + var7;
                     } else {
                        this.sExportFileUrl = this.sExportFileUrl + var3 + "/" + var6;
                     }
                     break label124;
                  }

                  return -3201;
               }

               return -3201;
            }
         }
      } catch (UnsupportedEncodingException var9) {
         return -2012;
      }

      if (objLogger.isDebugEnabled()) {
         objLogger.debug("export file path : " + this.sExportFilePath);
         objLogger.debug("export file url : " + this.sExportFileUrl);
      }

      return 0;
   }

   private Workbook createExcelWorkbook(DataSet var1, int var2) {
      Object var3 = null;
      if (this.objExportData.isAppendExport()) {
         InputStream var4 = null;
         InputStream var5 = null;

         try {
            if (objLogger.isDebugEnabled()) {
               objLogger.debug("Read file(workbook) : " + this.sExportFilePath);
            }

            XeniExcelDataStorageBase var6 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
            var5 = var6.loadTargetStream(this.sExportFilePath);
            if (!this.bIsExcel2007) {
               var3 = WorkbookFactory.create(var5);
            } else {
               String var7 = var1.getString(0, "password");
               if (var7 != null && var7.length() > 0) {
                  if (objLogger.isDebugEnabled()) {
                     objLogger.debug("Read decrypted stream.");
                  }

                  var4 = this.getDecryptedStream(var5, var7);
               } else {
                  var4 = var5;
               }

               Workbook var8 = WorkbookFactory.create(var4);
               var3 = new SXSSFWorkbook((XSSFWorkbook)var8, var2);
            }
         } catch (InvalidFormatException var21) {
            this.setErrorMessage(var21.getMessage());
         } catch (IOException var22) {
            this.setErrorMessage(var22.getMessage());
         } catch (Exception var23) {
            this.setErrorMessage(var23 + "");
         } finally {
            try {
               if (var5 != null) {
                  var5.close();
               }

               if (var4 != null) {
                  var5.close();
               }
            } catch (IOException var20) {
               this.setErrorMessage(var20.getMessage());
            }
         }
      } else if (!this.bIsExcel2007) {
         var3 = new HSSFWorkbook();
      } else {
         var3 = new SXSSFWorkbook(var2);
      }

      return (Workbook)var3;
   }

   private void setColumnLayout(Sheet var1) {
      int var2 = 0;
      int var3 = 0;

      for (int var4 = 0; var4 < this.objGridFmt.getColumnCount(); var4++) {
         FormatColumn var5 = this.objGridFmt.getColumn(var4);
         if (var5 != null) {
            var2 = Integer.parseInt(var5.getSize());
            if (var2 > 1500) {
               var2 = 1500;
            }

            if (this.bIsExcel2007) {
               var3 = var2 * 32;
            } else {
               var3 = var2 * 36;
            }

            var1.setColumnWidth(this.nStartCol + var4, var3);
         }
      }
   }

   private int createEachBandLayout(Sheet var1, String var2, int var3, boolean var4) {
      int var5 = this.objGridFmt.getColumnCount();
      int var6 = 0;
      int var7 = 0;

      for (int var8 = 0; var8 < this.objGridFmt.getRowCount(); var8++) {
         FormatRow var9 = this.objGridFmt.getRow(var8);
         if (var9.getBand().equals(var2)) {
            Row var10 = var1.createRow(var3 + var6);
            if (var4) {
               var7 = Integer.parseInt(var9.getSize());
               var10.setHeight((short)(var7 * 15));
            }

            for (int var11 = 0; var11 < var5; var11++) {
               var10.createCell(var11 + this.nStartCol);
            }

            var6++;
         }
      }

      return var6;
   }

   private int setHead(Sheet var1, int var2) {
      CellStyle var3 = null;
      FormatCell var4 = null;
      if (objLogger.isDebugEnabled()) {
         objLogger.debug("Export head( format count : " + this.objGridFmt.getHeadCellCount() + " )");
      }

      for (int var5 = 0; var5 < this.objGridFmt.getHeadCellCount(); var5++) {
         var4 = this.objGridFmt.getHeadCell(var5);
         Cell var6 = this.getCellInExcel(var1, var4, var2);
         if (var6 == null) {
            return -2015;
         }

         GridCellStyleInfoExt var7 = this.objStyleInfo.getCellStyle(var4.getStyle1());
         if (var7 != null) {
            var3 = this.objWorkbook.getCellStyleAt(var7.getStyleIndex());
            if (var3 != null) {
               var6.setCellStyle(var3);
            }
         }

         String var8 = var4.getText();
         if (var8 != null) {
            this.setCellValue(var6, var8, var7, true, var4);
         }

         this.setMergeCell(var1, var4, var6, var3);
      }

      return 0;
   }

   private int setBody(Sheet var1, DataSet var2, int var3, int var4) {
      int var5 = 0;
      int var6 = var2.getColumnCount();
      int var7 = this.objGridFmt.getBodyCellCount();
      String var8 = "";
      String var9 = "";
      String var10 = "";
      Object var11 = null;
      CellStyle var12 = null;
      Object var13 = null;
      if (var6 != var7) {
         this.setErrorMessage("The body format is fault : [ colum count of CELL dataset = " + var6 + ", body cell count of FORMAT  = " + var7 + " ]");
         return -2014;
      } else {
         for (int var14 = 0; var14 < var7; var14++) {
            var13 = this.objGridFmt.getBodyCell(var14);
            Cell var15 = this.getCellInExcel(var1, (FormatCell)var13, var4);
            if (var15 == null) {
               return -2015;
            }

            var8 = "";
            var9 = "";
            var10 = var2.getString(var3, var14);
            boolean var16 = true;
            if (var10 != null && var10.length() > 0) {
               String[] var17 = var10.split(String.valueOf(this.cGS));

               for (int var18 = 0; var18 < var17.length; var18++) {
                  if (var18 == 0) {
                     var8 = var17[0];
                  } else if (var18 == 1) {
                     var9 = var17[1];
                  } else if (var18 == 2 && "true".equals(var17[2])) {
                     var16 = false;
                  }
               }
            }

            var11 = this.getBodyCellStyle(var15, (FormatCell)var13, var9, var3);
            if (var11 != null) {
               var12 = this.objWorkbook.getCellStyleAt(((GridCellStyleInfoExt)var11).getStyleIndex());
               if (var12 != null) {
                  var15.setCellStyle(var12);
               }
            }

            var5 = this.setCellValue(var15, var8, (GridCellStyleInfoExt)var11, var16, (FormatCell)var13);
            if (var16) {
               this.setMergeCell(var1, (FormatCell)var13, var15, var12);
            }

            this.setSuppressCell(var1, (FormatCell)var13, var15, (GridCellStyleInfoExt)var11);
         }

         return var5;
      }
   }

   private int setBody(Sheet var1, GridExportExcel.CellDataObject var2, int var3, int var4) {
      int var5 = 0;
      int var6 = var2.getColumnCount();
      int var7 = this.objGridFmt.getBodyCellCount();
      String var8 = "";
      String var9 = "";
      String var10 = "";
      Object var11 = null;
      CellStyle var12 = null;
      Object var13 = null;
      if (var6 != var7) {
         this.setErrorMessage("The body format is fault : [ colum count of CELL dataset = " + var6 + ", body cell count of FORMAT  = " + var7 + " ]");
         return -2014;
      } else {
         for (int var14 = 0; var14 < var7; var14++) {
            var13 = this.objGridFmt.getBodyCell(var14);
            Cell var15 = this.getCellInExcel(var1, (FormatCell)var13, var4);
            if (var15 == null) {
               return -2015;
            }

            var8 = "";
            var9 = "";
            String var16 = this.findBindColumnNameInCelltext(((FormatCell)var13).getText());
            var10 = var2.getCellData(var3, var16);
            if (var10 != null && var10.length() > 0) {
               String[] var17 = var10.split(String.valueOf(this.cGS));

               for (int var18 = 0; var18 < var17.length; var18++) {
                  if (var18 == 0) {
                     var8 = var17[0];
                  } else if (var18 == 1) {
                     var9 = var17[1];
                  }
               }
            }

            var11 = this.getBodyCellStyle(var15, (FormatCell)var13, var9, var3);
            if (var11 != null) {
               var12 = this.objWorkbook.getCellStyleAt(((GridCellStyleInfoExt)var11).getStyleIndex());
               if (var12 != null) {
                  var15.setCellStyle(var12);
               }
            }

            var5 = this.setCellValue(var15, var8, (GridCellStyleInfoExt)var11, true, (FormatCell)var13);
            this.setMergeCell(var1, (FormatCell)var13, var15, var12);
            this.setSuppressCell(var1, (FormatCell)var13, var15, (GridCellStyleInfoExt)var11);
         }

         return var5;
      }
   }

   private GridCellStyleInfoExt getBodyCellStyle(Cell var1, FormatCell var2, String var3, int var4) {
      GridCellStyleInfoExt var5 = null;
      var5 = this.objStyleInfo.getCellStyle(var3);
      if (var5 == null) {
         String var6 = "";
         String var7 = var2.getStyle2();
         if (var7 == null || var7.length() <= 0) {
            var6 = var2.getStyle1();
         } else if (var4 % 2 == 0) {
            var6 = var2.getStyle1();
         } else {
            var6 = var2.getStyle2();
         }

         var5 = this.objStyleInfo.getCellStyle(var6);
      }

      return var5;
   }

   private int setCellValue(Cell var1, String var2, GridCellStyleInfoExt var3, boolean var4, FormatCell var5) {
      if (var2.length() > 0) {
         if (this.validator != null) {
            var2 = this.validator.checkData(var2);
         }

         if (var3.isImageData()) {
            this.setCellImage(var1, var2, var4, var5, var3.getImageStretch());
         } else {
            int var6 = var3.getDataType();
            if (var6 == 1) {
               var2 = var2.replaceAll("\\,|\\%", "");

               try {
                  if (!"".equals(var2)) {
                     double var7 = Double.parseDouble(var2);
                     if (var2.matches("^[-+]?\\d+\\.\\d+") && var3.getDataFormat() == null) {
                        this.setStyleDoubleFormat(var2, var1, var3);
                     }

                     if (var3.isbPercentage()) {
                        var7 /= 100.0;
                     }

                     var1.setCellValue(var7);
                  }
               } catch (NumberFormatException var9) {
                  this.setStyleNumberTextFormat(var1, var3);
                  var1.setCellValue(var2);
               }
            } else if (var6 == 2) {
               try {
                  DateTimeFormatter var12 = new DateTimeFormatterBuilder()
                     .appendPattern(var3.getDataFormat())
                     .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L)
                     .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L)
                     .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L)
                     .toFormatter();
                  LocalDateTime var8 = LocalDateTime.parse(var2, var12);
                  var1.setCellValue(var8);
               } catch (Exception var10) {
                  if (objLogger.isInfoEnabled()) {
                     objLogger.info(var10.getMessage());
                  }

                  var1.setCellValue(var2);
               }
            } else {
               if (var6 == 0 && var2.charAt(0) == '=') {
                  var1.setCellFormula(var2.substring(1));
                  return 0;
               }

               var1.setCellValue(var2);
            }
         }
      }

      return 0;
   }

   private void setStyleDoubleFormat(String var1, Cell var2, GridCellStyleInfoExt var3) {
      String var4 = "#,##0";
      String var5 = var1.substring(var1.indexOf(46) + 1);

      for (int var6 = 0; var6 < var5.length(); var6++) {
         if (var6 == 0) {
            var4 = var4 + ".";
         }

         var4 = var4 + "0";
      }

      var4 = var4 + "_ ";
      short var12 = this.objWorkbook.createDataFormat().getFormat(var4);
      String var7 = var3.getStyleName() + "_dbl" + var12;
      GridCellStyleInfoExt var8 = this.objStyleInfo.getCellStyle(var7);
      CellStyle var9 = null;
      if (var8 == null) {
         var9 = this.objWorkbook.createCellStyle();
         CellStyle var10 = this.objWorkbook.getCellStyleAt(var3.getStyleIndex());
         var9.cloneStyleFrom(var10);
         var9.setDataFormat(var12);
         this.objStyleInfo.appendStyle(var7, var9.getIndex(), var3.getDataType(), var3.getDataFormat(), var3.isImageData());
      } else {
         var9 = this.objWorkbook.getCellStyleAt(var8.getStyleIndex());
      }

      var2.setCellStyle(var9);
   }

   private void setStyleNumberTextFormat(Cell var1, GridCellStyleInfoExt var2) {
      CellStyle var3 = null;
      String var4 = var2.getStyleName();
      GridCellStyleInfoExt var5 = this.objStyleInfo.getCellStyle(var4 + "text");
      if (var5 == null) {
         var3 = this.objWorkbook.createCellStyle();
         CellStyle var6 = this.objWorkbook.getCellStyleAt(var2.getStyleIndex());
         var3.cloneStyleFrom(var6);
         var3.setDataFormat((short)BuiltinFormats.getBuiltinFormat("TEXT"));
         this.objStyleInfo.appendStyle(var4 + "text", var3.getIndex(), var2.getDataType(), var2.getDataFormat(), var2.isImageData());
      } else {
         var3 = this.objWorkbook.getCellStyleAt(var5.getStyleIndex());
      }

      var1.setCellStyle(var3);
   }

   private boolean setCellImage(Cell var1, String var2, boolean var3, FormatCell var4, int var5) {
      DataInputStream var6 = null;
      int var7 = -1;
      String var8 = "";
      Object var9 = null;

      try {
         if (var2.trim().startsWith("http")) {
            if (this.objPictures.containsKey(var2)) {
               var7 = this.objPictures.get(var2);
            } else {
               URL var10 = new URL(var2);
               URLConnection var34 = var10.openConnection();
               var34.setConnectTimeout(5000);
               var34.connect();
               var6 = new DataInputStream(new BufferedInputStream(var34.getInputStream()));
               var9 = IOUtils.toByteArray(var6);
               String var12 = var2.substring(var2.lastIndexOf(".") + 1);
               var7 = this.objWorkbook.addPicture((byte[])var9, this.getPictureType(var12));
               this.objPictures.put(var2, var7);
               var8 = var2;
            }
         } else {
            if (!var2.trim().startsWith("data:")) {
               return false;
            }

            int var31 = var2.indexOf(44);
            if (var31 < 0) {
               return false;
            }

            String var35 = "png";
            if (var31 > 5) {
               String var37 = var2.substring(5, var31);
               String[] var13 = var37.split(";");
               String[] var14 = var13[0].split("/");
               if (var14.length > 1) {
                  var35 = var14[1];
               }
            }

            var9 = Base64.getDecoder().decode(var2.substring(var31 + 1));
            if (var9 == null) {
               return false;
            }

            var7 = this.objWorkbook.addPicture((byte[])var9, this.getPictureType(var35));
            var8 = var35;
         }

         if (var7 >= 0) {
            CreationHelper var32 = this.objWorkbook.getCreationHelper();
            this.setPicture(var32, var1, var7, var8, var3, var4, var5);
         }

         return true;
      } catch (Exception var26) {
         this.setErrorMessage(var26.getMessage());
         return false;
      } finally {
         if (var6 != null) {
            try {
               var6.close();
            } catch (IOException var25) {
               this.setErrorMessage(var25.getMessage());
               return false;
            }
         }
      }
   }

   private int getPictureType(String var1) {
      var1 = var1.toUpperCase();
      if (var1.equals("JPEG") || var1.equals("JPG")) {
         return 5;
      } else if (var1.equals("DIB")) {
         return 7;
      } else if (var1.equals("EMF")) {
         return 2;
      } else if (var1.equals("PICT")) {
         return 4;
      } else if (var1.equals("PNG")) {
         return 6;
      } else {
         return var1.equals("WMF") ? 3 : 6;
      }
   }

   private void setPicture(CreationHelper var1, Cell var2, int var3, String var4, boolean var5, FormatCell var6, int var7) {
      try {
         ClientAnchor var8 = var1.createClientAnchor();
         var8.setAnchorType(AnchorType.MOVE_AND_RESIZE);
         var8.setCol1(var2.getColumnIndex());
         var8.setRow1(var2.getRowIndex());
         int var9 = 1;
         int var10 = 1;
         if (var5) {
            var9 = Integer.parseInt(var6.getRowspan());
            var10 = Integer.parseInt(var6.getColspan());
         }

         if (var7 == 2) {
            var8.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
            Picture var19 = this.objDrawing.createPicture(var8, var3);
            var19.resize();
            return;
         }

         var8.setCol2(var2.getColumnIndex() + var10);
         var8.setRow2(var2.getRowIndex() + var9);
         if (var7 == 0) {
            this.objDrawing.createPicture(var8, var3);
            return;
         }

         Picture var11 = this.objDrawing.createPicture(var8, var3);
         Sheet var12 = var2.getSheet();
         float var13 = 0.0F;
         float var14 = 0.0F;
         float var15 = 0.0F;
         int var16 = var12.getDefaultColumnWidth() * 256;
         if (!this.bIsExcel2007) {
            for (int var22 = var2.getRowIndex(); var22 < var2.getRowIndex() + var9; var22++) {
               var13 += (float)(var12.getRow(var22).getHeight() / 15);
            }

            for (int var23 = var2.getColumnIndex(); var23 < var2.getColumnIndex() + var10; var23++) {
               var14 += (float)var12.getColumnWidth(var23);
            }

            var15 = var14 == (float)var16 ? 32.0F : 36.56F;
            var14 /= var15;
         } else {
            for (int var17 = var2.getRowIndex(); var17 < var2.getRowIndex() + var9; var17++) {
               var13 += var12.getRow(var17).getHeightInPoints() * 96.0F / 72.0F;
            }

            for (int var21 = var2.getColumnIndex(); var21 < var2.getColumnIndex() + var10; var21++) {
               var14 += (float)var12.getColumnWidth(var21) / 32.0F;
            }
         }

         this.resize(var12, var11, var8, var14, var13, var7, var2.getCellStyle());
      } catch (Exception var18) {
         if (objLogger.isWarnEnabled()) {
            objLogger.warn("Fail to draw image : " + var4);
         }
      }
   }

   private int setSummary(Sheet var1, int var2) {
      CellStyle var3 = null;
      FormatCell var4 = null;
      if (objLogger.isDebugEnabled()) {
         objLogger.debug("Export summary( format count : " + this.objGridFmt.getSummaryCellCount() + " )");
      }

      for (int var5 = 0; var5 < this.objGridFmt.getSummaryCellCount(); var5++) {
         var4 = this.objGridFmt.getSummary(var5);
         Cell var6 = this.getCellInExcel(var1, var4, var2);
         if (var6 == null) {
            return -2015;
         }

         GridCellStyleInfoExt var7 = this.objStyleInfo.getCellStyle(var4.getStyle1());
         if (var7 != null) {
            var3 = this.objWorkbook.getCellStyleAt(var7.getStyleIndex());
            if (var3 != null) {
               var6.setCellStyle(var3);
            }
         }

         String var8 = var4.getText();
         if (var8 != null) {
            this.setCellValue(var6, var8, var7, true, var4);
         }

         this.setMergeCell(var1, var4, var6, var3);
      }

      return 0;
   }

   private Cell getCellInExcel(Sheet var1, FormatCell var2, int var3) {
      int var4 = Integer.parseInt(var2.getRow());
      int var5 = Integer.parseInt(var2.getCol());
      int var6 = var4 + var3;
      int var7 = var5 + this.nStartCol;
      Row var8 = var1.getRow(var6);
      Cell var9 = var8.getCell(var7);
      if (var9 == null) {
         this.setErrorMessage("The cell in excel is null : [ row = " + var6 + ", col = " + var7 + " ]");
         return null;
      } else {
         return var9;
      }
   }

   private CellStyle setStyleText(FormatCell var1, Cell var2) {
      CellStyle var3 = this.objStyleInfo.getCellStyle(this.objWorkbook, var1.getStyle1());
      if (var3 != null) {
         var3.setWrapText(true);
         var2.setCellStyle(var3);
      }

      String var4 = var1.getText();
      if (var4 != null) {
         CreationHelper var5 = this.objWorkbook.getCreationHelper();
         RichTextString var6 = var5.createRichTextString(var4);
         var2.setCellValue(var6);
         if (var3 != null) {
            var3.setDataFormat((short)BuiltinFormats.getBuiltinFormat("TEXT"));
         }
      }

      return var3;
   }

   private void setMergeCell(Sheet var1, FormatCell var2, Cell var3, CellStyle var4) {
      int var5 = Integer.parseInt(var2.getRowspan());
      int var6 = Integer.parseInt(var2.getColspan());
      if (var5 > 1 || var6 > 1) {
         int var7 = var3.getRowIndex();
         int var8 = var3.getColumnIndex();
         var1.addMergedRegionUnsafe(new CellRangeAddress(var7, var7 + var5 - 1, var8, var8 + var6 - 1));
         if (var4 == null) {
            return;
         }

         boolean var9 = false;
         boolean var10 = false;
         Row var11 = null;
         Cell var12 = null;

         for (int var13 = var7; var13 < var7 + var5; var13++) {
            var11 = var1.getRow(var13);
            if (var11 != null) {
               for (int var14 = var8; var14 < var8 + var6; var14++) {
                  if (var13 != var7 || var14 != var8) {
                     var12 = var11.getCell(var14);
                     if (var12 != null) {
                        var12.setCellStyle(var4);
                     }
                  }
               }
            }
         }
      }
   }

   private void setSuppressCell(Sheet var1, FormatCell var2, Cell var3, GridCellStyleInfoExt var4) {
      int var5 = var4.getiRowSuppressCount();
      int var6 = var4.getiColSuppressCount();
      if (var5 > 1 || var6 > 1) {
         int var7 = var3.getRowIndex();
         int var8 = var3.getColumnIndex();
         int var9 = Integer.parseInt(var2.getRowspan()) - 1;
         int var10 = Integer.parseInt(var2.getColspan()) - 1;
         int var11 = var7;
         int var12 = var8;
         if (var5 > 1) {
            var11 = var7 - (var5 - var9 - 1);
         }

         if (var6 > 1) {
            var12 = var8 - (var6 - var10 - 1);
         }

         CellRangeAddress var13 = new CellRangeAddress(var11, var7 + var9, var12, var8 + var10);
         this.removeMergedRegion(var1, var13);
         this.setMergedCellStyle(var1, var3.getCellStyle(), var13);
         var1.addMergedRegion(var13);
      }
   }

   private void removeMergedRegion(Sheet var1, CellRangeAddress var2) {
      int var3 = var2.getFirstRow();
      int var4 = var2.getLastRow();
      int var5 = var2.getFirstColumn();
      int var6 = var2.getLastColumn();

      for (int var7 = var1.getNumMergedRegions() - 1; var7 >= 0; var7--) {
         CellRangeAddress var8 = var1.getMergedRegion(var7);
         int var9 = var8.getFirstRow();
         int var10 = var8.getLastRow();
         int var11 = var8.getFirstColumn();
         int var12 = var8.getLastColumn();
         if (var3 <= var9 && var9 <= var4 && var3 <= var10 && var10 <= var4 && (var5 <= var11 && var11 <= var6 || var5 <= var12 && var12 <= var6)) {
            var1.removeMergedRegion(var7);
         }
      }
   }

   private void setMergedCellStyle(Sheet var1, CellStyle var2, CellRangeAddress var3) {
      if (var2 != null) {
         int var4 = var3.getFirstRow();
         int var5 = var3.getLastRow();
         int var6 = var3.getFirstColumn();
         int var7 = var3.getLastColumn();
         boolean var8 = false;
         boolean var9 = false;
         Row var10 = null;
         Cell var11 = null;

         for (int var12 = var4; var12 <= var5; var12++) {
            var10 = var1.getRow(var12);
            if (var10 != null) {
               for (int var13 = var6; var13 <= var7; var13++) {
                  var11 = var10.getCell(var13);
                  if (var11 != null) {
                     var11.setCellStyle(var2);
                  }
               }
            }
         }
      }
   }

   private int saveExportFile(VariableList var1, DataSet var2, HttpServletResponse var3) {
      int var4 = 0;

      try {
         ByteArrayOutputStream var5 = new ByteArrayOutputStream();
         this.objWorkbook.write(var5);
         if (this.bIsExcel2007) {
            ((SXSSFWorkbook)this.objWorkbook).dispose();
         }

         this.objWorkbook = null;
         this.objPictures.clear();
         this.objStyleInfo.clear();
         XeniExcelDataStorageBase var6 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
         if (var6 == null) {
            this.setErrorMessage("Could not create extend class : xeni.exportimport.storage");
            return -2004;
         }

         var4 = var6.saveExportStream(var1, var2, var5, this.sExportFilePath, this.sExportFileUrl, var3);
      } catch (FileNotFoundException var7) {
         this.setErrorMessage("No such file or directory");
         var4 = -2020;
      } catch (IOException var8) {
         this.setErrorMessage(var8.getMessage());
         var4 = -2003;
      } catch (Exception var9) {
         var4 = -2001;
         this.setErrorMessage(var9 + "");
      }

      return var4;
   }

   private DataSet saveExportFile(VariableList var1, DataSet var2) {
      DataSet var3 = null;

      try {
         ByteArrayOutputStream var4 = new ByteArrayOutputStream();
         this.objWorkbook.write(var4);
         if (this.bIsExcel2007) {
            ((SXSSFWorkbook)this.objWorkbook).dispose();
         }

         this.objWorkbook = null;
         this.objPictures.clear();
         this.objStyleInfo.clear();
         XeniExcelDataStorageBase var5 = XeniExcelDataStorageFactory.getExtendClass("xeni.exportimport.storage");
         if (var5 == null) {
            this.setErrorMessage("Could not create extend class : xeni.exportimport.storage");
            return null;
         }

         var3 = var5.saveExportStream(var1, var2, var4, this.sExportFilePath, this.sExportFileUrl);
      } catch (FileNotFoundException var6) {
         this.setErrorMessage(var6.getMessage());
      } catch (IOException var7) {
         this.setErrorMessage(var7.getMessage());
      } catch (Exception var8) {
         this.setErrorMessage(var8 + "");
      }

      return var3;
   }

   private int createMultiSheet(
      DataSet var1, String var2, int var3, boolean var4, boolean var5, boolean var6, boolean var7, int var8, boolean var9, boolean var10
   ) {
      int var11 = 0;
      int var12 = 0;
      int var13 = var1.getRowCount();
      int var14 = this.objGridFmt.getRowCountOfEachBand("summ");
      boolean var15 = false;

      for (int var16 = 1; var16 < var3 + 1; var16++) {
         Sheet var17 = null;
         if (this.objExportData.isAppendExport() && !var15) {
            var17 = this.objWorkbook.getSheet(var2);
            var15 = true;
         }

         if (var17 == null) {
            var17 = this.objWorkbook.createSheet(var2 + var16);
         }

         if (var6) {
            this.setColumnLayout(var17);
         }

         int var18 = 0;
         int var19 = this.nStartRow;
         this.objDrawing = var17.createDrawingPatriarch();
         if (!var4) {
            var18 = this.createEachBandLayout(var17, "head", var19, var7);
            if (var18 > 0) {
               var11 = this.setHead(var17, var19);
               if (var11 < 0) {
                  return var11;
               }
            }

            var19 += var18;
         }

         if (var9 && !var5) {
            var18 = this.createEachBandLayout(var17, "summ", var19, var7);
            if (var18 > 0) {
               var11 = this.setSummary(var17, var19);
               if (var11 < 0) {
                  return var11;
               }
            }

            var19 += var18;
         }

         for (int var20 = var12; var20 < var13; var20++) {
            var18 = this.createEachBandLayout(var17, "body", var19, var7);
            var11 = this.setBody(var17, var1, var20, var19);
            if (var11 < 0) {
               return var11;
            }

            var19 += var18;
            if (var20 < var13 - 1) {
               int var21 = 0;
               if (var5) {
                  var21 = var19 + var18;
               } else {
                  var21 = var19 + var18 + var14;
               }

               if (var21 > var8) {
                  if (var16 == var3) {
                     var3++;
                  }

                  var12 = var20 + 1;
                  break;
               }
            } else if (!var5 && var19 + var14 > var8) {
               if (var16 == var3) {
                  var3++;
               }

               var12 = var20 + 1;
               break;
            }
         }

         if (!var9 && !var5) {
            var18 = this.createEachBandLayout(var17, "summ", var19, var7);
            if (var18 > 0) {
               var11 = this.setSummary(var17, var19);
               if (var11 < 0) {
                  return var11;
               }
            }

            var19 += var18;
         }

         this.UpdateDimension(var17, var2 + var16, this.nStartCol, this.nStartRow + 1, this.objGridFmt.getColumnCount() - 1 + this.nStartCol, var19);
         if (var10) {
            var17.setRightToLeft(true);
         }
      }

      return 0;
   }

   private int createMultiSheetHead(String var1, int var2, boolean var3, boolean var4, boolean var5, boolean var6, int var7, boolean var8) {
      int var9 = 0;
      boolean var10 = false;
      int var11 = this.objGridFmt.getRowCountOfEachBand("summ");
      boolean var12 = false;

      for (int var13 = 1; var13 < var2 + 1; var13++) {
         Sheet var14 = null;
         if (this.objExportData.isAppendExport() && !var12) {
            var14 = this.objWorkbook.getSheet(var1);
            var12 = true;
         }

         if (var14 == null) {
            var14 = this.objWorkbook.createSheet(var1 + var13);
         }

         if (var5) {
            this.setColumnLayout(var14);
         }

         int var15 = 0;
         int var16 = this.nStartRow;
         this.objDrawing = var14.createDrawingPatriarch();
         if (!var3) {
            var15 = this.createEachBandLayout(var14, "head", var16, var6);
            if (var15 > 0) {
               var9 = this.setHead(var14, var16);
               if (var9 < 0) {
                  return var9;
               }
            }

            var16 += var15;
         }

         if (var8 && !var4) {
            var15 = this.createEachBandLayout(var14, "summ", var16, var6);
            if (var15 > 0) {
               var9 = this.setSummary(var14, var16);
               if (var9 < 0) {
                  return var9;
               }
            }

            var16 += var15;
         }
      }

      return 0;
   }

   private int createMultiSheetBody(
      GridExportExcel.CellDataObject var1, String var2, int var3, boolean var4, boolean var5, boolean var6, boolean var7, int var8
   ) {
      int var9 = 0;
      int var10 = 0;
      int var11 = var1.getRowCount();
      int var12 = this.objGridFmt.getRowCountOfEachBand("summ");

      for (int var13 = 1; var13 < var3 + 1; var13++) {
         Sheet var14 = this.objWorkbook.getSheet(var2);
         if (var14 == null) {
            var14 = this.objWorkbook.createSheet(var2 + var13);
         }

         for (int var15 = var10; var15 < var11; var15++) {
            this.nAddedRow = this.createEachBandLayout(var14, "body", this.nNextExlRowIdx, var7);
            var9 = this.setBody(var14, var1, var15, this.nNextExlRowIdx);
            if (var9 < 0) {
               return var9;
            }

            this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
            if (var15 < var11 - 1) {
               int var16 = 0;
               if (var5) {
                  var16 = this.nNextExlRowIdx + this.nAddedRow;
               } else {
                  var16 = this.nNextExlRowIdx + this.nAddedRow + var12;
               }

               if (var16 > var8) {
                  if (var13 == var3) {
                     var3++;
                  }

                  var10 = var15 + 1;
                  break;
               }
            } else if (!var5 && this.nNextExlRowIdx + var12 > var8) {
               if (var13 == var3) {
                  var3++;
               }

               var10 = var15 + 1;
               break;
            }
         }

         if (!var5) {
            this.nAddedRow = this.createEachBandLayout(var14, "summ", this.nNextExlRowIdx, var7);
            if (this.nAddedRow > 0) {
               var9 = this.setSummary(var14, this.nNextExlRowIdx);
               if (var9 < 0) {
                  return var9;
               }
            }

            this.nNextExlRowIdx = this.nNextExlRowIdx + this.nAddedRow;
         }

         this.UpdateDimension(
            var14, var2 + var13, this.nStartCol, this.nStartRow + 1, this.objGridFmt.getColumnCount() - 1 + this.nStartCol, this.nNextExlRowIdx
         );
      }

      return 0;
   }

   private void setPaletteOffsetOnSheet() {
      Sheet var1 = this.objWorkbook.getSheetAt(0);
      if (var1 != null) {
         CreationHelper var2 = this.objWorkbook.getCreationHelper();
         ClientAnchor var3 = var2.createClientAnchor();
         short var4 = this.objStyleInfo.getPaletteOffset();
         RichTextString var5 = var2.createRichTextString(String.valueOf((int)var4));
         CellAddress var6 = new CellAddress(0, 255);
         Comment var7 = var1.getCellComment(var6);
         if (var7 == null) {
            var7 = this.objDrawing.createCellComment(var3);
            var7.setString(var5);
            var7.setAuthor("nexacro API");
            var7.setVisible(false);
            Row var8 = var1.getRow(0);
            if (var8 == null) {
               var8 = var1.createRow(0);
            }

            Cell var9 = var8.getCell(255);
            if (var9 == null) {
               var9 = var8.createCell(255);
            }

            var9.setCellComment(var7);
         } else {
            var7.setString(var5);
         }
      }
   }

   private short getPaletteOffsetOnSheet() {
      Sheet var1 = this.objWorkbook.getSheetAt(0);
      if (var1 == null) {
         return -1;
      } else {
         CellAddress var2 = new CellAddress(0, 255);
         Comment var3 = var1.getCellComment(var2);
         if (var3 == null) {
            return -1;
         } else {
            RichTextString var4 = var3.getString();
            return (short)Integer.parseInt(var4.getString());
         }
      }
   }

   private String findBindColumnNameInCelltext(String var1) {
      Object var2 = null;
      if (var1 == null) {
         return (String)var2;
      } else {
         return (String)(var1.startsWith("bind:") ? var1.substring(var1.indexOf(":") + 1) : var2);
      }
   }

   private void UpdateDimension(Sheet var1, String var2, int var3, int var4, int var5, int var6) {
      if (this.bIsExcel2007) {
         String var7 = CellReference.convertNumToColString(var3) + var4;
         var7 = var7 + ":";
         var7 = var7 + CellReference.convertNumToColString(var5) + var6;
         XSSFWorkbook var8 = ((SXSSFWorkbook)this.objWorkbook).getXSSFWorkbook();
         XSSFSheet var9 = var8.getSheet(var2);
         var9.getCTWorksheet().getDimension().setRef(var7);
      }
   }

   private ClientAnchor getPreferredSize(Sheet var1, Picture var2, ClientAnchor var3, double var4, double var6, float var8, float var9, CellStyle var10) throws IOException {
      HorizontalAlignment var11 = HorizontalAlignment.LEFT;
      VerticalAlignment var12 = VerticalAlignment.CENTER;
      if (var10 != null) {
         var11 = var10.getAlignment();
         var12 = var10.getVerticalAlignment();
      }

      this.setColumnAnchor(var1, var3, var4, var8, var11);
      this.setRowAnchor(var1, var3, var6, var9, var12);
      return var3;
   }

   private Dimension getImageDimension(PictureData var1) throws IOException {
      if (!this.bIsExcel2007) {
         HSSFPictureData var3 = (HSSFPictureData)var1;
         return ImageUtils.getImageDimension(new ByteArrayInputStream(var3.getData()), var3.getFormat());
      } else {
         XSSFPictureData var2 = (XSSFPictureData)var1;
         return ImageUtils.getImageDimension(var2.getPackagePart().getInputStream(), var2.getPictureType());
      }
   }

   private void setColumnAnchor(Sheet var1, ClientAnchor var2, double var3, float var5, HorizontalAlignment var6) {
      double var7 = 0.0;
      float var9 = 0.0F;
      float var10 = 0.0F;
      int var11 = var1.getDefaultColumnWidth() * 256;
      int var12 = var2.getCol1();
      int var13 = var2.getCol2();
      int var14 = var2.getDx1();
      int var15 = var2.getDx2();
      int var16 = 0;
      int var17 = var13;
      double var18 = (double)var5 - var3;

      label54:
      for (double var20 = var18 / 2.0; var12 <= var17; var12++) {
         if (!this.bIsExcel2007) {
            var16 = var1.getColumnWidth(var12);
            var10 = var16 == var11 ? 32.0F : 36.56F;
            var9 = (float)var16 / var10;
         } else {
            var9 = (float)var1.getColumnWidth(var12) / 32.0F;
         }

         var7 += (double)var9;
         if (!(var7 < var20)) {
            if (!this.bIsExcel2007) {
               var14 = (int)Math.round((var20 - (var7 - (double)var9)) / (double)var9 * 1024.0);
            } else {
               var14 = (int)Math.round(9525.0 * (var20 - (var7 - (double)var9)));
            }

            var13 = var12;

            for (double var27 = var20 + var3; var13 <= var17; var7 += (double)var9) {
               if (var7 >= var27) {
                  if (!this.bIsExcel2007) {
                     var15 = (int)Math.round(((double)var9 - (var7 - var27)) / (double)var9 * 1024.0);
                  } else {
                     var15 = (int)Math.round(9525.0 * ((double)var9 - (var7 - var27)));
                  }
                  break label54;
               }

               var13++;
               if (!this.bIsExcel2007) {
                  var16 = var1.getColumnWidth(var13);
                  var10 = var16 == var11 ? 32.0F : 36.56F;
                  var9 = (float)var16 / var10;
               } else {
                  var9 = (float)var1.getColumnWidth(var13) / 32.0F;
               }
            }
            break;
         }
      }

      var2.setCol1(var12);
      var2.setCol2(var13);
      var2.setDx1(var14);
      var2.setDx2(var15);
   }

   private void setRowAnchor(Sheet var1, ClientAnchor var2, double var3, float var5, VerticalAlignment var6) {
      Row var7 = null;
      double var8 = 0.0;
      int var10 = var2.getRow1();
      int var11 = var2.getRow2();
      int var12 = var2.getDy1();
      int var13 = var2.getDy2();
      int var14 = var11;
      double var15 = (double)var5 - var3;

      label57:
      for (double var17 = var15 / 2.0; var10 <= var14; var10++) {
         float var19 = 0.0F;
         var7 = var1.getRow(var10);
         if (!this.bIsExcel2007) {
            if (var7 != null) {
               var19 = (float)(var7.getHeight() / 15);
            } else {
               var19 = (float)(var1.getDefaultRowHeight() / 15);
            }
         } else if (var7 != null) {
            var19 = var7.getHeightInPoints() * 96.0F / 72.0F;
         } else {
            var19 = var1.getDefaultRowHeightInPoints() * 96.0F / 72.0F;
         }

         var8 += (double)var19;
         if (!(var8 < var17)) {
            if (!this.bIsExcel2007) {
               var12 = (int)Math.round((var17 - (var8 - (double)var19)) / (double)var19 * 256.0);
            } else {
               var12 = (int)Math.round(9525.0 * (var17 - (var8 - (double)var19)));
            }

            var11 = var10;

            for (double var22 = var17 + var3; var11 <= var14; var8 += (double)var19) {
               if (var8 >= var22) {
                  if (!this.bIsExcel2007) {
                     var13 = (int)Math.round(((double)var19 - (var8 - var22)) / (double)var19 * 256.0);
                  } else {
                     var13 = (int)Math.round(9525.0 * ((double)var19 - (var8 - var22)));
                  }
                  break label57;
               }

               var7 = var1.getRow(++var11);
               if (!this.bIsExcel2007) {
                  if (var7 != null) {
                     var19 = (float)(var7.getHeight() / 15);
                  } else {
                     var19 = (float)(var1.getDefaultRowHeight() / 15);
                  }
               } else if (var7 != null) {
                  var19 = var7.getHeightInPoints() * 96.0F / 72.0F;
               } else {
                  var19 = var1.getDefaultRowHeightInPoints() * 96.0F / 72.0F;
               }
            }
            break;
         }
      }

      var2.setRow1(var10);
      var2.setRow2(var11);
      var2.setDy1(var12);
      var2.setDy2(var13);
   }

   private void resize(Sheet var1, Picture var2, ClientAnchor var3, float var4, float var5, int var6, CellStyle var7) throws IOException {
      if (!this.bIsExcel2007) {
         var3.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
      }

      Dimension var8 = this.getImageDimension(var2.getPictureData());
      BigDecimal var9 = BigDecimal.valueOf(var8.getHeight());
      BigDecimal var10 = BigDecimal.valueOf((double)(var5 - 2.0F));
      BigDecimal var11 = BigDecimal.valueOf(var8.getWidth());
      BigDecimal var12 = BigDecimal.valueOf((double)(var4 - 2.0F));
      BigDecimal var13 = BigDecimal.ONE;
      BigDecimal var14 = BigDecimal.ONE;
      BigDecimal var15 = BigDecimal.ONE;
      if (var9.compareTo(var10) != 0) {
         var14 = var10.divide(var9, 5, 1);
      }

      if (var11.compareTo(var12) != 0) {
         var15 = var12.divide(var11, 5, 1);
      }

      if (var14.compareTo(BigDecimal.ONE) != 0 && var15.compareTo(BigDecimal.ONE) != 0) {
         if (var14.compareTo(BigDecimal.ONE) == 1 && var15.compareTo(BigDecimal.ONE) == 1) {
            var13 = var14.compareTo(var15) == 1 ? var15 : var14;
         } else if (var14.compareTo(BigDecimal.ONE) == -1 && var15.compareTo(BigDecimal.ONE) == -1) {
            var13 = var14.compareTo(var15) == 1 ? var15 : var14;
         } else if (var15.compareTo(BigDecimal.ONE) == -1) {
            var13 = var15;
         } else if (var14.compareTo(BigDecimal.ONE) == -1) {
            var13 = var14;
         }
      } else if (var14.compareTo(BigDecimal.ONE) == -1) {
         var13 = var14;
      } else if (var15.compareTo(BigDecimal.ONE) == -1) {
         var13 = var15;
      }

      this.getPreferredSize(var1, var2, var3, var11.multiply(var13).doubleValue(), var9.multiply(var13).doubleValue(), var4, var5, var7);
   }

   private InputStream getDecryptedStream(InputStream var1, String var2) throws Exception {
      if (!var1.markSupported()) {
         var1 = new PushbackInputStream((InputStream)var1, 8);
      }

      if (FileMagic.valueOf((InputStream)var1) == FileMagic.OLE2) {
         POIFSFileSystem var3 = new POIFSFileSystem((InputStream)var1);
         if (var3.getRoot().hasEntry("EncryptionInfo")) {
            EncryptionInfo var4 = new EncryptionInfo(var3);
            Decryptor var5 = Decryptor.getInstance(var4);
            if (!var5.verifyPassword(var2)) {
               this.setErrorMessage("Unable to process: document is encrypted");
               return null;
            } else {
               return var5.getDataStream(var3);
            }
         } else {
            var3.close();
            return (InputStream)var1;
         }
      } else {
         return null;
      }
   }

   private Map<Integer, Integer> getUserHeight(VariableList var1) {
      String var2 = var1.getString("rowheight");
      if (var2 != null && var2.length() > 0) {
         HashMap var3 = new HashMap();
         String[] var4 = var2.split("\\,");

         for (String var8 : var4) {
            String[] var9 = var8.split("\\:");
            if (var9.length >= 2) {
               var3.put(Integer.parseInt(var9[0].trim()), Integer.parseInt(var9[1].trim()));
            }
         }

         return var3;
      } else {
         return null;
      }
   }

   private void setUserHeight(Sheet var1, Map<Integer, Integer> var2) {
      for (Integer var5 : var2.keySet()) {
         Row var6 = var1.getRow(var5);
         if (var6 != null) {
            Integer var7 = (Integer)var2.get(var5);
            if (var7 != null) {
               var6.setHeight((short)(var7 * 15));
            }
         }
      }
   }

   class CellDataObject {
      private DataSet dsCellData = null;
      private List<Map<String, Object>> cellDataList = null;

      public CellDataObject(DataSet var2) {
         this.dsCellData = var2;
         this.cellDataList = null;
      }

      public CellDataObject(List<Map<String, Object>> var2) {
         this.dsCellData = null;
         this.cellDataList = var2;
      }

      public int getColumnCount() {
         return this.dsCellData != null ? this.dsCellData.getColumnCount() : this.cellDataList.get(0).keySet().size();
      }

      public int getRowCount() {
         return this.dsCellData != null ? this.dsCellData.getRowCount() : this.cellDataList.size();
      }

      public String getCellData(int var1, String var2) {
         if (this.dsCellData != null) {
            return this.dsCellData.getString(var1, var2);
         } else {
            Object var3 = this.cellDataList.get(var1).get(var2);
            return var3 != null ? var3.toString() : null;
         }
      }
   }
}
