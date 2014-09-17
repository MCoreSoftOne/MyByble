package com.mcore.mybible.common.dto;

import java.util.Arrays;
import java.util.Comparator;

public class TranslationListDTO extends ResultInfoDTO {

	private static final long serialVersionUID = -1458406444953439720L;

	private TranslationDTO[] translations;

	public TranslationListDTO() {
	}

	public TranslationListDTO(int resultID, String resultDetails) {
		super(resultID, resultDetails);
	}

	public TranslationDTO[] getTranslations() {
		return translations;
	}

	public void setTranslations(TranslationDTO[] translations) {
		this.translations = translations;
	}

	@Override
	public String toString() {
		return "TranslationListDTO [translations="
				+ Arrays.toString(translations) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(translations);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TranslationListDTO other = (TranslationListDTO) obj;
		if (!Arrays.equals(orderTranslations(), other.orderTranslations()))
			return false;
		return true;
	}
	
	public TranslationDTO[] orderTranslations() {
		TranslationDTO[] result = null;
		if (translations != null) {
			result = new TranslationDTO[translations.length];
			System.arraycopy(translations, 0, result, 0, translations.length);
			Arrays.sort(result, new Comparator<TranslationDTO>() {
				@Override
				public int compare(TranslationDTO arg0, TranslationDTO arg1) {
					if (arg0 != null && arg1 != null && arg0.getId() != null && arg1.getId() != null) {
						return arg0.getId().compareTo(arg1.getId());
					}
					return 0;
				}
			});
		}
		return result;
	}

}
