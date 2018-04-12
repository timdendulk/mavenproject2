package Testutil;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */

/***/
public class LogStreamReader implements Runnable {

   private BufferedReader reader;
   private static Logger log;
   boolean error = false;
   StringBuilder all = new StringBuilder();

   private String line;
   private String logPrefix = "";
   private final AtomicBoolean hasErrors;

   public LogStreamReader(InputStream is, Logger log, boolean error, AtomicBoolean hasErrors) {
       this.reader = new BufferedReader(new InputStreamReader(is));
       this.log = log;
       this.hasErrors = hasErrors;
   }

   public void run() {
       try {
           line = reader.readLine();
           while (line != null) {
               all.append(line);
               if (error) {
                   System.out.println(logPrefix + line);
               } else {
                   System.out.println(logPrefix + line);
               }
               if (Boolean.FALSE.equals(hasErrors.get())) {
                   line = reader.readLine();
               } else {
                   line = null;
                   break;
               }
           }

       } catch (IOException e) {
           System.out.println(logPrefix+" Fout tijdens sluiten reader");
       }
       finally {
           if (reader !=null) {
               try {
                   reader.close();
               } catch (IOException e) {
                   System.out.println(logPrefix+" Fout tijdens sluiten reader");
               }
           }
       }
   }

   public String getLine() {
       return line;
   }

   public StringBuilder getAll() {
       return all;
   }

   public void clearAll() {
       all = new StringBuilder();
   }

   public String getLogPrefix() {
       return logPrefix;
   }

   public void setLogPrefix(String logPrefix) {
       this.logPrefix = logPrefix;
   }

   public AtomicBoolean getHasErrors() {
       return hasErrors;
   }

}