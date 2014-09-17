package com.mcore.mybible.services.webservices.rest.utilities;

import static com.mcore.mybible.common.utilities.CommonConstants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

import com.mcore.mybible.common.dto.DayStatisticDTO;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.services.statistics.model.StatisticsGroupData;
import com.mcore.mybible.services.webservices.rest.utilities.TemporalAuditHistoryLoaded.ILoadingFile;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class AuditUtils {

	public static int EVENT_TYPE_LOGIN = 1;

	public static int EVENT_TYPE_DOWNLOAD = 2;

	public static String AUDIT_TEXT_SEPARATOR = " - ";

	private static AuditUtils instance;

	private MongoClient mongo;

	private String dbHost;

	private String dbUserName;

	private String dbPassword;

	private int dbPort;

	private SimpleDateFormat format;

	private String resPath;

	private AuditUtils() {
		ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_RESOURCE_NAME);
		resPath = rb.getString(RESOURCE_PATH_KEY);
		dbHost = resolve(rb, DATABASE_HOST_KEY);
		dbUserName = resolve(rb, DATABASE_USERNAME_KEY);
		dbPassword = resolve(rb, DATABASE_PASSWORD_KEY);
		dbPort = Integer.parseInt(resolve(rb, DATABASE_PORT_KEY));
		format = new SimpleDateFormat("yyyy-MM-dd");
		initDB();
	}

	private String resolve(ResourceBundle rb, String key) {
		try {
			String result = rb.getString(key);
			if (result != null && result.contains("$")) {
				result = System.getenv(result.replaceAll("\\$", ""));
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	private DB initDB() {
		if (mongo == null) {
			try {
				mongo = new MongoClient(dbHost, dbPort);
			} catch (UnknownHostException e) {
			}
		}
		DB db = mongo.getDB("bibledb");
		if (dbUserName != null && !dbUserName.isEmpty()) {
			db.authenticate(dbUserName, dbPassword.toCharArray());
		}
		return db;
	}

	public static AuditUtils getInstance() {
		if (instance == null) {
			instance = new AuditUtils();
		}
		return instance;
	}

	public StatisticsDTO getStatistics(StatisticsInDTO filterInfo) {
		int daysBefore = 10;
		if (filterInfo != null && filterInfo.getDays() > 10) {
			daysBefore = filterInfo.getDays();
		}
		StatisticsDTO result = new StatisticsDTO();
		DB db = initDB();
		DBCollection daystats = db.getCollection("daystats");
		BasicDBObject searchQuery = new BasicDBObject().append("day",
				new BasicDBObject("$gt", getDateFromDaysBefore(daysBefore)));
		DBCursor cursor = daystats.find(searchQuery);
		cursor.sort(new BasicDBObject("day", -1));
		while (cursor.hasNext()) {
			DBObject daystat = cursor.next();
			result.getDayStatistics().add(
					new DayStatisticDTO((String) daystat.get("day"),
							getInt(daystat.get("usercount")), getInt(daystat
									.get("newusercount")), getInt(daystat
									.get("downloads"))));
		}
		return result;
	}

	private int getInt(Object obj) {
		if (obj != null) {
			if (obj instanceof Number) {
				return ((Number) obj).intValue();
			}
			if (obj instanceof String) {
				try {
					return Integer.parseInt(obj.toString());
				} catch (Exception e) {
				}
			}
		}
		return 0;
	}

	private String getDateFromDaysBefore(int daysBefore) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -daysBefore);
		return format.format(c.getTime());
	}

	protected synchronized String appendHistoryContents() {
		try {
			long initT = System.currentTimeMillis();
			if (initT > 0) throw new RuntimeException("NO PERMITIDO -- ");
			DB db = initDB();
			// -------------------------------------------------------
			DBCollection options = db.getCollection("options");
			DBCursor cursor = options.find();
			if (cursor.hasNext()) {
				return "ALREADY LOADED";
			}
			BasicDBObject document = new BasicDBObject();
			document.put("history_loaded", true);
			options.insert(document);
			ILoadingFile listener = new ILoadingFile() {
				@Override
				public void readed(Date date, String id, int tipo,
						String version, String opt) {
					internalAppendContentsMongo(tipo, date, id, version, opt);
				}
			};
			int total = 0;
			for (int i = 0; i < 20; i++) {
				StatisticsGroupData readUserDataFromFile = TemporalAuditHistoryLoaded
						.getInstance().readUserDataFromFile(String.valueOf(i),
								listener);
				total += readUserDataFromFile != null ? readUserDataFromFile
						.getUsers() : 0;
			}
			StatisticsGroupData readUserDataFromFile = TemporalAuditHistoryLoaded
					.getInstance().readUserDataFromFile(null, listener);
			total += readUserDataFromFile != null ? readUserDataFromFile
					.getUsers() : 0;
			return "OK " + total + " " + (System.currentTimeMillis() - initT)
					+ "ms";
		} catch (Exception e) {
			e.printStackTrace();
			return "FAILED: " + e.getMessage();
		}
	}

	public synchronized void appendContents(int eventType, Date date,
			String sourceId, String sourceVersion, String object) {
		try {
			internalAppendContentsMongo(eventType, date, sourceId,
					sourceVersion, object);
		} catch (Exception e) {
			if (eventType == EVENT_TYPE_LOGIN) {
				internalAppendContentsFile("Login " + sourceId + " "
						+ sourceVersion);
			} else if (eventType == EVENT_TYPE_DOWNLOAD) {
				internalAppendContentsFile("getBibleData " + sourceId + " "
						+ sourceVersion + " " + object);
			}
		}
	}

	private void internalAppendContentsMongo(int eventType, Date date,
			String sourceId, String sourceVersion, String object) {
		DB db = initDB();
		// -------------------------------------------------------
		/* No almacenamos mas esta tabla.
		DBCollection auditlog = db.getCollection("auditlog");
		BasicDBObject document = new BasicDBObject();
		document.put("event", eventType);
		document.put("date", date);
		document.put("sourceId", sourceId);
		document.put("sourceVersion", sourceVersion);
		document.put("download", object);
		auditlog.insert(document);
		*/
		// -------------------------------------------------------
		BasicDBObject document;
		DBCollection userstats = db.getCollection("userstats");
		BasicDBObject searchQuery = new BasicDBObject().append("sourceId",
				sourceId);
		DBCursor cursor = userstats.find(searchQuery);
		boolean newUser = true;
		Date lastuserDate = null;
		if (cursor.hasNext()) {
			newUser = false;
			DBObject next = cursor.next();
			lastuserDate = (Date) next.get("lastdate");
			document = new BasicDBObject();
			document.put("sourceId", next.get("sourceId"));
			document.put("lastdate", date);
			document.put("sourceId", next.get("sourceId"));
			document.put("sourceVersion", next.get("sourceVersion"));
			BasicDBList counters = (BasicDBList) next.get("counters");
			if (counters == null) {
				counters = new BasicDBList();
			}
			boolean modified = false;
			for (int i = 0; i < counters.size(); i++) {
				DBObject item = (DBObject) counters.get(i);
				if (item != null && item.containsField("TYPE_" + eventType)) {
					Integer count = (Integer) item.get("TYPE_" + eventType);
					count++;
					item.put("TYPE_" + eventType, count);
					modified = true;
				}
			}
			if (!modified) {
				counters.add(new BasicDBObject("TYPE_" + eventType, 1));
			}
			document.put("counters", counters);
			BasicDBList downloads = (BasicDBList) next.get("downloads");
			if (object != null && !object.isEmpty()) {
				if (downloads == null) {
					downloads = new BasicDBList();
				}
				boolean enc = false;
				for (int i = 0; i < downloads.size(); i++) {
					DBObject item = (DBObject) downloads.get(i);
					if (item != null && item.containsField("name")) {
						Object value = item.get("name");
						if (value != null && value.equals(object)) {
							enc = true;
						}
					}
				}
				if (!enc) {
					downloads.add(new BasicDBObject("name", object));
				}
			}
			document.put("downloads", downloads);
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", document);
			userstats.update(searchQuery, updateObj);
		} else {
			document = new BasicDBObject();
			document.put("sourceId", sourceId);
			document.put("lastdate", date);
			lastuserDate = date;
			document.put("sourceId", sourceId);
			document.put("sourceVersion", sourceVersion);
			BasicDBList counters = new BasicDBList();
			counters.add(new BasicDBObject("TYPE_" + eventType, 1));
			document.put("counters", counters);
			if (object != null && !object.isEmpty()) {
				BasicDBList downloads = new BasicDBList();
				downloads.add(new BasicDBObject("name", object));
				document.put("downloads", downloads);
			}
			userstats.insert(document);
		}
		// -------------------------------------------------------
		DBCollection daystats = db.getCollection("daystats");
		searchQuery = new BasicDBObject().append("day", getDaykeyFrom(date));
		cursor = daystats.find(searchQuery);
		boolean userToday = newUser
				|| (lastuserDate != null && !getDaykeyFrom(lastuserDate)
						.equals(getDaykeyFrom(date)));
		if (cursor.hasNext()) {
			DBObject next = cursor.next();
			if (userToday || newUser || eventType == EVENT_TYPE_DOWNLOAD) {
				document = new BasicDBObject();
				Object obj = next.get("usercount");
				if (userToday) {
					obj = obj instanceof Integer ? ((Integer) obj) + 1 : 1;
				}
				if (obj != null) {
					document.put("usercount", obj);
				}
				obj = next.get("newusercount");
				if (newUser) {
					obj = obj instanceof Integer ? ((Integer) obj) + 1 : 1;
				}
				if (obj != null) {
					document.put("newusercount", obj);
				}
				obj = next.get("downloads");
				if (eventType == EVENT_TYPE_DOWNLOAD) {
					obj = obj instanceof Integer ? ((Integer) obj) + 1 : 1;
				}
				if (obj != null) {
					document.put("downloads", obj);
				}
				BasicDBObject updateObj = new BasicDBObject();
				updateObj.put("$set", document);
				daystats.update(searchQuery, updateObj);
			}
		} else {
			document = new BasicDBObject();
			document.put("day", getDaykeyFrom(date));
			document.put("usercount", 1);
			document.put("newusercount", newUser ? 1 : 0);
			if (eventType == EVENT_TYPE_DOWNLOAD) {
				document.put("downloads", 1);
			}
			daystats.insert(document);
		}
		// -------------------------------------------------------
	}

	private String getBackUpFilePath() {
		return resPath + FAIL_LOGS_FILENAME + AUDIT_EXT;
	}

	private void internalAppendContentsFile(String sContent) {
		try {
			String sFileName = getBackUpFilePath();
			File oFile = new File(sFileName);
			if (!oFile.exists()) {
				oFile.createNewFile();
			}
			if (oFile.canWrite()) {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss.S");
				BufferedWriter oWriter = new BufferedWriter(new FileWriter(
						sFileName, true));
				oWriter.write(format.format(new Date()) + AUDIT_TEXT_SEPARATOR
						+ sContent + "\n");
				oWriter.close();
			}
		} catch (IOException oException) {
			// Nada
		}
	}

	public String displayFailDownloadFile() {
		Scanner scan = null;
		StringBuffer buffer;
		try {
			File file = new File(getBackUpFilePath());
			if (!file.exists() || file.length() <= 10) {
				return " - Vacio - ";
			}
			buffer = new StringBuffer();
			scan = new Scanner(file, "UTF-8");
			String readdata = "";
			while (scan.hasNext() && (readdata = scan.nextLine()) != null) {
				buffer.append(readdata).append('\n');
			}
			return buffer.toString();
		} catch (Exception e) {
			return " No se ha podido cargar.";
		} finally {
			if (scan != null) {
				scan.close();
				scan = null;
			}
		}
	}

	public void testDB() {
		DB db = initDB();
		DBCollection options = db.getCollection("options");
		DBCursor cursor = options.find();
		cursor.hasNext();
	}

	private String getDaykeyFrom(Date date) {
		return format.format(date);
	}

}
