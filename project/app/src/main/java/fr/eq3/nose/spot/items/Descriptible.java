package fr.eq3.nose.spot.items;

import android.os.Parcelable;

public interface Descriptible extends Parcelable {

    String getName();
    void setName(String name);
    String getDesription();
    void setDescription(String description);
    long getId();
}
