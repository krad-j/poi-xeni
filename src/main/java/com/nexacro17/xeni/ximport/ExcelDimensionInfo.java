package com.nexacro17.xeni.ximport;

public class ExcelDimensionInfo {
   private int startColumn = -1;
   private int endColumn = -1;
   private int startRow = -1;
   private int endRow = -1;
   private int userStartColumn = -1;
   private int userEndColumn = -1;
   private int userStartRow = -1;
   private int userEndRow = -1;

   public int getStartColumn() {
      return this.startColumn;
   }

   public void setStartColumn(int var1) {
      this.startColumn = var1;
   }

   public int getEndColumn() {
      return this.endColumn;
   }

   public void setEndColumn(int var1) {
      this.endColumn = var1;
   }

   public int getStartRow() {
      return this.startRow;
   }

   public void setStartRow(int var1) {
      this.startRow = var1;
   }

   public int getEndRow() {
      return this.endRow;
   }

   public void setEndRow(int var1) {
      this.endRow = var1;
   }

   public int getUserStartColumn() {
      return this.userStartColumn;
   }

   public void setUserStartColumn(int var1) {
      this.userStartColumn = var1;
   }

   public int getUserEndColumn() {
      return this.userEndColumn;
   }

   public void setUserEndColumn(int var1) {
      this.userEndColumn = var1;
   }

   public int getUserStartRow() {
      return this.userStartRow;
   }

   public void setUserStartRow(int var1) {
      this.userStartRow = var1;
   }

   public int getUserEndRow() {
      return this.userEndRow;
   }

   public void setUserEndRow(int var1) {
      this.userEndRow = var1;
   }

   public boolean isUserDimension() {
      return this.userStartColumn >= 0 || this.userEndColumn >= 0 || this.userStartRow >= 0 || this.userEndRow >= 0;
   }
}
