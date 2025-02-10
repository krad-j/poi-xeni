package com.nexacro17.xeni.export;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xeni.data.GridExportData;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public interface GridExportBase {
   void setExportType(String var1);

   void setExportData(GridExportData var1);

   String getExportFileUrl();

   int executeExport(VariableList var1, boolean var2, HttpServletResponse var3, VariableList var4);

   String getErrorMessage();

   void setExportFilePath(String var1, String var2, boolean var3);

   void receiveExportData(DataSetList var1);

   boolean isLastExportData(DataSetList var1);

   DataSet getResponseCommand();

   int createWorkbook();

   int createWorkbook(boolean var1);

   int appendBody(DataSet var1);

   int appendBody(List<Map<String, Object>> var1);

   DataSet disposeWorkbook();

   DataSet disposeWorkbook(VariableList var1);

   int disposeWorkbook(VariableList var1, HttpServletResponse var2);
}
