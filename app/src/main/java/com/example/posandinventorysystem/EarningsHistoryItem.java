package com.example.posandinventorysystem;

public class EarningsHistoryItem {
    private int id;
    private String monthYear;
    private String earnings;
    private String capital;
    private String total;

    public EarningsHistoryItem(int id, String monthYear, String earnings, String capital, String total) {
        this.id = id;
        this.monthYear = monthYear;
        this.earnings = earnings;
        this.capital = capital;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public String getEarnings() {
        return earnings;
    }

    public String getCapital() {
        return capital;
    }

    public String getTotal() {
        return total;
    }
}


