package com.nexacro17.xeni.ximport;

import com.nexacro17.xapi.data.PlatformData;
import com.nexacro17.xapi.tx.PartDataSerializer;
import com.nexacro17.xapi.tx.impl.PlatformSsvPartDataSerializer;
import com.nexacro17.xeni.data.importformats.ImportFormat;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;

public class GridImportContext {
   private PartDataSerializer serializer;
   private HttpServletResponse httpResponse;
   private Writer responseWriter;
   private PlatformData platformdata;
   private ImportFormat format;
   private String importId;
   private String userDomain;
   private String responseContentType;
   private String filePath;
   private String fileUrl;
   private String password;
   private String quoteChar;
   private char separator;
   private int corsResponseType;
   private int partSize = 100;
   private int partIndex = -1;
   private int compatibleMode = 0;
   private int importType;
   private float commandVersion = 1.0F;
   private boolean htmlTag;
   private boolean sentData;
   private boolean partData;
   private boolean serverMode;
   private boolean rawDate;
   private boolean rawNumber;
   private boolean monitor;
   private boolean csvQuote;

   public GridImportContext() {
      this.corsResponseType = 0;
      this.rawDate = false;
      this.htmlTag = true;
      this.rawNumber = true;
      this.csvQuote = true;
      this.serializer = null;
      this.httpResponse = null;
      this.separator = ',';
   }

   public void setPartSize(int var1) {
      this.partSize = var1;
   }

   public int getPartSize() {
      return this.partSize;
   }

   public void setPartIndex(int var1) {
      this.partIndex = var1;
   }

   public int getPartIndex() {
      return this.partIndex;
   }

   public boolean isPartData() {
      return this.partData;
   }

   public void setPartData(boolean var1) {
      this.partData = var1;
   }

   public boolean isSentData() {
      return this.sentData;
   }

   public void setSentData(boolean var1) {
      this.sentData = var1;
   }

   public HttpServletResponse getHttpReponse() {
      return this.httpResponse;
   }

   public void setHttpResponse(HttpServletResponse var1) {
      this.httpResponse = var1;
   }

   public Writer getResponseWriter() throws IOException {
      if (this.responseWriter == null) {
         this.responseWriter = this.httpResponse.getWriter();
      }

      return this.responseWriter;
   }

   public String getImportId() {
      return this.importId;
   }

   public void setImportId(String var1) {
      this.importId = var1;
   }

   public String getUserDomain() {
      return this.userDomain;
   }

   public void setUserDomain(String var1) {
      this.userDomain = var1;
   }

   public String getResponseContentType() {
      return this.responseContentType;
   }

   public void setResponseContentType(String var1) {
      this.responseContentType = var1;
   }

   public String getQuoteChar() {
      return this.quoteChar;
   }

   public void setQuoteChar(String var1) {
      if (var1 != null && !"default".equalsIgnoreCase(var1)) {
         if ("none".equalsIgnoreCase(var1)) {
            this.quoteChar = "";
         } else {
            this.quoteChar = "" + var1.charAt(0);
         }
      } else if (this.isCsvQuote()) {
         this.quoteChar = "\"";
      } else {
         this.quoteChar = "";
      }
   }

   public char getSeparator() {
      return this.separator;
   }

   public void setSeparator(String var1) {
      if (this.getImportType() == 1296 && var1 != null && var1.length() > 0) {
         this.separator = (char)Integer.parseInt(var1.substring(2), 16);
      }
   }

   public void initPartDataSerializer() {
      this.serializer = new PlatformSsvPartDataSerializer();
      this.serializer.setCharset("UTF-8");
   }

   public PartDataSerializer getPartDataSerializer() {
      return this.serializer;
   }

   public int getCorsResponseType() {
      return this.corsResponseType;
   }

   public void setCorsResponseType(int var1) {
      this.corsResponseType = var1;
   }

   public boolean isUseHtmlTag() {
      return this.htmlTag;
   }

   public void setUseHtmlTag(boolean var1) {
      this.htmlTag = var1;
   }

   public void setCompatibleMode(int var1) {
      this.compatibleMode = var1;
   }

   public int getCompatibleMode() {
      return this.compatibleMode;
   }

   public void setCommandVersion(float var1) {
      this.commandVersion = var1;
   }

   public float getCommandVersion() {
      return this.commandVersion;
   }

   public void setPlatformData(PlatformData var1) {
      this.platformdata = var1;
   }

   public PlatformData getPlatformData() {
      return this.platformdata;
   }

   public void setImportFormat(ImportFormat var1) {
      this.format = var1;
   }

   public ImportFormat getImportFormat() {
      return this.format;
   }

   public void setImportType(int var1) {
      this.importType = var1;
   }

   public int getImportType() {
      return this.importType;
   }

   public void setServerMode(boolean var1) {
      this.serverMode = var1;
   }

   public boolean isServerMode() {
      return this.serverMode;
   }

   public void setFilePath(String var1) {
      this.filePath = var1;
   }

   public String getFilePath() {
      return this.filePath;
   }

   public void setFileUrl(String var1) {
      this.fileUrl = var1;
   }

   public String getFileUrl() {
      return this.fileUrl;
   }

   public void setFilePassword(String var1) {
      this.password = var1;
   }

   public String getFilePassword() {
      return this.password;
   }

   public void setRawDateValue(boolean var1) {
      this.rawDate = var1;
   }

   public boolean isRawDateValue() {
      return this.rawDate;
   }

   public void setRawNumberValue(boolean var1) {
      this.rawNumber = var1;
   }

   public boolean isRawNumberValue() {
      return this.rawNumber;
   }

   public void setFileMonitor(boolean var1) {
      this.monitor = var1;
   }

   public boolean isFileMonitor() {
      return this.monitor;
   }

   public void setCsvQuote(boolean var1) {
      this.csvQuote = var1;
   }

   public boolean isCsvQuote() {
      return this.csvQuote;
   }
}
