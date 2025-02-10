package com.nexacro17.xeni.data;

import java.util.HashMap;

public class GridExportDataFactory {
   private static GridExportDataFactory INSTANCE = null;
   private static HashMap<String, GridExportData> HASHMAP = new HashMap<>();

   private GridExportDataFactory() {
   }

   public static synchronized GridExportDataFactory getExportDataFactoryInstance() {
      if (INSTANCE == null) {
         INSTANCE = new GridExportDataFactory();
      }

      return INSTANCE;
   }

   public HashMap<String, GridExportData> getExportDataFactory() {
      return HASHMAP;
   }
}
