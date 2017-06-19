package fgv.emap.kizzyterra.isoscope;

import java.util.ArrayList;

/**
 * Created by kizzyterra on 01/05/17.
 */

public interface Grid {

    void  buildGrid(int mode);
    void setTimeData(ArrayList<ArrayList<Double>> times);

}
