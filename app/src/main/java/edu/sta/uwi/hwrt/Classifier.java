package edu.sta.uwi.hwrt;

public interface Classifier {
    String name();

    Classification recogize(final float[] pixels);
}
