package de.freewarepoint.whohasmystuff.database;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import de.freewarepoint.whohasmystuff.LentObject;
import de.freewarepoint.whohasmystuff.database.OpenLendDbAdapter;
import de.freewarepoint.whohasmystuff.database.XMLContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.freewarepoint.whohasmystuff.AbstractListIntent.LOG_TAG;

public class DatabaseHelper {

	private final static String backUpFileName = "WhoHasMyStuff.xml";

	public static boolean exportDatabaseToXML(OpenLendDbAdapter database) {

		File storage = Environment.getExternalStorageDirectory();
		String backupPath = storage.getAbsolutePath() + File.separator +  backUpFileName;
		File backupFile = new File(backupPath);

		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupFile), "UTF8"));
			out.write(convertDatabaseToXml(database));
			out.close();
		} catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
			return false;
		} catch (ParseException e) {
            Log.e(LOG_TAG, e.getMessage());
			return false;
		} catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.getMessage());
			return false;
		} catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
			return false;
		}

		return true;
	}

	private static String convertDatabaseToXml(OpenLendDbAdapter database) throws ParseException {
		Cursor c = database.fetchAllObjects();

		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<DatabaseBackup version=\"" + OpenLendDbAdapter.DATABASE_VERSION + "\">\n");

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
				sb.append(" returned=\"").append(back).append("\"");

				sb.append("/>\n");

				c.moveToNext();
			}
		}

		sb.append("</DatabaseBackup>");

		return sb.toString();
	}

    public static boolean importDatabaseFromXML(OpenLendDbAdapter database) {
        File storage = Environment.getExternalStorageDirectory();
        String backupPath = storage.getAbsolutePath() + File.separator +  backUpFileName;
        File backupFile = new File(backupPath);

        XMLContentHandler contentHandler = new XMLContentHandler();

        System.setProperty ("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

        try {
            Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile)));
            InputSource source = new InputSource(in);
            XMLReader myReader = XMLReaderFactory.createXMLReader();
            myReader.setContentHandler(contentHandler);
            myReader.parse(source);
            in.close();
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
            return false;
        } catch (SAXException e) {
            Log.e(LOG_TAG, e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            return false;
        }

        database.clearDatabase();

        for (LentObject lentObject : contentHandler.lentObjects) {
            database.createLentObject(lentObject.description, lentObject.date, lentObject.personName,
                    lentObject.personKey, lentObject.returned);
        }

        return true;
    }


}
