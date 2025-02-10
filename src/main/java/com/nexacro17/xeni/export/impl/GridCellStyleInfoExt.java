package com.nexacro17.xeni.export.impl;

public class GridCellStyleInfoExt {
   private String sStyleName = null;
   private String sDataFormat = null;
   private int iRowSuppressCount = 1;
   private int iColSuppressCount = 1;
   private short nStyleIndex = -1;
   private boolean bImageData = false;
   private boolean bPercentage = false;
   private int iDataType = 0;
   private int iImageStretch = 1;

   public void addStyle(String var1, short var2) {
      this.sStyleName = var1;
      this.nStyleIndex = var2;
   }

   public String getStyleName() {
      return this.sStyleName;
   }

   public short getStyleIndex() {
      return this.nStyleIndex;
   }

   public boolean isImageData() {
      return this.bImageData;
   }

   public void setImageData(boolean var1) {
      this.bImageData = var1;
   }

   public int getImageStretch() {
      return this.iImageStretch;
   }

   public void setImageStretch(String var1) {
      if ("fit".equals(var1)) {
         this.iImageStretch = 0;
      } else if ("none".equals(var1)) {
         this.iImageStretch = 2;
      }
   }

   public String getDataFormat() {
      return this.sDataFormat;
   }

   public void setDataFormat(String var1) {
      this.sDataFormat = var1;
   }

   public void setDataType(int var1) {
      this.iDataType = var1;
   }

   public int getDataType() {
      return this.iDataType;
   }

   public boolean isbPercentage() {
      return this.bPercentage;
   }

   public void setbPercentage(boolean var1) {
      this.bPercentage = var1;
   }

   public int getiRowSuppressCount() {
      return this.iRowSuppressCount;
   }

   public void setiRowSuppressCount(int var1) {
      this.iRowSuppressCount = var1;
   }

   public int getiColSuppressCount() {
      return this.iColSuppressCount;
   }

   public void setiColSuppressCount(int var1) {
      this.iColSuppressCount = var1;
   }
}
