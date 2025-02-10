package com.nexacro17.xeni.data.exportformats;

public class FormatCell {
   private String row = null;
   private String col = null;
   private String rowspan = null;
   private String colspan = null;
   private String style = null;
   private String style1 = null;
   private String style2 = null;
   private String text = null;
   private String displaytype = null;
   private String edittype = null;
   private String combodisplay = null;
   private String type = null;

   public String getCol() {
      if (this.col == null) {
         this.col = "0";
      }

      return this.col;
   }

   public void setCol(String var1) {
      this.col = var1;
   }

   public String getDisplaytype() {
      if (this.displaytype == null) {
         this.displaytype = "normal";
      }

      return this.displaytype;
   }

   public void setDisplaytype(String var1) {
      this.displaytype = var1;
   }

   public String getEdittype() {
      if (this.edittype == null) {
         this.edittype = "none";
      }

      return this.edittype;
   }

   public void setEdittype(String var1) {
      this.edittype = var1;
   }

   public String getStyle() {
      if (this.style == null) {
         this.style = "";
      }

      return this.style;
   }

   public void setStyle(String var1) {
      this.style = var1;
   }

   public String getStyle1() {
      if (this.style1 == null) {
         this.style1 = "";
      }

      return this.style1;
   }

   public void setStyle1(String var1) {
      this.style1 = var1;
   }

   public String getStyle2() {
      if (this.style2 == null) {
         this.style2 = "";
      }

      return this.style2;
   }

   public void setStyle2(String var1) {
      this.style2 = var1;
   }

   public String getText() {
      if (this.text == null) {
         this.text = "";
      }

      return this.text;
   }

   public void setText(String var1) {
      this.text = var1;
   }

   public String getCombodisplay() {
      if (this.combodisplay == null) {
         this.combodisplay = "edit";
      }

      return this.combodisplay;
   }

   public void setCombodisplay(String var1) {
      this.combodisplay = var1;
   }

   public String getColspan() {
      if (this.colspan == null) {
         this.colspan = "1";
      }

      return this.colspan;
   }

   public void setColspan(String var1) {
      this.colspan = var1;
   }

   public String getRow() {
      if (this.row == null) {
         this.row = "0";
      }

      return this.row;
   }

   public void setRow(String var1) {
      this.row = var1;
   }

   public String getRowspan() {
      if (this.rowspan == null) {
         this.rowspan = "1";
      }

      return this.rowspan;
   }

   public void setRowspan(String var1) {
      this.rowspan = var1;
   }

   public String getType() {
      if (this.type == null) {
         this.type = "";
      }

      return this.type;
   }

   public void setType(String var1) {
      this.type = var1;
   }
}
