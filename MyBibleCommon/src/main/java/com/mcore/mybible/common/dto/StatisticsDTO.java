package com.mcore.mybible.common.dto;

import java.util.ArrayList;
import java.util.List;

public class StatisticsDTO extends ResultInfoDTO {

	private static final long serialVersionUID = -405176465413160074L;
	
	private List<DayStatisticDTO> dayStatistics;
	
	public StatisticsDTO() {
		dayStatistics = new ArrayList<DayStatisticDTO>();
	}
	
	public StatisticsDTO(int resultID, String resultDetails) {
		super(resultID, resultDetails);
	}

	public List<DayStatisticDTO> getDayStatistics() {
		return dayStatistics;
	}

	public void setDayStatistics(List<DayStatisticDTO> dayStatistics) {
		this.dayStatistics = dayStatistics;
	}

}
