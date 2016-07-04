package com.gamesPnL;

public class ResultData {
    private String dateTime;
    private double amount;
    private String game;
    private String limit;
    private String event;
    private int recId;
    private int timePlayed;

    public ResultData() {
        amount = 0;
        game = "";
        limit = "";
        dateTime = "";
        event = "";
        timePlayed = 0;

        return;
    }

    public void setRecId(int r) {
        recId = r;
    }

    public int getRecId() {
        return recId;
    }

    public String getRecIdString() {
        return String.valueOf(recId);
    }

    public void setAmount(Double p) {
        amount = p;
    }

    public Double getAmount() {
        return amount;
    }

    public String getAmountStr() {
        return String.valueOf(amount);
    }

    public void setGame(String p) {
        game = p;
    }

    public String getGame() {
        return game;
    }

    public void setLimit(String l) {
        limit = l;
        return;
    }

    public String getLimit() {
        return limit;
    }

    public void setEvent(String e) {
        event = e;
        return;
    }

    public String getEvent() {
        return event;
    }

    public void setDateTime(String d) {
        dateTime = d;
        return;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(int t) {
        timePlayed = t;
        return;
    }
}
