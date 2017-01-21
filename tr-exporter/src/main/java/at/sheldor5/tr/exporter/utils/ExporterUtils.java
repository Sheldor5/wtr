package at.sheldor5.tr.exporter.utils;

import at.sheldor5.tr.core.records.Day;
import at.sheldor5.tr.core.records.Month;
import at.sheldor5.tr.core.records.Year;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Michael Palata <a href="https://github.com/Sheldor5">@github.com/Sheldor5</a> on 13.01.2017.
 */
public class ExporterUtils {

  private static final int BUF_LEN = 4096;

  public static void toFile(final InputStream source, final File destination) throws IOException {
    if (!destination.exists()) {
      throw new FileNotFoundException("");
    }
    final FileOutputStream out = new FileOutputStream(destination);
    byte[] buffer = new byte[BUF_LEN];
    int len;
    while ((len = source.read(buffer, 0, BUF_LEN)) > 0) {
      out.write(buffer, 0, len);
    }
    out.flush();
    out.close();
  }

  public static void fillYear(final Year year) {
    for (int m = 1; m < 13; m++) {
      Month month = new Month(m);
      for (int d = 1; d < 32; d++) {
        Day day = new Day(d);
        month.addItem(day);
      }
      year.addItem(month);
    }
  }
}