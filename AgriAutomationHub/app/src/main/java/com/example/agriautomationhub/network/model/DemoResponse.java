package com.example.agriautomationhub.network.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DemoResponse {

    @SerializedName("result")
    public Result result;

    public static class Result {
        public Crop crop;
        public Disease disease;

        public static class Crop {
            public List<Suggestion> suggestions;
        }

        public static class Disease {
            public List<Suggestion> suggestions;
        }
    }

    public static class Suggestion {
        public String id;
        public String name;
        @SerializedName("scientific_name")
        public String scientificName;
        public double probability;
    }

    public Suggestion getTopCrop() {
        if (result != null && result.crop != null && result.crop.suggestions != null && !result.crop.suggestions.isEmpty()) {
            return result.crop.suggestions.get(0);
        }
        return null;
    }

    public Suggestion getTopDisease() {
        if (result != null && result.disease != null && result.disease.suggestions != null && !result.disease.suggestions.isEmpty()) {
            return result.disease.suggestions.get(0);
        }
        return null;
    }
}
