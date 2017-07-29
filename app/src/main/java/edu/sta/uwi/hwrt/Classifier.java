package edu.sta.uwi.hwrt;

public interface Classifier {
    String name();

    String predict(final int[] pixels);
}
