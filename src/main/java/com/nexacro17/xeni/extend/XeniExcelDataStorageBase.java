package com.nexacro17.xeni.extend;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.VariableList;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

public interface XeniExcelDataStorageBase {
   InputStream loadTargetStream(String var1) throws Exception;

   String saveImportStream(VariableList var1, InputStream var2, String var3) throws Exception;

   int saveExportStream(VariableList var1, DataSet var2, ByteArrayOutputStream var3, String var4, String var5, HttpServletResponse var6) throws Exception;

   DataSet saveExportStream(VariableList var1, DataSet var2, ByteArrayOutputStream var3, String var4, String var5) throws Exception;
}
