package com.prohect.sqlFrontend.main;

public class UpdateOfCellOfTable {
    int targetRowIndex;
    int targetColumnIndex;
    Object newValue;

    public UpdateOfCellOfTable(int targetRowIndex, int targetColumnIndex, Object newValue) {
        this.targetRowIndex = targetRowIndex;
        this.targetColumnIndex = targetColumnIndex;
        this.newValue = newValue;
    }

    public int getTargetRowIndex() {
        return targetRowIndex;
    }

    public int getTargetColumnIndex() {
        return targetColumnIndex;
    }

    public Object getNewValue() {
        return newValue;
    }
}
