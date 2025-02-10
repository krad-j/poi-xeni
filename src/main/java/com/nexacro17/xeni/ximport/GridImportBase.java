package com.nexacro17.xeni.ximport;

import java.io.InputStream;
import org.apache.poixeni.openxml4j.opc.OPCPackage;
import org.apache.poixeni.poifs.filesystem.POIFSFileSystem;

public interface GridImportBase {
   void setOPCPackage(OPCPackage var1);

   void setPOIFileSystem(POIFSFileSystem var1);

   void setInputStream(InputStream var1);

   void initialize(GridImportContext var1);

   int executeImport();

   void setPartIndex(int var1);

   int getPartIndex();

   void setErrorMessage(String var1);

   String getErrorMessage();

   int getErrorCode();

   void setErrorCode(int var1);
}
