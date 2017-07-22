package edu.sta.uwi.hwrt;

public interface Classifier {
    String name();

    String recogize(final int[] pixels);
}
