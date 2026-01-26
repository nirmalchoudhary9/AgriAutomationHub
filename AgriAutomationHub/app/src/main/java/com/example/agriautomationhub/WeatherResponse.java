package com.example.agriautomationhub;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private Weather[] weather;

    public String getName() {
        return name;
    }

    public Main getMain() {
        return main;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public static class Main {
        @SerializedName("temp")
        private double temp;
        @SerializedName("humidity")
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        @SerializedName("id")
        private int id;
        @SerializedName("description")
        private String description;

        public int getId() {
            return id;
        }


        public String getDescription() {
            return description;
        }
    }
}
