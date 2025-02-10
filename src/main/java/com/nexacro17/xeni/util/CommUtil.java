package com.nexacro17.xeni.util;

import com.nexacro17.xapi.data.DataSet;
import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.data.VariableList;
import com.nexacro17.xapi.tx.DataSerializer;
import com.nexacro17.xapi.tx.PlatformException;
import com.nexacro17.xapi.tx.impl.PlatformSsvDataSerializer;
import com.nexacro17.xeni.ximport.GridImportContext;
import com.nexacro17.xeni.ximport.PlatformCsvDataSerializer;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;

public class CommUtil {
   public static final int INDEX_INIT = 0;
   public static final int INDEX_STARTED = 1;
   public static final int INDEX_HEAD = 2;
   public static final int INDEX_VARIABLE = 3;
   public static final int INDEX_DATASET = 4;
   public static final int INDEX_TAIL = 5;
   public static final int INDEX_ENDED = 6;

   public static String generateSerialNo() {
      SimpleDateFormat var0 = new SimpleDateFormat("yyMMddHHmmssSSS");
      String var1 = var0.format(new Date(System.currentTimeMillis()));
      int var2 = new Random().nextInt(100);
      return String.format("%s%02d", var1, var2);
   }

   public static boolean isNumber(String var0) {
      if (var0.length() < 1) {
         return false;
      } else {
         for (int var1 = 0; var1 < var0.length(); var1++) {
            if (!Character.isDigit(var0.charAt(var1))) {
               return false;
            }
         }

         return true;
      }
   }

   public static String getDataRange(String var0, int[] var1, int[] var2) {
      if (var0 != null && !"".equals(var0)) {
         String[] var3 = var0.split("\\!");
         String var4 = var3[0];
         if (var3.length > 1 && !"".equals(var3[1])) {
            String[] var5 = var3[1].split("\\:");
            getRangeIndex(var5[0], var1);
            if (var5.length > 1) {
               getRangeIndex(var5[1], var2);
            }
         }

         return var4;
      } else {
         return null;
      }
   }

   public static void getRangeIndex(String var0, int[] var1) {
      String var2 = "";
      String var3 = "";

      for (int var4 = 0; var4 < var0.length(); var4++) {
         char var5 = var0.charAt(var4);
         if (Character.isDigit(var5)) {
            var3 = var0.substring(var4);
            break;
         }

         var2 = var2 + var5;
      }

      if (!"".equals(var2)) {
         var1[0] = getColumnIndexFormChars(var2);
      }

      if (!"".equals(var3)) {
         var1[1] = Integer.parseInt(var3) - 1;
      }
   }

   public static int getColumnIndexFormChars(String var0) {
      int var1 = 0;
      byte var2 = 1;
      char[] var3 = var0.toUpperCase().toCharArray();

      for (int var4 = var3.length - 1; var4 >= 0; var4--) {
         var1 += (var3[var4] - 'A' + 1) * var2;
         var2 *= 26;
      }

      return var1 - 1;
   }

   public static DataSet getDatasetImportResponse() {
      DataSet var0 = new DataSet("IMPORTFILES");
      var0.addColumn("filename", 2);
      var0.addColumn("filesize", 5);
      var0.addColumn("filetype", 2);
      var0.addColumn("filepath", 2);
      var0.addColumn("importid", 2);
      var0.newRow();
      return var0;
   }

   public static DataSet getDatasetExportResponse(DataSet var0) {
      DataSet var1 = new DataSet("RESPONSE");
      var1.addColumn("command", 2, 32);
      var1.addColumn("type", 3);
      var1.addColumn("item", 2, 256);
      var1.addColumn("instanceid", 2, 256);
      var1.addColumn("lastseq", 3);
      var1.addColumn("eof", 4);
      var1.addColumn("url", 2, 1024);
      int var2 = var1.newRow();
      var1.set(var2, "command", var0.getString(0, "command"));
      var1.set(var2, "type", var0.getInt(0, "type"));
      var1.set(var2, "item", var0.getString(0, "item"));
      var1.set(var2, "instanceid", var0.getString(0, "instanceid"));
      var1.set(var2, "lastseq", var0.getInt(0, "seq"));
      var1.set(var2, "eof", var0.getBoolean(0, "eof"));
      var1.set(var2, "url", "");
      return var1;
   }

   public static void deleteDir(String var0) {
      File var1 = null;
      File var2 = new File(var0);
      if (var2.isDirectory()) {
         var1 = var2;
      } else {
         var1 = var2.getParentFile();
      }

      if (var1 != null) {
         File[] var3 = var1.listFiles();
         if (var3 != null) {
            for (File var7 : var3) {
               var7.delete();
            }
         }

         var1.delete();
      }
   }

   public static void sendDomainResponse(HttpServletResponse var0, PlatformData var1, String var2, String var3) throws Exception {
      var0.addHeader("Content-Type", "text/html;charset=UTF-8");
      PrintWriter var4 = var0.getWriter();
      DataSerializer var5 = null;
      if (var3 != null && "csv".equalsIgnoreCase(var3)) {
         var5 = new PlatformCsvDataSerializer();
      } else {
         var5 = new PlatformSsvDataSerializer();
      }

      var4.write("<html><script>document.domain = \"" + var2 + "\";</script>");
      var5.writeData(var4, var1, null, "UTF-8");
      var4.write("</html>");
      var4.flush();
      var4.close();
   }

   public static void writePartDataHead(StringWriter var0, GridImportContext var1) throws PlatformException {
      if (var1.getCorsResponseType() == 1) {
         var0.write("<script type=\"text/javascript\">");
         var0.write("var retValue = \"\";");
         var0.write("window.onload = function() { ");
         var0.write("if (window.addEventListener) { ");
         var0.write("window.addEventListener (\"message\", OnMessage, false); }");
         var0.write("else { ");
         var0.write("if (window.attachEvent) { ");
         var0.write("window.attachEvent(\"onmessage\", OnMessage); }");
         var0.write(" } }; ");
         var0.write("function OnMessage (event) { ");
         var0.write("message = `");
      } else if (var1.getUserDomain() != null) {
         var0.write("<html><script>document.domain = \"" + var1.getUserDomain() + "\";</script>");
      } else if (var1.isUseHtmlTag()) {
         var0.write("<!--[if lt IE 9]><comment><![endif]--><noscript>");
      }

      var1.getPartDataSerializer().setWriter(var0);
      var1.getPartDataSerializer().writeHead();
   }

   public static void writePartDataTail(StringWriter var0, GridImportContext var1) throws PlatformException {
      var1.getPartDataSerializer().setWriter(var0);
      var1.getPartDataSerializer().writeTail();
      if (var1.getCorsResponseType() == 1) {
         var0.write("`;");
         var0.write("message = event.data + message;");
         var0.write("event.source.postMessage (message, event.origin); }");
         var0.write("</script>");
      } else if (var1.getUserDomain() != null) {
         var0.write("</html>");
      } else if (var1.isUseHtmlTag()) {
         var0.write("</noscript></comment>");
      }
   }

   public static void writePartDataVariableList(StringWriter var0, GridImportContext var1, VariableList var2) throws PlatformException {
      StringWriter var3 = null;
      if (var1.getCorsResponseType() == 1) {
         var3 = new StringWriter();
         var1.getPartDataSerializer().setWriter(var3);
      } else {
         var1.getPartDataSerializer().setWriter(var0);
      }

      for (int var4 = 0; var4 < var2.size(); var4++) {
         var1.getPartDataSerializer().writeVariable(var2.get(var4));
      }

      if (var1.getCorsResponseType() == 1) {
         var0.append(var3.toString().replace("`", "\\`").replace("${", "\\${"));
      }
   }

   public static void writePartDataDataset(StringWriter var0, GridImportContext var1, DataSet var2) throws PlatformException {
      StringWriter var3 = null;
      if (var1.getCorsResponseType() == 1) {
         var3 = new StringWriter();
         var1.getPartDataSerializer().setWriter(var3);
      } else {
         var1.getPartDataSerializer().setWriter(var0);
      }

      var1.getPartDataSerializer().writeDataSet(var2);
      var2.clearData();
      if (var1.getCorsResponseType() == 1) {
         var0.write(var3.toString().replace("`", "\\`").replace("${", "\\${"));
      }
   }
}
