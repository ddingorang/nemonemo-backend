// Created: 2026-04-07 22:42:03
package com.nemonemo.domain.unit.entity;

public enum UnitSize {
    XS(115, 105, 115),
    S(115, 105, 240),
    M(115, 170, 240),
    L(115, 230, 240),
    XL(210, 240, 240);

    private final int widthCm;
    private final int depthCm;
    private final int heightCm;

    UnitSize(int widthCm, int depthCm, int heightCm) {
        this.widthCm = widthCm;
        this.depthCm = depthCm;
        this.heightCm = heightCm;
    }

    public int getWidthCm() {
        return widthCm;
    }

    public int getDepthCm() {
        return depthCm;
    }

    public int getHeightCm() {
        return heightCm;
    }
}
