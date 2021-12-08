package com.xter.pickit.kit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {


	private static final ThreadLocal<SimpleDateFormat> NORMAL_DATE_1 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		}
	};


	private static final ThreadLocal<SimpleDateFormat> NORMAL_DATE_2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		}
	};

	private static final ThreadLocal<SimpleDateFormat> NORMAL_DATE_3 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMddHHmmss.SSS", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> NORMAL_DATE_4 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> NORMAL_TIME_1 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> NORMAL_TIME_2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> NORMAL_TIME_3 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
		}
	};


	public static String getNormalDate1() {
		return NORMAL_DATE_1.get().format(System.currentTimeMillis());
	}

	public static String getNormalDate1(long mills) {
		return NORMAL_DATE_1.get().format(mills);
	}

	public static String getNormalDate2() {
		return NORMAL_DATE_2.get().format(System.currentTimeMillis());
	}

	public static String getNormalDate2(long mills) {
		return NORMAL_DATE_2.get().format(mills);
	}

	public static String getNormalDate3() {
		return NORMAL_DATE_3.get().format(System.currentTimeMillis());
	}

	public static String getNormalDate3(long mills) {
		return NORMAL_DATE_3.get().format(mills);
	}

	public static String getNormalDate4() {
		return NORMAL_DATE_4.get().format(System.currentTimeMillis());
	}

	public static String getNormalDate4(long mills) {
		return NORMAL_DATE_4.get().format(mills);
	}

	public static String getNormalTime1() {
		return NORMAL_TIME_1.get().format(System.currentTimeMillis());
	}

	public static String getNormalTime2(long mills) {
		return NORMAL_TIME_2.get().format(mills);
	}

	public static String getNormalTime1(long mills) {
		return NORMAL_TIME_1.get().format(mills);
	}

	private static final long CHINA_ZONE_OFFSET = 8 * 60 * 60 * 1000;

	public static String getNormalTimeAbs(long mills) {
		return NORMAL_TIME_1.get().format(mills - CHINA_ZONE_OFFSET);
	}

	public static String getNormalTime3(long mills) {
		return NORMAL_TIME_3.get().format(mills);
	}

}
