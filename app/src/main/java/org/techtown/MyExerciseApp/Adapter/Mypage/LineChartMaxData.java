package org.techtown.MyExerciseApp.Adapter.Mypage;

public class LineChartMaxData {
    String date;
    private Double weight1Rm;
    private Double weightMax;
    private Double volMax;

    public LineChartMaxData() {}

    public LineChartMaxData(String date, Double weight1Rm, Double weightMax, Double volMax) {
        this.date = date;
        this.weight1Rm = weight1Rm;
        this.weightMax = weightMax;
        this.volMax = volMax;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getWeight1Rm() {
        return weight1Rm;
    }

    public void setWeight1Rm(Double weight1Rm) {
        this.weight1Rm = weight1Rm;
    }

    public Double getWeightMax() {
        return weightMax;
    }

    public void setWeightMax(Double weightMax) {
        this.weightMax = weightMax;
    }

    public Double getVolMax() {
        return volMax;
    }

    public void setVolMax(Double volMax) {
        this.volMax = volMax;
    }
}
