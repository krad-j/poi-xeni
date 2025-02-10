package com.nexacro17.xeni.data.exportformats;

import java.util.ArrayList;
import java.util.List;

public class ExportFormat {
   private String id = null;
   private List<FormatColumn> columns = new ArrayList<>();
   private List<FormatRow> rows = new ArrayList<>();
   private List<FormatCell> head = new ArrayList<>();
   private List<FormatCell> body = new ArrayList<>();
   private List<FormatCell> summary = new ArrayList<>();
   protected static final int DATA_BAND_HEAD = 3;
   protected static final int DATA_BAND_BODY = 4;
   protected static final int DATA_BAND_SUMMARY = 5;

   public String getId() {
      return this.id;
   }

   public void setId(String var1) {
      this.id = var1;
   }

   public void addColumn(String var1) {
      FormatColumn var2 = new FormatColumn();
      var2.setSize(var1);
      this.columns.add(var2);
   }

   public FormatColumn getColumn(int var1) {
      return this.columns.get(var1);
   }

   public int getColumnCount() {
      return this.columns.size();
   }

   public void addRow(String var1, String var2) {
      FormatRow var3 = new FormatRow();
      var3.setSize(var1);
      var3.setBand(var2 == null ? "body" : var2);
      this.rows.add(var3);
   }

   public FormatRow getRow(int var1) {
      return this.rows.get(var1);
   }

   public int getRowCount() {
      return this.rows.size();
   }

   public int getRowCountOfEachBand(String var1) {
      int var2 = 0;

      for (int var3 = 0; var3 < this.rows.size(); var3++) {
         FormatRow var4 = this.rows.get(var3);
         if (var4 != null && var4.getBand().equals(var1)) {
            var2++;
         }
      }

      return var2;
   }

   public void addCell(
      int var1,
      String var2,
      String var3,
      String var4,
      String var5,
      String var6,
      String var7,
      String var8,
      String var9,
      String var10,
      String var11,
      String var12,
      String var13
   ) {
      FormatCell var14 = new FormatCell();
      if (var2 == null) {
         var14.setRow("0");
      } else {
         var14.setRow(var2);
      }

      if (var3 == null) {
         var14.setCol("0");
      } else {
         var14.setCol(var3);
      }

      if (var4 == null) {
         var14.setRowspan("1");
      } else {
         var14.setRowspan(var4);
      }

      if (var5 == null) {
         var14.setColspan("1");
      } else {
         var14.setColspan(var5);
      }

      if (var6 == null) {
         var14.setStyle("");
      } else {
         var14.setStyle(var6);
      }

      if (var7 == null) {
         var14.setStyle1("");
      } else {
         var14.setStyle1(var7);
      }

      if (var8 == null) {
         var14.setStyle2("");
      } else {
         var14.setStyle2(var8);
      }

      if (var9 == null) {
         var14.setText("");
      } else {
         var14.setText(var9);
      }

      if (var13 == null) {
         var14.setType("");
      } else {
         var14.setType(var13);
      }

      if (var1 == 3) {
         this.head.add(var14);
      } else if (var1 == 4) {
         this.body.add(var14);
      } else if (var1 == 5) {
         this.summary.add(var14);
      }
   }

   public int getHeadCellCount() {
      return this.head.size();
   }

   public FormatCell getHeadCell(int var1) {
      return this.head.get(var1);
   }

   public int getBodyCellCount() {
      return this.body.size();
   }

   public FormatCell getBodyCell(int var1) {
      return this.body.get(var1);
   }

   public int getSummaryCellCount() {
      return this.summary.size();
   }

   public FormatCell getSummary(int var1) {
      return this.summary.get(var1);
   }
}
