package com.nexacro17.xeni.data.importformats;

import java.util.ArrayList;
import java.util.List;

public class ImportFormat {
   private List<ImportSheet> sheets = new ArrayList<>();

   public int getSheetCount() {
      return this.sheets.size();
   }

   public ImportSheet getSheet(int var1) {
      return this.sheets.get(var1);
   }

   public void addSheet(String var1, String var2, String var3, String var4) {
      ImportSheet var5 = new ImportSheet();
      var5.setCommand(var1);
      var5.setOutput(var2);
      var5.setHead(var3);
      var5.setBody(var4);
      this.sheets.add(var5);
   }
}
