package fr.eq3.nose.spot.items;

public interface Element<E> extends Descriptible {
    E getData();
    void setData(E data);
}
