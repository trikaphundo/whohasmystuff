package de.freewarepoint.whohasmystuff;

import android.database.Cursor;
import android.os.Environment;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper {

	private final static String backUpFileName = "WhoHasMyStuff.xml";

	public static boolean exportDatabaseToXML(OpenLendDbAdapter database) {

		File storage = Environment.getExternalStorageDirectory();
		String backupPath = storage.getAbsolutePath() + File.separator +  backUpFileName;
		File backupFile = new File(backupPath);

		try {
			PrintStream out = new PrintStream(backupFile);
			out.print(convertDatabaseToXml(database));
			out.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (ParseException e) {
			return false;
		}

		return true;
	}

	private static String convertDatabaseToXml(OpenLendDbAdapter database) throws ParseException {
		Cursor c = database.fetchAllObjects();

		StringBuilder sb = new StringBuilder();

		sb.append("<DatabaseBackup>\n");

		if (c.getCount() > 0) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			c.moveToFirst();

			while (!c.isAfterLast()) {
				sb.append("<LentObject");

				String description = c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DESCRIPTION));
				sb.append(" description=\"").append(description).append("\"");

				Date date = df.parse(c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_DATE)));
				sb.append(" date=\"").append(date.getTime()).append("\"");

				String personName = c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON));
				sb.append(" personName=\"").append(personName).append("\"");

				String personKey = c.getString(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_PERSON_KEY));
				sb.append(" personKey=\"").append(personKey).append("\"");

				int back = c.getInt(c.getColumnIndexOrThrow(OpenLendDbAdapter.KEY_BACK));
				sb.append(" back=\"").append(back).append("\"");

				sb.append("/>\n");

				c.moveToNext();
			}
		}

		sb.append("</DatabaseBackup>");

		return sb.toString();
	}


}
