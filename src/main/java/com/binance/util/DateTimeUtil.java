package com.binance.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateTimeUtil {

	public static ZonedDateTime zoneTravel(LocalDateTime datetime,
			ZoneId oldZone, ZoneId newZone) {
		return datetime.atZone(oldZone).withZoneSameInstant(newZone);
	}

	public static ZonedDateTime toZonedDateTime(Long time) {
		return ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
	}
}
