package com.nexacro17.xeni.data.exportformats;

public class FormatRow {
   private String size = null;
   private String band = null;

   public String getSize() {
      return this.size;
   }

   public void setSize(String var1) {
      this.size = var1;
   }

   public String getBand() {
      if (this.band == null) {
         this.band = "body";
      }

      return this.band;
   }

   public void setBand(String var1) {
      this.band = var1;
   }
}
