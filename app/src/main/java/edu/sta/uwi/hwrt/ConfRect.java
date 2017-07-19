package edu.sta.uwi.hwrt;

import android.graphics.Rect;

public class ConfRect {
    private Rect rect;
    private Classification classification;

    public static ConfRect create(int left, int top, int right, int bottom, Classification classification) {
        ConfRect cr = new ConfRect();
        Rect r = new Rect();
        r.set(left,top,right,bottom);
        cr.rect = r;
        cr.classification =  classification;
        return cr;
    }

    public static ConfRect create(Rect r, Classification classification) {
        ConfRect cr = new ConfRect();
        cr.rect = r;
        cr.classification = classification;
        return cr;
    }

    public boolean intersects(Rect r) {
        return rect.intersect(r);
    }

    public Rect getRect() { return rect; }

    public Classification getClassification() { return classification; }
}
