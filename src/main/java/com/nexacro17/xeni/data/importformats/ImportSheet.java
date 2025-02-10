package com.nexacro17.xeni.data.importformats;

public class ImportSheet {
   private String command = null;
   private String output = null;
   private String headrange = null;
   private String bodyrange = null;

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String var1) {
      this.command = var1;
   }

   public String getHead() {
      return this.headrange;
   }

   public void setHead(String var1) {
      this.headrange = var1;
   }

   public String getBody() {
      return this.bodyrange;
   }

   public void setBody(String var1) {
      this.bodyrange = var1;
   }

   public String getOutput() {
      return this.output;
   }

   public void setOutput(String var1) {
      this.output = var1;
   }
}
