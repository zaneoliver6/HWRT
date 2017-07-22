package edu.sta.uwi.hwrt;

import java.util.Arrays;

public class Utils {

    public float [] cumsum(float [] arr) {
        float [] out = new float[arr.length];
        float total = 0.0f;
        for (int i = 0; i < arr.length; i++) {
            total += arr[i];
            out[i] = total;
        }
        return out;
    }

    public float sum(float [] arr) {
        float total = 0.0f;
        for(int i=0; i < arr.length; i++)
            total += arr[i];

        return total;
    }

    public int searchSorted(float[] arr, float val) {
        if(val < arr[0]) return  0;

        if(val > arr[arr.length - 1])
            return arr.length - 1;

        int lo = 0;
        int hi = arr.length - 1;

        while(lo <= hi) {
            int mid = (hi+lo) / 2;
            if(val < arr[mid]) {
                hi = mid - 1;
            } else if(val > arr[mid]) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }

        return (arr[lo] - val) < (val - arr[hi]) ? lo : hi;
    }

    public int weightedPick(float [] arr) {
        float [] t = cumsum(arr);
        float s = sum(arr);
//        System.out.println("t:" + Arrays.toString(t));
//        System.out.println("s:" + s);
        float val = (float) Math.random()*s;
//        System.out.println(val);
        return  (int) searchSorted(t,val);
    }

}
