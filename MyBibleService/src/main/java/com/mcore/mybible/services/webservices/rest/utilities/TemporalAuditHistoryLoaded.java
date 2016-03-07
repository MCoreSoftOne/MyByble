package com.mcore.mybible.services.webservices.rest.utilities;

import static com.mcore.mybible.common.utilities.CommonConstants.*;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.mcore.mybible.services.statistics.model.StatisticsGroupData;
import com.mcore.mybible.services.statistics.model.StatisticsItemData;
import com.mcore.mybible.services.statistics.model.UserData;

public class TemporalAuditHistoryLoaded {
	
	private static TemporalAuditHistoryLoaded instance;
	
	public static String AUDIT_TEXT_SEPARATOR = " - ";

	private String resPath;

	private SimpleDateFormat format;

	public static TemporalAuditHistoryLoaded getInstance() {
		if (instance == null) {
			instance = new TemporalAuditHistoryLoaded();
		}
		return instance;
	}
	
	private TemporalAuditHistoryLoaded() {
		ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_RESOURCE_NAME);
		resPath = rb.getString(RESOURCE_PATH_KEY);
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	}
	

	public File getAuditFile(String option) {
		String add = "";
		if (option != null) {
			if (option.equalsIgnoreCase("last")) {
				return null;
			} else {
				try {
					String cnt = adjustFileSufix(Integer.parseInt(option));
					File oFile = new File(resPath + AUDIT_FILENAME + cnt
							+ AUDIT_EXT);
					if (oFile.exists()) {
						add = cnt;
					} else {
						return null;
					}
				} catch (Exception e) {

				}
			}
		}
		String sFileName = resPath + AUDIT_FILENAME + add + AUDIT_EXT;
		File oFile = new File(sFileName);
		if (!oFile.exists()) {
			try {
				oFile.createNewFile();
			} catch (Exception e) {
				// NADA
			}
		}
		return oFile;
	}

	public File getAuditFile() {
		return getAuditFile(null);
	}
	
	public void nextFile(File oFile) {
		int i = 1;
		File nextFile = nameIfFileNotExist(i);
		while (nextFile == null) {
			i++;
			nextFile = nameIfFileNotExist(i);
			if (i == 99999) {
				break;
			}
		}
		oFile.renameTo(nextFile);
	}

	private String adjustFileSufix(int count) {
		String cnt = String.valueOf(count);
		while (cnt.length() < 5) {
			cnt = "0" + cnt;
		}
		return cnt;
	}

	private File nameIfFileNotExist(int count) {
		String cnt = adjustFileSufix(count);
		File file = new File(resPath + AUDIT_FILENAME + cnt + AUDIT_EXT);
		return !file.exists() ? file : null;
	}
	
	public interface ILoadingFile {
		public void readed(Date date, String id, int tipo, String version, String opt);
	}
	
	public StatisticsGroupData readUserDataFromFile(String option, ILoadingFile listener) {
		StatisticsGroupData result = new StatisticsGroupData();
		List<StatisticsItemData> data = new ArrayList<StatisticsItemData>();
		List<UserData> userList = new ArrayList<UserData>();
		File auditFile = getAuditFile(option);
		if (auditFile == null) {
			return null;
		}
		System.err.println("Loading " + auditFile.getPath());
		result.setFileName(auditFile.getName().replaceFirst("[.][^.]+$", ""));
		try {
			BufferedReader br = new BufferedReader(new FileReader(auditFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (line != null && line.length() > 0 && line.contains(AUDIT_TEXT_SEPARATOR)) {
					int indx = line.indexOf(AUDIT_TEXT_SEPARATOR);
					String dateData = line.substring(0, indx);
					String eventData = line.substring(indx + AUDIT_TEXT_SEPARATOR.length());
					String[] eData = eventData.split(" ");
					if (eData != null && eData.length > 2) {
						UserData userData = getUserData(userList, eData[1]);
						if (eData[0].equals("getBibleData")) {
							userData.setTotalDownload(userData.getTotalDownload()+1);
							userData.setVersion(eData[2]);
							userData.addDownloadedVersions(eData[3]);
						}
						if (eData[0].equals("Login")) {
							userData.setTotalLogin(userData.getTotalLogin()+1);
							userData.setVersion(eData[2]);
						}
						try {
							Date date = format.parse(dateData);
							userData.setLastDownload(date);
							StatisticsItemData statisticItem = getStatisticItem(data, date);
							statisticItem.addUser(userData);
							listener.readed(date, eData[1], eData[0].equals("getBibleData")? AuditUtils.EVENT_TYPE_DOWNLOAD: AuditUtils.EVENT_TYPE_LOGIN, 
									eData[2], eData.length > 3? eData[3]:null);
						} catch (Exception e) {
							// NADA
						}
						if (listener != null) {						
						}
					}
				}
			}
			br.close();
			Collections.sort(data, new Comparator<StatisticsItemData>() {
				@Override
				public int compare(StatisticsItemData o1, StatisticsItemData o2) {
					// TODO Auto-generated method stub
					return o2.getDate().compareTo(o1.getDate());
				}
			});
		} catch (Exception e) {
			// NADA
		}
		result.setData(data);
		result.sort();
		return result;
	}
	
	private StatisticsItemData getStatisticItem(List<StatisticsItemData> list, Date date) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			StatisticsItemData item = (StatisticsItemData) iterator.next();
			if (isDateEquals(date, item.getDate()) ) {
				return item;
			}
		}
		StatisticsItemData item = new StatisticsItemData();
		item.setDate(date);
		list.add(item);
		return item;
	}
	
	private boolean isDateEquals(Date date1, Date date2) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
				c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && 
				c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
	}
	
	private UserData getUserData(List<UserData> list, String userId) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			UserData userData = (UserData) iterator.next();
			if (userData.getId().equals(userId)) {
				return userData;
			}
		}
		UserData userData = new UserData();
		userData.setId(userId);
		list.add(userData);
		return userData;
	}
	
	public boolean compareDates(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
						cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
	}

}
