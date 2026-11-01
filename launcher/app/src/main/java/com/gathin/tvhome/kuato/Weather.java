package com.gathin.tvhome.kuato;

import org.json.JSONObject;

/** Current temperature and today's range from Open-Meteo. No Android types, so it runs in JVM tests. */
final class Weather {
    final int tempF, highF, lowF;
    final int code;

    private Weather(int tempF, int highF, int lowF, int code) {
        this.tempF = tempF;
        this.highF = highF;
        this.lowF = lowF;
        this.code = code;
    }

    static String url(double lat, double lon) {
        return "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min"
                + "&temperature_unit=fahrenheit&timezone=auto&forecast_days=1";
    }

    /** Null on any malformed input. */
    static Weather parse(String json) {
        try {
            JSONObject o = new JSONObject(json);
            JSONObject cur = o.getJSONObject("current");
            JSONObject day = o.getJSONObject("daily");
            return new Weather(
                    (int) Math.round(cur.getDouble("temperature_2m")),
                    (int) Math.round(day.getJSONArray("temperature_2m_max").getDouble(0)),
                    (int) Math.round(day.getJSONArray("temperature_2m_min").getDouble(0)),
                    cur.getInt("weather_code"));
        } catch (Exception e) {
            return null;
        }
    }
}
