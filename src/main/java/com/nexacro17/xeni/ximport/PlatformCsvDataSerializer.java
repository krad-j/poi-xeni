package com.nexacro17.xeni.ximport;

import com.nexacro17.xapi.data.ColumnHeader;
import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.DataSetList;
import com.nexacro17.xapi.data.DataTypes;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.Variable;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.DataSerializer;
import com.nexacro17.xapi.tx.DataTypeChanger;
import com.nexacro17.xapi.tx.PlatformException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlatformCsvDataSerializer implements DataSerializer {
   private Log log = LogFactory.getLog(PlatformCsvDataSerializer.class);
   private char etx = 3;
   private String CR = "\r\n";
   private static final int BUFFER_SIZE = 4096;
   private byte[] buffer;

   public void setProperty(String var1, Object var2) {
   }

   public void writeData(OutputStream var1, PlatformData var2, DataTypeChanger var3, String var4) throws PlatformException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("Writing data: this=" + this + ", charset=" + var4);
      }

      try {
         this.writeData(new OutputStreamWriter(var1, var4), var2, var3, var4);
      } catch (UnsupportedEncodingException var6) {
         if (this.log.isErrorEnabled()) {
            this.log.error("Unsupported charset: " + var4, var6);
         }

         throw new PlatformException("Unsupported charset: " + var4, var6);
      }
   }

   public void writeData(Writer var1, PlatformData var2, DataTypeChanger var3, String var4) throws PlatformException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("Writing data: this=" + this + ", charset=" + var4);
      }

      try {
         this.write(var1, var2, var3, var4);
      } catch (IOException var7) {
         String var6 = "PlatformSsv";
         if (this.log.isErrorEnabled()) {
            this.log.error("Could not serialize: contentType=" + var6, var7);
         }

         throw new PlatformException("Could not serialize: contentType=" + var6, var7);
      }
   }

   private void write(Writer var1, PlatformData var2, DataTypeChanger var3, String var4) throws IOException {
      if (this.log.isTraceEnabled()) {
         this.log.trace("write(Writer, PlatformData, DataTypeChanger, String): started");
      }

      this.writeHeader(var1, var2, var4);
      this.writeVariableList(var1, var2, var3);
      this.writeDataSetList(var1, var2, var3);
      var1.flush();
      if (this.log.isTraceEnabled()) {
         this.log.trace("write(Writer, PlatformData, DataTypeChanger, String): finished");
      }
   }

   private void writeHeader(Writer var1, PlatformData var2, String var3) throws IOException {
      if (var3 == null) {
         var1.write("CSV");
      } else {
         var1.write("CSV:" + var3);
      }
   }

   private void writeVariableList(Writer var1, PlatformData var2, DataTypeChanger var3) throws IOException {
      VariableList var4 = var2.getVariableList();
      int var5 = var4.size();
      if (this.log.isDebugEnabled()) {
         this.log.debug("Writing VariableList: count=" + var5);
      }

      if (var5 > 0) {
         var1.write(this.CR);

         for (int var6 = 0; var6 < var5; var6++) {
            if (var6 > 0) {
               var1.write(44);
            }

            this.writeVariable(var1, var4.get(var6), var3);
         }
      }
   }

   private void writeVariable(Writer var1, Variable var2, DataTypeChanger var3) throws IOException {
      if (DataTypes.isBinary(var2.getType())) {
         this.writeBinaryVariable(var1, var2);
      } else {
         this.writeStringVariable(var1, var2);
      }
   }

   private void writeStringVariable(Writer var1, Variable var2) throws IOException {
      String var3 = var2.getName();
      String var4 = this.getDataType(var2.getType());
      String var5 = this.getStringValue(var2);
      int var6 = var5 == null ? 0 : var5.length();
      StringBuffer var7 = new StringBuffer(var6 + 32);
      if (var5 == null) {
         var7.append(var3).append('=');
      } else {
         var7.append(var3).append('=').append(var5);
      }

      var1.write(var7.toString());
      if (this.log.isTraceEnabled()) {
         this.log.trace("writeString(Writer, Variable): name=" + var3 + ", type=" + var4 + ", value=" + var5);
      }
   }

   private void writeBinaryVariable(Writer var1, Variable var2) throws IOException {
      String var3 = var2.getName();
      String var4 = this.getDataType(var2.getType());
      byte[] var5 = this.getBinaryValue(var2);
      if (var5 == null) {
         StringBuffer var6 = new StringBuffer(32);
         var6.append(var3).append('=');
         var1.write(var6.toString());
      } else {
         String var9 = Base64.encodeBase64String(var5);
         int var7 = var9 == null ? 0 : var9.length();
         StringBuffer var8 = new StringBuffer(var7 + 32);
         var8.append(var3).append('=');
         if (var9 != null) {
            var8.append(var9);
         }

         var1.write(var8.toString());
      }
   }

   private String getStringValue(Variable var1) {
      int var2 = var1.getType();
      if (var2 == 4) {
         return var1.getBoolean() ? "1" : "0";
      } else {
         return var1.getString();
      }
   }

   private byte[] getBinaryValue(Variable var1) {
      return var1.getBlob();
   }

   private void writeDataSetList(Writer var1, PlatformData var2, DataTypeChanger var3) throws IOException {
      DataSetList var4 = var2.getDataSetList();
      int var5 = var4.size();
      if (this.log.isDebugEnabled()) {
         this.log.debug("Writing DataSetList: count=" + var5);
      }

      for (int var6 = 0; var6 < var5; var6++) {
         if (var6 > 0) {
            var1.write(this.CR);
         }

         this.writeDataSet(var1, var2, var4.get(var6), var3);
      }
   }

   private void writeDataSet(Writer var1, PlatformData var2, DataSet var3, DataTypeChanger var4) throws IOException {
      String var5 = var3.getName();
      String var6 = var3.getAlias();
      if (this.log.isDebugEnabled()) {
         this.log
            .debug(
               "Writing DataSet: name="
                  + var5
                  + ", alias="
                  + var6
                  + ", columnCount="
                  + var3.getColumnCount()
                  + ", rowCount="
                  + var3.getRowCount()
                  + ", removedRowCount="
                  + var3.getRemovedRowCount()
            );
      }

      var1.write(this.CR);
      var1.write("Dataset:" + var6);
      int[] var7 = this.getTargetDataTypes(var3, var4);
      this.writeColumns(var1, var3, var7);
      this.writeRows(var1, var2, var3, var7);
   }

   private void writeColumns(Writer var1, DataSet var2, int[] var3) throws IOException {
      int var4 = var2.getColumnCount();
      StringBuffer var5 = new StringBuffer();

      for (int var6 = 0; var6 < var4; var6++) {
         ColumnHeader var7 = var2.getColumn(var6);
         int var8 = var3[var6];
         if (var6 > 0) {
            var5.append(',');
         }

         if (var7.getType() == 0) {
            this.writeDefaultColumnHeader(var5, var7, var8);
         } else if (var7.getType() != 1) {
            throw new IOException("Invalid column type: " + var7.getType());
         }
      }

      if (var5.length() > 0) {
         var1.write(this.CR);
         var1.write(var5.toString());
      }
   }

   private void writeDefaultColumnHeader(StringBuffer var1, ColumnHeader var2, int var3) throws IOException {
      String var4 = var2.getName();
      String var5 = this.getDataType(var3);
      int var6 = var2.getDataSize();
      var1.append(var4);
      var1.append(':');
      var1.append(var5);
      var1.append('(');
      var1.append(var6);
      var1.append(')');
   }

   private void writeRows(Writer var1, PlatformData var2, DataSet var3, int[] var4) throws IOException {
      int var5 = var3.getSaveType();
      if (var5 == 0) {
         var5 = var2.getSaveType();
      }

      if (var5 == 0) {
         var5 = 2;
      }

      int var6 = var3.getRowCount();
      if (this.log.isDebugEnabled()) {
         this.log.debug("Writing rows: count=" + var6);
      }

      boolean var7 = var3.isCheckingGetterDataIndex();
      if (var7) {
         var3.setCheckingGetterDataIndex(false);
      }

      for (int var8 = 0; var8 < var6; var8++) {
         this.writeRow(var1, var3, var4, var8, var5);
      }

      if (var7) {
         var3.setCheckingGetterDataIndex(true);
      }
   }

   private void writeRow(Writer var1, DataSet var2, int[] var3, int var4, int var5) throws IOException {
      int var6 = var2.getRowType(var4);
      if ((var4 % 10 == 0 || var4 == var2.getRowCount() - 1) && this.log.isTraceEnabled()) {
         this.log.trace("writeRow(Writer, DataSet, int[], int, int): row=" + var4 + ", saveType=" + var5 + ", rowType=" + var6);
      }

      if (var5 != 1) {
         if (var5 == 2) {
            if (var6 == 3) {
               return;
            }
         } else if (var5 == 3) {
            if (var6 == 0 || var6 == 3) {
               return;
            }
         } else if (var5 == 4) {
            if (var6 == 0 || var6 == 1 || var6 == 2) {
               return;
            }
         } else if (var5 == 5 && var6 == 0) {
            return;
         }
      }

      StringBuffer var7 = new StringBuffer();
      int var8 = var2.getColumnCount();

      for (int var9 = 0; var9 < var8; var9++) {
         ColumnHeader var10 = var2.getColumn(var9);
         if (!var10.isConstant()) {
            if (var9 > 0) {
               var7.append(',');
            }

            int var11 = var10.getDataType();
            int var12 = var3[var9];
            if (var12 != 13 && !DataTypes.isBinary(var12)) {
               String var15 = var11 == 13 ? this.getStringValueFromFile(var2, var4, var9, var12) : this.getStringValue(var2, var4, var9);
               if (var15 != null) {
                  var7.append(var15);
               }
            } else {
               byte[] var13 = var11 == 13 ? this.getBinaryValueFromFile(var2, var4, var9, var12) : this.getBinaryValue(var2, var4, var9);
               if (var13 != null) {
                  String var14 = Base64.encodeBase64String(var13);
                  var7.append(var14);
               }
            }
         }
      }

      var1.write(this.CR);
      var1.write(var7.toString());
   }

   private byte[] getBinaryValueFromFile(DataSet var1, int var2, int var3, int var4) throws IOException {
      if (var4 != 12 && var4 != 13) {
         return null;
      } else {
         String var5 = var1.getString(var2, var3);
         byte[] var6 = this.loadFile(var5);
         if (this.log.isDebugEnabled()) {
            this.log.debug("Loading data from file: filename=" + var5 + ", content=" + var6);
         }

         return var6;
      }
   }

   private String getStringValueFromFile(DataSet var1, int var2, int var3, int var4) throws IOException {
      if (var4 == 2) {
         String var5 = var1.getString(var2, var3);
         byte[] var6 = this.loadFile(var5);
         if (this.log.isDebugEnabled()) {
            this.log.debug("Loading data from file: filename=" + var5 + ", content=" + var6);
         }

         return var6 == null ? null : new String(var6);
      } else {
         return null;
      }
   }

   private byte[] loadFile(String var1) throws IOException {
      if (var1 == null) {
         return null;
      } else {
         File var2 = new File(var1);
         if (!var2.canRead()) {
            return null;
         } else {
            FileInputStream var3 = new FileInputStream(var2);
            ByteArrayOutputStream var4 = new ByteArrayOutputStream();
            byte[] var5 = this.getBuffer();

            try {
               while (true) {
                  int var6 = var3.read(var5);
                  if (var6 == -1) {
                     var4.close();
                     return var4.toByteArray();
                  }

                  var4.write(var5, 0, var6);
               }
            } finally {
               var3.close();
            }
         }
      }
   }

   private byte[] getBuffer() {
      if (this.buffer == null) {
         this.buffer = new byte[4096];
      }

      return this.buffer;
   }

   private byte[] getBinaryValue(DataSet var1, int var2, int var3) {
      return var1.getBlob(var2, var3);
   }

   private String getStringValue(DataSet var1, int var2, int var3) {
      int var4 = var1.getColumn(var3).getDataType();
      if (var4 == 4) {
         return var1.getBoolean(var2, var3) ? "1" : "0";
      } else {
         return var1.getString(var2, var3);
      }
   }

   private int[] getTargetDataTypes(DataSet var1, DataTypeChanger var2) {
      int var3 = var1.getColumnCount();
      int[] var4 = new int[var3];

      for (int var5 = 0; var5 < var3; var5++) {
         ColumnHeader var6 = var1.getColumn(var5);
         if (var2 == null) {
            var4[var5] = var6.getDataType();
         } else {
            String var7 = var1.getAlias();
            String var8 = var6.getName();
            int var9 = var6.getDataType();
            var4[var5] = var2.getDataType(var7, var8, var9);
         }
      }

      return var4;
   }

   private String getDataType(int var1) {
      if (var1 == 4) {
         return DataTypes.toStringType(3);
      } else if (var1 == 5) {
         return DataTypes.toStringType(8);
      } else if (var1 == 7) {
         return DataTypes.toStringType(6);
      } else {
         return var1 == 13 ? DataTypes.toStringType(12) : DataTypes.toStringType(var1);
      }
   }
}
