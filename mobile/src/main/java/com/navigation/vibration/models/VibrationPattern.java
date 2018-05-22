package com.navigation.vibration.models;

public class VibrationPattern
{
    private String name;
    private int id;
    private long[] patternAhead;
    private long[] patternLeft;
    private long[] patternRight;
    private long[] patternBack;

    public VibrationPattern(String name, int id){
        this.name = name;
        this.id = id;
    }

    public VibrationPattern(String name, int id,long[] patternAhead, long[] patternLeft, long[] patternRight, long[] patternBack){
        this.name = name;
        this.id = id;
        this.patternAhead = patternAhead;
        this.patternBack = patternBack;
        this.patternLeft = patternLeft;
        this.patternRight = patternRight;
    }

    public long[] getPatternRight() {
        return patternRight;
    }

    public void setPatternRight(long[] patternRight) {
        this.patternRight = patternRight;
    }

    public long[] getPatternBack() {
        return patternBack;
    }

    public void setPatternBack(long[] patternBack) {
        this.patternBack = patternBack;
    }

    public long[] getPatternLeft() {
        return patternLeft;
    }

    public void setPatternLeft(long[] patternLeft) {
        this.patternLeft = patternLeft;
    }

    public long[] getPatternAhead() {
        return patternAhead;
    }

    public void setPatternAhead(long[] patternAhead) {
        this.patternAhead = patternAhead;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
