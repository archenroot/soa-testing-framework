package com.ibm.soatf.gui;

import java.io.IOException;
import java.io.OutputStream;
import javafx.application.Platform;
import javafx.scene.control.TextArea;



public class TextAreaOutputStream extends OutputStream {

   private TextArea textArea;
   private StringBuilder sb = new StringBuilder();
   private String title;

   public TextAreaOutputStream(TextArea textArea, String title) {
      this.textArea = textArea;
      this.title = title;
      sb.append(title + "> ");
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() {
   }

   @Override
   public void write(int b) throws IOException {

      if (b == '\r')
         return;

      if (b == '\n') {
         final String text = sb.toString() + "\n";
         Platform.runLater(new Runnable() {
            public void run() {
               textArea.appendText(text);
            }
         });
         sb.setLength(0);
         sb.append(title + "> ");
         return;
      }

      sb.append((char) b);
   }
}