package com.nexacro17.xeni.util;

import com.nexacro17.xapi.util.JavaEnvUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XeniProperties {
   private static final String PROPERTIES_NAME = "xeni.properties";
   private static final String DEFAULT_VALUE_STRING = null;
   private static final char DEFAULT_VALUE_CHAR = ' ';
   private static final int DEFAULT_VALUE_INT = 0;
   private static final boolean DEFAULT_VALUE_BOOLEAN = false;
   private static Map propMap = new HashMap();

   private XeniProperties() {
   }

   public static Object getProperty(String var0) {
      return propMap.get(var0);
   }

   public static void setProperty(String var0, Object var1) {
      propMap.put(var0, var1);
   }

   public static void setProperty(String var0, int var1) {
      setProperty(var0, new Integer(var1));
   }

   public static void setProperty(String var0, boolean var1) {
      setProperty(var0, var1 ? Boolean.TRUE : Boolean.FALSE);
   }

   public static void removeProperty(String var0) {
      propMap.remove(var0);
   }

   public static String getStringProperty(String var0) {
      return getStringProperty(var0, DEFAULT_VALUE_STRING);
   }

   public static String getStringProperty(String var0, String var1) {
      Object var2 = getProperty(var0);
      return var2 == null ? var1 : var2.toString();
   }

   public static char getCharProperty(String var0) {
      return getCharProperty(var0, ' ');
   }

   public static char getCharProperty(String var0, char var1) {
      Object var2 = getProperty(var0);
      if (var2 instanceof String) {
         String var3 = var2.toString();
         if (var3.length() > 1) {
            var3 = var3.trim();
         }

         if (var3.length() == 1) {
            return var3.charAt(0);
         }
      } else if (var2 instanceof Character) {
         return (Character)var2;
      }

      return var1;
   }

   public static int getIntProperty(String var0) {
      return getIntProperty(var0, 0);
   }

   public static int getIntProperty(String var0, int var1) {
      Object var2 = getProperty(var0);
      if (var2 instanceof String) {
         try {
            return Integer.parseInt(var2.toString());
         } catch (NumberFormatException var4) {
         }
      } else if (var2 instanceof Integer) {
         return (Integer)var2;
      }

      return var1;
   }

   public static boolean getBooleanProperty(String var0) {
      return getBooleanProperty(var0, false);
   }

   public static boolean getBooleanProperty(String var0, boolean var1) {
      Object var2 = getProperty(var0);
      if (!(var2 instanceof String)) {
         return var2 instanceof Boolean ? (Boolean)var2 : var1;
      } else {
         String var3 = var2.toString().toLowerCase();
         return "true".equals(var3) || "on".equals(var3) || "yes".equals(var3);
      }
   }

   private static void load(URL var0) {
      if (var0 != null) {
         InputStream var1 = null;

         try {
            var1 = var0.openStream();
            Properties var2 = new Properties();
            var2.load(var1);
            propMap.putAll(var2);
         } catch (IOException var12) {
            Log var3 = LogFactory.getLog(XeniProperties.class);
            if (var3.isTraceEnabled()) {
               var3.trace("Could not load properties file", var12);
            }
         } finally {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (IOException var11) {
               }
            }
         }
      }
   }

   private static URL getResource(String var0) {
      ClassLoader var1 = Thread.currentThread().getContextClassLoader();
      URL var2 = null;

      try {
         var2 = getJarLocationResource(var0);
         if (var2 == null) {
            var2 = getClasspathResource(var0);
         }

         return var2;
      } catch (Throwable var5) {
         Log var4 = LogFactory.getLog(XeniProperties.class);
         if (var4.isTraceEnabled()) {
            var4.trace("Could not find properties file", var5);
         }

         return null;
      }
   }

   private static URL getJarLocationResource(String var0) {
      String var1 = XeniProperties.class.getName().replace('.', '/') + ".class";
      URL var2 = XeniProperties.class.getClassLoader().getResource(var1);
      Log var3 = LogFactory.getLog(XeniProperties.class);
      if (var3.isDebugEnabled()) {
         var3.debug("getJarLocationResource(): path=" + var1 + ", url=" + var2);
      }

      String var4 = var2.getProtocol();
      String var5 = var2.getFile();
      String var6 = var5;
      if (JavaEnvUtils.isAtLeastJavaVersion("1.4")) {
         try {
            var6 = URLDecoder.decode(var6, "UTF-8");
         } catch (UnsupportedEncodingException var15) {
            if (var3.isErrorEnabled()) {
               var3.error("Could not decode filename: urlFile=" + var5, var15);
            }
         }
      } else {
         var6 = URLDecoder.decode(var5);
      }

      if (var3.isDebugEnabled()) {
         var3.debug("getJarLocationResource(): protocol=" + var4 + ", file=" + var6 + ", urlFile=" + var5);
      }

      int var7 = var6.startsWith("file:") ? "file:".length() : 0;
      int var8 = var6.indexOf("!/", var7);
      if (var8 > 0) {
         String var9 = var6.substring(var7, var8);
         File var10 = new File(var9);
         File var11 = new File(var10.getParent(), var0);
         boolean var12 = var11.exists();
         if (var3.isDebugEnabled()) {
            var3.debug("getJarLocationResource(): jarFile=" + var10 + ", licenseFile=" + var11 + ", licenseExists=" + var12);
         }

         if (var12) {
            try {
               URL var13 = var11.toURL();
               if (var3.isInfoEnabled()) {
                  var3.info("Loaded property file in JAR dir: path=" + var11.getAbsolutePath());
               }

               return var13;
            } catch (MalformedURLException var14) {
               if (var3.isInfoEnabled()) {
                  var3.info("Could not find property file in JAR dir: " + var14.getMessage());
               }
            }
         }
      }

      return null;
   }

   private static URL getClasspathResource(String var0) {
      ClassLoader var1 = Thread.currentThread().getContextClassLoader();
      Log var2 = LogFactory.getLog(XeniProperties.class);

      try {
         URL var3 = var1.getResource(var0);
         if (var3 != null && var2.isInfoEnabled()) {
            var2.info("Loaded property file in CLASSPATH: path=" + var3);
         }

         return var3;
      } catch (Throwable var4) {
         if (var2.isInfoEnabled()) {
            var2.info("Could not find property file in CLASSPATH: " + var4.getMessage());
         }

         return null;
      }
   }

   static {
      load(getResource("xeni.properties"));
   }
}
