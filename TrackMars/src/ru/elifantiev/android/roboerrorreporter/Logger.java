package ru.elifantiev.android.roboerrorreporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

public class Logger {
	
	static private final String logDirTmp = "/Android/data/log/%s/files/"; 
    static private final DateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
    static private final DateFormat fileFormatter = new SimpleDateFormat("dd-MM-yy");
			
	static public void log(String text) {
		
		final Date dumpDate = new Date(System.currentTimeMillis());

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder
                .append("\n")
                .append(formatter.format(dumpDate)).append("\n")
                .append(text);
		
		
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

        	final String logDir = String.format(logDirTmp, "trackmars");
        	
            File sd = Environment.getExternalStorageDirectory();
            File stacktrace = new File(
                    sd.getPath() + logDir,
                    String.format(
                            "out.debug",
                            fileFormatter.format(dumpDate)));
            
            File dumpdir = stacktrace.getParentFile();
            
            boolean dirReady = dumpdir.isDirectory() || dumpdir.mkdirs();
            
            if (dirReady) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(stacktrace, true);
                    writer.write(reportBuilder.toString() + "\n");
                } catch (IOException e) {
                    // ignore
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
		
	}
}
