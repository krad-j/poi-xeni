package com.nexacro17.xeni.export;

import com.nexacro17.xeni.export.impl.GridExportCsv;
import com.nexacro17.xeni.export.impl.GridExportExcel;

public class GridExportTypeFactory {
   private GridExportTypeFactory() {
   }

   public static GridExportBase getGridExporter(int var0) {
      if (var0 == 256 || var0 == 272 || var0 == 288 || var0 == 1024 || var0 == 1040) {
         return new GridExportExcel();
      } else {
         return var0 != 1280 && var0 != 1296 ? null : new GridExportCsv();
      }
   }
}
