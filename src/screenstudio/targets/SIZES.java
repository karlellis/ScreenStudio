/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenstudio.targets;

/**
 *
 * @author patrick
 */
public enum SIZES {

    SOURCE,
    OUT_240P,
    OUT_360P,
    OUT_480P,
    OUT_720P,
    OUT_1080P;

    @Override
    public String toString() {
        String retValue = "ND";
        switch (this) {
            case OUT_1080P:
                retValue = "1080p";
                break;
            case OUT_240P:
                retValue = "240p";
                break;
            case OUT_360P:
                retValue = "360p";
                break;
            case OUT_480P:
                retValue = "480p";
                break;
            case OUT_720P:
                retValue = "720p";
                break;
            case SOURCE:
                retValue = "Display";
                break;
        }
        return retValue;
    }

}
